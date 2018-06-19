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
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary
@ConditionalOnProperty(name = "graph.type", havingValue = "neo4j", matchIfMissing=true)
public class SuggestionDAONeo4j extends SuggestionDAODefaultImpl<GraphSource<Neo4jGraph>> {

    @Autowired
    public SuggestionDAONeo4j(GraphSource<Neo4jGraph> graphSource) {
        super(graphSource);
    }

    protected Optional<String> lastToken(String suggestionString) {
        return tokenize(suggestionString)
                .reduce((first, second) -> second);
    }

    @Override
    public List<Suggestion> suggest(String suggestionString, int limit, Set<String> nodeTypes) throws Exception {
        List<String> tokens = tokenize(suggestionString, false).collect(Collectors.toList());
        if(tokens.size() < 1) {
            return Collections.emptyList();
        }

        try(GraphSource.AutoTransaction<Neo4jGraph> g = graphSource.autoRollback()) {

            StringBuilder cypherQuery = new StringBuilder("MATCH (n) WHERE ");
            // extend query by 'prefix term' (unfinished word)
            cypherQuery.append("n.name CONTAINS '").append(tokens.remove(tokens.size() - 1)).append("'");

            for (String token : tokens) {
                // extend query by 'normal terms'
                cypherQuery.append(" AND n.name CONTAINS '").append(token).append("'");
            }
            if (nodeTypes != null && nodeTypes.size() > 0) {
                // filter nodeTypes
                cypherQuery.append(" AND (");
                String[] nodeTypeArray = nodeTypes.toArray(new String[nodeTypes.size()]);
                cypherQuery.append("n:").append(nodeTypeArray[0]);
                for (String nodeType : Arrays.copyOfRange(nodeTypeArray, 1, nodeTypeArray.length)) {
                    cypherQuery.append(" OR n:").append(nodeType);
                }
                cypherQuery.append(") ");
            }
            cypherQuery.append("RETURN ID(n) as id, n.name as name, labels(n) as label LIMIT ").append(limit <= 0 ? MAX_SUGGESTIONS : Math.min(MAX_SUGGESTIONS, limit));
            GraphTraversal<?, ?> query = g.graph.cypher(cypherQuery.toString());

            Function<Object, Suggestion> toSuggestion = (Object o) -> {
                Map m = (Map) o;
                String id = m.get("id").toString();
                List<?> labels = (List) m.get("label");
                String label = labels!=null && labels.size()>0?labels.get(0).toString():null;
                String name = m.get("name").toString();
                return new Suggestion(id, label, name);
            };

            // transform query results into Suggestion beans
            return query
                    .toStream()
                    .map(toSuggestion)
                    .collect(Collectors.toList());
        }
    }
}
