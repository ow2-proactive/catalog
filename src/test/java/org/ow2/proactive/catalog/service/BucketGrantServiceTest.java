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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.repository.BucketGrantRepository;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.*;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class BucketGrantServiceTest {

    @InjectMocks
    BucketGrantService bucketGrantService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private BucketGrantRepository bucketGrantRepository;

    @Mock
    private CatalogObjectGrantService catalogObjectGrantService;

    private final String DUMMY_USERNAME = "dummyUser";

    private final String DUMMY_CURRENT_USERNAME = "dummyAdmin";

    private final String DUMMY_GROUP = "dummyGroup";

    private final String DUMMY_BUCKET = "dummyBucket";

    private final String DUMMY_ACCESS_TYPE = "read";

    private final long DUMMY_BUCKET_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateBucketGrantForASpecificUser() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user",
                                                                    DUMMY_CURRENT_USERNAME,
                                                                    DUMMY_USERNAME,
                                                                    DUMMY_ACCESS_TYPE,
                                                                    mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findAllBucketGrantByUsername(mockedBucket.getId(),
                                                                DUMMY_USERNAME)).thenReturn(bucketGrantEntity);

        BucketGrantMetadata result = bucketGrantService.updateBucketGrantForASpecificUser(DUMMY_BUCKET,
                                                                                          DUMMY_USERNAME,
                                                                                          DUMMY_ACCESS_TYPE);

        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals("user", result.getGranteeType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(DUMMY_USERNAME, result.getGrantee());
        assertEquals(1L, result.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAllBucketGrantByUsername(DUMMY_BUCKET_ID, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).save(bucketGrantEntity);
    }

    @Test
    public void testUpdateBucketGrantForASpecificUserGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("group",
                                                                    DUMMY_CURRENT_USERNAME,
                                                                    DUMMY_GROUP,
                                                                    DUMMY_ACCESS_TYPE,
                                                                    mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findAllBucketGrantByUserGroup(mockedBucket.getId(),
                                                                 DUMMY_GROUP)).thenReturn(bucketGrantEntity);

        BucketGrantMetadata result = bucketGrantService.updateBucketGrantForASpecificUserGroup(DUMMY_BUCKET,
                                                                                               DUMMY_GROUP,
                                                                                               DUMMY_ACCESS_TYPE,
                                                                                               1);

        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals("group", result.getGranteeType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(DUMMY_GROUP, result.getGrantee());
        assertEquals(1L, result.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAllBucketGrantByUserGroup(DUMMY_BUCKET_ID, DUMMY_GROUP);
        verify(bucketGrantRepository, times(1)).save(bucketGrantEntity);
    }

    @Test
    public void testGetAllGrantsCreatedByUsername() {
        assertThat(bucketGrantService.getAllGrantsCreatedByUsername(DUMMY_USERNAME)).isEmpty();
        verify(bucketGrantRepository, times(1)).findBucketGrantEntitiesByCreator(DUMMY_USERNAME);
    }

    @Test
    public void testCreateBucketGrantForUSer() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user",
                                                                    DUMMY_CURRENT_USERNAME,
                                                                    DUMMY_USERNAME,
                                                                    DUMMY_ACCESS_TYPE,
                                                                    mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);

        BucketGrantMetadata userGrantMetadata = bucketGrantService.createBucketGrantForAUser(DUMMY_BUCKET,
                                                                                             DUMMY_CURRENT_USERNAME,
                                                                                             DUMMY_ACCESS_TYPE,
                                                                                             DUMMY_USERNAME);

        assertEquals(DUMMY_ACCESS_TYPE, userGrantMetadata.getAccessType());
        assertEquals("user", userGrantMetadata.getGranteeType());
        assertEquals(DUMMY_CURRENT_USERNAME, userGrantMetadata.getCreator());
        assertEquals(DUMMY_USERNAME, userGrantMetadata.getGrantee());
        assertEquals(1L, userGrantMetadata.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAccessibleBucketGrantByUsername(1L, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).save(bucketGrantEntity);
    }

    @Test
    public void testCreateBucketGrantForUserGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity groupBucketGrantEntity = new BucketGrantEntity("group",
                                                                         DUMMY_CURRENT_USERNAME,
                                                                         DUMMY_GROUP,
                                                                         DUMMY_ACCESS_TYPE,
                                                                         1,
                                                                         mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(groupBucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);

        BucketGrantMetadata userGroupGrantMetadata = bucketGrantService.createBucketGrantForAGroup(DUMMY_BUCKET,
                                                                                                   DUMMY_CURRENT_USERNAME,
                                                                                                   DUMMY_ACCESS_TYPE,
                                                                                                   1,
                                                                                                   DUMMY_GROUP);

        assertEquals(userGroupGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(userGroupGrantMetadata.getGranteeType(), "group");
        assertEquals(userGroupGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(userGroupGrantMetadata.getGrantee(), DUMMY_GROUP);
        assertEquals(userGroupGrantMetadata.getBucketId(), 1L);

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAccessibleBucketGrantByUserGroup(1L, DUMMY_GROUP);
        verify(bucketGrantRepository, times(1)).save(groupBucketGrantEntity);
    }

    @Test
    public void testDeleteBucketGrantForUser() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity userBucketGrantEntity = new BucketGrantEntity("user",
                                                                        DUMMY_CURRENT_USERNAME,
                                                                        DUMMY_USERNAME,
                                                                        DUMMY_ACCESS_TYPE,
                                                                        mockedBucket);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findAccessibleBucketGrantByUsername(1L,
                                                                       DUMMY_USERNAME)).thenReturn(userBucketGrantEntity);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));

        BucketGrantMetadata userGrantMetadata = bucketGrantService.deleteBucketGrantForAUser(DUMMY_BUCKET,
                                                                                             DUMMY_USERNAME);

        assertEquals(userGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(userGrantMetadata.getGranteeType(), "user");
        assertEquals(userGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(userGrantMetadata.getGrantee(), DUMMY_USERNAME);
        assertEquals(userGrantMetadata.getBucketId(), 1L);

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAccessibleBucketGrantByUsername(1L, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).delete(userBucketGrantEntity);
    }

    @Test
    public void testDeleteBucketGrantForGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity groupBucketGrantEntity = new BucketGrantEntity("group",
                                                                         DUMMY_CURRENT_USERNAME,
                                                                         DUMMY_GROUP,
                                                                         DUMMY_ACCESS_TYPE,
                                                                         mockedBucket);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findAccessibleBucketGrantByUserGroup(1L,
                                                                        DUMMY_GROUP)).thenReturn(groupBucketGrantEntity);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));

        BucketGrantMetadata groupGrantMetadata = bucketGrantService.deleteBucketGrantForAGroup(DUMMY_BUCKET,
                                                                                               DUMMY_GROUP);

        assertEquals(groupGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(groupGrantMetadata.getGranteeType(), "group");
        assertEquals(groupGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(groupGrantMetadata.getGrantee(), DUMMY_GROUP);
        assertEquals(groupGrantMetadata.getBucketId(), 1L);

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findAccessibleBucketGrantByUserGroup(1L, DUMMY_GROUP);
        verify(bucketGrantRepository, times(1)).delete(groupBucketGrantEntity);
    }

    @Test
    public void testDeleteAllBucketGrants() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity userBucketGrantEntity = new BucketGrantEntity("user",
                                                                        DUMMY_CURRENT_USERNAME,
                                                                        DUMMY_USERNAME,
                                                                        DUMMY_ACCESS_TYPE,
                                                                        mockedBucket);
        List<BucketGrantEntity> mockedBucketGrants = new LinkedList<>();
        mockedBucketGrants.add(userBucketGrantEntity);

        when(bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(1L)).thenReturn(mockedBucketGrants);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));
        doNothing().when(catalogObjectGrantService).deleteAllCatalogObjectsGrantsAssignedToABucket(1L);

        bucketGrantService.deleteAllGrantsAssignedToABucketAndItsObjects(mockedBucket.getId());

        verify(bucketGrantRepository, times(1)).findBucketGrantEntitiesByBucketEntityId(mockedBucket.getId());
        verify(bucketGrantRepository, times(1)).delete(mockedBucketGrants);
        verify(catalogObjectGrantService,
               times(1)).deleteAllCatalogObjectsGrantsAssignedToABucket(mockedBucket.getId());
    }

    private BucketEntity newMockedBucket(Long id) {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(id);
        when(mockedBucket.getBucketName()).thenReturn(DUMMY_BUCKET);
        return mockedBucket;
    }

}
