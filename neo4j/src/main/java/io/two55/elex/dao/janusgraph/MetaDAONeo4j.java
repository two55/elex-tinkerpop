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
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
@ConditionalOnProperty(name = "graph.type", havingValue = "neo4j", matchIfMissing=true)
public class MetaDAONeo4j extends MetaDAODefaultImpl {

    private GraphSource<Neo4jGraph> graphSource;

    @Autowired
    public MetaDAONeo4j(GraphSource<Neo4jGraph> graphSource) {
        this.graphSource = graphSource;
    }

    @Override
    public SchemaMetaInformation meta() {
        SchemaMetaInformation config = new SchemaMetaInformation();

        try(GraphSource.AutoTransaction<Neo4jGraph> g = graphSource.autoRollback()) {
            // setup nodes
            config.nodes.put("__default", new SchemaMetaInformation.Node());
            Map<String, SchemaMetaInformation.Node> nodeInfoConfigs = displayConfig.getNodes()
                    .stream()
                    .collect(Collectors.toMap(SchemaMetaInformation.Node::getType, Function.identity()));

            for (Object m : graphSource.graph().cypher("MATCH (n) RETURN distinct labels(n) as label").<Map>toList()) {
                for(String labelName : ((Map<String, List<String>>)m).get("label")){
                    if(config.nodes.containsKey(labelName)) {
                        continue;
                    }
                    if (nodeInfoConfigs.containsKey(labelName)) {
                        config.nodes.put(labelName, nodeInfoConfigs.get(labelName));
                    } else {
                        SchemaMetaInformation.Node newNode = new SchemaMetaInformation.Node();
                        newNode.setType(labelName);
                        newNode.setExplore(false);
                        config.nodes.put(labelName, newNode);
                    }
                }
            }

            // setup links
            config.links.put("__default", new SchemaMetaInformation.Link());

            Map<String, SchemaMetaInformation.Link> linkInfoConfigs = displayConfig.getLinks()
                    .stream()
                    .collect(Collectors.toMap(SchemaMetaInformation.Link::getType, Function.identity()));

            for (Object m : graphSource.graph().cypher("MATCH ()-[n]-() RETURN distinct TYPE(n) as label").<Map>toList()) {
                String labelName = ((Map<String, String>)m).get("label");
                if(config.links.containsKey(labelName)) {
                    continue;
                }
                if (linkInfoConfigs.containsKey(labelName)) {
                    config.links.put(labelName, linkInfoConfigs.get(labelName));
                } else {
                    SchemaMetaInformation.Link newNode = new SchemaMetaInformation.Link();
                    newNode.setType(labelName);
                    newNode.setExplore(false);
                    config.links.put(labelName, newNode);
                }
            }
        }
        return config;
    }
}
