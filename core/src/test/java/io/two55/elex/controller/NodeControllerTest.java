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

import io.two55.elex.beans.Link;
import io.two55.elex.beans.Neighbors;
import io.two55.elex.beans.Node;
import io.two55.elex.beans.Suggestion;
import io.two55.elex.dao.NeighborhoodDAO;
import io.two55.elex.dao.NodeDetailsDAO;
import io.two55.elex.dao.SuggestionDAO;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(NodeController.class)
public class NodeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SuggestionDAO suggestionDAO;

    @MockBean
    private NeighborhoodDAO neighborhoodDAO;

    @MockBean
    private NodeDetailsDAO nodeDetailsDAO;

    @Test
    public void testGetNeighborhoodDefault() throws Exception {
        Neighbors actualNeighbors = new Neighbors();
        actualNeighbors.add(new Node(
                "node_42",
                "person",
                Maps.newHashMap("name", "Peter")));
        actualNeighbors.add(new Node(
                "node_13",
                "person",
                Maps.newHashMap("nickname", "Tom")));
        actualNeighbors.add(new Link(
                "link_id_1",
                "knows",
                "node_42",
                "node_13",
                Maps.newHashMap("since", "1970-01-01")));
        String expectedJson =
                "{" +
                        "  'links': [" +
                        "    {'id': 'link_id_1', 'type': 'knows', 'sourceId': 'node_42', 'targetId': 'node_13', 'props': {'since': '1970-01-01'}}" +
                        "  ], " +
                        "  'nodes': [" +
                        "    {'id': 'node_42', 'type': 'person', 'props': {'name': 'Peter'}}," +
                        "    {'id': 'node_13', 'type': 'person', 'props': {'nickname': 'Tom'}}" +
                        "  ]" +
                        "}";

        given(this.neighborhoodDAO.neighbors("node_42", -1, 1, emptySet(), emptySet()))
                .willReturn(actualNeighbors);
        this.mvc.perform(get("/api/node/_id/node_42/_neighbors")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetNeighborhoodComplex() throws Exception {
        Neighbors actualNeighbors = new Neighbors();
        actualNeighbors.add(new Node(
                "node_42",
                "person",
                Maps.newHashMap("name", "Peter")));
        actualNeighbors.add(new Node(
                "node_13",
                "person",
                Maps.newHashMap("nickname", "Tom")));
        actualNeighbors.add(new Node(
                "node_128",
                "person",
                Maps.newHashMap("name", "Frank")));
        actualNeighbors.add(new Node(
                "node_1024",
                "book",
                Maps.newHashMap("title", "Tom Sawyer")));
        actualNeighbors.add(new Link(
                "link_id_1",
                "knows",
                "node_42",
                "node_13",
                Maps.newHashMap("since", "1970-01-01")));
        actualNeighbors.add(new Link(
                "link_id_2",
                "knows",
                "node_42",
                "node_128",
                Maps.newHashMap("since", "2000-01-01")));
        actualNeighbors.add(new Link(
                "link_id_3",
                "read",
                "node_128",
                "node_1024",
                Maps.newHashMap("language", "en")));
        String expectedJson =
                "{" +
                "  'links': [" +
                "    {'id': 'link_id_1', 'type': 'knows', 'sourceId': 'node_42', 'targetId': 'node_13', 'props': {'since': '1970-01-01'}}," +
                "    {'id': 'link_id_2', 'type': 'knows', 'sourceId': 'node_42', 'targetId': 'node_128', 'props': {'since': '2000-01-01'}}," +
                "    {'id': 'link_id_3', 'type': 'read', 'sourceId': 'node_128', 'targetId': 'node_1024', 'props': {'language': 'en'}}" +
                "  ], " +
                "  'nodes': [" +
                "    {'id': 'node_42', 'type': 'person', 'props': {'name': 'Peter'}}," +
                "    {'id': 'node_13', 'type': 'person', 'props': {'nickname': 'Tom'}}," +
                "    {'id': 'node_128', 'type': 'person', 'props': {'name': 'Frank'}}," +
                "    {'id': 'node_1024', 'type': 'book', 'props': {'title': 'Tom Sawyer'}}" +
                "  ]" +
                "}";

        given(this.neighborhoodDAO.neighbors("node_42", 3, 2, new HashSet<>(asList("person", "book")), new HashSet<>(asList("read", "knows"))))
                .willReturn(actualNeighbors);
        this.mvc.perform(get("/api/node/_id/node_42/_neighbors")
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
        List<Suggestion> actualNeighbors = new ArrayList<>();
        actualNeighbors.add(new Suggestion(
                "node_42",
                "person",
                "Peter"));
        actualNeighbors.add(new Suggestion(
                "node_13",
                "person",
                "Hans-Peter"));
        String expectedJson =
                "[" +
                        "  {'id': 'node_42', 'type': 'person', 'name': 'Peter'}," +
                        "  {'id': 'node_13', 'type': 'person', 'name': 'Hans-Peter'}" +
                        "]";

        given(this.suggestionDAO.suggest("Peter", -1, emptySet()))
                .willReturn(actualNeighbors);
        this.mvc.perform(get("/api/node/_suggest/Peter")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetSuggestionComplex() throws Exception {
        List<Suggestion> actualNeighbors = new ArrayList<>();
        actualNeighbors.add(new Suggestion(
                "node_42",
                "person",
                "Peter"));
        actualNeighbors.add(new Suggestion(
                "node_13",
                "person",
                "Hans-Peter"));
        String expectedJson =
                "[" +
                "  {'id': 'node_42', 'type': 'person', 'name': 'Peter'}," +
                "  {'id': 'node_13', 'type': 'person', 'name': 'Hans-Peter'}" +
                "]";

        given(this.suggestionDAO.suggest("Peter", 10, new HashSet<>(asList("person", "book"))))
                .willReturn(actualNeighbors);
        this.mvc.perform(get("/api/node/_suggest/Peter")
                .param("limit", "10")
                .param("nodeTypes", "book|person")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void testGetDetails() throws Exception {
        String expectedHtml = "<h1>Details for node: node_42</h1>";

        given(this.nodeDetailsDAO.detailsOf("node_42"))
                .willReturn(expectedHtml);
        this.mvc.perform(get("/api/node/_id/node_42/_details")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedHtml));
    }
}
