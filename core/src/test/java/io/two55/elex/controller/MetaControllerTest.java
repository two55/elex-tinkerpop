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

import io.two55.elex.beans.SchemaMetaInformation;
import io.two55.elex.dao.MetaDAO;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.*;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(MetaController.class)
public class MetaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MetaDAO metaDAO;

    @Test
    public void testGetMetaInformation() throws Exception {
        SchemaMetaInformation actualInfo = new SchemaMetaInformation();
        actualInfo.add(new SchemaMetaInformation.Node("person").setColor("#FF0000").setIcon("person"));
        actualInfo.add(new SchemaMetaInformation.Node("book").setExplore(false).setHasDetails(true));
        actualInfo.add(new SchemaMetaInformation.Link("knows").setColor("#00FF00").setIsDirected(false));
        actualInfo.add(new SchemaMetaInformation.Link("wrote"));
        actualInfo.add(new SchemaMetaInformation.Link("read").setLabelAttribute("date").setWeightAttribute("n"));

        String expectedJson =
                "{" +
                "  'nodes': {" +
                "    'person': {'type': 'person', 'color': '#FF0000', 'icon': 'person'}," +
                "    'book': {'type': 'book', 'explore': false, 'hasDetails': true}" +
                "  }, " +
                "  'links': {" +
                "    'knows': {'type': 'knows', 'color': '#00FF00', 'isDirected': false}," +
                "    'wrote': {'type': 'wrote'}," +
                "    'read': {'type': 'read', 'labelAttribute': 'date', 'weightAttribute': 'n'}" +
                "  }" +
                "}";

        given(this.metaDAO.meta())
                .willReturn(actualInfo);
        this.mvc.perform(get("/api/meta")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }
}
