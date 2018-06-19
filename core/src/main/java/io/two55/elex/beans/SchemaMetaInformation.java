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
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaMetaInformation {
    public final Map<String, Node> nodes = new HashMap<>();
    public final Map<String, Link> links = new HashMap<>();

    public SchemaMetaInformation add(Node node) {
        nodes.put(node.type, node);
        return this;
    }
    public SchemaMetaInformation add(Link link) {
        links.put(link.type, link);
        return this;
    }

    @Override
    public String toString() {
        return "SchemaMetaInformation{" +
                "nodes=" + nodes +
                ", links=" + links +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Node extends MetaInfoItem<Node> {
        String icon;
        public Node() {
            this(null);
        }
        public Node(String type) {
            this(type, "#000000", "brightness_1");
        }
        public Node(String type, String color, String icon) {
            this.type = type;
            this.color = color;
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }

        public Node setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        @Override
        public String toString() {
            return "MetaInfoItem.Node{" +
                    "type='" + type + '\'' +
                    ", labelAttribute='" + labelAttribute + '\'' +
                    ", weightAttribute='" + weightAttribute + '\'' +
                    ", color='" + color + '\'' +
                    ", icon='" + icon + '\'' +
                    ", explore=" + explore +
                    ", hasDetails=" + hasDetails +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link extends MetaInfoItem<Link> {
        boolean isDirected;
        public Link() {
            this(null);
        }

        public Link(String type) {
            this(type, "#b3b3b3", true);
        }

        public Link(String type, String color, boolean isDirected) {
            this.type = type;
            this.color = color;
            this.isDirected = isDirected;
        }

        public boolean getIsDirected() {
            return isDirected;
        }

        public Link setIsDirected(boolean directed) {
            isDirected = directed;
            return this;
        }

        @Override
        public String toString() {
            return "MetaInfoItem.Link{" +
                    "type='" + type + '\'' +
                    ", labelAttribute='" + labelAttribute + '\'' +
                    ", weightAttribute='" + weightAttribute + '\'' +
                    ", color='" + color + '\'' +
                    ", explore=" + explore +
                    ", isDirected=" + isDirected +
                    ", hasDetails=" + hasDetails +
                    '}';
        }
    }

    public static abstract class MetaInfoItem<T extends MetaInfoItem<T>> {
        String type;
        String labelAttribute = "name";
        String weightAttribute = "__weight";
        String color = null;
        boolean explore = true;
        Boolean hasDetails = null;

        public String getType() {
            return type;
        }

        public T setType(String type) {
            this.type = type;
            return (T) this;
        }

        public String getLabelAttribute() {
            return labelAttribute;
        }

        public T setLabelAttribute(String labelAttribute) {
            this.labelAttribute = labelAttribute;
            return (T) this;
        }

        public String getWeightAttribute() {
            return weightAttribute;
        }

        public T setWeightAttribute(String weightAttribute) {
            this.weightAttribute = weightAttribute;
            return (T) this;
        }

        public String getColor() {
            return color;
        }

        public T setColor(String color) {
            this.color = color;
            return (T) this;
        }

        public boolean getExplore() {
            return explore;
        }

        public T setExplore(boolean explore) {
            this.explore = explore;
            return (T) this;
        }

        public Boolean getHasDetails() {
            return hasDetails;
        }

        public T setHasDetails(Boolean hasDetails) {
            this.hasDetails = hasDetails;
            return (T) this;
        }
    }
}
