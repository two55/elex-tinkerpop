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

import io.two55.elex.beans.SchemaMetaInformation;
import io.two55.elex.dao.MetaDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetaController {
    private static final Logger log = LoggerFactory.getLogger(MetaController.class);

    private MetaDAO metaDAO;
    private SchemaMetaInformation cachedMetaInformation;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public MetaController(MetaDAO metaDAO) {
        this.metaDAO = metaDAO;
        this.resetMetaInformation();
    }

    @RequestMapping(value = "/api/meta", method = RequestMethod.GET)
    public SchemaMetaInformation getMetaInformation() {
        if(cachedMetaInformation == null) {
            synchronized (this) {
                // try to synchronize assignment of the schema info instance
                if(cachedMetaInformation == null) {
                    try {
                        cachedMetaInformation = metaDAO.meta();
                    } catch (Exception e) {
                        log.error("Error while getting schema meta information!", e);
                    }
                }
            }

        }
        return cachedMetaInformation;
    }

    @RequestMapping(value = "/api/meta/reset", method = RequestMethod.GET)
    public boolean resetMetaInformation() {
        if(cachedMetaInformation != null) {
            synchronized (this) {
                cachedMetaInformation = null;
                return true;
            }
        }
        return false;
    }
}
