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
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Suggestion {
    private static final Logger log = LoggerFactory.getLogger(Suggestion.class);
    public final String id;
    public final String type;
    public final String name;

    public Suggestion(String id, String type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Suggestion)) return false;
        Suggestion that = (Suggestion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static Suggestion fromVertex(Vertex v) {
        if (log.isDebugEnabled()) {
            log.debug("extracting suggestion from: " + v.toString());
        }
        // create the suggestion instance
        return new Suggestion(
                v.id().toString(),
                v.label(),
                // TODO enable different suggestion attributes than just "name"
                v.<String>property("name").value());
    }
}
