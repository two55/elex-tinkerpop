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

package io.two55.elex.controller;

import io.two55.elex.beans.Neighbors;
import io.two55.elex.beans.Suggestion;
import io.two55.elex.dao.NeighborhoodDAO;
import io.two55.elex.dao.NodeDetailsDAO;
import io.two55.elex.dao.SuggestionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class NodeController {
    private static final Logger log = LoggerFactory.getLogger(NodeController.class);

    private final SuggestionDAO suggestionDAO;
    private final NodeDetailsDAO nodeDetailsDAO;
    private final NeighborhoodDAO neighborhoodDAO;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public NodeController(SuggestionDAO suggestionDAO,
                          NodeDetailsDAO nodeDetailsDAO,
                          NeighborhoodDAO neighborhoodDAO) {
        this.suggestionDAO = suggestionDAO;
        this.nodeDetailsDAO = nodeDetailsDAO;
        this.neighborhoodDAO = neighborhoodDAO;
    }

    public static Set<String> parseTypes(String typeDefinitions) {
        if(typeDefinitions==null || typeDefinitions.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(typeDefinitions.split("\\|")));
    }

    @CrossOrigin
    @RequestMapping(value = "/api/node/_id/{_id}/_neighbors", method = RequestMethod.GET)
    public Neighbors getNeighborhood(@PathVariable("_id") String nodeId,
                                     @RequestParam(
                                             name = "limit",
                                             required = false,
                                             defaultValue = "-1") int limit,
                                     @RequestParam(
                                             name = "depth",
                                             required = false,
                                             defaultValue = "1") int depth,
                                     @RequestParam(
                                             name = "nodeTypes",
                                             required = false,
                                             defaultValue = "") String nodeTypeStr,
                                     @RequestParam(
                                             name = "linkTypes",
                                             required = false,
                                             defaultValue = "") String linkTypeStr) {
        try {
            Set<String> nodeTypes = parseTypes(nodeTypeStr);
            Set<String> linkTypes = parseTypes(linkTypeStr);
            return neighborhoodDAO.neighbors(nodeId, limit, depth, nodeTypes, linkTypes);
        } catch (Exception e) {
            log.error(String.format(
                    "Error while getting neighbors for %s (with node filters %s, link filters %s, and limit %d)!",
                    nodeId,
                    nodeTypeStr,
                    linkTypeStr,
                    limit), e);
            return null;
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/api/node/_suggest/{suggestion_string}", method = RequestMethod.GET)
    public List<Suggestion> getSuggestion(@PathVariable("suggestion_string") String suggestionString,
                                          @RequestParam(
                                                  name = "limit",
                                                  required = false,
                                                  defaultValue = "-1") int limit,
                                          @RequestParam(
                                                  name = "nodeTypes",
                                                  required = false,
                                                  defaultValue = "") String nodeTypeStr) {
        try {
            Set<String> nodeTypes = parseTypes(nodeTypeStr);
            return suggestionDAO.suggest(suggestionString, limit, nodeTypes);
        } catch (Exception e) {
            log.error(String.format(
                    "Error while getting suggestions for %s (with node filters %s and limit %d)!",
                    suggestionString,
                    nodeTypeStr,
                    limit), e);
            return null;
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/api/node/_id/{_id}/_details", method = RequestMethod.GET)
    public String getDetails(@PathVariable("_id") String nodeId) {
        try {
            return nodeDetailsDAO.detailsOf(nodeId);
        } catch (Exception e) {
            log.error(String.format(
                    "Error while getting details for node %s!", nodeId));
            return null;
        }
    }
}
