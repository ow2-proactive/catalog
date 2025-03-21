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

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.service.*;


/**
 * @author ActiveEon Team
 */
public class BucketControllerTest {

    @InjectMocks
    private BucketController bucketController;

    @Mock
    private BucketService bucketService;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Mock
    private BucketGrantService bucketGrantService;

    @Mock
    GrantRightsService grantRightsService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(grantRightsService.getBucketRights(any(), anyString())).thenReturn("admin");
        when(grantRightsService.getBucketRights(any(), anyString())).thenReturn("admin");
        when(grantRightsService.getCatalogObjectRights(any(), anyString(), anyString())).thenReturn("admin");
    }

    @Test
    public void testCreate() throws Exception {
        final String bucketTestUser = "BucketControllerTestUser";
        bucketController.create("", "dummy", bucketTestUser);
        verify(bucketService, times(1)).createBucket("dummy", bucketTestUser, "");
    }

    @Test
    public void testGetMetadata() throws Exception {
        bucketController.getMetadata("", "bucket-name");
        verify(bucketService, times(1)).getBucketMetadata("bucket-name");
    }

    @Test
    public void testList() throws Exception {
        Optional kind = Optional.ofNullable("kind");
        Optional type = Optional.ofNullable("type");
        bucketController.list(null,
                              null,
                              null,
                              kind,
                              type,
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              Optional.ofNullable(null),
                              "false");
        verify(bucketService, times(1)).listBuckets((String) null,
                                                    null,
                                                    null,
                                                    kind,
                                                    type,
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    null,
                                                    false);
    }

    @Test
    public void testEmptyParamsGetBucketsList() throws Exception {
        Optional kind = Optional.ofNullable("");
        Optional type = Optional.ofNullable("");
        Optional name = Optional.ofNullable("");
        Optional bucket = Optional.ofNullable("");
        Optional tag = Optional.ofNullable("");
        Optional associationStatus = Optional.ofNullable("");
        Optional projectName = Optional.ofNullable("");
        Optional lastCommittedBy = Optional.ofNullable("");
        Optional committedAtLeastOnceBy = Optional.ofNullable("");
        Optional committedTimeGreater = Optional.ofNullable(0L);
        Optional committedTimeLessThan = Optional.ofNullable(0L);
        bucketController.list(null,
                              null,
                              null,
                              kind,
                              type,
                              name,
                              bucket,
                              tag,
                              associationStatus,
                              projectName,
                              lastCommittedBy,
                              committedAtLeastOnceBy,
                              committedTimeGreater,
                              committedTimeLessThan,
                              "false");
        verify(bucketService, times(1)).listBuckets((String) null,
                                                    null,
                                                    null,
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.empty(),
                                                    Optional.ofNullable(0L),
                                                    Optional.ofNullable(0L),
                                                    null,
                                                    false);
    }

    @Test
    public void testDelete() throws Exception {
        bucketService.deleteEmptyBucket("bucket-name");
        verify(bucketService, times(1)).deleteEmptyBucket("bucket-name");
    }
}
