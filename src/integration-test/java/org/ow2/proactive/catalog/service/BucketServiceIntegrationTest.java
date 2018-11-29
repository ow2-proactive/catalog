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
import static junit.framework.TestCase.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
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
        assertThat(bucket.getName()).isNotNull();
    }

    @After
    public void deleteBucket() {
        bucketService.cleanAll();
    }

    @Test
    public void testThatEmptyOwnerListReturnsAndEmptyListAndDoesNotReturnAnException() {
        List emptyResult = bucketService.listBuckets(Collections.emptyList(), Optional.empty(), Optional.empty());
        assertThat(emptyResult).isEmpty();
    }

    @Test
    public void testDeleteEmptyBucket() {
        bucket = bucketService.createBucket("bucketnotempty", "emptyBucketTest");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 "object",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        BucketMetadata emptyBucket = bucketService.createBucket("bucketempty", "emptyBucketTest");

        List<BucketMetadata> emptyBucketTest = bucketService.listBuckets("emptyBucketTest",
                                                                         Optional.empty(),
                                                                         Optional.empty());
        assertThat(emptyBucketTest).hasSize(2);

        bucketService.cleanAllEmptyBuckets();
        emptyBucketTest = bucketService.listBuckets("emptyBucketTest", Optional.empty(), Optional.empty());
        assertThat(emptyBucketTest).hasSize(1);
        assertThat(emptyBucketTest.get(0).getName()).isEqualTo("bucketnotempty");

    }

    @Test
    public void testGetBucket() {
        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets("BucketServiceIntegrationTest",
                                                                         Optional.empty(),
                                                                         Optional.empty());
        assertThat(bucketMetadatas).hasSize(1);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(bucket.getName());
        assertThat(bucketMetadata).isNotNull();
        assertThat(bucketMetadata.getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadata.getName()).isEqualTo(bucket.getName());
    }

    @Test
    public void testListBucketByOwnerAndCaseInsensitiveByKind() {
        bucket = bucketService.createBucket("bucket-workflow", "owner");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 "WORKFLOW",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets("owner", Optional.empty(), Optional.empty());
        assertThat(bucketMetadatas).hasSize(1);
        assertThat(bucketMetadatas.get(0).getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadatas.get(0).getName()).isEqualTo(bucket.getName());

        bucketMetadatas = bucketService.listBuckets((String) null, Optional.of("workflow"), Optional.empty());
        assertThat(bucketMetadatas).hasSize(2);
        //assertThat(bucketMetadatas.get(1).getName()).isEqualTo(bucket.getName());
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));

    }

    @Test
    public void testListBucketsCaseInsensitiveFilterByKindPrefix() {
        //create bucket with kind object workflow inside
        bucket = bucketService.createBucket("bucket-workflow", "owner");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 "Workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/standard inside
        BucketMetadata bucketWfStandard = bucketService.createBucket("bucket-wf-standard", "owner");
        catalogObjectService.createCatalogObject(bucketWfStandard.getName(),
                                                 "catalog",
                                                 "WorkFlow/Standard",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/pca inside
        BucketMetadata bucketWfPCA = bucketService.createBucket("bucket-wf-pca", "owner");
        catalogObjectService.createCatalogObject(bucketWfPCA.getName(),
                                                 "catalog",
                                                 "workflow/PCA",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object not-workflow inside
        BucketMetadata bucketNotWf = bucketService.createBucket("bucket-not-workflow", "different-owner");
        catalogObjectService.createCatalogObject(bucketNotWf.getName(),
                                                 "catalog",
                                                 "not-workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        // test filtering by owner
        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets("owner", Optional.empty(), Optional.empty());
        assertThat(bucketMetadatas).hasSize(3);
        //assertThat(bucketMetadatas.get(0).getOwner()).isEqualTo(bucket.getOwner());
        //assertThat(bucketMetadatas.get(0).getName()).isEqualTo(bucket.getName());
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("owner",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getOwner()))));
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));

        //we expect to get only workflow/pca bucket and empty bucket
        List<BucketMetadata> bucketMetadatasWfPCA = bucketService.listBuckets((String) null,
                                                                              Optional.of("Workflow/pca"),
                                                                              Optional.empty());
        assertThat(bucketMetadatasWfPCA).hasSize(2);
        //assertThat(bucketMetadatasWfPCA.get(1).getName()).isEqualTo(bucketWfPCA.getName());
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWfPCA,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucketWfPCA.getName()))));

        //we expect to get only workflow/standard bucket and empty bucket
        List<BucketMetadata> bucketMetadatasWfStandard = bucketService.listBuckets((String) null,
                                                                                   Optional.of("workflow/STANDARD"),
                                                                                   Optional.empty());
        assertThat(bucketMetadatasWfStandard).hasSize(2);
        //assertThat(bucketMetadatasWfStandard.get(1).getName()).isEqualTo(bucketWfStandard.getName());
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWfStandard,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucketWfStandard.getName()))));

        //we expect to get all workflow kind bucket and empty bucket
        List<BucketMetadata> bucketMetadatasWorkflows = bucketService.listBuckets((String) null,
                                                                                  Optional.of("WORKFLOW"),
                                                                                  Optional.empty());
        assertThat(bucketMetadatasWorkflows).hasSize(4);
        //assertThat(bucketMetadatasWorkflows.get(1).getName()).isEqualTo(bucket.getName());
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWorkflows,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));
    }

}
