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

import static org.ow2.proactive.catalog.service.CatalogObjectService.PROJECT_NAME;

import java.util.List;

import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Component
public class ProjectNameStartupAdder {

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Autowired
    CatalogObjectService catalogObjectService;

    @Transactional
    public void synchronizeProjectName() {
        log.info("Checking catalog object revision entities ... ");
        List<CatalogObjectRevisionEntity> catalogObjectRevisionEntityList = catalogObjectRevisionRepository.findWithEmptyOrNullProjectName();
        if (!catalogObjectRevisionEntityList.isEmpty()) {
            log.info("Resynchronization of project name ...");
            for (CatalogObjectRevisionEntity objectRevisionEntity : catalogObjectRevisionEntityList) {
                String projectName = catalogObjectService.getProjectNameIfExistsOrEmptyString(KeyValueLabelMetadataHelper.convertFromEntity(objectRevisionEntity.getKeyValueMetadataList()),
                                                                                              PROJECT_NAME);

                objectRevisionEntity.setProjectName(projectName);
                catalogObjectRevisionRepository.save(objectRevisionEntity);

            }
            log.info("Resynchronization of project name ended successfully.");
        }

    }
}
