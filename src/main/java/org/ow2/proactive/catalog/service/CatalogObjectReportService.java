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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.report.CatalogObjectReportPDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Service
@Transactional
public class CatalogObjectReportService {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private CatalogObjectReportPDFGenerator catalogObjectReportPDFGenerator;

    public byte[] generateBytesReport(List<String> authorisedBucketsNames, Optional<String> kind,
            Optional<String> contentType) {

        if (authorisedBucketsNames.isEmpty()) {
            return new byte[0];
        }

        List<CatalogObjectMetadata> metadataList = getListOfCatalogObjects(kind, contentType, authorisedBucketsNames);

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(metadataList);

        return catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket);

    }

    private List<CatalogObjectMetadata> getListOfCatalogObjects(Optional<String> kind, Optional<String> contentType,
            List<String> authorisedBucketsNames) {
        List<CatalogObjectMetadata> metadataList = new ArrayList<>();

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

    private TreeSet<CatalogObjectMetadata> sortObjectsPerBucket(List<CatalogObjectMetadata> metadataList) {

        Comparator<CatalogObjectMetadata> sortBasedOnName = Comparator.comparing(catalogObject -> catalogObject.getBucketName());
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(catalogObject -> catalogObject.getProjectName()));
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(catalogObject -> catalogObject.getName()));

        TreeSet<CatalogObjectMetadata> sortedObjects = new TreeSet<CatalogObjectMetadata>(sortBasedOnName);

        sortedObjects.addAll(metadataList);

        return sortedObjects;
    }

}
