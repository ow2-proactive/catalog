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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.callgraph.CatalogObjectCallGraphPDFGenerator;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Log4j2
@Service
public class CatalogObjectCallGraphService {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private CatalogObjectCallGraphPDFGenerator catalogObjectCallGraphPDFGenerator;

    public byte[] generateBytesCallGraphForAllBucketsAsZip(List<String> authorisedBucketsNames, Optional<String> kind,
            Optional<String> contentType) {

        Map<String, List<CatalogObjectMetadata>> bucketNameCatalogObjectMetadata = catalogObjectService.listCatalogObjects(authorisedBucketsNames,
                                                                                                                           kind,
                                                                                                                           contentType)
                                                                                                       .stream()
                                                                                                       .collect(Collectors.groupingBy(CatalogObjectMetadata::getBucketName,
                                                                                                                                      Collectors.toList()));

        List<ZipEntrySource> zipEntrySources = new ArrayList<>(bucketNameCatalogObjectMetadata.keySet().size());

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            for (Map.Entry<String, List<CatalogObjectMetadata>> entry : bucketNameCatalogObjectMetadata.entrySet()) {
                byte[] pdfData = catalogObjectCallGraphPDFGenerator.generatePdfImage(entry.getValue(),
                                                                                     kind,
                                                                                     contentType);
                ByteSource byteSource = new ByteSource(entry.getKey() + "_report.pdf", pdfData);
                zipEntrySources.add(byteSource);
            }

            ZipEntrySource[] sources = zipEntrySources.toArray(new ZipEntrySource[0]);
            ZipUtil.pack(sources, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public byte[] generateBytesCallGraphForSelectedObjects(String bucketName, List<String> catalogObjectsNames,
            Optional<String> kind, Optional<String> contentType) {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listSelectedCatalogObjects(bucketName,
                                                                                                   catalogObjectsNames);

        return catalogObjectCallGraphPDFGenerator.generatePdfImage(metadataList, kind, contentType);

    }
}
