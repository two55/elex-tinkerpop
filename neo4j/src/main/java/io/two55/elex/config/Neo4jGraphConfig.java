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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "graph.type", havingValue = "neo4j", matchIfMissing=true)
public class Neo4jGraphConfig {
    private static final Logger log = LoggerFactory.getLogger(Neo4jGraphConfig.class);

    private String configResource;

    public Neo4jGraphConfig(@Value("${graph.neo4j.config}") String configResource) {
        this.configResource = configResource;
    }

    @Bean
    public Neo4jGraphConfig.Source graphSource() {
        return new Neo4jGraphConfig.Source(this.configResource);
    }

    public static class Source implements GraphSource<Neo4jGraph> {
        private Neo4jGraph neo4jGraph;

        public Source(String configResource) {
            this.init(configResource);
        }

        Neo4jGraph init(String configResource) {
            if (neo4jGraph != null) {
                try {
                    neo4jGraph.close();
                } catch (Exception e) {
                    // log.error("Unable to close Neo4jGraph instance!", e);
                    throw new IllegalStateException("Unable to close Neo4jGraph instance!", e);
                }
            }
            try {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                neo4jGraph = Neo4jGraph.open(new PropertiesConfiguration(classloader.getResource(configResource)));
                return neo4jGraph;
            } catch (ConfigurationException e) {
                // log.error("Unable to load Neo4jGraph config resource: " + configResource, e);
                throw new IllegalStateException("Unable to load Neo4jGraph config resource: " + configResource, e);
            }
        }

        public Neo4jGraph graph() {
            return neo4jGraph;
        }
    }
}
