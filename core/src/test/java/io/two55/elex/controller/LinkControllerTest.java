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
import io.two55.elex.dao.LinkDetailsDAO;
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
@WebMvcTest(LinkController.class)
public class LinkControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LinkDetailsDAO linkDetailsDAO;

    @Test
    public void testGetDetails() throws Exception {
        String expectedHtml = "<h1>Details for link: link_id_1</h1>";

        given(this.linkDetailsDAO.detailsOf("link_id_1"))
                .willReturn(expectedHtml);
        this.mvc.perform(get("/api/link/_id/link_id_1/_details")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedHtml));
    }
}
