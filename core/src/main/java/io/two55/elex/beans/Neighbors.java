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

package io.two55.elex.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Neighbors {
    private static final Logger log = LoggerFactory.getLogger(Neighbors.class);
    public Set<Link> links = new HashSet<>();

    public Collection<Node> getNodes() {
        return nodeMap.values();
    }

    public Collection<String> containedNodeIds() {
        return nodeMap.keySet();
    }

    public Optional<String> nodeType(String nodeId) {
        return Optional.ofNullable(nodeMap.get(nodeId)).map(n -> n.type);
    }

    private Map<String, Node> nodeMap = new HashMap<>();

    public void addEdge(Edge e) {
        if (log.isDebugEnabled()) {
            log.debug("adding neighbors based on: " + e.toString());
        }
        // add link entry
        add(Link.fromEdge(e));

        // add node entry for from vertex
        if (!nodeMap.containsKey(e.outVertex().id().toString())) {
            add(Node.fromVertex(e.outVertex()));
        }
        // add node entry to from vertex
        if (!nodeMap.containsKey(e.inVertex().id().toString())) {
            add(Node.fromVertex(e.inVertex()));
        }
    }

    public Neighbors add(Node n) {
        nodeMap.put(n.id, n);
        return this;
    }

    public Neighbors add(Link l) {
        links.add(l);
        return this;
    }

    @Override
    public String toString() {
        return "Neighbors{" +
                "links=" + links +
                ", nodeMap=" + nodeMap +
                '}';
    }
}
