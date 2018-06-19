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

package io.two55.elex.dao.janusgraph;

import io.two55.elex.beans.Suggestion;
import io.two55.elex.config.GraphSource;
import io.two55.elex.dao.impl.SuggestionDAODefaultImpl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.attribute.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
@ConditionalOnProperty(name = "graph.type", havingValue = "janusgraph", matchIfMissing=true)
public class SuggestionDAOJanusGraph extends SuggestionDAODefaultImpl<GraphSource<?>> {

    @Autowired
    public SuggestionDAOJanusGraph(GraphSource<JanusGraph> graphSource) {
        super(graphSource);
    }

    protected Optional<String> lastToken(String suggestionString) {
        return tokenize(suggestionString)
                .reduce((first, second) -> second);
    }

    @Override
    public List<Suggestion> suggest(String suggestionString, int limit, Set<String> nodeTypes) throws Exception {
        Optional<String> lastTokenOpt = lastToken(suggestionString);
        if(!lastTokenOpt.isPresent()) {
            return Collections.emptyList();
        }
        // extend the logic so that the last word/token can be incomplete
        String lastToken = lastTokenOpt.get();

        Set<String> firstTokens = tokenizeSet(suggestionString);
        firstTokens.remove(lastToken);

        try(GraphSource.AutoTransaction<?> g = graphSource.autoRollback()) {
            GraphTraversal<Vertex, Vertex> query = g.traversal().V();

            if (nodeTypes!=null && nodeTypes.size() > 0) {
                String[] nodeTypeArray = nodeTypes.toArray(new String[nodeTypes.size()]);
                // filter nodeTypes
                query = query.hasLabel(
                        nodeTypeArray[0],
                        Arrays.copyOfRange(nodeTypeArray, 1, nodeTypeArray.length));
            }
            if(firstTokens.size()>0) {
                // extend query by 'normal terms'
                for (String token : firstTokens) {
                    query = query.has("name", Text.textContains(token));
                }
                // extend query by 'prefix term' (unfinished word)
                query = query.has("name", Text.textContainsPrefix(lastToken));
            } else {
                // query is equal to the last (and only) token
                query = query.has("name", Text.textContains(lastToken));
            }

            // transform query results into Suggestion beans
            return query
                    .range(0, limit <= 0 ? MAX_SUGGESTIONS : Math.min(MAX_SUGGESTIONS, limit))
                    .toStream()
                    .map(Suggestion::fromVertex)
                    .collect(Collectors.toList());
        }
    }
}
