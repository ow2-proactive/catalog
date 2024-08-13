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
package org.ow2.proactive.catalog.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.callgraph.CallGraphHolder;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.annotations.VisibleForTesting;

import be.quodlibet.boxable.BaseTable;


@Component
public class CatalogObjectReportPDFGenerator {

    @VisibleForTesting
    static final float MARGIN = 10f;

    @VisibleForTesting
    static final String FIRST_TITLE = "ProActive Catalog Report";

    @VisibleForTesting
    static final String SECOND_TITLE = "Object Dependencies";

    @Autowired
    private TableDataBuilder tableDataBuilder;

    @Autowired
    private HeadersBuilder headersBuilder;

    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

    @Autowired
    private TableCatalogObjectsDependenciesBuilder tableCatalogObjectsDependenciesBuilder;

    public ArchiveManagerHelper.ZipArchiveContent getAllCatalogObjectsReportPDFAsArchive(
            Set<CatalogObjectMetadata> orderedObjectsPerBucket, Optional<String> kind, Optional<String> contentType) {

        ArchiveManagerHelper.ZipArchiveContent zipContent = new ArchiveManagerHelper.ZipArchiveContent();

        // Create a Map with bucketName as key and CatalogObjectMetadata as value
        Map<String, Set<CatalogObjectMetadata>> catalogObjectMetadataMap = orderedObjectsPerBucket.stream()
                                                                                                  .collect(Collectors.groupingBy(CatalogObjectMetadata::getBucketName, // Group by bucketName
                                                                                                                                 Collectors.toSet() // Collect items into a Set
        ));

        List<ZipEntrySource> zipEntrySources = new ArrayList<>(catalogObjectMetadataMap.keySet().size());

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            for (Map.Entry<String, Set<CatalogObjectMetadata>> entry : catalogObjectMetadataMap.entrySet()) {
                byte[] pdfData = generatePDF(entry.getValue(), kind, contentType);
                zipEntrySources.add(new ByteSource(entry.getKey() + "_report.pdf", pdfData));
            }

            ZipEntrySource[] sources = zipEntrySources.toArray(new ZipEntrySource[0]);
            ZipUtil.pack(sources, byteArrayOutputStream);
            zipContent.setContent(byteArrayOutputStream.toByteArray());
            return zipContent;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public byte[] generatePDF(Set<CatalogObjectMetadata> orderedObjectsPerBucket, Optional<String> kind,
            Optional<String> contentType) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()) {

            //Load font for all languages
            reportGeneratorHelper.setFontToUse(doc);

            // Initialize Document
            PDPage page1 = reportGeneratorHelper.addNewPage(doc);

            // Initialize table
            BaseTable table1 = reportGeneratorHelper.initializeTable(doc, MARGIN, page1);

            // Create Header row
            headersBuilder.createMainHeader(table1, FIRST_TITLE);

            // Create Header row
            headersBuilder.createInfoHeader(table1, orderedObjectsPerBucket, kind, contentType);

            // Create table data
            tableDataBuilder.buildTableData(orderedObjectsPerBucket, table1);

            table1.draw();

            PDPage page2 = reportGeneratorHelper.addNewPage(doc);

            // Initialize table
            BaseTable table2 = reportGeneratorHelper.initializeTable(doc, MARGIN, page2);

            // Create Header row
            headersBuilder.createMainHeader(table2, SECOND_TITLE);

            // Build call graph
            CallGraphHolder globalCallGraph = reportGeneratorHelper.buildCatalogCallGraph(new ArrayList(orderedObjectsPerBucket));

            tableCatalogObjectsDependenciesBuilder.buildCatalogObjectsDependenciesTable(doc,
                                                                                        globalCallGraph,
                                                                                        new ArrayList(orderedObjectsPerBucket),
                                                                                        table2);
            table2.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new PDFGenerationException(e);
        }
    }

}
