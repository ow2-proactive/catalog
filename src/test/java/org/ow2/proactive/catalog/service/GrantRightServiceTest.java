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
import static org.mockito.Mockito.*;
import static org.ow2.proactive.catalog.util.AccessType.noAccess;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;


@RunWith(MockitoJUnitRunner.class)
public class GrantRightServiceTest {

    @InjectMocks
    GrantRightsService grantRightsService;

    @Mock
    CatalogObjectGrantService catalogObjectGrantService;

    @Mock
    BucketGrantService bucketGrantService;

    private final String bucketName = "test-bucket";

    @Test
    public void testRemoveAllObjectsWithNoAccessGrantWhenTheUserHasNotANoAccessGrantForTheCurrentBucket() {
        List<String> userGroups = new LinkedList<>();
        userGroups.add("user");
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder().name("user").groups(userGroups).build();
        when(catalogObjectGrantService.getUserNoAccessGrant(authenticatedUser)).thenReturn(new LinkedList<>());
        when(catalogObjectGrantService.getAccessibleObjectsGrantsAssignedToAUser(authenticatedUser)).thenReturn(new LinkedList<>());

        List<BucketGrantMetadata> bucketGrantMetadataList = new LinkedList<>();
        bucketGrantMetadataList.add(createBucketGrantMetadata("user", noAccess.toString(), "bucket"));
        bucketGrantMetadataList.add(createBucketGrantMetadata("user1", noAccess.toString(), "bucket"));

        when(bucketGrantService.getAllNoAccessGrants()).thenReturn(bucketGrantMetadataList);

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();

        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeAllObjectsWithNoAccessGrant(metadataList, new LinkedList<>(), new LinkedList<>());

        assertEquals(3, metadataList.size());
    }

    @Test
    public void testRemoveAllObjectsWithNoAccessGrantWhenTheUserHasANoAccessGrantForTheCurrentBucket() {
        List<String> userGroups = new LinkedList<>();
        userGroups.add("user");
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder().name("user").groups(userGroups).build();
        when(catalogObjectGrantService.getUserNoAccessGrant(authenticatedUser)).thenReturn(new LinkedList<>());
        when(catalogObjectGrantService.getAccessibleObjectsGrantsAssignedToAUser(authenticatedUser)).thenReturn(new LinkedList<>());

        List<BucketGrantMetadata> bucketGrantMetadataList = new LinkedList<>();
        bucketGrantMetadataList.add(createBucketGrantMetadata("user", noAccess.toString(), bucketName));

        when(bucketGrantService.getAllNoAccessGrants()).thenReturn(bucketGrantMetadataList);

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();

        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeAllObjectsWithNoAccessGrant(metadataList, bucketGrantMetadataList, new LinkedList<>());

        assertEquals(0, metadataList.size());
    }

    @Test
    public void testRemoveAllObjectsWhenTheUserHasANoAccessGrantForTheCurrentBucketButKeepObjectsWithPositiveGrants() {
        List<String> userGroups = new LinkedList<>();
        userGroups.add("user");
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder().name("user").groups(userGroups).build();
        when(catalogObjectGrantService.getUserNoAccessGrant(authenticatedUser)).thenReturn(new LinkedList<>());

        CatalogObjectGrantMetadata catalogObjectGrantMetadata = createObjectGrantMetadata(bucketName, "object1");
        List<CatalogObjectGrantMetadata> objectGrants = new LinkedList<>();
        objectGrants.add(catalogObjectGrantMetadata);

        when(catalogObjectGrantService.getAccessibleObjectsGrantsAssignedToAUser(authenticatedUser)).thenReturn(objectGrants);

        List<BucketGrantMetadata> bucketGrantMetadataList = new LinkedList<>();
        bucketGrantMetadataList.add(createBucketGrantMetadata("user", noAccess.toString(), bucketName));

        when(bucketGrantService.getAllNoAccessGrants()).thenReturn(bucketGrantMetadataList);

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();

        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeAllObjectsWithNoAccessGrant(metadataList, bucketGrantMetadataList, objectGrants);

        assertEquals(1, metadataList.size());
    }

    private BucketGrantMetadata createBucketGrantMetadata(String userName, String accessType, String bucketName) {
        return new BucketGrantMetadata(userName, "admin", "user", accessType, 0, 1L, bucketName);
    }

    private CatalogObjectMetadata createCatalogObject(String bucketName, String objectName) {
        return new CatalogObjectMetadata(bucketName, objectName, "", "", "", 0, "", "", new LinkedList<>(), "");
    }

    private CatalogObjectGrantMetadata createObjectGrantMetadata(String bucketName, String objectName) {
        return new CatalogObjectGrantMetadata("user", "admin", "user", "read", 0, null, objectName, 0, bucketName);
    }
}
