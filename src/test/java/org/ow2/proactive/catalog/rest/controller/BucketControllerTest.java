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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.RestApiAccessService;


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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        final String bucketTestUser = "BucketControllerTestUser";
        bucketController.create("", "dummy", bucketTestUser);
        verify(bucketService, times(1)).createBucket("dummy", bucketTestUser);
    }

    @Test
    public void testGetMetadata() throws Exception {
        bucketController.getMetadata("", "bucket-name");
        verify(bucketService, times(1)).getBucketMetadata("bucket-name");
    }

    @Test
    public void testList() throws Exception {
        bucketController.list(null, null, null, null);
        verify(bucketService, times(1)).listBuckets((String) null, null, null);
    }

    @Test
    public void testDelete() throws Exception {
        bucketService.deleteEmptyBucket("bucket-name");
        verify(bucketService, times(1)).deleteEmptyBucket("bucket-name");
    }
}
