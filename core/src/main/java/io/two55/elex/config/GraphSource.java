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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;

public interface GraphSource<T extends Graph> {
    T graph();

    default AutoTransaction<T> autoRollback() {
        return new AutoTransaction<T>(graph(), false);
    }
    default AutoTransaction<T> autoCommit() {
        return new AutoTransaction<T>(graph(), true);
    }

    class AutoTransaction<T extends Graph> implements AutoCloseable {
        public final T graph;
        public final boolean onCloseCommit;
        public AutoTransaction(T graph, boolean onCloseCommit) {
            this.graph = graph;
            this.onCloseCommit = onCloseCommit;
        }

        public GraphTraversalSource traversal() {
            return graph.traversal();
        }

        @Override
        public void close() {
            if(graph.tx().isOpen()) {
                if(onCloseCommit) {
                    graph.tx().commit();
                } else {
                    graph.tx().rollback();
                }
            }
        }
    }
}
