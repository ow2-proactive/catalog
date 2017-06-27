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
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.Link;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ActiveEon Team
 * @since 25/06/2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD, classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringApplicationConfiguration(classes = { IntegrationTestConfig.class })
@Transactional
public class CatalogObjectServiceTest {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Autowired
    private BucketRepository bucketRepository;

    private CatalogObjectService mockedCatalogObjectService;

    private BucketEntity bucket;

    private List<KeyValueMetadataEntity> keyValues;

    private byte[] workflowAsByteArray;

    private byte[] workflowAsByteArrayUpdated;

    private long firstCommitTime;

    private long secondCommitTime;

    @Before
    public void setup() throws IOException {

        mockedCatalogObjectService = new CatalogObjectService(catalogObjectRepository,
                                                              catalogObjectRevisionRepository,

                                                              bucketRepository) {
            @Override
            public Link createLink(Long bucketId, String name, long commitTime) throws UnsupportedEncodingException {
                return mock(Link.class);
            }
        };

        bucket = bucketRepository.save(new BucketEntity("bucket", "toto"));
        keyValues = Collections.singletonList(new KeyValueMetadataEntity("key", "value", "type"));
        workflowAsByteArray = IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml");
        workflowAsByteArrayUpdated = IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml");
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucket.getId(),
                                                                                       "catalog1",
                                                                                       "object",
                                                                                       "commit message",
                                                                                       "application/xml",
                                                                                       keyValues,
                                                                                       workflowAsByteArray);
        firstCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        catalogObject = catalogObjectService.createCatalogObjectRevision(bucket.getId(),
                                                                         "catalog1",
                                                                         "commit message 2",
                                                                         keyValues,
                                                                         workflowAsByteArrayUpdated);
        secondCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        catalogObjectService.createCatalogObject(bucket.getId(),
                                                 "catalog2",
                                                 "object",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray);

        catalogObjectService.createCatalogObject(bucket.getId(),
                                                 "catalog3",
                                                 "workflow",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray);
    }

    @Test
    public void testListCatalogObjectsInBucket() {
        List<CatalogObjectMetadata> catalogObjecs = catalogObjectService.listCatalogObjects(bucket.getId());
        assertThat(catalogObjecs).hasSize(3);
    }

    @Test
    public void testListCatalogObjectsByKindInBucket() {
        List<CatalogObjectMetadata> catalogObjecs = catalogObjectService.listCatalogObjectsByKind(bucket.getId(),
                                                                                                  "object");
        assertThat(catalogObjecs).hasSize(2);

        catalogObjecs = catalogObjectService.listCatalogObjectsByKind(bucket.getId(), "workflow");
        assertThat(catalogObjecs).hasSize(1);
    }

    @Test
    public void testGetDefaultCatalogObject() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucket.getId(),
                                                                                                    "catalog1");
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message 2");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("object");
        assertThat(catalogObjectMetadata.getKeyValueMetadataList()).hasSize(1);
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
    }

    @Test
    public void testGetDefaultCatalogRawObject() {
        CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucket.getId(), "catalog1");
        assertThat(rawObject.getRawObject()).isNotNull();
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArrayUpdated);
    }

    @Test
    public void testListCatalogObjectRevisions() {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listCatalogObjectRevisions(bucket.getId(),
                                                                                                   "catalog1");
        assertThat(metadataList).hasSize(2);
    }

    @Test
    public void testGetCatalogObjectRevision() throws UnsupportedEncodingException {

        CatalogObjectMetadata metadata = mockedCatalogObjectService.getCatalogObjectRevision(bucket.getId(),
                                                                                             "catalog1",
                                                                                             firstCommitTime);
        assertThat(metadata.getCommitMessage()).isEqualTo("commit message");
    }

    @Test
    public void testGetCatalogObjectRevisionRaw() throws UnsupportedEncodingException {

        CatalogRawObject rawObject = mockedCatalogObjectService.getCatalogObjectRevisionRaw(bucket.getId(),
                                                                                            "catalog1",
                                                                                            firstCommitTime);
        assertThat(rawObject.getCommitMessage()).isEqualTo("commit message");
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArray);
    }

}
