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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.report.CatalogObjectReportPDFGenerator;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectReportServiceTest {

    public static final String COMMIT_MESSAGE = "commit message";

    public static final String APPLICATION_XML = "application/xml";

    public static final String OBJECT = "object";

    public static final String NAME = "catalog";

    @InjectMocks
    private CatalogObjectReportService catalogObjectReportService;

    @Mock
    private CatalogObjectReportPDFGenerator catalogObjectReportPDFGenerator;

    @Mock
    private CatalogObjectService catalogObjectService;

    @Test
    public void testGenerateBytesReportEmptyBucket() {
        List<String> authorisedBucketsNames = Lists.newArrayList();
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList();
        when(catalogObjectService.listCatalogObjects(anyList(),
                                                     any(Integer.class),
                                                     any(Integer.class))).thenReturn(objectsMetadata);

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReport(authorisedBucketsNames, kind, contentType);

        assertThat(content).isNotNull();

    }

    @Test
    public void testGenerateBytesReportForSelectedObjects() {
        String bucketsName = "bucket3";
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        List<String> objectsName = Lists.newArrayList("one", "two");

        CatalogObjectMetadata one = createObjectMetadata(bucketsName, "one");
        CatalogObjectMetadata two = createObjectMetadata(bucketsName, "two");

        List<CatalogObjectMetadata> objectsMetadata = Lists.newArrayList(one, two);
        when(catalogObjectService.listSelectedCatalogObjects(anyString(), anyList())).thenReturn(objectsMetadata);

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwo".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReportForSelectedObjects(bucketsName,
                                                                                          objectsName,
                                                                                          kind,
                                                                                          contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwo".length());

    }

    @Test
    public void testGenerateBytesReport() {
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

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReport(authorisedBucketsNames, kind, contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesReportWithKind() {
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

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReport(authorisedBucketsNames, kind, contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesReportWithContentType() {
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

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReport(authorisedBucketsNames, kind, contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    @Test
    public void testGenerateBytesReportWithKindAndContentType() {
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

        TreeSet<CatalogObjectMetadata> orderedObjectsPerBucket = sortObjectsPerBucket(objectsMetadata);

        when(catalogObjectReportPDFGenerator.generatePDF(orderedObjectsPerBucket,
                                                         kind,
                                                         contentType)).thenReturn("onetwothree".getBytes());

        byte[] content = catalogObjectReportService.generateBytesReport(authorisedBucketsNames, kind, contentType);

        assertThat(content).isNotNull();
        assertThat(content.length).isEqualTo("onetwothree".length());

    }

    private TreeSet<CatalogObjectMetadata> sortObjectsPerBucket(List<CatalogObjectMetadata> metadataList) {

        Comparator<CatalogObjectMetadata> sortBasedOnName = Comparator.comparing(catalogObject -> catalogObject.getBucketName());
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(catalogObject -> catalogObject.getProjectName()));
        sortBasedOnName = sortBasedOnName.thenComparing(Comparator.comparing(catalogObject -> catalogObject.getName()));

        TreeSet<CatalogObjectMetadata> sortedObjects = new TreeSet<CatalogObjectMetadata>(sortBasedOnName);

        sortedObjects.addAll(metadataList);

        return sortedObjects;
    }

    private CatalogObjectMetadata createObjectMetadata(String bucketName, String name) {
        List<Metadata> metadataList = Lists.newArrayList(new Metadata("project_name", "project " + name, "label"));
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
