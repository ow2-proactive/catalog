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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.image.Image;


@Component
public class CatalogObjectReportPDFGenerator {

    @Value("${pa.scheduler.url}")
    private String schedulerUrl;

    private static final String ACTIVEEON_LOGO = "/automation-dashboard/styles/patterns/AE-Logo.png";

    private static final String MAIN_TITLE = "ProActive Catalog report";

    public byte[] generatePDF(SortedSet<CatalogObjectMetadata> orderedObjectsPerBucket) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()) {

            // Set margins
            float margin = 10;

            List<String[]> catalogObjects = getCatalogObjects(orderedObjectsPerBucket);

            // Initialize Document
            PDPage page = addNewPage(doc);

            // Initialize table
            BaseTable table = initializeTable(doc, margin, page);

            // Create Header row
            createMainHeader(table);

            // Create 2 column row
            createSecondaryHeader(table);

            // Create data header row
            createDataHeader(table);

            // Add multiple rows with catalog object data
            populateTable(catalogObjects, table);

            table.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            throw new PDFGenerationException(e);
        }
    }

    private void populateTable(List<String[]> catalogObjects, BaseTable table)
            throws MalformedURLException, IOException {
        for (String[] catalogObjectData : catalogObjects) {

            Row<PDPage> dataRow = table.createRow(10f);
            createDataCell(dataRow, (100 / 8f), catalogObjectData[0]);
            createDataCell(dataRow, (100 / 8f), catalogObjectData[1]);
            createDataCell(dataRow, (100 / 8f), catalogObjectData[2]);
            createDataCell(dataRow, (100 / 8f) * 2, catalogObjectData[3]);
            createDataCell(dataRow, (100 / 8f), catalogObjectData[4]);
            createDataCell(dataRow, (100 / 8f), catalogObjectData[5]);

            createIconCell(dataRow, catalogObjectData[6]);

        }
    }

    private BaseTable initializeTable(PDDocument doc, float margin, PDPage page) throws IOException {
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        boolean drawContent = true;
        boolean drawLines = true;
        float yStart = yStartNewPage;
        float bottomMargin = 70;
        BaseTable table = new BaseTable(yStart,
                                        yStartNewPage,
                                        bottomMargin,
                                        tableWidth,
                                        margin,
                                        doc,
                                        page,
                                        drawLines,
                                        drawContent);
        return table;
    }

    private void createIconCell(Row<PDPage> dataRow, String url_path) throws MalformedURLException, IOException {
        if (url_path.isEmpty()) {
            createDataCell(dataRow, (100 / 8f), "");
        } else {
            URL url = new URL(url_path);
            BufferedImage imageFile = ImageIO.read(url);
            dataRow.createImageCell((100 / 8f), new Image(imageFile));
        }
    }

    private void createDataCell(Row<PDPage> row, float width, String catalogObjectData) {
        Cell<PDPage> bucketNameCell = row.createCell(width, catalogObjectData);
        bucketNameCell.setFontSize(6);
        bucketNameCell.setAlign(HorizontalAlignment.CENTER);
    }

    private void createSecondaryHeader(BaseTable table) {
        Row<PDPage> row = table.createRow(15f);
        row.createCell(75, "Source:");
        row.createCell(25, schedulerUrl + "/catalog");
    }

    private void createMainHeader(BaseTable table) throws IOException {
        Row<PDPage> headerRow = table.createRow(15f);

        URL url = new URL(schedulerUrl + ACTIVEEON_LOGO);
        BufferedImage imageFile = ImageIO.read(url);
        headerRow.createImageCell((100 / 8f), new Image(imageFile));

        Cell<PDPage> cell = headerRow.createCell((100 / 8f) * 7, MAIN_TITLE);
        cell.setFillColor(java.awt.Color.decode("#EE7939"));
        cell.setTextColor(java.awt.Color.decode("#0E2C65"));

        table.addHeaderRow(headerRow);
    }

    private void createDataHeader(BaseTable table) {
        Row<PDPage> factHeaderrow = table.createRow(15f);
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Bucket Name");
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Project Name");
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Object Name");
        createDataHeaderCell(factHeaderrow, (100 / 8f) * 2, "Description");
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Kind");
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Content Type");
        createDataHeaderCell(factHeaderrow, (100 / 8f), "Icon");
        table.addHeaderRow(factHeaderrow);
    }

    private void createDataHeaderCell(Row<PDPage> factHeaderrow, float width, String title) {
        Cell<PDPage> cell;
        cell = factHeaderrow.createCell(width, title);
        cell.setFontSize(6);
        cell.setFillColor(java.awt.Color.decode("#F3F3F4"));
        cell.setAlign(HorizontalAlignment.CENTER);
    }

    private List<String[]> getCatalogObjects(SortedSet<CatalogObjectMetadata> orderedObjectsPerBucket)
            throws IOException {
        //Create the data
        List<String[]> data = new ArrayList<String[]>();
        for (CatalogObjectMetadata catalogObject : orderedObjectsPerBucket) {

            data.add(new String[] { catalogObject.getBucketName(), getProjectName(catalogObject),
                                    catalogObject.getName(), getDescription(catalogObject), catalogObject.getKind(),
                                    catalogObject.getContentType(), getIcon(catalogObject) });

        }

        return data;

    }

    private String getProjectName(CatalogObjectMetadata catalogObject) {
        return Optional.ofNullable(catalogObject.getProjectName()).orElse("");
    }

    private String getDescription(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList()
                            .stream()
                            .filter(metadata -> metadata.getKey().equals("description"))
                            .map(metadata -> metadata.getValue())
                            .map(description -> description.replaceAll("\n", "").replace("\r", "").replace("\t", ""))
                            .findAny()
                            .orElse("");

    }

    private String getIcon(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList()
                            .stream()
                            .filter(metadata -> metadata.getKey().equals("workflow.icon"))
                            .map(metadata -> metadata.getValue())
                            .map(image_url -> {
                                if (image_url.startsWith("/")) {
                                    return schedulerUrl + image_url;
                                } else {
                                    return schedulerUrl;
                                }
                            })
                            .findAny()
                            .orElse("");

    }

    private PDPage addNewPage(PDDocument doc) {
        PDPage page = new PDPage();
        doc.addPage(page);
        return page;
    }

}
