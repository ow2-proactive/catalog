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
package org.ow2.proactive.catalog.mocks;

import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.service.model.RestApiAccessResponse;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;

import com.google.common.collect.Lists;


public class RestApiAccessServiceMock extends RestApiAccessService {

    public RestApiAccessServiceMock() {
        super(null, null, null);
    }

    public RestApiAccessResponse getUserDataFromSessionidAndCheckAccess(String sessionId, String bucketName)
            throws NotAuthenticatedException, AccessDeniedException {

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("username")
                                                               .groups(Lists.newArrayList())
                                                               .build();
        return RestApiAccessResponse.builder().authorized(true).authenticatedUser(authenticatedUser).build();

    }

    public RestApiAccessResponse checkAccessBySessionIdForOwnerOrGroupOrTenantAndThrowIfDeclined(String sessionId,
            String ownerOrGroup, String tenant) throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("username")
                                                               .groups(Lists.newArrayList())
                                                               .build();
        return RestApiAccessResponse.builder().authorized(true).authenticatedUser(authenticatedUser).build();
    }

    public void checkAccessBySessionIdForBucketAndThrowIfDeclined(boolean sessionIdRequired, String sessionId,
            String bucketName) throws NotAuthenticatedException, AccessDeniedException {

    }

    public boolean isAPublicBucket(String bucketName) {
        return true;
    }

    public boolean isSessionActive(String sessionId) {
        return true;
    }

    public boolean isBucketAccessibleByUser(boolean sessionIdRequired, String sessionId, String bucketName) {
        return true;
    }

    public AuthenticatedUser getUserFromSessionId(String sessionId) {
        return AuthenticatedUser.builder().name("username").groups(Lists.newArrayList()).build();
    }

}
