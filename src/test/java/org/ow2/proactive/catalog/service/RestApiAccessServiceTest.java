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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;


/**
 * @author ActiveEon Team
 * @since 31/07/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class RestApiAccessServiceTest {

    @InjectMocks
    private RestApiAccessService restApiAccessService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private SchedulerUserAuthenticationService schedulerUserAuthenticationService;

    @Mock
    private BucketService bucketService;

    @Test
    public void testSessionIdIsHandedToAuthenticationService() throws NotAuthenticatedException, AccessDeniedException {

        when(schedulerUserAuthenticationService.authenticateBySessionId(any())).thenReturn(AuthenticatedUser.EMPTY);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(true);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(true);

        BucketMetadata bucketMetadata = new BucketMetadata("name", BucketService.DEFAULT_BUCKET_OWNER);

        when(bucketService.getBucketMetadata(any())).thenReturn(bucketMetadata);

        restApiAccessService.getUserDataFromSessionidAndCheckAccess(true, "testSessionId", "test");

        verify(schedulerUserAuthenticationService).authenticateBySessionId("testSessionId");

    }

    @Test
    public void testSessionIdIsHandedToAuthenticationServiceWithOwnerNull()
            throws NotAuthenticatedException, AccessDeniedException {

        when(schedulerUserAuthenticationService.authenticateBySessionId(any())).thenReturn(AuthenticatedUser.EMPTY);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(true);

        BucketMetadata bucketMetadata = new BucketMetadata("name", BucketService.DEFAULT_BUCKET_OWNER);

        when(bucketService.getBucketMetadata(any())).thenReturn(bucketMetadata);

        restApiAccessService.getUserDataFromSessionidAndCheckAccess(true, "testSessionId", null);

        verify(schedulerUserAuthenticationService).authenticateBySessionId("testSessionId");

    }

    @Test
    public void testThatBucketServiceRequestsCorrectBucketId() throws NotAuthenticatedException, AccessDeniedException {

        when(schedulerUserAuthenticationService.authenticateBySessionId(any())).thenReturn(AuthenticatedUser.EMPTY);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(true);

        when(bucketService.getBucketMetadata("bucket-name")).thenReturn(new BucketMetadata("bucket-name", "owner"));

        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(true, "testSessionId", "bucket-name");

        verify(bucketService, times(2)).getBucketMetadata("bucket-name");

    }

    @Test
    public void testThatBucketOwnerAndAuthenticatedUserIsPassedToAuthenticationService()
            throws NotAuthenticatedException, AccessDeniedException {

        when(schedulerUserAuthenticationService.authenticateBySessionId(any())).thenReturn(AuthenticatedUser.EMPTY);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(true);

        when(bucketService.getBucketMetadata("bucket-name")).thenReturn(new BucketMetadata("bucket-name", "owner"));

        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(true, "testSessionId", "bucket-name");

        verify(authorizationService).askUserAuthorizationByBucketOwner(AuthenticatedUser.EMPTY, "owner");

    }

    @Test(expected = AccessDeniedException.class)
    public void testThatNotAuthorizedExceptionIsThrownIfAuthorizationServiceReturnsFalseBucketId()
            throws NotAuthenticatedException, AccessDeniedException {

        when(schedulerUserAuthenticationService.authenticateBySessionId(any())).thenReturn(AuthenticatedUser.EMPTY);

        when(authorizationService.askUserAuthorizationByBucketOwner(any(), any())).thenReturn(false);

        when(bucketService.getBucketMetadata("bucket-name")).thenReturn(new BucketMetadata("bucket-name", "owner"));

        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(true, "testSessionId", "bucket-name");

    }

}
