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

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ActiveEon Team
 * @since 25/06/2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { IntegrationTestConfig.class })
@Transactional
public class BucketServiceTest {

    @Autowired
    private BucketService bucketService;

    private BucketMetadata bucket;

    @Autowired
    private BucketRepository bucketRepository;

    @Before
    public void createBucket() {
        bucket = bucketService.createBucket("bucket", "toto");
        assertThat(bucket).isNotNull();
        assertThat(bucket.getOwner()).isEqualTo("toto");
        assertThat(bucket.getMetaDataId()).isNotNull();
    }

    @After
    public void deleteBucket() {
        bucketRepository.deleteAll();
    }

    @Test
    public void testGetBucket() {
        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets(Optional.of("toto"));
        assertThat(bucketMetadatas).hasSize(1);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(bucket.getMetaDataId());
        assertThat(bucketMetadata).isNotNull();
        assertThat(bucketMetadata.getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadata.getMetaDataId()).isEqualTo(bucket.getMetaDataId());
    }

    @Test
    public void testListBucket() {
        List<BucketMetadata> bucketMetadatas = bucketService.listBuckets(Optional.of("toto"));
        assertThat(bucketMetadatas).hasSize(1);
        assertThat(bucketMetadatas.get(0).getOwner()).isEqualTo(bucket.getOwner());
        assertThat(bucketMetadatas.get(0).getMetaDataId()).isEqualTo(bucket.getMetaDataId());
    }
}
