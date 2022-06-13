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
import org.ow2.proactive.catalog.service.GrantRightsService;
import org.ow2.proactive.catalog.service.RestApiAccessService;


public class CatalogObjectGrantControllerTest {

    @InjectMocks
    CatalogObjectGrantController catalogObjectGrantController;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Mock
    private GrantRightsService grantRightsService;

    @Mock
    private BucketGrantService bucketGrantService;

    @Mock
    private CatalogObjectGrantService catalogObjectGrantService;

    private final String DUMMY_SESSION_ID = "12345";

    private final String DUMMY_BUCKET_NAME = "dummy-bucket";

    private final String DUMMY_USER = "dummyUser";

    private final String DUMMY_GROUP = "dummyGroup";

    private final String DUMMY_OBJECT = "dummyObject";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(grantRightsService.getBucketRights(any(), anyString())).thenReturn("admin");
        when(grantRightsService.getBucketRights(any(), anyString())).thenReturn("admin");
        when(grantRightsService.getCatalogObjectRights(any(), anyString(), anyString())).thenReturn("admin");
        when(catalogObjectGrantService.checkInCatalogGrantsIfUserOrUserGroupHasGrantsOverABucket(any(),
                                                                                                 any())).thenReturn(true);
    }

    @Test
    public void testCreateCatalogObjectGrant() {
        String DUMMY_ACCESS_TYPE = "admin";
        catalogObjectGrantController.createCatalogObjectGrantForAUser(DUMMY_SESSION_ID,
                                                                      DUMMY_BUCKET_NAME,
                                                                      DUMMY_OBJECT,
                                                                      DUMMY_ACCESS_TYPE,
                                                                      DUMMY_USER);
        String DUMMY_CURRENT_USER = "";
        verify(catalogObjectGrantService, times(1)).createCatalogObjectGrantForAUser(DUMMY_BUCKET_NAME,
                                                                                     DUMMY_OBJECT,
                                                                                     DUMMY_CURRENT_USER,
                                                                                     DUMMY_ACCESS_TYPE,
                                                                                     DUMMY_USER);
        catalogObjectGrantController.createCatalogObjectGrantForAGroup(DUMMY_SESSION_ID,
                                                                       DUMMY_BUCKET_NAME,
                                                                       DUMMY_OBJECT,
                                                                       DUMMY_ACCESS_TYPE,
                                                                       1,
                                                                       DUMMY_GROUP);
        verify(catalogObjectGrantService, times(1)).createCatalogObjectGrantForAGroup(DUMMY_BUCKET_NAME,
                                                                                      DUMMY_OBJECT,
                                                                                      DUMMY_CURRENT_USER,
                                                                                      DUMMY_ACCESS_TYPE,
                                                                                      1,
                                                                                      DUMMY_GROUP);
    }

    @Test
    public void testDeleteCatalogObjectGrant() {
        catalogObjectGrantController.deleteCatalogObjectGrantForAUser(DUMMY_SESSION_ID,
                                                                      DUMMY_BUCKET_NAME,
                                                                      DUMMY_OBJECT,
                                                                      DUMMY_USER);
        verify(catalogObjectGrantService, times(1)).deleteCatalogObjectGrantForAUser(DUMMY_BUCKET_NAME,
                                                                                     DUMMY_OBJECT,
                                                                                     DUMMY_USER);
        catalogObjectGrantController.deleteCatalogObjectGrantForAGroup(DUMMY_SESSION_ID,
                                                                       DUMMY_BUCKET_NAME,
                                                                       DUMMY_OBJECT,
                                                                       DUMMY_GROUP);
        verify(catalogObjectGrantService, times(1)).deleteCatalogObjectGrantForAGroup(DUMMY_BUCKET_NAME,
                                                                                      DUMMY_OBJECT,
                                                                                      DUMMY_GROUP);
    }
}
