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
package org.ow2.proactive.catalog.util;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.callgraph.CallGraphHolder;
import org.ow2.proactive.catalog.callgraph.GraphNode;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;

import be.quodlibet.boxable.BaseTable;


@RunWith(value = MockitoJUnitRunner.class)
public class ReportGeneratorHelperTest {

    @InjectMocks
    private ReportGeneratorHelper reportGeneratorHelper;

    @Mock
    private SeparatorUtility separatorUtility;

    @Mock
    private CatalogObjectService catalogObjectService;

    private PDDocument document;

    @Before
    public void setUp() {
        when(separatorUtility.getConcatWithSeparator(anyString(), anyString())).thenCallRealMethod();
        when(separatorUtility.getSplitBySeparator(anyString())).thenCallRealMethod();

        document = new PDDocument();
    }

    @Test
    public void initializeTableTest() throws IOException {
        final PDPage pdPage = new PDPage();
        final float margin = 1f;

        BaseTable result = reportGeneratorHelper.initializeTable(document, margin, pdPage);
        assertThat(result.getCurrentPage()).isEqualTo(pdPage);
        assertThat(result.getMargin()).isWithin(0.0001f).of(margin);
        assertThat(result.document).isEqualTo(document);
    }

    @Test
    public void addNewPage() {
        assertThat(document.getPages().getCount()).isEqualTo(0);
        reportGeneratorHelper.addNewPage(document);
        assertThat(document.getPages().getCount()).isEqualTo(1);
        reportGeneratorHelper.addNewPage(document);
        assertThat(document.getPages().getCount()).isEqualTo(2);
    }

    @Test
    public void extractBucketSetTest() {

        final String name1 = "bucketName1";
        final String name2 = "bucketName2";

        CallGraphHolder callGraphHolder = new CallGraphHolder();
        callGraphHolder.addNode(name1, "objectName", "kind1", true);
        callGraphHolder.addNode(name2, "objectName", "kind2", false);
        callGraphHolder.addNode(name2, "objectName2", "kind2", true);
        assertThat(reportGeneratorHelper.extractBucketSet(callGraphHolder)).containsExactly(name1, name2);
    }

    @Test
    public void extractObjectSetTest() {

        final String name1 = "objectName1";
        final String name2 = "objectName2";

        CallGraphHolder callGraphHolder = new CallGraphHolder();
        callGraphHolder.addNode("bucketName", name1, "kind", true);
        callGraphHolder.addNode("bucketName2", name2, "kind", false);
        callGraphHolder.addNode("bucketName", name2, "kind2", false);
        assertThat(reportGeneratorHelper.extractObjectSet(callGraphHolder)).containsExactly(name1, name2);
    }

    @Test
    public void buildCatalogCallGraphTest() {

        final String bucketName = "bucketName";
        final String objectName1 = "objectName1";
        final String objectName2 = "objectName2";

        Metadata metadata1 = new Metadata(separatorUtility.getConcatWithSeparator(bucketName, objectName2),
                                          "value",
                                          WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL);

        CatalogObjectMetadata catalogObjectMetadata1 = new CatalogObjectMetadata(bucketName,
                                                                                 objectName1,
                                                                                 "object",
                                                                                 "application/xml",
                                                                                 1400343L,
                                                                                 "commit message",
                                                                                 "username",
                                                                                 Collections.singletonList(metadata1),
                                                                                 "xml");

        CatalogObjectMetadata catalogObjectMetadata2 = new CatalogObjectMetadata("bucket-name",
                                                                                 objectName2,
                                                                                 "object",
                                                                                 "application/xml",
                                                                                 1400343L,
                                                                                 "commit message",
                                                                                 "username",
                                                                                 Collections.emptyList(),
                                                                                 "xml");

        when(catalogObjectService.isDependsOnObjectExistInCatalog(eq(bucketName),
                                                                  eq(objectName1),
                                                                  anyString())).thenReturn(true);
        when(catalogObjectService.getCatalogObjectMetadata(bucketName, objectName1)).thenReturn(catalogObjectMetadata2);

        List<CatalogObjectMetadata> catalogObjectMetadataList = Arrays.asList(catalogObjectMetadata1,
                                                                              catalogObjectMetadata2);

        CallGraphHolder result = reportGeneratorHelper.buildCatalogCallGraph(catalogObjectMetadataList);

        assertThat(result.nodeSet()).hasSize(2);
        assertThat(result.nodeSet().iterator().next().getBucketName()).isEqualTo(bucketName);
        assertThat(result.nodeSet()
                         .stream()
                         .map(GraphNode::getObjectName)
                         .collect(Collectors.toList())).containsExactly(objectName1, objectName2);
        assertThat(result.nodeSet()
                         .stream()
                         .map(GraphNode::getObjectKind)
                         .collect(Collectors.toList())).containsExactly("object", "N/A");
        assertThat(result.nodeSet()
                         .stream()
                         .map(GraphNode::isInCatalog)
                         .collect(Collectors.toList())).containsExactly(true, false);

    }

}
