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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectGrantRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.*;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;


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

    private final String DUMMY_ACCESS_TYPE = "read";

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
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         DUMMY_CURRENT_USERNAME,
                                                                                         DUMMY_USERNAME,
                                                                                         DUMMY_ACCESS_TYPE,
                                                                                         null);
        CatalogObjectGrantEntity dbCatalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                           "admin",
                                                                                           "user",
                                                                                           "admin",
                                                                                           newCatalogObject());

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                    DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(anyString(),
                                                                                    anyString(),
                                                                                    anyLong())).thenReturn(dbCatalogObjectGrantEntity);
        when(catalogObjectGrantRepository.save(any(CatalogObjectGrantEntity.class))).thenReturn(catalogObjectGrantEntity);

        CatalogObjectGrantMetadata result = catalogObjectGrantService.createCatalogObjectGrantForAUser(DUMMY_BUCKET,
                                                                                                       DUMMY_OBJECT,
                                                                                                       DUMMY_CURRENT_USERNAME,
                                                                                                       DUMMY_ACCESS_TYPE,
                                                                                                       DUMMY_USERNAME);

        assertEquals(USER_GRANTEE_TYPE, result.getGranteeType());
        assertEquals(DUMMY_USERNAME, result.getGrantee());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                                 DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsernameForUpdate(DUMMY_OBJECT,
                                                                                                 DUMMY_USERNAME,
                                                                                                 BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).save((CatalogObjectGrantEntity) anyObject());
    }

    @Test
    public void testDeleteCatalogObjectGrant() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         DUMMY_CURRENT_USERNAME,
                                                                                         DUMMY_USERNAME,
                                                                                         DUMMY_ACCESS_TYPE,
                                                                                         null);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                    DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(anyString(),
                                                                                    anyString(),
                                                                                    anyLong())).thenReturn(catalogObjectGrantEntity);
        doNothing().when(catalogObjectGrantRepository).delete(any(CatalogObjectGrantEntity.class));

        CatalogObjectGrantMetadata result = catalogObjectGrantService.deleteCatalogObjectGrantForAUser(DUMMY_BUCKET,
                                                                                                       DUMMY_OBJECT,
                                                                                                       DUMMY_USERNAME);

        assertEquals(USER_GRANTEE_TYPE, result.getGranteeType());
        assertEquals(DUMMY_USERNAME, result.getGrantee());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                                 DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsernameForUpdate(DUMMY_OBJECT,
                                                                                                 DUMMY_USERNAME,
                                                                                                 BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).delete(catalogObjectGrantEntity);
    }

    @Test
    public void testUpdateCatalogObjectGrant() {
        List<String> dummyBucketsName = new LinkedList<>();
        dummyBucketsName.add(DUMMY_BUCKET);
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectRevisionEntity dummyCatalogObjectRevisionEntity = newCatalogObjectRevisionEntity();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         DUMMY_CURRENT_USERNAME,
                                                                                         DUMMY_USERNAME,
                                                                                         DUMMY_ACCESS_TYPE,
                                                                                         null);

        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                    DUMMY_OBJECT)).thenReturn(dummyCatalogObjectRevisionEntity);
        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(anyString(),
                                                                                    anyString(),
                                                                                    anyLong())).thenReturn(catalogObjectGrantEntity);
        when(catalogObjectGrantRepository.save(any(CatalogObjectGrantEntity.class))).thenReturn(catalogObjectGrantEntity);
        CatalogObjectGrantMetadata result = catalogObjectGrantService.updateCatalogObjectGrantForAUser(AuthenticatedUser.EMPTY,
                                                                                                       DUMMY_USERNAME,
                                                                                                       DUMMY_OBJECT,
                                                                                                       DUMMY_BUCKET,
                                                                                                       DUMMY_ACCESS_TYPE);

        assertEquals(USER_GRANTEE_TYPE, result.getGranteeType());
        assertEquals(DUMMY_USERNAME, result.getGrantee());
        assertEquals(DUMMY_ACCESS_TYPE, result.getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, result.getCreator());
        assertEquals(BUCKET_ID, result.getCatalogObjectBucketId());

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(dummyBucketsName,
                                                                                                 DUMMY_OBJECT);
        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantByUsernameForUpdate(DUMMY_OBJECT,
                                                                                                 DUMMY_USERNAME,
                                                                                                 BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).save(catalogObjectGrantEntity);
    }

    @Test
    public void testFindAllCatalogObjectGrantsAssignedToABucket() {
        BucketEntity mockedBucket = newMockedBucket();
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         DUMMY_CURRENT_USERNAME,
                                                                                         DUMMY_USERNAME,
                                                                                         DUMMY_ACCESS_TYPE,
                                                                                         null);
        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(bucketRepository.findOneByBucketName(DUMMY_BUCKET)).thenReturn(mockedBucket);
        when(catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(BUCKET_ID)).thenReturn(dbUserObjectGrants);

        List<CatalogObjectGrantMetadata> results = catalogObjectGrantService.getObjectsGrantsInABucket(DUMMY_BUCKET);

        assertEquals(1, results.size());
        assertEquals(USER_GRANTEE_TYPE, results.get(0).getGranteeType());
        assertEquals(DUMMY_USERNAME, results.get(0).getGrantee());
        assertEquals(DUMMY_ACCESS_TYPE, results.get(0).getAccessType());
        assertEquals(DUMMY_CURRENT_USERNAME, results.get(0).getCreator());
        assertEquals(BUCKET_ID, results.get(0).getCatalogObjectBucketId());

        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantEntitiesByBucketEntityId(BUCKET_ID);
        verify(bucketRepository, times(1)).findOneByBucketName(DUMMY_BUCKET);
    }

    @Test
    public void testDeleteAllCatalogObjectsGrantsAssignedToABucket() {
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         DUMMY_CURRENT_USERNAME,
                                                                                         DUMMY_USERNAME,
                                                                                         DUMMY_ACCESS_TYPE,
                                                                                         null);
        List<CatalogObjectGrantEntity> dbUserObjectGrants = new LinkedList<>();
        dbUserObjectGrants.add(catalogObjectGrantEntity);

        when(catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(BUCKET_ID)).thenReturn(dbUserObjectGrants);
        doNothing().when(catalogObjectGrantRepository).delete(any(CatalogObjectGrantEntity.class));

        catalogObjectGrantService.deleteAllCatalogObjectsGrantsAssignedToABucket(BUCKET_ID);

        verify(catalogObjectGrantRepository, times(1)).findCatalogObjectGrantEntitiesByBucketEntityId(BUCKET_ID);
        verify(catalogObjectGrantRepository, times(1)).delete(dbUserObjectGrants);
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevisionEntity() {
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = mock(CatalogObjectRevisionEntity.class);
        when(catalogObjectRevisionEntity.getId()).thenReturn(CATALOG_OBJECT_ID);
        return catalogObjectRevisionEntity;
    }

    private CatalogObjectEntity newCatalogObject() {
        return CatalogObjectEntity.builder()
                                  .id(new CatalogObjectEntity.CatalogObjectEntityKey(1L, DUMMY_OBJECT))
                                  .bucket(newMockedBucket())
                                  .build();
    }

    private BucketEntity newMockedBucket() {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(BUCKET_ID);
        when(mockedBucket.getBucketName()).thenReturn(DUMMY_BUCKET);
        return mockedBucket;
    }
}
