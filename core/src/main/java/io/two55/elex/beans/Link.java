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
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {
    private static final Logger log = LoggerFactory.getLogger(Link.class);
    public final String id;
    public final String type;
    public final String sourceId;
    public final String targetId;

    // FIXME remove when frontend supports type settings for links correctly
    public Long weight = null;
    public Long size = null;
    public Boolean isDirected = null;
    public Long __weight = null;
    public Long __size = null;
    public final Map<String, Object> props;

    public Link(String id, String type, String sourceId, String targetId, Map<String, Object> props) {
        this.id = id;
        this.type = type;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.props = props;
    }

    public static Link fromEdge(Edge e) {
        if (log.isDebugEnabled()) {
            log.debug("extracting link from: " + e.toString());
        }
        // get property map
        Map<String, Object> properties = StreamUtils
                .toStream(e.properties())
                .collect(Collectors.toMap(
                        Property::key,
                        Property::value));

        // create the link instance
        return new Link(e.id().toString(),
                e.label(),
                e.outVertex().id().toString(),
                e.inVertex().id().toString(),
                properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link)) return false;
        Link link = (Link) o;
        return Objects.equals(id, link.id) &&
                Objects.equals(type, link.type) &&
                Objects.equals(sourceId, link.sourceId) &&
                Objects.equals(targetId, link.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, sourceId, targetId);
    }

    @Override
    public String toString() {
        return "Link{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", props=" + props +
                '}';
    }
}