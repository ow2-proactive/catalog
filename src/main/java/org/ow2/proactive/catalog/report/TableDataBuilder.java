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
import java.net.URL;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.image.Image;


@Component
public class TableDataBuilder {

    private static final String LIGHT_GRAY = "#D3D3D3";
    private static final String LIGHT_CYAN = "#F3F3F4";
    private static final String WHITE = "#ffffff";
    @Value("${pa.scheduler.url}")
    private String schedulerUrl;

    public void buildTableData(Set<CatalogObjectMetadata> orderedObjectsPerBucket, BaseTable table) {

        // Create data header row
        createDataHeader(table);

        String currentBucketName = "";

        for (CatalogObjectMetadata catalogObject : orderedObjectsPerBucket) {

            if (!currentBucketName.equals(catalogObject.getBucketName())) {
                currentBucketName = catalogObject.getBucketName();
                Row<PDPage> dataRow = table.createRow(10f);
                createDataCell(dataRow, (100), currentBucketName, 8);
            }

            Row<PDPage> dataRow = table.createRow(10f);
            createDataCell(dataRow, (100 / 8f), catalogObject.getBucketName());
            createDataCell(dataRow, (100 / 8f), getProjectName(catalogObject));
            createDataCell(dataRow, (100 / 8f), catalogObject.getName());
            createDataCell(dataRow, (100 / 8f) * 2, getDescription(catalogObject));
            createDataCell(dataRow, (100 / 8f), catalogObject.getKind());
            createDataCell(dataRow, (100 / 8f), catalogObject.getContentType());
            createIconCell(dataRow, getIcon(catalogObject));

        }
        if (orderedObjectsPerBucket.isEmpty()) {
            createEmptyTableRow(table);
        }

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
        cell.setFontSize(8);
        cell.setFillColor(java.awt.Color.decode(LIGHT_CYAN));
        cell.setAlign(HorizontalAlignment.CENTER);
    }

    private void createDataCell(Row<PDPage> row, float width, String catalogObjectData, int size) {
        createDataCell(row, width, catalogObjectData, 6, WHITE);
    }

    private void createDataCell(Row<PDPage> row, float width, String catalogObjectData, int size,
            String color) {
        Cell<PDPage> bucketNameCell = row.createCell(width, catalogObjectData);
        bucketNameCell.setFontSize(size);
        bucketNameCell.setAlign(HorizontalAlignment.CENTER);
        bucketNameCell.setFillColor(java.awt.Color.decode(color));
    }

    private void createDataCell(Row<PDPage> row, float width, String catalogObjectData) {
        createDataCell(row, width, catalogObjectData, 6);
    }

    private String getProjectName(CatalogObjectMetadata catalogObject) {
        return Optional.ofNullable(catalogObject.getProjectName()).orElse("");
    }

    private String getDescription(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList().stream()
                .filter(metadata -> metadata.getKey().equals("description"))
                .map(metadata -> metadata.getValue())
                .map(description -> description.replaceAll("\n", "").replace("\r", "").replace("\t", ""))
                .findAny().orElse("");

    }

    private String getIcon(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList().stream()
                .filter(metadata -> metadata.getKey().equals("workflow.icon"))
                .map(metadata -> metadata.getValue()).map(image_url -> {
                    if (image_url.startsWith("/")) {
                        return schedulerUrl + image_url;
                    } else {
                        return schedulerUrl;
                    }
                }).findAny().orElse("");

    }

    private void createEmptyTableRow(BaseTable table) {
        Row<PDPage> dataRow = table.createRow(10f);
        createDataCell(dataRow, (100 / 8f), "");
        createDataCell(dataRow, (100 / 8f), "");
        createDataCell(dataRow, (100 / 8f), "");
        createDataCell(dataRow, (100 / 8f) * 2, "");
        createDataCell(dataRow, (100 / 8f), "");
        createDataCell(dataRow, (100 / 8f), "");
        createDataCell(dataRow, (100 / 8f), "");
    }

    private void createIconCell(Row<PDPage> dataRow, String url_path) {
        try {
            URL url = new URL(url_path);
            BufferedImage imageFile = ImageIO.read(url);
            dataRow.createImageCell((100 / 8f), new Image(imageFile));
        } catch (Exception e) {
            createDataCell(dataRow, (100 / 8f), url_path, 6, LIGHT_GRAY);
        }
    }

}
