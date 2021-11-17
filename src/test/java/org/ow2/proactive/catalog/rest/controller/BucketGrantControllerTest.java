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
package org.ow2.proactive.catalog.rest.controller;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.service.BucketGrantService;
import org.ow2.proactive.catalog.service.CatalogObjectGrantService;
import org.ow2.proactive.catalog.service.RestApiAccessService;


public class BucketGrantControllerTest {

    @InjectMocks
    BucketGrantController bucketGrantController;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Mock
    private BucketGrantService bucketGrantService;

    @Mock
    private CatalogObjectGrantService catalogObjectGrantService;

    private final String DUMMY_SESSION_ID = "12345";

    private final String DUMMY_BUCKET_NAME = "dummy-bucket";

    private final String DUMMY_CURRENT_USER = "dummyAdmin";

    private final String DUMMY_ACCESS_TYPE = "admin";

    private final String DUMMY_USER = "dummyUser";

    private final String DUMMY_GROUP = "dummyGroup";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(any(), any(), any())).thenReturn(true);
        when(catalogObjectGrantService.checkInCatalogObjectGrantsIfTheUserOrUserGroupHasAdminRightsOverTheCatalogObject(any(),
                                                                                                                        any(),
                                                                                                                        any())).thenReturn(true);
        when(catalogObjectGrantService.checkInCatalogGrantsIfUserOrUserGroupHasGrantsOverABucket(any(),
                                                                                                 any())).thenReturn(true);
    }

    @Test
    public void testGetAssignedBucketGrantsForUserAndHisGroup() {
        bucketGrantController.getAssignedBucketGrantsForUserAndHisGroup(DUMMY_SESSION_ID,
                                                                        DUMMY_CURRENT_USER,
                                                                        DUMMY_GROUP);
        verify(bucketGrantService, times(1)).getAllAssignedGrantsForUserAndHisGroup(DUMMY_CURRENT_USER, DUMMY_GROUP);
    }

    @Test
    public void testUpdateBucketGrant() {
        bucketGrantController.updateBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_CURRENT_USER,
                                                DUMMY_USER,
                                                "",
                                                DUMMY_ACCESS_TYPE,
                                                DUMMY_BUCKET_NAME);
        verify(bucketGrantService, times(1)).updateBucketGrantForASpecificUser(DUMMY_BUCKET_NAME,
                                                                               DUMMY_USER,
                                                                               DUMMY_ACCESS_TYPE);
        bucketGrantController.updateBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_CURRENT_USER,
                                                "",
                                                DUMMY_GROUP,
                                                DUMMY_ACCESS_TYPE,
                                                DUMMY_BUCKET_NAME);
        verify(bucketGrantService, times(1)).updateBucketGrantForASpecificUserGroup(DUMMY_BUCKET_NAME,
                                                                                    DUMMY_GROUP,
                                                                                    DUMMY_ACCESS_TYPE);
    }

    @Test
    public void testGetCreatedBucketGrants() {
        bucketGrantController.getCreatedBucketGrants(DUMMY_SESSION_ID, DUMMY_CURRENT_USER);
        verify(bucketGrantService, times(1)).getAllGrantsCreatedByUsername(DUMMY_CURRENT_USER);
    }

    @Test
    public void testCreateBucketGrant() {
        bucketGrantController.createBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_BUCKET_NAME,
                                                DUMMY_CURRENT_USER,
                                                DUMMY_ACCESS_TYPE,
                                                DUMMY_USER,
                                                "");
        verify(bucketGrantService, times(1)).createBucketGrant(DUMMY_BUCKET_NAME,
                                                               DUMMY_CURRENT_USER,
                                                               DUMMY_ACCESS_TYPE,
                                                               DUMMY_USER,
                                                               "");
        bucketGrantController.createBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_BUCKET_NAME,
                                                DUMMY_CURRENT_USER,
                                                DUMMY_ACCESS_TYPE,
                                                "",
                                                DUMMY_GROUP);
        verify(bucketGrantService, times(1)).createBucketGrant(DUMMY_BUCKET_NAME,
                                                               DUMMY_CURRENT_USER,
                                                               DUMMY_ACCESS_TYPE,
                                                               "",
                                                               DUMMY_GROUP);
    }

    @Test
    public void testDeleteBucketGrant() {
        bucketGrantController.deleteBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_BUCKET_NAME,
                                                DUMMY_CURRENT_USER,
                                                DUMMY_USER,
                                                "");
        verify(bucketGrantService, times(1)).deleteBucketGrant(DUMMY_BUCKET_NAME, DUMMY_USER, "");
        bucketGrantController.deleteBucketGrant(DUMMY_SESSION_ID,
                                                DUMMY_BUCKET_NAME,
                                                DUMMY_CURRENT_USER,
                                                "",
                                                DUMMY_GROUP);
        verify(bucketGrantService, times(1)).deleteBucketGrant(DUMMY_BUCKET_NAME, "", DUMMY_GROUP);
    }
}
