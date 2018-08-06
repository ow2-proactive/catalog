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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;


@Component
public class CatalogObjectReportPDFGenerator {

    private static final List<String> EMPTY_ROW = Arrays.asList("", "", "", "", "");

    private static final List<String> HEADER = Arrays.asList("Bucket Name",
                                                             "Project Name",
                                                             "Object name",
                                                             "Object kind",
                                                             "Content Type");

    private static final PDRectangle PAGE_SHAPE_A4 = new PDRectangle(PDRectangle.A4.getHeight(),
                                                                     PDRectangle.A4.getWidth());

    public byte[] generatePDF(SortedSet<CatalogObjectMetadata> orderedObjectsPerBucket) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage();
            //Create a landscape page
            page.setMediaBox(PAGE_SHAPE_A4);
            doc.addPage(page);

            BaseTable dataTable = createAndIntitializeBaseTable(doc, page);
            populateTable(page, dataTable, orderedObjectsPerBucket);

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new PDFGenerationException(e);
        }
    }

    private BaseTable createAndIntitializeBaseTable(PDDocument doc, PDPage page) throws IOException {
        //Initialize table
        float margin = 10;
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        float yStart = yStartNewPage;
        float bottomMargin = 0;

        return new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
    }

    private void populateTable(PDPage page, BaseTable dataTable,
            SortedSet<CatalogObjectMetadata> orderedObjectsPerBucket) throws IOException {
        //Create the data
        List<List> data = new ArrayList();
        data.add(new ArrayList<>(HEADER));

        addRowForEachObject(orderedObjectsPerBucket, data);

        DataTable t = new DataTable(dataTable, page);
        styleHeader(t);
        t.addListToTable(data, DataTable.HASHEADER);
        dataTable.draw();

    }

    private void addRowForEachObject(SortedSet<CatalogObjectMetadata> orderedObjectsPerBucket, List<List> data) {

        String currentBucketName = "";

        for (CatalogObjectMetadata catalogObject : orderedObjectsPerBucket) {

            currentBucketName = separateBucketsWithEmptyRow(data, currentBucketName, catalogObject);

            data.add(new ArrayList<>(Arrays.asList(catalogObject.getBucketName(),
                                                   catalogObject.getProjectName(),
                                                   catalogObject.getName(),
                                                   catalogObject.getKind(),
                                                   catalogObject.getContentType())));
        }
    }

    private String separateBucketsWithEmptyRow(List<List> data, String currentBucketName,
            CatalogObjectMetadata catalogObject) {
        if (StringUtils.isEmpty(currentBucketName) || !currentBucketName.equals(catalogObject.getBucketName())) {
            currentBucketName = catalogObject.getBucketName();
            data.add(new ArrayList<>(EMPTY_ROW));
        }
        return currentBucketName;
    }

    private void styleHeader(DataTable t) {
        t.getHeaderCellTemplate().setHeight(50.0f);
        t.getHeaderCellTemplate().setFontSize(14.0f);
        t.getHeaderCellTemplate().setFillColor(java.awt.Color.decode("#CD772E"));
        t.getTable().removeAllBorders(true);
    }

}
