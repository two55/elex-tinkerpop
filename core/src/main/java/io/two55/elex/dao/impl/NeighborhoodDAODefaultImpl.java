/*
 * Copyright 2018 ELEx Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.two55.elex.dao.impl;

import io.two55.elex.beans.Link;
import io.two55.elex.beans.Neighbors;
import io.two55.elex.beans.Node;
import io.two55.elex.config.GraphSource;
import io.two55.elex.dao.NeighborhoodDAO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "graph.type", havingValue = "default", matchIfMissing=true)
public class NeighborhoodDAODefaultImpl implements NeighborhoodDAO {
    private static final Logger log = LoggerFactory.getLogger(NeighborhoodDAODefaultImpl.class);
    public static boolean RETURN_EMPTY_NEIGHBORS_LIST = true;
    public static long MAX_NODE_SIZE = 1000;

    protected GraphSource<?> graphSource;

    // TODO remove dependency when frontend supports type settings for nodes correctly
    protected MetaDAODefaultImpl metaDAO;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public NeighborhoodDAODefaultImpl(GraphSource<?> graphSource, MetaDAODefaultImpl metaDAO) {
        this.graphSource = graphSource;
        this.metaDAO = metaDAO;
    }

    @Override
    public Neighbors neighbors(String nodeId,
                               int limit,
                               int depth,
                               Set<String> nodeTypes,
                               Set<String> linkTypes) throws Exception {
        Neighbors neighbors = new Neighbors();

        String[] linkTypeArray = new String[0];
        if(linkTypes!=null&&linkTypes.size()>0) {
            linkTypeArray = linkTypes.toArray(new String[linkTypes.size()]);
        }
        try(GraphSource.AutoTransaction<?> g = graphSource.autoRollback()) {
            GraphTraversalSource t = g.traversal();
            Set<String> knownNeighborhoods = new HashSet<>();
            Set<String> nextNodeIds = new HashSet<>();
            nextNodeIds.add(nodeId);
            // explore neighborhood sequentially (direct neighbors, indirect neighbors, neighbors with distance 3, ..)
            for(int i=0; i<depth; i++) {
                if(limit>0 && neighbors.links.size()>=limit) {
                    // enough neighbors found
                    break;
                }
                extendNeighbors(
                        neighbors,
                        nextNodeIds,
                        t,
                        limit,
                        nodeTypes,
                        linkTypeArray);
                // did we see more neighbors?
                int numKnownNeighborhoods = knownNeighborhoods.size();
                knownNeighborhoods.addAll(nextNodeIds);
                if(numKnownNeighborhoods==knownNeighborhoods.size()) {
                    // no new neighbors found
                    break;
                }
                nextNodeIds = new HashSet<>(neighbors.containedNodeIds());
                nextNodeIds.removeAll(knownNeighborhoods);
            }

            if (RETURN_EMPTY_NEIGHBORS_LIST && neighbors.getNodes().isEmpty()) {
                // in case we didn't find any edges, we still might be able to return the vertex/node itself
                t.V(nodeId)
                        .toStream()
                        .map(Node::fromVertex)
                        .forEach(neighbors::add);
            }

            neighbors = updateProperties(neighbors, t);

            if(log.isDebugEnabled()) {
                log.debug(String.format(
                        "Neighbor request for %s yields %d nodes and %d links.",
                        nodeId,
                        neighbors.getNodes().size(),
                        neighbors.links.size()));
            }
        }
        return neighbors;
    }

    protected void extendNeighbors(Neighbors neighbors, Set<String> nodeIds, GraphTraversalSource t,
                                   int limit, Set<String> validNodeLabels, String[] linkLabelArray) {
        // for each edges from/to v[nodeIds] update neighbors bean
        GraphTraversal<Vertex, Edge> edgeTraversal = t
                // missing/empty edge labels are automatically handled
                .V(nodeIds.toArray(new Object[nodeIds.size()]))
                .bothE(linkLabelArray);

        while(edgeTraversal.hasNext() && ( limit <= 0 || neighbors.links.size() < limit )) {
            for(Edge e : edgeTraversal.next(limit>0?limit:Integer.MAX_VALUE)) {
                // filter node labels
                // TODO this filter should be done on the query side (but bothV destroys the type selection)
                if(validNodeLabels!=null) {
                    if(validNodeLabels.size()>0 && !nodeIds.contains(e.inVertex().id().toString()) &&
                            !validNodeLabels.contains(e.inVertex().label())) {
                        // in vertex is neither in the source ids nor has it a valid type
                        continue;
                    }
                    if(validNodeLabels.size()>0 && !nodeIds.contains(e.outVertex().id().toString()) &&
                            !validNodeLabels.contains(e.outVertex().label())) {
                        // out vertex is neither in the source ids nor has it a valid type
                        continue;
                    }
                }
                neighbors.addEdge(e);
                if (limit > 0 && neighbors.links.size() >= limit) {
                    break;
                }
            }
        }
    }

    protected Neighbors updateProperties(Neighbors neighbors, GraphTraversalSource t) {
        // normalize links & count
        long numLinks = neighbors.links.stream()
                .map(l -> this.updateLinkProperties(l, t))
                .count();
        // normalize nodes & count
        long numNodes = neighbors.getNodes().stream()
                .map(n -> this.updateNodeProperties(n, t))
                .count();
        return neighbors;
    }

    protected Node updateNodeProperties(Node node, GraphTraversalSource t) {
        // apply all functions in order
        node = updateWeight(node, t);
        node = updateHasDetails(node);
        return node;
    }

    protected Node updateWeight(Node node, GraphTraversalSource t) {
        if(!node.props.containsKey("_degree")) {
            // if no degree is set: try to calculate and set it
            long neighbors = t.V(node.id).bothE().count().next();
            t.V(node.id).property("_degree",neighbors);
            t.tx().commit();
            node.props.put("_degree", neighbors);
        }
        // set the node size to degree or max
        node.props.putIfAbsent("__weight", Math.min(MAX_NODE_SIZE, (Long) node.props.remove("_degree")));
        return node;
    }

    // TODO remove when frontend supports type settings for nodes correctly
    protected Node updateHasDetails(Node node) {
        if(metaDAO.cache().nodes.containsKey(node.type)) {
            Boolean hasDetails = metaDAO.cache().nodes.get(node.type).getHasDetails();
            node.props.putIfAbsent("__hasDetails", hasDetails);
        }
        return node;
    }

    protected Link updateLinkProperties(Link link, GraphTraversalSource t) {
        // apply all functions in order
        link = updateWeight(link);
        link = updateIsDirected(link);
        link = updateHasDetails(link);
        link = updateColor(link);
        return link;
    }


    private Link updateWeight(Link link) {
        link.props.putIfAbsent("__weight", 2);
        return link;
    }

    // TODO remove when frontend supports type settings for links correctly
    protected Link updateIsDirected(Link link) {
        if(metaDAO.cache().links.containsKey(link.type)) {
            Boolean isDirected = metaDAO.cache().links.get(link.type).getIsDirected();
            link.props.putIfAbsent("__isDirected", isDirected);
            link.isDirected = isDirected;
        }
        return link;
    }
    protected Link updateHasDetails(Link link) {
        if(metaDAO.cache().links.containsKey(link.type)) {
            Boolean isDirected = metaDAO.cache().links.get(link.type).getHasDetails();
            link.props.putIfAbsent("__hasDetails", isDirected);
        }
        return link;
    }
    protected Link updateColor(Link link) {
        if(metaDAO.cache().links.containsKey(link.type)) {
            String color = metaDAO.cache().links.get(link.type).getColor();
            link.props.putIfAbsent("__color", color);
        }
        return link;
    }
}
