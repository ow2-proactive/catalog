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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.catalog.report.TableCatalogObjectsDependenciesBuilder.CELL_HEIGHT;
import static org.ow2.proactive.catalog.report.TableCatalogObjectsDependenciesBuilder.CELL_WIDTH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.callgraph.CallGraphHolder;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.DependsOnCatalogObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;
import org.ow2.proactive.catalog.util.SeparatorUtility;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;


@RunWith(value = MockitoJUnitRunner.class)
public class TableCatalogObjectsDependenciesBuilderTest {

    @InjectMocks
    private TableCatalogObjectsDependenciesBuilder tableCatalogObjectsDependenciesBuilder;

    @Mock
    private CellFactory cellFactory;

    @Mock
    private SeparatorUtility separatorUtility;

    @Mock
    private CatalogObjectService catalogObjectService;

    private PDDocument pdDocument;

    private CallGraphHolder callGraphHolder;

    private List<CatalogObjectMetadata> catalogObjectMetadatas;

    private BaseTable table;

    @Before
    public void setUp() throws IOException {
        pdDocument = new PDDocument();
        callGraphHolder = new CallGraphHolder();
        catalogObjectMetadatas = new ArrayList<>();
        table = new ReportGeneratorHelper().initializeTable(pdDocument, 1f, new PDPage());

        when(separatorUtility.getConcatWithSeparator(anyString(), anyString(), anyString())).thenCallRealMethod();
    }

    @Test
    public void testBuildCatalogObjectsDependenciesTableWithEmptyGraph() throws IOException {

        tableCatalogObjectsDependenciesBuilder.buildCatalogObjectsDependenciesTable(null, callGraphHolder, null, table);

        assertThat(table.getRows()).hasSize(1);
        assertThat(table.getRows().get(0).getHeight()).isWithin(0.1f).of(CELL_HEIGHT);

        verify(cellFactory, times(1)).createDataHeaderCell(any(Row.class), eq(CELL_WIDTH), anyString());
    }

    @Test
    public void testBuildCatalogObjectsDependenciesTableWithOneItemGraph() throws IOException {

        final String bucketName = "bucket";
        final String objectName = "object";
        final String kind = "kind";

        final String bucketAndObjectName = "bucket/ObjectName";

        final CatalogObjectMetadata catalogObjectMetadata = new CatalogObjectMetadata(bucketName,
                                                                                      objectName,
                                                                                      "projectName",
                                                                                      "tag",
                                                                                      kind,
                                                                                      "projectName",
                                                                                      1L,
                                                                                      "commit message",
                                                                                      "username",
                                                                                      new ArrayList<>(),
                                                                                      "xml");

        final DependsOnCatalogObject dependsOnCatalogObject = new DependsOnCatalogObject(bucketAndObjectName,
                                                                                         kind,
                                                                                         "revision",
                                                                                         false);
        final CatalogObjectDependencies catalogObjectDependencies = new CatalogObjectDependencies(Arrays.asList(dependsOnCatalogObject),
                                                                                                  Arrays.asList(bucketAndObjectName));

        when(catalogObjectService.getCatalogObjectMetadata(bucketName, objectName)).thenReturn(catalogObjectMetadata);
        when(catalogObjectService.getObjectDependencies(bucketName, objectName)).thenReturn(catalogObjectDependencies);

        when(separatorUtility.getSplitBySeparator(anyString())).thenCallRealMethod().thenCallRealMethod();

        callGraphHolder.addNode(bucketName, objectName, kind, true);

        tableCatalogObjectsDependenciesBuilder.buildCatalogObjectsDependenciesTable(pdDocument,
                                                                                    callGraphHolder,
                                                                                    catalogObjectMetadatas,
                                                                                    table);

        assertThat(table.getRows()).hasSize(2);
    }

}
