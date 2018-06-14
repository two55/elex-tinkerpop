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

package io.two55.elex;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.two55.elex.config.DisplayConfig;
import io.two55.elex.config.GraphSource;
import io.two55.elex.config.JanusGraphConfig;
import io.two55.elex.controller.LinkController;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.Mapping;
import org.janusgraph.core.schema.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@Component
public class CreateExampleGraph implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CreateExampleGraph.class);

    @Autowired
    private GraphSource<JanusGraph> graphSource;

    @Override
    public void run(String... args) {
        if (args.length < 1 || !Sets.newHashSet(args).contains("--createExampleGraph")) {
            return;
        }
        JanusGraph graph = graphSource.graph();
        JanusGraphManagement mgmt = graph.openManagement();
        if(mgmt.containsGraphIndex("nameIndex")) {
            log.info("Graph already created!");
        } else {
            log.info("Creating small example graph ...");
            // we need to add an index to the name property
            PropertyKey nameProp = mgmt.makePropertyKey("name").dataType(String.class).make();
            mgmt.buildIndex("nameIndex", Vertex.class)
                    .addKey(nameProp, Mapping.TEXT.asParameter()).buildMixedIndex("search");
            mgmt.commit();

            // let's generate a a small example graph
            // add vertices
            JanusGraphVertex vLarry = graph.addVertex(T.label, "person",
                    "name", "Lawrence Edward Page",
                    "born", "1973-03-26");
            JanusGraphVertex vSergey = graph.addVertex(T.label, "person",
                    "name", "Sergey Mikhaylovich Brin",
                    "born", "1973-08-21");
            JanusGraphVertex vTerry = graph.addVertex(T.label, "person",
                    "name", "Terry Allen Winograd",
                    "born", "1946-02-24");
            JanusGraphVertex vAlphabet = graph.addVertex(T.label, "company",
                    "name", "Alphabet Inc.",
                    "founded", "2015");
            JanusGraphVertex vStanford = graph.addVertex(T.label, "company",
                    "name", "Stanford University",
                    "founded", "1885");

            JanusGraphVertex vPageRank = graph.addVertex(T.label, "article",
                    "name", "The PageRank citation ranking: Bringing order to the web.",
                    "published", "1999");

            // add edges
            vLarry.addEdge("works for", vAlphabet, "since", "2015");
            vSergey.addEdge("works for", vAlphabet, "since", "2015");
            vLarry.addEdge("works for", vStanford, "since", "1993", "until", "2008");
            vSergey.addEdge("works for", vStanford, "since", "1993", "until", "2008");
            vTerry.addEdge("works for", vStanford, "since", "1973");
            vLarry.addEdge("wrote", vPageRank, "in", "1999");
            vSergey.addEdge("wrote", vPageRank, "in", "1999");
            vTerry.addEdge("wrote", vPageRank, "in", "1999");
            vLarry.addEdge("knows", vTerry);
            vTerry.addEdge("knows", vSergey);
            vSergey.addEdge("knows", vLarry);
            graph.tx().commit();
            log.info("Example graph created!");
        }
    }
}
