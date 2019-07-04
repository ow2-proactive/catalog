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

    public byte[] generateBytesReportForSelectedObjects(String bucketName, List<String> catalogObjectsNames,
            Optional<String> kind, Optional<String> contentType) {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listSelectedCatalogObjects(bucketName,
                                                                                                   catalogObjectsNames);

        return orderCatalogObjectsByBucketAndProjectName(kind, contentType, metadataList);

    }

    public byte[] generateBytesReport(List<String> authorisedBucketsNames, Optional<String> kind,
            Optional<String> contentType) {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listCatalogObjects(authorisedBucketsNames,
                                                                                           kind,
                                                                                           contentType);

        return orderCatalogObjectsByBucketAndProjectName(kind, contentType, metadataList);

    }

    private byte[] orderCatalogObjectsByBucketAndProjectName(Optional<String> kind, Optional<String> contentType,
            List<CatalogObjectMetadata> metadataList) {
        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(metadataList);

        return catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket, kind, contentType);
    }

    private TreeSet<CatalogObjectMetadata> sortObjectsPerBucket(List<CatalogObjectMetadata> metadataList) {

        Comparator<CatalogObjectMetadata> sortBasedOnName = Comparator.comparing(CatalogObjectMetadata::getBucketName);
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(CatalogObjectMetadata::getProjectName));
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(CatalogObjectMetadata::getName));

        TreeSet<CatalogObjectMetadata> sortedObjects = new TreeSet<CatalogObjectMetadata>(sortBasedOnName);

        sortedObjects.addAll(metadataList);

        return sortedObjects;
    }

}
