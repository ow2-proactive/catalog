package org.ow2.proactive.catalog.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketGrantRepository;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.*;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;

import java.util.LinkedList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class BucketGrantServiceTest{

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

    private final String DUMMY_ACCESS_TYPE = "dummyAccessType";

    private final long DUMMY_BUCKET_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllAssignedGrantsForUserAndHisGroup() {
        assertThat(bucketGrantService.getAllAssignedGrantsForUserAndHisGroup(DUMMY_USERNAME, DUMMY_GROUP)).isEmpty();
        verify(bucketGrantRepository, times(1)).findAllGrantsAssignedToAUsername(DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).findAllGrantsAssignedToAUserGroup(DUMMY_GROUP);
    }

    @Test
    public void testUpdateBucketGrantForASpecificUser() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findBucketGrantByUsername(mockedBucket.getId(), DUMMY_USERNAME)).thenReturn(bucketGrantEntity);

        BucketGrantMetadata result = bucketGrantService.updateBucketGrantForASpecificUser(DUMMY_BUCKET, DUMMY_USERNAME, DUMMY_ACCESS_TYPE);

        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals("user", result.getGrantee());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(DUMMY_USERNAME, result.getProfiteer());
        assertEquals(1L, result.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUsername(DUMMY_BUCKET_ID, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).save(bucketGrantEntity);
    }

    @Test
    public void testUpdateBucketGrantForASpecificUserGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("group", DUMMY_CURRENT_USERNAME, DUMMY_GROUP, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findBucketGrantByUserGroup(mockedBucket.getId(), DUMMY_GROUP)).thenReturn(bucketGrantEntity);

        BucketGrantMetadata result = bucketGrantService.updateBucketGrantForASpecificUserGroup(DUMMY_BUCKET, DUMMY_GROUP, DUMMY_ACCESS_TYPE);

        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals("group", result.getGrantee());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(DUMMY_GROUP, result.getProfiteer());
        assertEquals(1L, result.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUserGroup(DUMMY_BUCKET_ID, DUMMY_GROUP);
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
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(bucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);

        BucketGrantMetadata userGrantMetadata = bucketGrantService.createBucketGrant(DUMMY_BUCKET, DUMMY_CURRENT_USERNAME, DUMMY_ACCESS_TYPE, DUMMY_USERNAME, "");

        assertEquals(DUMMY_ACCESS_TYPE, userGrantMetadata.getAccessType());
        assertEquals("user", userGrantMetadata.getGrantee());
        assertEquals(DUMMY_CURRENT_USERNAME, userGrantMetadata.getCreator());
        assertEquals(DUMMY_USERNAME, userGrantMetadata.getProfiteer());
        assertEquals(1L, userGrantMetadata.getBucketId());

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUsername(1L, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).save(bucketGrantEntity);
    }

    @Test
    public void testCreateBucketGrantForUserGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity groupBucketGrantEntity = new BucketGrantEntity("group", DUMMY_CURRENT_USERNAME, DUMMY_GROUP, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketGrantRepository.save(any(BucketGrantEntity.class))).thenReturn(groupBucketGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);

        BucketGrantMetadata userGroupGrantMetadata = bucketGrantService.createBucketGrant(DUMMY_BUCKET, DUMMY_CURRENT_USERNAME, DUMMY_ACCESS_TYPE, "", DUMMY_GROUP);

        assertEquals(userGroupGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(userGroupGrantMetadata.getGrantee(), "group");
        assertEquals(userGroupGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(userGroupGrantMetadata.getProfiteer(), DUMMY_GROUP);
        assertEquals(userGroupGrantMetadata.getBucketId(), 1L);

        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUserGroup(1L, DUMMY_GROUP);
        verify(bucketGrantRepository, times(1)).save(groupBucketGrantEntity);
    }

    @Test
    public void testDeleteBucketGrantForUser() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity userBucketGrantEntity = new BucketGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findBucketGrantByUsername(1L, DUMMY_USERNAME)).thenReturn(userBucketGrantEntity);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));

        BucketGrantMetadata userGrantMetadata = bucketGrantService.deleteBucketGrant(DUMMY_BUCKET, DUMMY_USERNAME, "");

        assertEquals(userGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(userGrantMetadata.getGrantee(), "user");
        assertEquals(userGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(userGrantMetadata.getProfiteer(), DUMMY_USERNAME);
        assertEquals(userGrantMetadata.getBucketId(), 1L);


        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUsername(1L, DUMMY_USERNAME);
        verify(bucketGrantRepository, times(1)).delete(userBucketGrantEntity);
    }

    @Test
    public void testDeleteBucketGrantForGroup() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity groupBucketGrantEntity = new BucketGrantEntity("group", DUMMY_CURRENT_USERNAME, DUMMY_GROUP, DUMMY_ACCESS_TYPE, mockedBucket);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(bucketGrantRepository.findBucketGrantByUserGroup(1L, DUMMY_GROUP)).thenReturn(groupBucketGrantEntity);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));

        BucketGrantMetadata groupGrantMetadata = bucketGrantService.deleteBucketGrant(DUMMY_BUCKET, "", DUMMY_GROUP);

        assertEquals(groupGrantMetadata.getAccessType(), DUMMY_ACCESS_TYPE);
        assertEquals(groupGrantMetadata.getGrantee(), "group");
        assertEquals(groupGrantMetadata.getCreator(), DUMMY_CURRENT_USERNAME);
        assertEquals(groupGrantMetadata.getProfiteer(), DUMMY_GROUP);
        assertEquals(groupGrantMetadata.getBucketId(), 1L);


        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
        verify(bucketGrantRepository, times(1)).findBucketGrantByUserGroup(1L, DUMMY_GROUP);
        verify(bucketGrantRepository, times(1)).delete(groupBucketGrantEntity);
    }

    @Test
    public void testGetBucketsForUserByGrants() {
        BucketEntity firstMockedBucket = newMockedBucket(1L);
        BucketEntity secondMockedBucket = newMockedBucket(2L);

        List<Long> idsFromCatalogObjectGrants = new LinkedList<>();
        idsFromCatalogObjectGrants.add(firstMockedBucket.getId());

        List<Long> idsFromGroupObjectGrants = new LinkedList<>();
        idsFromGroupObjectGrants.add(secondMockedBucket.getId());

        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, firstMockedBucket);

        List<BucketGrantEntity> mockedBucketGrants = new LinkedList<>();
        mockedBucketGrants.add(bucketGrantEntity);

        when(catalogObjectGrantService.getAllBucketIdsFromGrantsAssignedToUsername(DUMMY_CURRENT_USERNAME)).thenReturn(idsFromCatalogObjectGrants);
        when(bucketGrantRepository.findAllGrantsAssignedToAUsername(DUMMY_USERNAME)).thenReturn(mockedBucketGrants);
        when(bucketGrantRepository.findAllGrantsAssignedToAUserGroup(DUMMY_GROUP)).thenReturn(null);
        when(catalogObjectGrantService.getAllBucketIdsFromGrantsAssignedToUserGroup(DUMMY_GROUP)).thenReturn(idsFromGroupObjectGrants);
        when(bucketRepository.findOne(1L)).thenReturn(firstMockedBucket);
        when(bucketRepository.findOne(2L)).thenReturn(secondMockedBucket);

        List<String> groups = new LinkedList<>();
        groups.add(DUMMY_GROUP);
        AuthenticatedUser user = AuthenticatedUser.builder()
                .name(DUMMY_CURRENT_USERNAME)
                .groups(groups)
                .build();
        List<BucketMetadata> results = bucketGrantService.getBucketsForUserByGrants(user);

        assertEquals(2, results.size());
        assertTrue(results.contains(new BucketMetadata(firstMockedBucket)));
        assertTrue(results.contains(new BucketMetadata(secondMockedBucket)));
    }

    @Test
    public void testDeleteAllBucketGrants() {
        BucketEntity mockedBucket = newMockedBucket(1L);
        BucketGrantEntity userBucketGrantEntity = new BucketGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, mockedBucket);
        List<BucketGrantEntity> mockedBucketGrants = new LinkedList<>();
        mockedBucketGrants.add(userBucketGrantEntity);

        when(bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(1L)).thenReturn(mockedBucketGrants);
        doNothing().when(bucketGrantRepository).delete(any(BucketGrantEntity.class));
        doNothing().when(catalogObjectGrantService).deleteAllCatalogObjectsGrantsAssignedToABucket(1L);

        bucketGrantService.deleteAllBucketGrants(mockedBucket.getId());

        verify(bucketGrantRepository, times(1)).findBucketGrantEntitiesByBucketEntityId(mockedBucket.getId());
        verify(bucketGrantRepository, times(1)).delete(mockedBucketGrants);
        verify(catalogObjectGrantService, times(1)).deleteAllCatalogObjectsGrantsAssignedToABucket(mockedBucket.getId());
    }

    private BucketEntity newMockedBucket(Long id) {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(id);
        when(mockedBucket.getBucketName()).thenReturn(DUMMY_BUCKET);
        return mockedBucket;
    }

}