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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.service.exception.KindOrContentTypeIsNotValidException;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author ActiveEon Team
 * @since 25/06/2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class CatalogObjectServiceIntegrationTest {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    private BucketMetadata bucket;

    private List<Metadata> keyValues;

    private byte[] workflowAsByteArray;

    private byte[] workflowAsByteArrayUpdated;

    private long firstCommitTime;

    private long secondCommitTime;

    @Before
    public void setup() throws IOException, InterruptedException {
        bucket = bucketService.createBucket("bucket", "CatalogObjectServiceIntegrationTest");
        keyValues = Collections.singletonList(new Metadata("key", "value", "type"));

        workflowAsByteArray = IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml");
        workflowAsByteArrayUpdated = IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml");
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucket.getName(),
                                                                                       "object-name-1",
                                                                                       "object",
                                                                                       "commit message",
                                                                                       "username",
                                                                                       "application/xml",
                                                                                       keyValues,
                                                                                       workflowAsByteArray,
                                                                                       null);
        firstCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Thread.sleep(1); // to be sure that a new revision time will be different from previous revision time
        catalogObject = catalogObjectService.createCatalogObjectRevision(bucket.getName(),
                                                                         "object-name-1",
                                                                         "commit message 2",
                                                                         "username",
                                                                         keyValues,
                                                                         workflowAsByteArrayUpdated);
        secondCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-2",
                                                 "object",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-3",
                                                 "workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
    }

    @After
    public void deleteBucket() {
        bucketService.cleanAll();
    }

    @Test
    public void testGetWorkflowCatalogObjectWithDependsOnModel() throws IOException {
        String wfName = "workflow-depends-on";
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 wfName,
                                                 "workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model.xml"),
                                                 null);

        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucket.getName(),
                                                                                                    wfName);
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("workflow");
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(14);

        List<String> dependsOnKeys = catalogObjectMetadata.getMetadataList()
                                                          .stream()
                                                          .filter(metadata -> metadata.getLabel()
                                                                                      .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                                          .map(Metadata::getKey)
                                                          .collect(Collectors.toList());
        assertThat(dependsOnKeys).contains("basic-examples/Native_Task");
        assertThat(dependsOnKeys).contains("data-connectors/FTP");
        assertThat(dependsOnKeys).contains("finance/QuantLib");
        assertThat(dependsOnKeys).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(dependsOnKeys).hasSize(4);
    }

    @Test
    public void testListCatalogObjectsInBucket() {
        List<CatalogObjectMetadata> catalogObjects = catalogObjectService.listCatalogObjects(Arrays.asList(bucket.getName()));
        assertThat(catalogObjects).hasSize(3);
    }

    @Test
    public void testGetAllKinds() {
        Set<String> listKinds = catalogObjectService.getKinds();
        assertThat(listKinds).hasSize(2);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-4",
                                                 "workflow/new",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
        listKinds = catalogObjectService.getKinds();
        assertThat(listKinds).hasSize(3);
        assertThat(listKinds).contains("workflow/new");
    }

    @Test
    public void testUpdateObjectMetadata() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.updateObjectMetadata(bucket.getName(),
                                                                                                "object-name-1",
                                                                                                Optional.of("updated-kind"),
                                                                                                Optional.of("updated-contentType"));
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message 2");
        assertThat(catalogObjectMetadata.getUsername()).isEqualTo("username");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(3);
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("updated-kind");
    }

    @Test(expected = KindOrContentTypeIsNotValidException.class)
    public void testUpdateObjectMetadataWrongKind() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.updateObjectMetadata(bucket.getName(),
                                                                                                "object-name-1",
                                                                                                Optional.of("updated-kind//a asdf"),
                                                                                                Optional.of("updated-contentType"));
    }

    @Test(expected = KindOrContentTypeIsNotValidException.class)
    public void testCreateObjectWrongKind() {
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-2",
                                                 "updated-kind//a asdf",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
    }

    @Test
    public void testListCatalogObjectsByKindInBucket() {
        List<CatalogObjectMetadata> catalogObjects = catalogObjectService.listCatalogObjectsByKind(Arrays.asList(bucket.getName()),
                                                                                                   "object");
        assertThat(catalogObjects).hasSize(2);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog4",
                                                 "workflow-general",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjects = catalogObjectService.listCatalogObjectsByKind(Arrays.asList(bucket.getName()),
                                                                       "workflow-general");
        assertThat(catalogObjects).hasSize(1);

        catalogObjects = catalogObjectService.listCatalogObjectsByKind(Arrays.asList(bucket.getName()), "WORKFLOW");
        assertThat(catalogObjects).hasSize(2);
    }

    @Test
    public void testGetDefaultCatalogObject() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucket.getName(),
                                                                                                    "object-name-1");
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message 2");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("object");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(3);
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
    }

    @Test
    public void testGetDefaultCatalogRawObject() {
        CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucket.getName(), "object-name-1");
        assertThat(rawObject.getRawObject()).isNotNull();
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArrayUpdated);
    }

    @Test
    public void testListCatalogObjectRevisions() {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listCatalogObjectRevisions(bucket.getName(),
                                                                                                   "object-name-1");
        assertThat(metadataList).hasSize(2);
    }

    @Test
    public void testGetCatalogObjectRevision() throws UnsupportedEncodingException {

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectRevision(bucket.getName(),
                                                                                       "object-name-1",
                                                                                       firstCommitTime);
        assertThat(metadata.getCommitMessage()).isEqualTo("commit message");
    }

    @Test
    public void testGetCatalogObjectRevisionRaw() throws UnsupportedEncodingException {

        CatalogRawObject rawObject = catalogObjectService.getCatalogObjectRevisionRaw(bucket.getName(),
                                                                                      "object-name-1",
                                                                                      firstCommitTime);
        assertThat(rawObject.getCommitMessage()).isEqualTo("commit message");
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArray);
    }

}
