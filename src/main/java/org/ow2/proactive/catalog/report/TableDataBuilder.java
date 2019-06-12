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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;


@Component
public class TableDataBuilder {

    private static final String KEY_VALUE_SEPARATOR = " : ";

    private static final float TOTAL_NUMBER_OF_COLUMNS = 11f;

    private static final float SINGLE_COLUMN = (100 / TOTAL_NUMBER_OF_COLUMNS);

    private static final float DOUBLE_COLUMN = (100 / TOTAL_NUMBER_OF_COLUMNS) * 2;

    @Value("${pa.scheduler.url}")
    private String schedulerUrl;

    @Autowired
    private CellFactory cellFactory;

    public void buildTableData(Set<CatalogObjectMetadata> orderedObjectsPerBucket, BaseTable table) {

        // Create data header row
        createDataHeader(table);

        String currentBucketName = "";

        for (CatalogObjectMetadata catalogObject : orderedObjectsPerBucket) {

            if (!currentBucketName.equals(catalogObject.getBucketName())) {
                currentBucketName = catalogObject.getBucketName();
                Row<PDPage> dataRow = table.createRow(10f);
                cellFactory.createDataCellBucketName(dataRow, (100), currentBucketName);
            }

            Row<PDPage> dataRow = table.createRow(10f);
            cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, catalogObject.getName());

            cellFactory.createKeyValueContentDataCell(dataRow, DOUBLE_COLUMN, getAllObjectInfo(catalogObject));

            cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, getDescription(catalogObject), HorizontalAlignment.LEFT);
            cellFactory.createKeyValueContentDataCell(dataRow,
                                                      DOUBLE_COLUMN,
                                                      getKeyValuesAsUnorderedHTMLList(catalogObject, "variable"));

            cellFactory.createKeyValueContentDataCell(dataRow,
                                                      DOUBLE_COLUMN,
                                                      getKeyValuesAsUnorderedHTMLList(catalogObject,
                                                                                      "generic_information"));
            cellFactory.createIconCell(dataRow, SINGLE_COLUMN, getIcon(catalogObject));

        }
        if (orderedObjectsPerBucket.isEmpty()) {
            createEmptyTableRow(table);
        }

    }

    private String getAllObjectInfo(CatalogObjectMetadata catalogObject) {
        return "<p>" + "<b>Bucket Name</b>" + KEY_VALUE_SEPARATOR + catalogObject.getBucketName() + "</p>" + "<p>" +
               "<b>Project Name</b>" + KEY_VALUE_SEPARATOR + getProjectName(catalogObject) + "</p>" +
               getCommittedDetails(catalogObject) + getKindPlusContentType(catalogObject);
    }

    private String getCommittedDetails(CatalogObjectMetadata catalogObject) {
        return "<p>" + "<b>Last Commit date</b>" + KEY_VALUE_SEPARATOR +
               getHumanReadableDate(catalogObject.getCommitTimeRaw()) + "</p>" + "<p>" + "<b>Committed by</b>" +
               KEY_VALUE_SEPARATOR + catalogObject.getUsername() + "</p>";
    }

    private String getKindPlusContentType(CatalogObjectMetadata catalogObject) {
        return "<p>" + "<b>Kind</b>" + KEY_VALUE_SEPARATOR + catalogObject.getKind() + "</p>" + "<p>" +
               "<b>Content-Type</b>" + KEY_VALUE_SEPARATOR + catalogObject.getContentType() + "</p>";
    }

    private String getHumanReadableDate(String rawDate) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(Long.parseLong(rawDate))
                                             .atZone(ZoneId.systemDefault())
                                             .toLocalDateTime();
        return new StringBuilder().append(localDateTime.getYear())
                                  .append("-")
                                  .append(localDateTime.getMonthValue())
                                  .append("-")
                                  .append(localDateTime.getDayOfMonth())
                                  .append(" ")
                                  .append(localDateTime.getHour())
                                  .append(":")
                                  .append(localDateTime.getMinute())
                                  .append(":")
                                  .append(localDateTime.getSecond())
                                  .toString();

    }

    private String getKeyValuesAsUnorderedHTMLList(CatalogObjectMetadata catalogObject, String key) {
        return catalogObject.getMetadataList()
                            .stream()
                            .filter(metadata -> metadata.getLabel().equals(key))
                            .map(metadata -> "<p><b>" + metadata.getKey() + "</b>" + KEY_VALUE_SEPARATOR +
                                             metadata.getValue() + "</p>")
                            .collect(Collectors.joining(""));

    }

    private void createDataHeader(BaseTable table) {
        Row<PDPage> factHeaderrow = table.createRow(15f);
        cellFactory.createDataHeaderCell(factHeaderrow, DOUBLE_COLUMN, "Object Name");
        cellFactory.createDataHeaderCell(factHeaderrow, DOUBLE_COLUMN, "Info");
        cellFactory.createDataHeaderCell(factHeaderrow, DOUBLE_COLUMN, "Description");
        cellFactory.createDataHeaderCell(factHeaderrow, DOUBLE_COLUMN, "Variables");
        cellFactory.createDataHeaderCell(factHeaderrow, DOUBLE_COLUMN, "Generic Info");
        cellFactory.createDataHeaderCell(factHeaderrow, SINGLE_COLUMN, "Icon");
        table.addHeaderRow(factHeaderrow);
    }

    private String getProjectName(CatalogObjectMetadata catalogObject) {
        return Optional.ofNullable(catalogObject.getProjectName()).orElse("");
    }

    private String getDescription(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList()
                            .stream()
                            .filter(metadata -> metadata.getLabel().equals(AbstractCatalogObjectParser.GENERAL_LABEL) &&
                                                metadata.getKey().equals("description"))
                            .map(metadata -> metadata.getValue())
                            .map(description -> description.replace("\n", " ").replace("\r", "").replace("\t", ""))
                            .map(description -> "<p>" + description + "</p>")
                            .findAny()
                            .orElse("");

    }

    private String getIcon(CatalogObjectMetadata catalogObject) {
        return catalogObject.getMetadataList()
                            .stream()
                            .filter(metadata -> metadata.getLabel().equals(AbstractCatalogObjectParser.GENERAL_LABEL) &&
                                                metadata.getKey().equals("main.icon"))
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

    private void createEmptyTableRow(BaseTable table) {
        Row<PDPage> dataRow = table.createRow(10f);
        cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, "");
        cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, "");
        cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, "");
        cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, "");
        cellFactory.createDataCell(dataRow, DOUBLE_COLUMN, "");
        cellFactory.createDataCell(dataRow, SINGLE_COLUMN, "");

    }

}
