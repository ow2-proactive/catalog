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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.callgraph.CatalogObjectCallGraphPDFGenerator;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Log4j2
@Service
@Transactional
public class CatalogObjectCallGraphService {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private CatalogObjectCallGraphPDFGenerator catalogObjectCallGraphPDFGenerator;

    public byte[] generateBytesCallGraphImage(List<String> authorisedBucketsNames, Optional<String> kind,
            Optional<String> contentType) {

        List<CatalogObjectMetadata> metadataList = getListOfCatalogObjects(kind, contentType, authorisedBucketsNames);

        return catalogObjectCallGraphPDFGenerator.generatePdfImage(metadataList, kind, contentType);

    }

    private List<CatalogObjectMetadata> getListOfCatalogObjects(Optional<String> kind, Optional<String> contentType,
            List<String> authorisedBucketsNames) {

        if (authorisedBucketsNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<CatalogObjectMetadata> metadataList;

        if (kind.isPresent() && contentType.isPresent()) {
            metadataList = catalogObjectService.listCatalogObjectsByKindAndContentType(authorisedBucketsNames,
                                                                                       kind.get(),
                                                                                       contentType.get());
        } else if (contentType.isPresent()) {
            metadataList = catalogObjectService.listCatalogObjectsByContentType(authorisedBucketsNames,
                                                                                contentType.get());
        } else if (kind.isPresent()) {
            metadataList = catalogObjectService.listCatalogObjectsByKind(authorisedBucketsNames, kind.get());
        } else {
            metadataList = catalogObjectService.listCatalogObjects(authorisedBucketsNames);
        }

        return metadataList;
    }
}
