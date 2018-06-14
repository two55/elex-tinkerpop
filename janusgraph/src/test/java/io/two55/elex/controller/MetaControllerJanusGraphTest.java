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

import static org.mockito.BDDMockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {ElexApp.class})
public class MetaControllerJanusGraphTest extends JanusGraphTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private GraphSource<JanusGraph> graphSource;

    @Before
    public void bindBackend() {
        given(graphSource.graph())
                .willReturn(testGraph);
    }

    @Test
    public void testGetMetaInformation() throws Exception {
        // add test instances of type book and article to the graph
        JanusGraphVertex p1 = testGraph.addVertex(T.label, "person");
        JanusGraphVertex p2 = testGraph.addVertex(T.label, "person");
        JanusGraphVertex a1 = testGraph.addVertex(T.label, "article");
        // type not contained in the graph: company
        p1.addEdge("knows", p2);
        a1.addEdge("mentions", p2);
        // type not contained in the graph: works for
        testGraph.tx().commit();

        // knows, works for, mentions
        String expectedJson =
                "{" +
                "  'nodes': {" +
                "    'person': {'type': 'person', 'color': '#3693D2', 'icon': 'person', 'explore': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}," +
                "    'article': {'type': 'article', 'color': '#009E73', 'icon': 'description', 'explore': false, 'hasDetails': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}," +
                "    '__default': {'color': '#000000', 'icon': 'brightness_1', 'explore': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}" +
                "  }, " +
                "  'links': {" +
                "    'knows': {'type': 'knows', 'color': '#FF3977', 'isDirected': false, 'explore': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}," +
                "    'mentions': {'type': 'mentions', 'color': '#b3b3b3','isDirected': true, 'explore': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}," +
                "    '__default': {'color': '#b3b3b3','isDirected': true, 'explore': true, 'labelAttribute': 'name', 'weightAttribute': '__weight'}" +
                "  }" +
                "}";

        this.mvc.perform(get("/api/meta")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }
}
