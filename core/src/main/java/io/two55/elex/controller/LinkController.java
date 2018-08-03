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


import io.two55.elex.dao.LinkDetailsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LinkController {
    private static final Logger log = LoggerFactory.getLogger(LinkController.class);

    private LinkDetailsDAO linkDetailsDAO;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public LinkController(LinkDetailsDAO linkDetailsDAO) {
        this.linkDetailsDAO = linkDetailsDAO;
    }

    @CrossOrigin
    @RequestMapping(value = "/api/link/_id/{_id}/_details", method = RequestMethod.GET)
    public String getDetails(@PathVariable("_id") String linkId) {
        try {
            return linkDetailsDAO.detailsOf(linkId);
        } catch (Exception e) {
            log.error(String.format(
                    "Error while getting details for link %s!",
                    linkId), e);
            return null;
        }
    }
}
