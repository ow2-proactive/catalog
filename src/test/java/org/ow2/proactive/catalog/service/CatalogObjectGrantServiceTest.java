package org.ow2.proactive.catalog.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetaData;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectGrantRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.*;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectGrantServiceTest {

    @InjectMocks
    CatalogObjectGrantService catalogObjectGrantService;

    @Mock
    private CatalogObjectGrantRepository catalogObjectGrantRepository;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    private final String DUMMY_USERNAME = "dummyUser";

    private final String DUMMY_CURRENT_USERNAME = "dummyAdmin";

    private final String DUMMY_BUCKET = "dummyBucket";

    private final String DUMMY_OBJECT = "dummyObject";

    private final String DUMMY_ACCESS_TYPE = "dummyAccessType";

    private final long BUCKET_ID = 1L;

    private final long CATALOG_OBJECT_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCatalogObjectGrant() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);
        CatalogObjectGrantEntity dbCatalogObjectGrantEntity = new CatalogObjectGrantEntity("user", "admin", "user", "admin", dummyCatalogObjectRevisionEntity, mockedBucket);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsername(anyLong(), anyString(), anyLong())).thenReturn(dbCatalogObjectGrantEntity);
        when(catalogObjectGrantRepository.save(any(CatalogObjectGrantEntity.class))).thenReturn(catalogObjectGrantEntity);

        CatalogObjectGrantMetaData result = catalogObjectGrantService.createCatalogObjectGrant(DUMMY_BUCKET, DUMMY_OBJECT, DUMMY_CURRENT_USERNAME, DUMMY_ACCESS_TYPE, DUMMY_USERNAME, "");

        assertEquals("user", result.getGrantee());
        assertEquals(DUMMY_USERNAME, result.getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, result.getCatalogObjectId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName, DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsername(CATALOG_OBJECT_ID, DUMMY_USERNAME, BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).save(catalogObjectGrantEntity);
    }

    @Test
    public void testDeleteCatalogObjectGrant() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsername(anyLong(), anyString(), anyLong())).thenReturn(catalogObjectGrantEntity);
        doNothing().when(catalogObjectGrantRepository).delete(any(CatalogObjectGrantEntity.class));

        CatalogObjectGrantMetaData result = catalogObjectGrantService.deleteCatalogObjectGrant(DUMMY_BUCKET, DUMMY_OBJECT, DUMMY_USERNAME, "");

        assertEquals("user", result.getGrantee());
        assertEquals(DUMMY_USERNAME, result.getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, result.getCatalogObjectId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName, DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsername(CATALOG_OBJECT_ID, DUMMY_USERNAME, BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).delete(catalogObjectGrantEntity);
    }

    @Test
    public void testUpdateCatalogObjectGrant() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsername(anyLong(), anyString(), anyLong())).thenReturn(catalogObjectGrantEntity);
        when(catalogObjectGrantRepository.save(any(CatalogObjectGrantEntity.class))).thenReturn(catalogObjectGrantEntity);

        CatalogObjectGrantMetaData result = catalogObjectGrantService.updateCatalogObjectGrant(DUMMY_USERNAME, "", DUMMY_OBJECT, DUMMY_BUCKET, DUMMY_ACCESS_TYPE);

        assertEquals("user", result.getGrantee());
        assertEquals(DUMMY_USERNAME, result.getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, result.getCatalogObjectId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName, DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsername(CATALOG_OBJECT_ID, DUMMY_USERNAME, BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).save(catalogObjectGrantEntity);
    }

    @Test
    public void testGetAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);

        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUsername(DUMMY_CURRENT_USERNAME, CATALOG_OBJECT_ID, BUCKET_ID)).thenReturn(dbUserObjectGrants);
        String DUMMY_GROUP = "dummyGroup";
        when(catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUserGroup(DUMMY_GROUP, CATALOG_OBJECT_ID, BUCKET_ID)).thenReturn(new LinkedList<>());

        List<CatalogObjectGrantMetaData> results = catalogObjectGrantService.getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(DUMMY_CURRENT_USERNAME, DUMMY_GROUP, DUMMY_OBJECT, DUMMY_BUCKET);

        assertEquals(1, results.size());
        assertEquals("user", results.get(0).getGrantee());
        assertEquals(DUMMY_USERNAME, results.get(0).getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, results.get(0).getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, results.get(0).getCreator());
        assertEquals(BUCKET_ID, results.get(0).getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, results.get(0).getCatalogObjectId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName, DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findAllCatalogObjectGrantsAssignedToAUsername(DUMMY_CURRENT_USERNAME, CATALOG_OBJECT_ID, BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).findAllCatalogObjectGrantsAssignedToAUserGroup(DUMMY_GROUP, CATALOG_OBJECT_ID, BUCKET_ID);
        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
    }

    @Test
    public void testGetAllCreatedCatalogObjectGrantsForThisBucket() {
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);

        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findAllCatalogObjectsGrantsCreatedByUsernameInASpecificBucket(DUMMY_CURRENT_USERNAME, BUCKET_ID)).thenReturn(dbUserObjectGrants);

        List<CatalogObjectGrantMetaData> results = catalogObjectGrantService.getAllCreatedCatalogObjectGrantsForThisBucket(DUMMY_CURRENT_USERNAME, DUMMY_BUCKET);

        assertEquals(1, results.size());
        assertEquals("user", results.get(0).getGrantee());
        assertEquals(DUMMY_USERNAME, results.get(0).getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, results.get(0).getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, results.get(0).getCreator());
        assertEquals(BUCKET_ID, results.get(0).getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, results.get(0).getCatalogObjectId());

        verify(catalogObjectGrantRepository, times(1)).findAllCatalogObjectsGrantsCreatedByUsernameInASpecificBucket(DUMMY_CURRENT_USERNAME, BUCKET_ID);
        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);

    }

    @Test
    public void testFindAllCatalogObjectGrantsAssignedToABucket() {
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);
        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToABucket(BUCKET_ID)).thenReturn(dbUserObjectGrants);

        List<CatalogObjectGrantMetaData> results = catalogObjectGrantService.findAllCatalogObjectGrantsAssignedToABucket(DUMMY_BUCKET);

        assertEquals(1, results.size());
        assertEquals("user", results.get(0).getGrantee());
        assertEquals(DUMMY_USERNAME, results.get(0).getProfiteer());
        assertEquals(DUMMY_ACCESS_TYPE, results.get(0).getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, results.get(0).getCreator());
        assertEquals(BUCKET_ID, results.get(0).getCatalogObjectBucketId());
        assertEquals(CATALOG_OBJECT_ID, results.get(0).getCatalogObjectId());

        verify(catalogObjectGrantRepository, times(1)).findAllCatalogObjectGrantsAssignedToABucket(BUCKET_ID);
        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
    }

    @Test
    public void testDeleteAllCatalogObjectsGrantsAssignedToABucket() {
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);
        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToABucket(BUCKET_ID)).thenReturn(dbUserObjectGrants);
        doNothing().when(catalogObjectGrantRepository).delete(any(CatalogObjectGrantEntity.class));

        catalogObjectGrantService.deleteAllCatalogObjectsGrantsAssignedToABucket(BUCKET_ID);

        verify(catalogObjectGrantRepository, times(1)).findAllCatalogObjectGrantsAssignedToABucket(BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).delete(dbUserObjectGrants);
    }

    @Test
    public void testDeleteAllCatalogObjectGrants() {
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user", DUMMY_CURRENT_USERNAME, DUMMY_USERNAME, DUMMY_ACCESS_TYPE, dummyCatalogObjectRevisionEntity, mockedBucket);
        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(catalogObjectGrantRepository.findAllGrantsByCatalogObjectIdInBucket(CATALOG_OBJECT_ID, BUCKET_ID)).thenReturn(dbUserObjectGrants);
        doNothing().when(catalogObjectGrantRepository).delete(any(CatalogObjectGrantEntity.class));

        catalogObjectGrantService.deleteAllCatalogObjectGrants(BUCKET_ID, CATALOG_OBJECT_ID);

        verify(catalogObjectGrantRepository, times(1)).findAllGrantsByCatalogObjectIdInBucket(CATALOG_OBJECT_ID, BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).delete(dbUserObjectGrants);
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevisionEntity() {
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = mock(CatalogObjectRevisionEntity.class);
        when(catalogObjectRevisionEntity.getId()).thenReturn(CATALOG_OBJECT_ID);
        return catalogObjectRevisionEntity;
    }

    private BucketEntity newMockedBucket() {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(BUCKET_ID);
        when(mockedBucket.getBucketName()).thenReturn(DUMMY_BUCKET);
        return mockedBucket;
    }
}