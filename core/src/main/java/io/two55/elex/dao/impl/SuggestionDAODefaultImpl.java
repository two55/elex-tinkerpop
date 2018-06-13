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

package io.two55.elex.dao.impl;

import io.two55.elex.beans.Suggestion;
import io.two55.elex.config.GraphSource;
import io.two55.elex.dao.SuggestionDAO;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(name = "graph.type", havingValue = "default", matchIfMissing=true)
public class SuggestionDAODefaultImpl implements SuggestionDAO {
    public static int MIN_SUGGESTION_LENGTH = 3;
    public static int MAX_SUGGESTIONS = 50;

    protected GraphSource<?> graphSource;
    
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SuggestionDAODefaultImpl(GraphSource<?> graphSource) {
        this.graphSource = graphSource;
    }

    protected Stream<String> tokenize(String suggestionString) {
        // tokenize like solr StandardTokenizer: split on whitespace and punctuations, where 'periods (dots) that are
        // not followed by whitespace are kept as part of the token, including Internet domain names.
        return Arrays
                .stream(suggestionString.split("[\\s!\"#$%&'()*+,\\-/:;<=>?@\\[\\\\\\]_`{|}~]+"))
                .map(tok -> StringUtils.stripEnd(tok, "."))
                .filter(tok -> !tok.isEmpty())
                .map(String::toLowerCase);
    }

    protected Set<String> tokenizeSet(String suggestionString) {
        return tokenize(suggestionString)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Suggestion> suggest(String suggestionString, int limit, Set<String> nodeTypes) throws Exception {
        if (suggestionString != null && suggestionString.length() >= MIN_SUGGESTION_LENGTH) {
            final Set<String> queryToken = tokenizeSet(suggestionString);

            Predicate<Traverser<Vertex>> containsAllQueryTokens = vt -> {
                VertexProperty vp = vt.get().property("name");
                if(vp.isPresent()) {
                    return tokenizeSet(vp.value().toString()).containsAll(queryToken);
                }
                return false;
            };
            try (GraphSource.AutoTransaction<?> g = graphSource.autoRollback()) {
                // this is a very slow & basic implementation based on gremlin (indices should be used instead)
                // we are just iterating over all vertices and return the ones that contain all token
                GraphTraversal<Vertex, Vertex> query = g.traversal().V();
                if (nodeTypes!=null && nodeTypes.size() > 0) {
                    String[] nodeTypeArray = nodeTypes.toArray(new String[nodeTypes.size()]);
                    // filter nodeTypes
                    query = query.hasLabel(
                            nodeTypeArray[0],
                            Arrays.copyOfRange(nodeTypeArray, 1, nodeTypeArray.length));
                }
                return query
                        .filter(containsAllQueryTokens)
                        .range(0, limit <= 0 ? MAX_SUGGESTIONS : Math.min(MAX_SUGGESTIONS, limit))
                        .toStream()
                        .map(Suggestion::fromVertex)
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
