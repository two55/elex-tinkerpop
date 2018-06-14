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

import io.two55.elex.beans.SchemaMetaInformation;
import io.two55.elex.config.DisplayConfig;
import io.two55.elex.dao.MetaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "graph.type", havingValue = "default", matchIfMissing=true)
public class MetaDAODefaultImpl implements MetaDAO {
    protected DisplayConfig displayConfig;

    @Autowired
    public void setDisplayConfig(DisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }

    public SchemaMetaInformation meta() {
        SchemaMetaInformation config = new SchemaMetaInformation();

        // setup nodes
        config.nodes.put("__default", new SchemaMetaInformation.Node());

        displayConfig
                .getNodes()
                .forEach(config::add);

        // setup links
        config.links.put("__default", new SchemaMetaInformation.Link());

        displayConfig
                .getLinks()
                .forEach(config::add);

        return config;
    }

    // TODO remove when frontend supports default type settings for nodes/links correctly
    private SchemaMetaInformation cache = null;

    SchemaMetaInformation cache() {
        if(cache==null) {
            cache = meta();
        }
        return cache;
    }
}
