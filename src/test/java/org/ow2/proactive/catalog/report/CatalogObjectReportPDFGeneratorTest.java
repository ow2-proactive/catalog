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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.catalog.report.CatalogObjectReportPDFGenerator.FIRST_TITLE;
import static org.ow2.proactive.catalog.report.CatalogObjectReportPDFGenerator.MARGIN;
import static org.ow2.proactive.catalog.report.CatalogObjectReportPDFGenerator.SECOND_TITLE;

import java.io.IOException;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.callgraph.CallGraphHolder;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;

import be.quodlibet.boxable.BaseTable;


@RunWith(value = MockitoJUnitRunner.class)
public class CatalogObjectReportPDFGeneratorTest {

    @InjectMocks
    private CatalogObjectReportPDFGenerator catalogObjectReportPDFGenerator;

    @Mock
    private TableDataBuilder tableDataBuilder;

    @Mock
    private HeadersBuilder headersBuilder;

    @Mock
    private ReportGeneratorHelper reportGeneratorHelper;

    @Mock
    private TableCatalogObjectsDependenciesBuilder tableCatalogObjectsDependenciesBuilder;

    private PDPage pdPage1;

    private PDPage pdPage2;

    private CallGraphHolder callGraphHolder;

    private Set<CatalogObjectMetadata> catalogObjectMetadataSet;

    private Optional<String> kind;

    private Optional<String> contentType;

    @Before
    public void setUp() throws IOException {
        pdPage1 = new PDPage();
        pdPage2 = new PDPage();
        callGraphHolder = new CallGraphHolder();

        when(reportGeneratorHelper.initializeTable(any(PDDocument.class),
                                                   eq(MARGIN),
                                                   any(PDPage.class))).thenCallRealMethod();
        when(reportGeneratorHelper.addNewPage(any(PDDocument.class))).thenReturn(pdPage1).thenReturn(pdPage2);
    }

    @Test
    public void testThatGeneratePDFWorksWithEmptyValues() throws IOException {

        catalogObjectMetadataSet = new HashSet<>();
        kind = Optional.empty();
        contentType = Optional.empty();

        when(reportGeneratorHelper.buildCatalogCallGraph(new ArrayList<>(catalogObjectMetadataSet))).thenReturn(callGraphHolder);

        byte[] bytes = catalogObjectReportPDFGenerator.generatePDF(catalogObjectMetadataSet, kind, contentType);
        assertThat(bytes).asList().isNotEmpty();

        verify(reportGeneratorHelper, times(1)).setFontToUse(any(PDDocument.class));
        verify(headersBuilder, times(1)).createMainHeader(any(BaseTable.class), eq(FIRST_TITLE));
        verify(headersBuilder, times(1)).createMainHeader(any(BaseTable.class), eq(SECOND_TITLE));
        verify(headersBuilder, times(1)).createInfoHeader(any(BaseTable.class),
                                                          eq(catalogObjectMetadataSet),
                                                          eq(kind),
                                                          eq(contentType));
        verify(tableDataBuilder, times(1)).buildTableData(eq(catalogObjectMetadataSet), any(BaseTable.class));
        verify(tableCatalogObjectsDependenciesBuilder,
               times(1)).buildCatalogObjectsDependenciesTable(any(PDDocument.class),
                                                              eq(callGraphHolder),
                                                              eq(new ArrayList<>(catalogObjectMetadataSet)),
                                                              any(BaseTable.class));
    }

    @Test
    public void testThatGeneratePDFWorks() throws IOException {

        kind = Optional.of("kind");
        contentType = Optional.of("contentType");

        catalogObjectMetadataSet = Sets.newSet(new CatalogObjectMetadata("bucketName",
                                                                         "objectName",
                                                                         "projectName",
                                                                         kind.get(),
                                                                         "projectName",
                                                                         1L,
                                                                         "commit message",
                                                                         "username",
                                                                         new ArrayList<>(),
                                                                         "xml"));

        when(reportGeneratorHelper.buildCatalogCallGraph(new ArrayList<>(catalogObjectMetadataSet))).thenReturn(callGraphHolder);

        byte[] bytes = catalogObjectReportPDFGenerator.generatePDF(catalogObjectMetadataSet, kind, contentType);
        assertThat(bytes).asList().isNotEmpty();

        verify(reportGeneratorHelper, times(1)).setFontToUse(any(PDDocument.class));
        verify(headersBuilder, times(1)).createMainHeader(any(BaseTable.class), eq(FIRST_TITLE));
        verify(headersBuilder, times(1)).createMainHeader(any(BaseTable.class), eq(SECOND_TITLE));
        verify(headersBuilder, times(1)).createInfoHeader(any(BaseTable.class),
                                                          eq(catalogObjectMetadataSet),
                                                          eq(kind),
                                                          eq(contentType));
        verify(tableDataBuilder, times(1)).buildTableData(eq(catalogObjectMetadataSet), any(BaseTable.class));
        verify(tableCatalogObjectsDependenciesBuilder,
               times(1)).buildCatalogObjectsDependenciesTable(any(PDDocument.class),
                                                              eq(callGraphHolder),
                                                              eq(new ArrayList<>(catalogObjectMetadataSet)),
                                                              any(BaseTable.class));
    }
}
