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

package io.two55.elex.dao.janusgraph;

import io.two55.elex.beans.SchemaMetaInformation;
import io.two55.elex.config.GraphSource;
import io.two55.elex.dao.impl.MetaDAODefaultImpl;
import io.two55.utils.StreamUtils;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
@ConditionalOnProperty(name = "graph.type", havingValue = "janusgraph", matchIfMissing=true)
public class MetaDAOJanusGraph extends MetaDAODefaultImpl {

    private GraphSource<JanusGraph> graphSource;

    @Autowired
    public MetaDAOJanusGraph(GraphSource<JanusGraph> graphSource) {
        this.graphSource = graphSource;
    }

    @Override
    public SchemaMetaInformation meta() {
        SchemaMetaInformation config = new SchemaMetaInformation();
        JanusGraphManagement man = graphSource.graph().openManagement();

        // setup nodes
        config.nodes.put("__default", new SchemaMetaInformation.Node());
        Map<String, SchemaMetaInformation.Node> nodeInfoConfigs = displayConfig.getNodes()
                .stream()
                .collect(Collectors.toMap(SchemaMetaInformation.Node::getType, Function.identity()));
        for (String vertexLabel : getValidVertexLabels(man)) {
            if (nodeInfoConfigs.containsKey(vertexLabel)) {
                config.nodes.put(vertexLabel, nodeInfoConfigs.get(vertexLabel));
            } else {
                SchemaMetaInformation.Node newNode = new SchemaMetaInformation.Node();
                newNode.setType(vertexLabel);
                newNode.setExplore(false);
                config.nodes.put(vertexLabel, newNode);
            }
        }

        // setup links
        config.links.put("__default", new SchemaMetaInformation.Link());

        Map<String, SchemaMetaInformation.Link> linkInfoConfigs = displayConfig.getLinks()
                .stream()
                .collect(Collectors.toMap(SchemaMetaInformation.Link::getType, Function.identity()));
        for (String edgeLabel : getValidEdgeLabels(man)) {
            if (linkInfoConfigs.containsKey(edgeLabel)) {
                config.links.put(edgeLabel, linkInfoConfigs.get(edgeLabel));
            } else {
                SchemaMetaInformation.Link newNode = new SchemaMetaInformation.Link();
                newNode.setType(edgeLabel);
                newNode.setExplore(false);
                config.links.put(edgeLabel, newNode);
            }
        }
        man.rollback();
        return config;
    }

    protected Set<String> getValidVertexLabels(JanusGraphManagement man) {
        return StreamUtils
                .toStream(man.getVertexLabels())
                .map(VertexLabel::name)
                .collect(Collectors.toSet());
    }

    protected Set<String> getValidEdgeLabels(JanusGraphManagement man) {
        return StreamUtils
                .toStream(man.getRelationTypes(EdgeLabel.class))
                .map(EdgeLabel::name)
                .collect(Collectors.toSet());
    }
}
