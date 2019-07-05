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
package org.ow2.proactive.catalog.callgraph;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.report.HeadersBuilder;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Component
public class CatalogObjectCallGraphPDFGenerator {

    private static final float MARGIN = 10f;

    private static final String MAIN_TITLE = "ProActive Call Graph Report";

    @Autowired
    private HeadersBuilder headersBuilder;

    @Autowired
    private SeparatorUtility separatorUtility;

    @Autowired
    private TableCallGraphsBuilder tableCallGraphsBuilder;

    @Autowired
    private ReportGeneratorHelper reportGeneratorHelper;

    public byte[] generatePdfImage(List<CatalogObjectMetadata> catalogObjectMetadataList, Optional<String> kind,
            Optional<String> contentType, CatalogObjectService catalogObjectService) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()) {

            // Build call graph
            CallGraphHolder globalCallGraph = reportGeneratorHelper.buildCatalogCallGraph(catalogObjectMetadataList,
                                                                                          catalogObjectService);

            //Load font for all languages
            reportGeneratorHelper.setFontToUse(doc);

            // Initialize Document
            PDPage page = reportGeneratorHelper.addNewPage(doc);

            // Initialize table
            BaseTable table = reportGeneratorHelper.initializeTable(doc, MARGIN, page);

            // Create Header row
            headersBuilder.createMainHeader(table, MAIN_TITLE);

            // Create Header row

            headersBuilder.createInfoHeader(table,
                                            reportGeneratorHelper.extractBucketSet(globalCallGraph),
                                            reportGeneratorHelper.extractObjectSet(globalCallGraph),
                                            kind,
                                            contentType);

            //Create call graphs table
            tableCallGraphsBuilder.buildCallGraphsTable(globalCallGraph, table);

            table.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new PDFGenerationException(e);
        }
    }
}
