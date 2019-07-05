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
package org.ow2.proactive.catalog.service;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.callgraph.CatalogObjectCallGraphPDFGenerator;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 * @since 2019-04-09
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectCallGraphServiceTest {

    public static final String COMMIT_MESSAGE = "commit message";

    public static final String APPLICATION_XML = "application/xml";

    public static final String OBJECT = "object";

    public static final String NAME = "catalog";

    @InjectMocks
    private CatalogObjectCallGraphService catalogObjectCallGraphService;

    @Mock
    private CatalogObjectCallGraphPDFGenerator catalogObjectCallGraphPDFGenerator;

    @Mock
    private CatalogObjectService catalogObjectService;

    private SeparatorUtility separatorUtility = new SeparatorUtility();

    @Test
    public void testGenerateBytesCallGraph() {
        List<String> authorisedBucketsNames = Lists.newArrayList("bucket3", "bucket6");
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        CatalogObjectMetadata one = createObjectMetadata("bucket3", "one");
        CatalogObjectMetadata two = createObjectMetadata("bucket3", "two");
        CatalogObjectMetadata three = createObjectMetadata("bucket6", "three");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two, three);
        when(catalogObjectService.listCatalogObjects(anyList(),
                                                     any(Optional.class),
                                                     any(Optional.class))).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesCallGraphForSelectedObjects() {
        String bucketsName = "bucket3";
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        List<String> objectsName = Lists.newArrayList("one", "two");

        CatalogObjectMetadata one = createObjectMetadata(bucketsName, "one");
        CatalogObjectMetadata two = createObjectMetadata(bucketsName, "two");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two);
        when(catalogObjectService.listSelectedCatalogObjects(anyString(), anyList())).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwo".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraphForSelectedObjects(bucketsName,
                                                                                                objectsName,
                                                                                                kind,
                                                                                                contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwo".length());

    }

    @Test
    public void testGenerateBytesCallGraphWithKind() {
        List<String> authorisedBucketsNames = Lists.newArrayList("bucket3", "bucket6");
        Optional<String> kind = Optional.of("kind");
        Optional<String> contentType = Optional.empty();

        CatalogObjectMetadata one = createObjectMetadata("bucket3", "one");
        CatalogObjectMetadata two = createObjectMetadata("bucket3", "two");
        CatalogObjectMetadata three = createObjectMetadata("bucket6", "three");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two, three);
        when(catalogObjectService.listCatalogObjects(anyList(),
                                                     any(Optional.class),
                                                     any(Optional.class))).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesCallGraphWithContentType() {
        List<String> authorisedBucketsNames = Lists.newArrayList("bucket3", "bucket6");
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.of("contentType");

        CatalogObjectMetadata one = createObjectMetadata("bucket3", "one");
        CatalogObjectMetadata two = createObjectMetadata("bucket3", "two");
        CatalogObjectMetadata three = createObjectMetadata("bucket6", "three");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two, three);
        when(catalogObjectService.listCatalogObjects(anyList(),
                                                     any(Optional.class),
                                                     any(Optional.class))).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesCallGraphWithKindAndContentType() {
        List<String> authorisedBucketsNames = Lists.newArrayList("bucket3", "bucket6");
        Optional<String> kind = Optional.of("kind");
        Optional<String> contentType = Optional.of("contentType");

        CatalogObjectMetadata one = createObjectMetadata("bucket3", "one");
        CatalogObjectMetadata two = createObjectMetadata("bucket3", "two");
        CatalogObjectMetadata three = createObjectMetadata("bucket6", "three");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two, three);
        when(catalogObjectService.listCatalogObjects(anyList(),
                                                     any(Optional.class),
                                                     any(Optional.class))).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesCallGraphEmptyBucket() {
        List<String> authorisedBucketsNames = Lists.newArrayList();
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList();
        when(catalogObjectService.listCatalogObjects(anyList())).thenReturn(objectsMetadata);

        when(catalogObjectCallGraphPDFGenerator.generatePdfImage(objectsMetadata,
                                                                 kind,
                                                                 contentType,
                                                                 catalogObjectService)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        assertThat(content).isNotNull();

    }

    private CatalogObjectMetadata createObjectMetadata(String bucketName, String name) {
        List<Metadata> metadataList = Lists.newArrayList(new Metadata(separatorUtility.getConcatWithSeparator("bucket3",
                                                                                                              "one"),
                                                                      "LATEST",
                                                                      WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL));
        CatalogObjectMetadata catalogObjectMetadata = new CatalogObjectMetadata(bucketName,
                                                                                name,
                                                                                "kind",
                                                                                "contentType",
                                                                                System.currentTimeMillis(),
                                                                                "commitMessage",
                                                                                "username",
                                                                                metadataList,
                                                                                "xml");
        return catalogObjectMetadata;
    }
}
