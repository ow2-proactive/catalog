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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;


@RunWith(value = MockitoJUnitRunner.class)
public class TableDataBuilderTest {

    @InjectMocks
    private TableDataBuilder tableDataBuilder;

    @Mock
    private CellFactory cellFactory;

    @Test
    public void buildTableDataTestWithEmptyCatalogObjectMetadataSet() throws IOException {

        Set<CatalogObjectMetadata> catalogObjectMetadataSet = new HashSet<>();
        BaseTable baseTable = new ReportGeneratorHelper().initializeTable(new PDDocument(), 1f, new PDPage());

        tableDataBuilder.buildTableData(catalogObjectMetadataSet, baseTable);

        verify(cellFactory, times(5)).createDataCell(any(Row.class), eq(tableDataBuilder.DOUBLE_COLUMN), eq(""));
        verify(cellFactory, times(1)).createDataCell(any(Row.class), eq(tableDataBuilder.SINGLE_COLUMN), eq(""));
    }

    @Test
    public void buildTableDataTestWithOneCatalogObjectMetadata() throws IOException {

        final String objectName = "objectName";
        final String bucketName = "bucketName";
        final String kind = "kind";
        final String contentType = "contentType";
        final String projectName = "projectName";
        final String tags = "tag1,tag2";
        final String description = "objectDescription";
        final String variable1Key = "var1Key";
        final String variable1Value = "var1Value";
        final String variable2Key = "var2Key";
        final String variable2Value = "var2Value";
        final String genericInformation1Key = "gi1Key";
        final String genericInformation1Value = "gi1Val";
        final String genericInformation2Key = "gi2Key";
        final String genericInformation2Value = "gi2Val";

        List<Metadata> metadataList = Arrays.asList(new Metadata(variable1Key,
                                                                 variable1Value,
                                                                 WorkflowParser.ATTRIBUTE_VARIABLE_LABEL),
                                                    new Metadata(variable2Key,
                                                                 variable2Value,
                                                                 WorkflowParser.ATTRIBUTE_VARIABLE_LABEL),
                                                    new Metadata(genericInformation1Key,
                                                                 genericInformation1Value,
                                                                 WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL),
                                                    new Metadata(genericInformation2Key,
                                                                 genericInformation2Value,
                                                                 WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL),
                                                    new Metadata(WorkflowParser.JOB_DESCRIPTION_KEY,
                                                                 description,
                                                                 AbstractCatalogObjectParser.GENERAL_LABEL));

        ArgumentCaptor<String> objectInfoCaptor = ArgumentCaptor.forClass(String.class);

        doNothing().when(cellFactory).createKeyValueContentDataCell(any(Row.class),
                                                                    eq(tableDataBuilder.DOUBLE_COLUMN),
                                                                    objectInfoCaptor.capture());

        Set<CatalogObjectMetadata> catalogObjectMetadataSet = new HashSet<>();
        catalogObjectMetadataSet.add(new CatalogObjectMetadata(bucketName,
                                                               objectName,
                                                               projectName,
                                                               tags,
                                                               kind,
                                                               contentType,
                                                               1400343L,
                                                               "commit message",
                                                               "username",
                                                               metadataList,
                                                               "xml"));

        BaseTable baseTable = new ReportGeneratorHelper().initializeTable(new PDDocument(), 1f, new PDPage());

        tableDataBuilder.buildTableData(catalogObjectMetadataSet, baseTable);

        verify(cellFactory, times(1)).createDataCellBucketName(any(Row.class), anyFloat(), eq(bucketName));
        verify(cellFactory, times(1)).createDataCell(any(Row.class),
                                                     eq(tableDataBuilder.DOUBLE_COLUMN),
                                                     eq(objectName));
        verify(cellFactory, times(1)).createDataCell(any(Row.class),
                                                     eq(tableDataBuilder.DOUBLE_COLUMN),
                                                     eq(description),
                                                     eq(HorizontalAlignment.LEFT));
        verify(cellFactory, times(3)).createKeyValueContentDataCell(any(Row.class),
                                                                    eq(tableDataBuilder.DOUBLE_COLUMN),
                                                                    anyString());
        verify(cellFactory, times(1)).createIconCell(any(Row.class), eq(tableDataBuilder.SINGLE_COLUMN), eq(""));

        List<String> capturedInfo = objectInfoCaptor.getAllValues();
        assertThat(capturedInfo).hasSize(3);

        //Ensure that the created cell contains the following information

        assertThat(capturedInfo.stream()
                               .peek(info -> System.out.println("info: " + info))
                               .anyMatch(info -> info.contains(variable1Key))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(variable1Value))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(variable2Key))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(variable2Value))).isTrue();

        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(genericInformation1Key))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(genericInformation1Value))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(genericInformation2Key))).isTrue();
        assertThat(capturedInfo.stream().anyMatch(info -> info.contains(genericInformation2Value))).isTrue();

        //Ensure that the var1 and var2 are in the same cell
        assertThat(capturedInfo.stream()
                               .filter(info -> info.contains(variable1Key))
                               .findAny()
                               .get()).isEqualTo(capturedInfo.stream()
                                                             .filter(info -> info.contains(variable2Value))
                                                             .findAny()
                                                             .get());

        //Ensure that the gi1 and gi2 are in the same cell
        assertThat(capturedInfo.stream()
                               .filter(info -> info.contains(genericInformation1Value))
                               .findAny()
                               .get()).isEqualTo(capturedInfo.stream()
                                                             .filter(info -> info.contains(genericInformation2Key))
                                                             .findAny()
                                                             .get());

    }
}
