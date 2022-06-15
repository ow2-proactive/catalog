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
import static org.ow2.proactive.catalog.util.AccessType.admin;
import static org.ow2.proactive.catalog.util.AccessType.noAccess;
import static org.ow2.proactive.catalog.util.AccessType.read;
import static org.ow2.proactive.catalog.util.AccessType.write;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;


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
    public void testRemoveInaccessibleObjectsGivenUserHasAdminBucketGrant() {
        List<BucketGrantMetadata> bucketGrants = new LinkedList<>();
        bucketGrants.add(createBucketGrantMetadata("user", admin.toString(), bucketName));

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();
        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeInaccessibleObjectsInBucket(metadataList, bucketGrants, new LinkedList<>());

        assertEquals(3, metadataList.size());
    }

    @Test
    public void testRemoveInaccessibleObjectsGivenUserHasNoAccessBucketGrant() {
        List<BucketGrantMetadata> bucketGrants = new LinkedList<>();
        bucketGrants.add(createBucketGrantMetadata("user", noAccess.toString(), bucketName));

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();
        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeInaccessibleObjectsInBucket(metadataList, bucketGrants, new LinkedList<>());

        assertEquals(0, metadataList.size());
    }

    @Test
    public void testRemoveInaccessibleObjectsGivenUserHasNoAccessBucketGrantAndPositiveObjectGrants() {
        List<BucketGrantMetadata> bucketGrants = new LinkedList<>();
        bucketGrants.add(createBucketGrantMetadata("user", noAccess.toString(), bucketName));

        CatalogObjectGrantMetadata catalogObjectGrantMetadata = createObjectGrantMetadata(bucketName,
                                                                                          "object1",
                                                                                          read.name());
        List<CatalogObjectGrantMetadata> objectGrants = new LinkedList<>();
        objectGrants.add(catalogObjectGrantMetadata);

        List<CatalogObjectMetadata> metadataList = new LinkedList<>();
        metadataList.add(createCatalogObject(bucketName, "object1"));
        metadataList.add(createCatalogObject(bucketName, "object2"));
        metadataList.add(createCatalogObject(bucketName, "object3"));

        grantRightsService.removeInaccessibleObjectsInBucket(metadataList, bucketGrants, objectGrants);

        assertEquals(1, metadataList.size());
    }

    @Test
    public void testGetTheNumberOfAccessibleObjectsInTheBucketWithReadGrantBucketAndNoObjectGrants() {
        BucketMetadata bucketRead = new BucketMetadata(bucketName, "admin-group", 5);
        List<BucketGrantMetadata> bucketReadGrants = Collections.singletonList(createBucketGrantMetadata("user",
                                                                                                         read.toString(),
                                                                                                         bucketName));
        int numberOfObjectsInBucketRead = grantRightsService.getNumberOfAccessibleObjectsInBucket(bucketRead,
                                                                                                  bucketReadGrants,
                                                                                                  new ArrayList<>());
        assertEquals(5, numberOfObjectsInBucketRead);
    }

    @Test
    public void testGetTheNumberOfAccessibleObjectsInTheBucketWithWriteGrantBucketAndNoAccessObjects() {
        List<CatalogObjectGrantMetadata> objectGrants = new LinkedList<>();
        objectGrants.add(createObjectGrantMetadata(bucketName, "object1", noAccess.name()));
        objectGrants.add(createObjectGrantMetadata(bucketName, "object2", noAccess.name()));
        objectGrants.add(createObjectGrantMetadata(bucketName, "object3", read.name()));

        BucketMetadata bucketWrite = new BucketMetadata("test-write", "admin-group", 4);
        List<BucketGrantMetadata> bucketWriteGrants = Collections.singletonList(createBucketGrantMetadata("user",
                                                                                                          write.toString(),
                                                                                                          bucketWrite.getName()));
        int numberOfObjectsInBucketWrite = grantRightsService.getNumberOfAccessibleObjectsInBucket(bucketWrite,
                                                                                                   bucketWriteGrants,
                                                                                                   objectGrants);
        assertEquals(2, numberOfObjectsInBucketWrite);
    }

    @Test
    public void testGetTheNumberOfAccessibleObjectsInTheBucketWithNoAccessBucketAndObjectsGrants() {
        List<CatalogObjectGrantMetadata> objectGrants = new LinkedList<>();
        objectGrants.add(createObjectGrantMetadata(bucketName, "object1", read.name()));
        objectGrants.add(createObjectGrantMetadata(bucketName, "object2", write.name()));
        objectGrants.add(createObjectGrantMetadata(bucketName, "object3", noAccess.name()));

        BucketMetadata bucketNoAccess = new BucketMetadata("test-noaccess", "admin-group", 7);
        List<BucketGrantMetadata> bucketNoAccessGrants = Collections.singletonList(createBucketGrantMetadata("user",
                                                                                                             noAccess.toString(),
                                                                                                             bucketNoAccess.getName()));
        int numberOfObjectsInBucketNoAccess = grantRightsService.getNumberOfAccessibleObjectsInBucket(bucketNoAccess,
                                                                                                      bucketNoAccessGrants,
                                                                                                      objectGrants);
        assertEquals(2, numberOfObjectsInBucketNoAccess);
    }

    private BucketGrantMetadata createBucketGrantMetadata(String userName, String accessType, String bucketName) {
        return new BucketGrantMetadata(userName, "admin", "user", accessType, 0, 1L, bucketName);
    }

    private CatalogObjectMetadata createCatalogObject(String bucketName, String objectName) {
        return new CatalogObjectMetadata(bucketName, objectName, "", "", "", 0, "", "", new LinkedList<>(), "");
    }

    private CatalogObjectGrantMetadata createObjectGrantMetadata(String bucketName, String objectName,
            String accessType) {
        return new CatalogObjectGrantMetadata("user", "admin", "user", accessType, 0, null, objectName, 0, bucketName);
    }
}
