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

package io.two55.elex.controller;

import io.two55.elex.ElexApp;
import io.two55.elex.JanusGraphTest;
import io.two55.elex.config.GraphSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphEdge;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {ElexApp.class})
public class NodeControllerJanusGraphTest extends JanusGraphTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphSource<JanusGraph> graphSource;

    @Before
    public void bindBackend(){
        given(graphSource.graph())
                .willReturn(testGraph);
        given(graphSource.autoRollback())
                .willReturn(new GraphSource.AutoTransaction<>(testGraph, false));
        given(graphSource.autoCommit())
                .willReturn(new GraphSource.AutoTransaction<>(testGraph, true));
    }

    @Test
    public void testGetNeighborhoodDefault() throws Exception {
        // add test instances to the graph
        JanusGraphVertex p42 = testGraph.addVertex(T.label, "person", "name", "Peter");
        JanusGraphVertex p13 = testGraph.addVertex(T.label, "person", "nickname", "Tom");
        // connect instances
        JanusGraphEdge l1 = p42.addEdge("knows", p13, "since", "1970-01-01");
        testGraph.tx().commit();


        String expectedJson = String.format(
                "{" +
                "  'links': [" +
                "    {'id': '%3$s', 'type': 'knows', 'sourceId': '%1$s', 'targetId': '%2$s', 'props': {'since': '1970-01-01'}}" +
                "  ], " +
                "  'nodes': [" +
                "    {'id': '%1$s', 'type': 'person', 'props': {'name': 'Peter'}}," +
                "    {'id': '%2$s', 'type': 'person', 'props': {'nickname': 'Tom'}}" +
                "  ]" +
                "}", p42.id(), p13.id(), l1.id());

        this.mvc.perform(get(String.format("/api/node/_id/%s/_neighbors", p42.id()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetNeighborhoodComplex() throws Exception {
        // add test instances to the graph
        JanusGraphVertex p42 = testGraph.addVertex(T.label, "person", "name", "Peter");
        JanusGraphVertex p13 = testGraph.addVertex(T.label, "person", "nickname", "Tom");
        JanusGraphVertex p128 = testGraph.addVertex(T.label, "person", "name", "Frank");
        JanusGraphVertex b1024 = testGraph.addVertex(T.label, "book", "title", "Tom Sawyer");
        JanusGraphVertex p2048 = testGraph.addVertex(T.label, "person", "name", "Paul");
        // connect instances
        JanusGraphEdge l1 = p42.addEdge("knows", p13, "since", "1970-01-01");
        JanusGraphEdge l2 = p42.addEdge("knows", p128, "since", "2000-01-01");
        JanusGraphEdge l3 = p128.addEdge("read", b1024, "language", "en");
        JanusGraphEdge l4 = p2048.addEdge("read", b1024, "language", "fr");
        testGraph.tx().commit();

        String expectedJson = String.format(
                "{" +
                "  'nodes': [" +
                "    {'id': '%1$s', 'type': 'person', 'props': {'name': 'Peter'}}," +
                "    {'id': '%2$s', 'type': 'person', 'props': {'nickname': 'Tom'}}," +
                "    {'id': '%3$s', 'type': 'person', 'props': {'name': 'Frank'}}," +
                "    {'id': '%4$s', 'type': 'book', 'props': {'title': 'Tom Sawyer'}}" +
                "  ], " +
                "  'links': [" +
                "    {'id': '%5$s', 'type': 'knows', 'sourceId': '%1$s', 'targetId': '%2$s', 'props': {'since': '1970-01-01'}}," +
                "    {'id': '%6$s', 'type': 'knows', 'sourceId': '%1$s', 'targetId': '%3$s', 'props': {'since': '2000-01-01'}}," +
                "    {'id': '%7$s', 'type': 'read', 'sourceId': '%3$s', 'targetId': '%4$s', 'props': {'language': 'en'}}" +
                "  ]" +
                "}", p42.id(), p13.id(), p128.id(), b1024.id(), l1.id(), l2.id(), l3.id());

        this.mvc.perform(get(String.format("/api/node/_id/%s/_neighbors", p42.id()))
                .param("limit", "3")
                .param("depth", "2")
                .param("nodeTypes", "book|person")
                .param("linkTypes", "read|knows")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetSuggestionDefault() throws Exception {
        JanusGraphVertex p1 = testGraph.addVertex(T.label, "person", "name", "Peter Smith");
        JanusGraphVertex p2 = testGraph.addVertex(T.label, "person", "nickname", "Tom", "name", "Thomas Thomasson");
        JanusGraphVertex p3 = testGraph.addVertex(T.label, "person", "name", "Peter Miller");
        JanusGraphVertex p4 = testGraph.addVertex(T.label, "book", "name", "Schwarzer Peter");
        JanusGraphVertex p5 = testGraph.addVertex(T.label, "person", "name", "Pete Miller");
        JanusGraphVertex p6 = testGraph.addVertex(T.label, "person", "name", "Hans-Peter Miller");
        testGraph.tx().commit();
        String expectedJson = String.format(
                "[" +
                "  {'id': '%1$s', 'type': 'person', 'name': 'Peter Smith'}," +
                "  {'id': '%2$s', 'type': 'person', 'name': 'Peter Miller'}," +
                "  {'id': '%3$s', 'type': 'book', 'name': 'Schwarzer Peter'}," +
                "  {'id': '%4$s', 'type': 'person', 'name': 'Hans-Peter Miller'}" +
                "]", p1.id(), p3.id(), p4.id(), p6.id()
        );

        this.mvc.perform(get("/api/node/_suggest/Peter")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        expectedJson = String.format(
                "[" +
                "  {'id': '%1$s', 'type': 'person', 'name': 'Thomas Thomasson'}" +
                "]", p2.id()
        );

        this.mvc.perform(get("/api/node/_suggest/Thomas")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        expectedJson = String.format(
                "[" +
                "  {'id': '%1$s', 'type': 'person', 'name': 'Peter Miller'}," +
                "  {'id': '%2$s', 'type': 'person', 'name': 'Pete Miller'}," +
                "  {'id': '%3$s', 'type': 'person', 'name': 'Hans-Peter Miller'}" +
                "]", p3.id(), p5.id(), p6.id()
        );

        this.mvc.perform(get("/api/node/_suggest/Miller Pet")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetSuggestionLimited() throws Exception {

        JanusGraphVertex p1 = testGraph.addVertex(T.label, "person", "name", "Peter Smith");
        JanusGraphVertex p2 = testGraph.addVertex(T.label, "person", "nickname", "Tom", "name", "Thomas Thomasson");
        JanusGraphVertex p3 = testGraph.addVertex(T.label, "person", "name", "Peter Miller");
        JanusGraphVertex p4 = testGraph.addVertex(T.label, "book", "name", "Schwarzer Peter");
        JanusGraphVertex p5 = testGraph.addVertex(T.label, "person", "name", "Pete Miller");
        JanusGraphVertex p6 = testGraph.addVertex(T.label, "person", "name", "Hans-Peter Miller");
        testGraph.tx().commit();
        String expectedJson = String.format(
                "[" +
                "  {'id': '%1$s', 'type': 'person', 'name': 'Peter Smith'}," +
                "  {'id': '%2$s', 'type': 'person', 'name': 'Peter Miller'}," +
                "  {'id': '%4$s', 'type': 'person', 'name': 'Hans-Peter Miller'}" +
                "]", p1.id(), p3.id(), p4.id(), p6.id()
        );

        this.mvc.perform(get("/api/node/_suggest/Peter")
                .param("limit", "3")
                .param("nodeTypes", "article|person")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        expectedJson = String.format(
                "[" +
                        "  {'id': '%1$s', 'type': 'person', 'name': 'Thomas Thomasson'}" +
                        "]", p2.id()
        );

        this.mvc.perform(get("/api/node/_suggest/Thomas")
                .param("limit", "3")
                .param("nodeTypes", "article|person")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        expectedJson = String.format(
                "[" +
                "  {'id': '%1$s', 'type': 'person', 'name': 'Peter Miller'}," +
                "  {'id': '%2$s', 'type': 'person', 'name': 'Pete Miller'}," +
                "  {'id': '%3$s', 'type': 'person', 'name': 'Hans-Peter Miller'}" +
                "]", p3.id(), p5.id(), p6.id()
        );


        this.mvc.perform(get("/api/node/_suggest/Miller Pet")
                .param("limit", "3")
                .param("nodeTypes", "article|person")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetDetails() throws Exception {
        String expectedHtml = "<h1>No details for node: node_42</h1>";

        this.mvc.perform(get("/api/node/_id/node_42/_details")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedHtml));
    }
}
