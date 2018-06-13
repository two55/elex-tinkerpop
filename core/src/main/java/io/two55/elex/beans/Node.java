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
import io.two55.utils.StreamUtils;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {
    private static final Logger log = LoggerFactory.getLogger(Node.class);
    public final String id;
    public final String type;

    // FIXME remove when frontend supports type settings for nodes correctly
    public Long weight = null;
    public Long size = null;
    public Long __weight = null;
    public Long __size = null;
    public final Map<String, Object> props;

    public Node(String id, String type, Map<String, Object> props) {
        this.id = id;
        this.type = type;
        this.props = props;
    }

    public static Node fromVertex(Vertex v) {
        if (log.isDebugEnabled()) {
            log.debug("extracting node from: " + v.toString());
        }

        // get property map
        Map<String, Object> properties = StreamUtils
                .toStream(v.properties())
                .collect(Collectors.toMap(
                        Property::key,
                        Property::value,
                        (s1, s2) -> s1 + ", " + s2));

        // create the node instance
        return new Node(v.id().toString(),
                v.label(),
                properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id) &&
                Objects.equals(type, node.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "Node{" +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", props=" + props +
                '}';
    }
}