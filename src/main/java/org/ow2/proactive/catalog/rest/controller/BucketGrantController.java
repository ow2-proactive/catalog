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

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.List;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.service.BucketGrantService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@RestController
@RequestMapping(value = "/buckets/grant")
public class BucketGrantController {

    private final String ADMIN_ACCESS_TYPE = "admin";

    @Autowired
    private BucketGrantService bucketGrantService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all assigned grants for the user and his group")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketGrantMetadata> getAssignedBucketGrantsForUserAndHisGroup(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The current userGroup") @RequestParam(value = "userGroup", required = true) String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        return bucketGrantService.getAllAssignedGrantsForUserAndHisGroup(currentUser, userGroup);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update the access type of an existing bucket grant")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata updateBucketGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The user who is updating this grant") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "username", required = false) @RequestHeader(value = "username", required = false) String username,
            @ApiParam(value = "userGroup", required = false, defaultValue = "") @RequestHeader(value = "userGroup", required = false, defaultValue = "") String userGroup,
            @ApiParam(value = "accessType", required = true, defaultValue = "") @RequestHeader(value = "accessType", required = true, defaultValue = "") String accessType,
            @PathVariable String bucketName) throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        // Check Grants
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user,
                                                                                                          bucketName,
                                                                                                          ADMIN_ACCESS_TYPE)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        BucketGrantMetadata result = null;

        if (!username.equals("") && userGroup.equals("")) {
            // Case if the grant targets a specific user
            result = bucketGrantService.updateBucketGrantForASpecificUser(bucketName, username, accessType);
        } else if (username.equals("") && !userGroup.equals("")) {
            // Case if the grant targets a specific group
            result = bucketGrantService.updateBucketGrantForASpecificUserGroup(bucketName, userGroup, accessType);
        }
        return result;

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all created grants by a specific user")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{username}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketGrantMetadata> getCreatedBucketGrants(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String username) throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, username)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        return bucketGrantService.getAllGrantsCreatedByUsername(username);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new username grant or a user group grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketGrantMetadata createBucketGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the Bucket") @RequestParam(value = "bucketName", required = true) String bucketName,
            @ApiParam(value = "The user who is creating this grant") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The access type of the grant") @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that will have grant access", defaultValue = "") @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @ApiParam(value = "The name of the user group that will have grant access", defaultValue = "") @RequestParam(value = "userGroup", required = false, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        // Check if user is admin or has admin Grants over the bucket
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user,
                                                                                                          bucketName,
                                                                                                          ADMIN_ACCESS_TYPE)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        return bucketGrantService.createBucketGrant(bucketName, currentUser, accessType, username, userGroup);

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata deleteBucketGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the Bucket") @RequestParam(value = "bucketName", required = true) String bucketName,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The name of the user that have grant access over this bucket", defaultValue = "") @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @ApiParam(value = "The name of the user group that have grant access over this bucket", defaultValue = "") @RequestParam(value = "userGroup", required = false, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        // Check if user is admin or has admin Grants over the bucket
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user,
                                                                                                          bucketName,
                                                                                                          ADMIN_ACCESS_TYPE)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        return bucketGrantService.deleteBucketGrant(bucketName, username, userGroup);
    }

}
