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
package org.ow2.proactive.catalog.rest.service;

import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.entity.KeyValueMetadata;
import org.ow2.proactive.catalog.rest.query.QueryExpressionBuilderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * @author ActiveEon Team
 */
@Service
public class CatalogObjectService {

    @Autowired
    private CatalogObjectRevisionService catalogObjectRevisionService;

    public CatalogObjectMetadata createCatalogObject(String kind, String name, String commitMessage, Long bucketId,
            List<KeyValueMetadata> keyValueMetadataList, byte[] rawObject) {
        return catalogObjectRevisionService.createCatalogObjectRevision(bucketId,
                                                                        kind,
                                                                        name,
                                                                        commitMessage,
                                                                        Optional.empty(),
                                                                        Optional.empty(),
                                                                        keyValueMetadataList,
                                                                        rawObject);
    }

    public CatalogObjectMetadata createCatalogObject(Long bucketId, String kind, String name, String commitMessage,
            Optional<String> layout, byte[] rawObject) {
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

    public PagedResources listCatalogObjects(Long bucketId, Optional<String> query, Pageable pageable,
            PagedResourcesAssembler assembler) throws QueryExpressionBuilderException {
        return catalogObjectRevisionService.listCatalogObjects(bucketId, Optional.empty(), query, pageable, assembler);
    }

    public ResponseEntity<CatalogObjectMetadata> delete(Long bucketId, Long workflowId) {
        return catalogObjectRevisionService.delete(bucketId, workflowId, Optional.empty());
    }

}
