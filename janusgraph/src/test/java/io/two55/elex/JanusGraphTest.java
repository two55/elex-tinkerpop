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

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.Mapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class JanusGraphTest {
    protected JanusGraph testGraph = null;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void initGraph() throws Exception {
        synchronized (this) {
            dropTestGraph();
            // create a temporary lucene folder for the tests
            testGraph = createTestGraph(temporaryFolder.newFolder());
        }
    }

    @After
    public void dropTestGraph() throws Exception {
        synchronized (this) {
            if (testGraph != null) {
                JanusGraphFactory.drop(testGraph);
                // cleanup previous index files in lucene directory shouldn't be necessary after dropping the graph:
                // FileUtils.deleteDirectory(luceneDirectory);
            }
            testGraph = null;
        }
    }

    private static JanusGraph createTestGraph(File luceneDirectory) {
        JanusGraph graph = JanusGraphFactory.build()
                .set("gremlin.graph", "org.janusgraph.core.JanusGraphFactory")
                // don't use custom vertex IDs
                .set("graph.set-vertex-id", "false")
                // enable default schema maker enabled
                .set("schema.default", "default")
                // disable the batch loading
                .set("storage.batch-loading", "false")
                // use in-memory storage
                .set("storage.backend", "inmemory")
                .set("index.search.backend", "lucene")
                .set("index.search.directory", luceneDirectory.getAbsolutePath())
                .open();

        // enable text index on name attribute
        JanusGraphManagement man = graph.openManagement();
        PropertyKey nameProp = man.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        JanusGraphManagement.IndexBuilder indexBuilder = man.buildIndex("nameContains", Vertex.class);
        indexBuilder.addKey(nameProp, Mapping.TEXT.asParameter());
        indexBuilder.buildMixedIndex("search");

        man.commit();

        return graph;
    }
}
