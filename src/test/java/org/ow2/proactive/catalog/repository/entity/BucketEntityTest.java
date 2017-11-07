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
package org.ow2.proactive.catalog.repository.entity;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;


/**
 * @author ActiveEon Team
 */
public class BucketEntityTest {

    private final String DEFAULT_BUCKET_NAME = "test";

    private final String DEFAULT_BUCKET_USER = "BucketTestUser";

    private BucketEntity bucket;

    @Before
    public void setUp() {
        bucket = new BucketEntity(DEFAULT_BUCKET_NAME, DEFAULT_BUCKET_USER);
    }

    @Test
    public void testAddWorkflow() throws Exception {
        CatalogObjectEntity catalogObject = new CatalogObjectEntity();
        catalogObject.setBucket(bucket);
        catalogObject.setId(new CatalogObjectEntity.CatalogObjectEntityKey(null, "name"));
        bucket.addCatalogObject(catalogObject);

        assertThat(bucket.getCatalogObjects()).hasSize(1);
        assertThat(catalogObject.getBucket()).isEqualTo(bucket);
    }

    @Test
    public void testSetName() {
        final String expectedName = "EXPECTED_BUCKET_NAME";
        bucket.setBucketName(expectedName);
        assertEquals(expectedName, bucket.bucketName);
    }

    @Test
    public void testSetWorkflows() throws Exception {
        Set<CatalogObjectEntity> catalogObjectList = ImmutableSet.of();
        bucket.setCatalogObjects(catalogObjectList);
        assertEquals(catalogObjectList, bucket.getCatalogObjects());
    }

    @Test
    public void testGetId() throws Exception {
        Long expectedId = 42L;
        bucket.id = expectedId;
        assertEquals(expectedId, bucket.getId());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(DEFAULT_BUCKET_NAME, bucket.getBucketName());
    }

    @Test
    public void testGetWorkflows() throws Exception {
        Set<CatalogObjectEntity> catalogObjectList = ImmutableSet.of();
        bucket.setCatalogObjects(catalogObjectList);
        assertEquals(catalogObjectList, bucket.getCatalogObjects());
    }

    @Test
    public void testGetOwner() throws Exception {
        assertEquals(DEFAULT_BUCKET_USER, bucket.getOwner());
    }

    @Test
    public void testSetOwner() throws Exception {
        final String expectedOWner = "TOTO";
        bucket.setOwner(expectedOWner);
        assertEquals(expectedOWner, bucket.getOwner());
    }
}
