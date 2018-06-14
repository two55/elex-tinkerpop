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
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
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
        for (VertexLabel l : man.getVertexLabels()) {
            if (nodeInfoConfigs.containsKey(l.name())) {
                config.nodes.put(l.name(), nodeInfoConfigs.get(l.name()));
            } else {
                SchemaMetaInformation.Node newNode = new SchemaMetaInformation.Node();
                newNode.setType(l.name());
                newNode.setExplore(false);
                config.nodes.put(l.name(), newNode);
            }
        }

        // setup links
        config.links.put("__default", new SchemaMetaInformation.Link());

        Map<String, SchemaMetaInformation.Link> linkInfoConfigs = displayConfig.getLinks()
                .stream()
                .collect(Collectors.toMap(SchemaMetaInformation.Link::getType, Function.identity()));
        for (EdgeLabel l : man.getRelationTypes(EdgeLabel.class)) {
            if (linkInfoConfigs.containsKey(l.name())) {
                config.links.put(l.name(), linkInfoConfigs.get(l.name()));
            } else {
                SchemaMetaInformation.Link newNode = new SchemaMetaInformation.Link();
                newNode.setType(l.name());
                newNode.setExplore(false);
                config.links.put(l.name(), newNode);
            }
        }
        man.rollback();
        return config;
    }
}
