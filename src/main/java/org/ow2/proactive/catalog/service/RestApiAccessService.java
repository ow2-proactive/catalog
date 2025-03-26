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
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.service.model.RestApiAccessResponse;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 27/07/2017
 */
@Component
@Lazy
public class RestApiAccessService {

    private final BucketService bucketService;

    private final AuthorizationService authorizationService;

    private final SchedulerUserAuthenticationService schedulerUserAuthenticationService;

    @Value("${pa.catalog.tenant.filtering}")
    private boolean isTenantFiltering;

    @Autowired
    public RestApiAccessService(BucketService bucketService, AuthorizationService authorizationService,
            SchedulerUserAuthenticationService schedulerUserAuthenticationService) {
        this.bucketService = bucketService;
        this.authorizationService = authorizationService;
        this.schedulerUserAuthenticationService = schedulerUserAuthenticationService;
    }

    public RestApiAccessResponse getUserDataFromSessionidAndCheckAccess(boolean sessionIdRequired, String sessionId,
            String bucketName) {
        if (!isAPublicBucket(bucketName) && sessionIdRequired) {
            return checkBucketPermission(sessionId, bucketName);
        } else {
            return RestApiAccessResponse.builder()
                                        .authorized(true)
                                        .authenticatedUser(getAuthenticatedUser(sessionIdRequired, sessionId))
                                        .build();

        }

    }

    public void checkAccessBySessionIdForBucketAndThrowIfDeclined(boolean sessionIdRequired, String sessionId,
            String bucketName) {
        if (!isAPublicBucket(bucketName) && sessionIdRequired) {
            checkBucketPermission(sessionId, bucketName);

        }

    }

    public boolean isAPublicBucket(String bucketName) {
        return BucketService.DEFAULT_BUCKET_OWNER.equals(bucketService.getBucketMetadata(bucketName).getOwner());
    }

    public RestApiAccessResponse checkAccessBySessionIdForOwnerOrGroupOrTenantAndThrowIfDeclined(String sessionId,
            String ownerOrGroup, String tenant) throws NotAuthenticatedException, AccessDeniedException {
        RestApiAccessResponse restApiAccessResponse = this.checkAccessBySessionIdToOwnerOrGroupOrTenant(sessionId,
                                                                                                        ownerOrGroup,
                                                                                                        tenant);
        if (!restApiAccessResponse.isAuthorized()) {
            String message = isTenantFiltering ? "SessionId: " + sessionId +
                                                 " is not allowed to access buckets with owner or group " +
                                                 ownerOrGroup + " and tenant " + tenant
                                               : "SessionId: " + sessionId +
                                                 " is not allowed to access buckets with owner or group " +
                                                 ownerOrGroup;
            throw new AccessDeniedException(message);
        }
        return restApiAccessResponse;
    }

    public AuthenticatedUser getUserFromSessionId(String sessionId) {
        return schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
    }

    public boolean isUserSessionActive(String sessionId, String username) {
        AuthenticatedUser authenticatedUser = schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
        if (authenticatedUser.getName().equals(username)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSessionActive(String sessionId) {
        AuthenticatedUser authenticatedUser = schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
        if (authenticatedUser != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBucketAccessibleByUser(boolean sessionIdRequired, String sessionId, String bucketName) {
        if (!isAPublicBucket(bucketName) && sessionIdRequired) {
            return this.checkAccessBySessionForBucketToOwnerOrGroup(sessionId, bucketName).isAuthorized();
        }
        return true;
    }

    private RestApiAccessResponse checkBucketPermission(String sessionId, String bucketName) {
        RestApiAccessResponse restApiAccessResponse = this.checkAccessBySessionForBucketToOwnerOrGroup(sessionId,
                                                                                                       bucketName);
        if (!restApiAccessResponse.isAuthorized()) {
            throw new AccessDeniedException("SessionId: " + sessionId + " is not allowed to access buckets with id " +
                                            bucketName);
        }

        return restApiAccessResponse;
    }

    private RestApiAccessResponse checkAccessBySessionForBucketToOwnerOrGroup(String sessionId, String bucketName)
            throws NotAuthenticatedException {
        return checkAccessBySessionIdToOwnerOrGroupOrTenant(sessionId,
                                                            bucketService.getBucketMetadata(bucketName).getOwner(),
                                                            null);
    }

    private RestApiAccessResponse checkAccessBySessionIdToOwnerOrGroupOrTenant(String sessionId, String ownerOrGroup,
            String tenant) throws NotAuthenticatedException {
        AuthenticatedUser authenticatedUser = schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
        boolean ownerOrGroupAuthorized = authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser,
                                                                                                ownerOrGroup);
        boolean isAuthorized = ownerOrGroupAuthorized;
        if (isTenantFiltering) {
            boolean tenantAuthorized = authorizationService.askUserAuthorizationByTenant(authenticatedUser, tenant);
            isAuthorized = ownerOrGroupAuthorized && tenantAuthorized;
        }
        return RestApiAccessResponse.builder().authorized(isAuthorized).authenticatedUser(authenticatedUser).build();
    }

    private AuthenticatedUser getAuthenticatedUser(boolean sessionIdRequired, String sessionId) {
        try {
            return schedulerUserAuthenticationService.authenticateBySessionId(sessionId);
        } catch (NotAuthenticatedException nae) {
            if (sessionIdRequired) {
                throw nae;
            }
        }
        return AuthenticatedUser.EMPTY;

    }

}
