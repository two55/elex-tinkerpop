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

package io.two55.elex.config;

import io.two55.elex.beans.SchemaMetaInformation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("graph.display")
public class DisplayConfig {
    private final List<SchemaMetaInformation.Node> nodes = new ArrayList<>();
    private final List<SchemaMetaInformation.Link> links = new ArrayList<>();

    public List<SchemaMetaInformation.Node> getNodes() {
        return this.nodes;
    }
    public List<SchemaMetaInformation.Link> getLinks() {
        return this.links;
    }
}