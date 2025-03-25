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

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectService catalogObjectService;

    private BucketMetadata bucket;

    private List<Metadata> keyValues;

    private static final String PROJECT_NAME = "projectName";

    private static final String TAGS = "tag1,tag2";

    @Before
    public void createBucket() {
        bucket = bucketService.createBucket("bucket", "BucketServiceIntegrationTest", null);
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
    public void testThatEmptyOwnerListReturnsABucketListAndDoesNotReturnAnException() {
        List emptyResult = bucketService.listBuckets(Collections.emptyList(),
                                                     null,
                                                     null,
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     Optional.empty(),
                                                     (String) null,
                                                     false);
        assertThat(emptyResult).hasSize(1);
    }

    @Test
    public void testThatNotAuthorisedTenantCannotSeeBucket() {
        bucketService.createBucket("bucketnotempty", "emptyBucketTest", "");
        List<BucketMetadata> emptyBucketTest = bucketService.listBuckets("emptyBucketTest",
                                                                         "notAuthorized",
                                                                         null,
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         Optional.empty(),
                                                                         (String) null,
                                                                         false);
        assertThat(emptyBucketTest).isEmpty();
    }

    @Test
    public void testDeleteEmptyBucket() {
        bucket = bucketService.createBucket("bucketnotempty", "emptyBucketTest", "");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "object",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        bucketService.createBucket("bucketempty", "emptyBucketTest", "");

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
    public void testUpdateBucketOwner() {
        BucketMetadata bucketMetadataOrigin = bucketService.getBucketMetadata("bucket");
        assertThat(bucketMetadataOrigin.getOwner()).isEqualTo(bucket.getOwner());

        String newOwner = "newOwner";
        bucketService.updateOwnerByBucketName("bucket", newOwner);

        BucketMetadata bucketMetadataUpdatedOwner = bucketService.getBucketMetadata("bucket");
        assertThat(bucketMetadataUpdatedOwner.getOwner()).isEqualTo(newOwner);

        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets(newOwner, Optional.empty(), Optional.empty());
        assertThat(bucketMetadatas).hasSize(1);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(bucket.getName());
        assertThat(bucketMetadata).isNotNull();
        assertThat(bucketMetadata.getOwner()).isEqualTo(newOwner);
        assertThat(bucketMetadata.getName()).isEqualTo(bucket.getName());
    }

    @Test
    public void testListBucketByOwnerAndCaseInsensitiveByKind() {
        bucket = bucketService.createBucket("bucket-workflow", "owner", "");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
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
        assertThat(bucketMetadatas).hasSize(1);

        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));
        //check the order of retrieved buckets
        assertThat(bucketMetadatas.get(0).getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadatas.get(0).getName()).isEqualTo(bucket.getName());
    }

    @Test
    public void testListBucketsCaseInsensitiveFilterByKindPrefix() {
        //create bucket with kind object workflow inside
        bucket = bucketService.createBucket("bucket-workflow", "owner", "");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "Workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/standard inside
        BucketMetadata bucketWfStandard = bucketService.createBucket("bucket-wf-standard", "owner", "");
        catalogObjectService.createCatalogObject(bucketWfStandard.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "WorkFlow/Standard",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/pca inside
        BucketMetadata bucketWfPCA = bucketService.createBucket("bucket-wf-pca", "owner", "");
        catalogObjectService.createCatalogObject(bucketWfPCA.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "workflow/PCA",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object not-workflow inside
        BucketMetadata bucketNotWf = bucketService.createBucket("bucket-not-workflow", "different-owner", "");
        catalogObjectService.createCatalogObject(bucketNotWf.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
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

        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("owner",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getOwner()))));
        org.hamcrest.MatcherAssert.assertThat(bucketMetadatas,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));

        //we expect to get only workflow/pca bucket
        List<BucketMetadata> bucketMetadatasWfPCA = bucketService.listBuckets(null,
                                                                              Optional.of("Workflow/pca"),
                                                                              Optional.empty());
        assertThat(bucketMetadatasWfPCA).hasSize(1);

        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWfPCA,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucketWfPCA.getName()))));

        //we expect to get only workflow/standard bucket
        List<BucketMetadata> bucketMetadatasWfStandard = bucketService.listBuckets(null,
                                                                                   Optional.of("workflow/STANDARD"),
                                                                                   Optional.empty());
        assertThat(bucketMetadatasWfStandard).hasSize(1);

        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWfStandard,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucketWfStandard.getName()))));

        //we expect to get all workflow kind bucket
        List<BucketMetadata> bucketMetadatasWorkflows = bucketService.listBuckets(null,
                                                                                  Optional.of("WORKFLOW"),
                                                                                  Optional.empty());
        assertThat(bucketMetadatasWorkflows).hasSize(3);
        //check the order of retrieved buckets
        assertThat(bucketMetadatasWorkflows.get(1).getOwner()).isEqualTo(bucketWfStandard.getOwner());
        assertThat(bucketMetadatasWorkflows.get(1).getName()).isEqualTo(bucketWfStandard.getName());
        assertThat(bucketMetadatasWorkflows.get(2).getOwner()).isEqualTo(bucketWfPCA.getOwner());
        assertThat(bucketMetadatasWorkflows.get(2).getName()).isEqualTo(bucketWfPCA.getName());

        org.hamcrest.MatcherAssert.assertThat(bucketMetadatasWorkflows,
                                              org.hamcrest.Matchers.hasItem(org.hamcrest.beans.HasPropertyWithValue.hasProperty("name",
                                                                                                                                org.hamcrest.CoreMatchers.is(bucket.getName()))));
    }

    @Test
    public void testListBucketsFilterByKindList() {
        //create bucket with kind object workflow inside
        bucket = bucketService.createBucket("bucket-workflow", "owner", "");
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "Workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/standard inside
        BucketMetadata bucketWfStandard = bucketService.createBucket("bucket-rule", "owner", "");
        catalogObjectService.createCatalogObject(bucketWfStandard.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object workflow/pca inside
        BucketMetadata bucketWfPCA = bucketService.createBucket("bucket-wf-pca", "owner", "");
        catalogObjectService.createCatalogObject(bucketWfPCA.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "workflow/PCA",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //create bucket with kind object not-workflow inside
        BucketMetadata bucketNotWf = bucketService.createBucket("bucket-script", "different-owner", "");
        catalogObjectService.createCatalogObject(bucketNotWf.getName(),
                                                 "catalog",
                                                 PROJECT_NAME,
                                                 TAGS,
                                                 "script",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 null,
                                                 null);

        //we expect to get workflow buckets
        List<BucketMetadata> bucketMetadatasWorkflow = bucketService.listBuckets(null,
                                                                                 Optional.of("Workflow,Workflow/PCA"),
                                                                                 Optional.empty());
        assertThat(bucketMetadatasWorkflow).hasSize(2);

        //we expect to get all buckets
        List<BucketMetadata> bucketMetadatasWorkflowRuleScript = bucketService.listBuckets(null,
                                                                                           Optional.of("Workflow,RULE,ScriPT"),
                                                                                           Optional.empty());
        assertThat(bucketMetadatasWorkflowRuleScript).hasSize(4);

    }

}
