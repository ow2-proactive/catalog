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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.service.BucketGrantService;
import org.ow2.proactive.catalog.service.CatalogObjectGrantService;
import org.ow2.proactive.catalog.service.RestApiAccessService;


public class CatalogObjectGrantControllerTest {

    @InjectMocks
    CatalogObjectGrantController catalogObjectGrantController;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Mock
    private BucketGrantService bucketGrantService;

    @Mock
    private CatalogObjectGrantService catalogObjectGrantService;

    private final String DUMMY_SESSION_ID = "12345";

    private final String DUMMY_BUCKET_NAME = "dummy-bucket";

    private final String DUMMY_CURRENT_USER = "dummyAdmin";

    private final String DUMMY_USER = "dummyUser";

    private final String DUMMY_GROUP = "dummyGroup";

    private final String DUMMY_OBJECT = "dummyObject";

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
    public void testCreateCatalogObjectGrant() {
        String DUMMY_ACCESS_TYPE = "admin";
        catalogObjectGrantController.createCatalogObjectGrant(DUMMY_SESSION_ID,
                                                              DUMMY_BUCKET_NAME,
                                                              DUMMY_OBJECT,
                                                              DUMMY_CURRENT_USER,
                                                              DUMMY_ACCESS_TYPE,
                                                              DUMMY_USER,
                                                              "");
        verify(catalogObjectGrantService, times(1)).createCatalogObjectGrant(DUMMY_BUCKET_NAME,
                                                                             DUMMY_OBJECT,
                                                                             DUMMY_CURRENT_USER,
                                                                             DUMMY_ACCESS_TYPE,
                                                                             DUMMY_USER,
                                                                             "");
        catalogObjectGrantController.createCatalogObjectGrant(DUMMY_SESSION_ID,
                                                              DUMMY_BUCKET_NAME,
                                                              DUMMY_OBJECT,
                                                              DUMMY_CURRENT_USER,
                                                              DUMMY_ACCESS_TYPE,
                                                              "",
                                                              DUMMY_GROUP);
        verify(catalogObjectGrantService, times(1)).createCatalogObjectGrant(DUMMY_BUCKET_NAME,
                                                                             DUMMY_OBJECT,
                                                                             DUMMY_CURRENT_USER,
                                                                             DUMMY_ACCESS_TYPE,
                                                                             "",
                                                                             DUMMY_GROUP);
    }

    @Test
    public void testDeleteCatalogObjectGrant() {
        catalogObjectGrantController.deleteCatalogObjectGrant(DUMMY_SESSION_ID,
                                                              DUMMY_BUCKET_NAME,
                                                              DUMMY_OBJECT,
                                                              DUMMY_CURRENT_USER,
                                                              DUMMY_USER,
                                                              "");
        verify(catalogObjectGrantService, times(1)).deleteCatalogObjectGrant(DUMMY_BUCKET_NAME,
                                                                             DUMMY_OBJECT,
                                                                             DUMMY_USER,
                                                                             "");
        catalogObjectGrantController.deleteCatalogObjectGrant(DUMMY_SESSION_ID,
                                                              DUMMY_BUCKET_NAME,
                                                              DUMMY_OBJECT,
                                                              DUMMY_CURRENT_USER,
                                                              "",
                                                              DUMMY_GROUP);
        verify(catalogObjectGrantService, times(1)).deleteCatalogObjectGrant(DUMMY_BUCKET_NAME,
                                                                             DUMMY_OBJECT,
                                                                             "",
                                                                             DUMMY_GROUP);
    }

    @Test
    public void testGetAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup() {
        catalogObjectGrantController.getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(DUMMY_SESSION_ID,
                                                                                                   DUMMY_BUCKET_NAME,
                                                                                                   DUMMY_OBJECT,
                                                                                                   DUMMY_CURRENT_USER,
                                                                                                   DUMMY_GROUP);
        verify(catalogObjectGrantService,
               times(1)).getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(DUMMY_CURRENT_USER,
                                                                                       DUMMY_GROUP,
                                                                                       DUMMY_OBJECT,
                                                                                       DUMMY_BUCKET_NAME);
    }

    @Test
    public void testGetAllCreatedCatalogObjectGrantsByTheCurrentUSerForTheCurrentUserBucket() {
        catalogObjectGrantController.getAllCreatedCatalogObjectGrantsByTheCurrentUSerForTheCurrentUserBucket(DUMMY_SESSION_ID,
                                                                                                             DUMMY_BUCKET_NAME,
                                                                                                             DUMMY_CURRENT_USER);
        verify(catalogObjectGrantService, times(1)).getAllCreatedCatalogObjectGrantsForThisBucket(DUMMY_CURRENT_USER,
                                                                                                  DUMMY_BUCKET_NAME);
    }
}
