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
import java.util.Optional;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadataList;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ActiveEon Team
 */
@Service
@Transactional
public class CatalogObjectService {

    @Autowired
    private CatalogObjectRevisionService catalogObjectRevisionService;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    public CatalogObjectMetadata createCatalogObject(String kind, String name, String commitMessage, Long bucketId,
            String layout, List<KeyValueMetadataEntity> keyValueMetadataList, byte[] rawObject) {
        return catalogObjectRevisionService.createCatalogObjectRevision(bucketId,
                                                                        kind,
                                                                        name,
                                                                        commitMessage,
                                                                        Optional.empty(),
                                                                        layout,
                                                                        keyValueMetadataList,
                                                                        rawObject);
    }

    public CatalogObjectMetadata createCatalogObject(Long bucketId, String kind, String name, String commitMessage,
            String layout, byte[] rawObject) {
        return catalogObjectRevisionService.createCatalogObjectRevision(bucketId,
                                                                        kind,
                                                                        name,
                                                                        commitMessage,
                                                                        Optional.empty(),
                                                                        layout,
                                                                        rawObject);
    }

    public ResponseEntity<?> getCatalogObjectMetadata(long bucketId, long catalogObjectId) {
        return catalogObjectRevisionService.getCatalogObject(bucketId, catalogObjectId, Optional.empty());
    }

    public CatalogObjectMetadataList listCatalogObjects(Long bucketId, Optional<String> kind) {
        return catalogObjectRevisionService.listCatalogObjects(bucketId, kind);
    }

    public ResponseEntity<CatalogObjectMetadata> delete(Long bucketId, Long catalogObjectId) {
        return catalogObjectRevisionService.delete(bucketId, catalogObjectId, Optional.empty());
    }

    public CatalogObjectEntity findObjectById(long objectId) {
        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(objectId);

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException();
        }

        return catalogObject;
    }

    public CatalogObjectRevisionEntity getMostRecentCatalogObjectRevision(Long bucketId, Long catalogObjectId) {
        return catalogObjectRepository.getMostRecentCatalogObjectRevision(bucketId, catalogObjectId);
    }

    public List<CatalogObjectRevisionEntity> getMostRecentRevisions(Long bucketId, Optional<String> kind) {
        List<CatalogObjectRevisionEntity> list;
        if (kind.isPresent()) {
            list = catalogObjectRepository.getMostRecentRevisions(bucketId, kind.get());
        } else {
            list = catalogObjectRepository.getMostRecentRevisions(bucketId);
        }
        return list;
    }

    public void save(CatalogObjectEntity catalogObject) {
        catalogObjectRepository.save(catalogObject);
    }

    public void delete(CatalogObjectEntity catalogObject) {
        catalogObjectRepository.delete(catalogObject);
    }
}
