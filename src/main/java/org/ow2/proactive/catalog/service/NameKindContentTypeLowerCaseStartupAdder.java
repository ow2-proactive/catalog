/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.catalog.service;

import java.util.List;

import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Component
public class NameKindContentTypeLowerCaseStartupAdder {

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Autowired
    CatalogObjectService catalogObjectService;

    @Transactional
    public void synchronizeNameKindAndContentType() {
        log.info("Checking catalog object entities ... ");
        List<CatalogObjectEntity> catalogObjectEntityList = catalogObjectRepository.findWithNullNameKindOrContentType();
        if (!catalogObjectEntityList.isEmpty()) {
            log.info("Resynchronization of name, kind and content type ...");
            for (CatalogObjectEntity objectEntity : catalogObjectEntityList) {
                if (objectEntity.getNameLower() == null) {
                    objectEntity.setNameLower(objectEntity.getId().getName());
                }
                if (objectEntity.getKindLower() == null && objectEntity.getKind() != null) {
                    objectEntity.setKindLower(objectEntity.getKind());
                }
                if (objectEntity.getContentTypeLower() == null && objectEntity.getContentType() != null) {
                    objectEntity.setContentTypeLower(objectEntity.getContentType());
                }

                catalogObjectRepository.save(objectEntity);

            }
            log.info("Resynchronization of name, kind and content type ended successfully.");
        }

    }
}
