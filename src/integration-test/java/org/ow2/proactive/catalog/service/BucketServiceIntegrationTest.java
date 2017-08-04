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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { IntegrationTestConfig.class })
public class BucketServiceIntegrationTest {

    private static final String DEFAULT_OBJECTS_FOLDER = "/default-objects";

    private static final String RAW_OBJECTS_FOLDER = "/raw-objects";

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectService catalogObjectService;

    private BucketMetadata bucket;

    private List<Metadata> keyValues;

    @Before
    public void createBucket() {
        bucket = bucketService.createBucket("bucket", "BucketServiceIntegrationTest");
        keyValues = Collections.singletonList(new Metadata("key", "value", "type"));
        assertThat(bucket).isNotNull();
        assertThat(bucket.getOwner()).isEqualTo("BucketServiceIntegrationTest");
        assertThat(bucket.getMetaDataId()).isNotNull();
    }

    @After
    public void deleteBucket() {
        bucketService.cleanAll();
    }

    @Test
    public void testThatEmptyOwnerListReturnsAndEmptyListAndDoesNotReturnAnException() {
        List emptyResult = bucketService.listBuckets(Collections.emptyList(), null);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    public void testPopulateCatalogEmpty() throws Exception {
        bucketService.populateCatalog(new String[] {}, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);
        List<BucketMetadata> bucketMetadataList = bucketService.listBuckets((String) null, null);
        assertThat(bucketMetadataList).hasSize(1);
    }

    /*
     * Create 3 buckets, check that the buckets exists
     * 1 bucket is empty
     */
    @Test
    public void testPopulateCatalogCheckBucketsCreation() throws Exception {

        final String[] buckets = { "Examples", "Cloud-automation", "Toto" };
        bucketService.populateCatalog(buckets, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);

        // verify that all buckets have been created in the Catalog
        List<BucketMetadata> bucketMetadataList = bucketService.listBuckets((String) null, null);

        bucketMetadataList.forEach(bucket -> {
            String name = bucket.getName();
            Long id = bucket.getMetaDataId();
            int nbWorkflows = 0;
            String[] workflows = new File(Application.class.getResource(DEFAULT_OBJECTS_FOLDER).getPath() +
                                          File.separator + name).list();
            if (workflows != null) {
                nbWorkflows = workflows.length;
            }

            List<CatalogObjectMetadata> catalogObjectMetadataList = catalogObjectService.listCatalogObjects(id);

            assertThat(catalogObjectMetadataList).hasSize(nbWorkflows);
        });

    }

    @Test
    public void testDeleteEmptyBucket() {
        bucket = bucketService.createBucket("bucketnotempty", "emptyBucketTest");
        catalogObjectService.createCatalogObject(bucket.getMetaDataId(),
                                                 "catalog",
                                                 "object",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 null);

        BucketMetadata emptyBucket = bucketService.createBucket("bucketempty", "emptyBucketTest");

        List<BucketMetadata> emptyBucketTest = bucketService.listBuckets("emptyBucketTest", null);
        assertThat(emptyBucketTest).hasSize(2);

        bucketService.cleanAllEmptyBuckets();
        emptyBucketTest = bucketService.listBuckets("emptyBucketTest", null);
        assertThat(emptyBucketTest).hasSize(1);
        assertThat(emptyBucketTest.get(0).getName()).isEqualTo("bucketnotempty");
    }

    @Test
    public void testGetBucket() {
        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets("BucketServiceIntegrationTest", null);
        assertThat(bucketMetadatas).hasSize(1);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(bucket.getMetaDataId());
        assertThat(bucketMetadata).isNotNull();
        assertThat(bucketMetadata.getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadata.getMetaDataId()).isEqualTo(bucket.getMetaDataId());
    }

    @Test
    public void testListBucket() {
        bucket = bucketService.createBucket("owner", "bucket2");
        catalogObjectService.createCatalogObject(bucket.getMetaDataId(),
                                                 "catalog",
                                                 "workflow",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 null);

        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets("bucket2", null);
        assertThat(bucketMetadatas).hasSize(1);
        assertThat(bucketMetadatas.get(0).getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadatas.get(0).getMetaDataId()).isEqualTo(bucket.getMetaDataId());

        bucketMetadatas = bucketService.listBuckets((String) null, "workflow");
        assertThat(bucketMetadatas).hasSize(2);
        assertThat(bucketMetadatas.get(1).getMetaDataId()).isEqualTo(bucket.getMetaDataId());
    }

}
