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
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "graph.type", havingValue = "janusgraph", matchIfMissing=true)
public class JanusGraphConfig {
    private static final Logger log = LoggerFactory.getLogger(JanusGraphConfig.class);

    private String configResource;

    public JanusGraphConfig(@Value("${graph.janusgraph.config}") String configResource) {
        this.configResource = configResource;
    }

    @Bean
    public Source graphSource() {
        return new Source(this.configResource);
    }

    public static class Source implements GraphSource<JanusGraph> {
        private JanusGraph janusGraph;

        public Source(String configResource) {
            this.init(configResource);
        }

        JanusGraph init(String configResource) {
            if (janusGraph != null) {
                janusGraph.close();
            }
            try {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                janusGraph = JanusGraphFactory.open(new PropertiesConfiguration(classloader.getResource(configResource)));
                return janusGraph;
            } catch (ConfigurationException e) {
                // log.error("Unable to load JanusGraph config resource: " + configResource, e);
                throw new IllegalStateException("Unable to load JanusGraph config resource: " + configResource, e);
            }
        }

        public JanusGraph graph() {
            return janusGraph;
        }
    }
}
