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

import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.service.model.RestApiAccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 27/07/2017
 */
@Component

public class RestApiAccessService {

    private final BucketService bucketService;

    private final AuthorizationService authorizationService;

    private final SchedulerUserAuthenticationService schedulerUserAuthenticationService;

    @Autowired
    public RestApiAccessService(BucketService bucketService, AuthorizationService authorizationService,
            SchedulerUserAuthenticationService schedulerUserAuthenticationService) {

        this.bucketService = bucketService;
        this.authorizationService = authorizationService;
        this.schedulerUserAuthenticationService = schedulerUserAuthenticationService;
    }

    public RestApiAccessResponse checkAccessBySessionIdAndThrowIfDeclined(String sessionId, long bucketId)
            throws NotAuthenticatedException, AccessDeniedException {

        RestApiAccessResponse restApiAccessResponse = this.checkAccessBySessionIdToOwnerOrGroup(sessionId, bucketId);
        if (!restApiAccessResponse.isAuthorized()) {

            throw new AccessDeniedException("SessionId: " + sessionId + " is not allowed to access buckets with id " +
                                            bucketId);
        }
        return restApiAccessResponse;
    }

    public RestApiAccessResponse checkAccessBySessionIdAndThrowIfDeclined(String sessionId, String ownerOrGroup)
            throws NotAuthenticatedException, AccessDeniedException {

        RestApiAccessResponse restApiAccessResponse = this.checkAccessBySessionIdToOwnerOrGroup(sessionId,
                                                                                                ownerOrGroup);
        if (!restApiAccessResponse.isAuthorized()) {
            throw new AccessDeniedException("SessionId: " + sessionId +
                                            " is not allowed to access buckets with owner or group " + ownerOrGroup);
        }
        return restApiAccessResponse;
    }

    private RestApiAccessResponse checkAccessBySessionIdToOwnerOrGroup(String sessionId, long bucketId)
            throws NotAuthenticatedException {

        return checkAccessBySessionIdToOwnerOrGroup(sessionId, bucketService.getBucketMetadata(bucketId).getOwner());

    }

    private RestApiAccessResponse checkAccessBySessionIdToOwnerOrGroup(String sessionId, String ownerOrGroup)
            throws NotAuthenticatedException {

        if (authorizationService.isPublicAccess(ownerOrGroup)) {

            return RestApiAccessResponse.builder().authorized(true).build();
        } else {

            AuthenticatedUser authenticatedUser = schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
            boolean authorized = authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser,
                                                                                        ownerOrGroup);
            return RestApiAccessResponse.builder().authorized(authorized).authenticatedUser(authenticatedUser).build();
        }
    }
}
