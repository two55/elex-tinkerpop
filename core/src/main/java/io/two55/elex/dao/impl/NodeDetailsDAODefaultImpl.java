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

import io.two55.elex.dao.NodeDetailsDAO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "graph.type", havingValue = "default", matchIfMissing=true)
public class NodeDetailsDAODefaultImpl implements NodeDetailsDAO {
    @Override
    public String detailsOf(String nodeId) {
        return String.format("<h1>No details for node: %s</h1>",nodeId);
    }
}
