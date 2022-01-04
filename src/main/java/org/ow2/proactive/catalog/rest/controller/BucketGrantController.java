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

import static org.ow2.proactive.catalog.util.AccessType.admin;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.*;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.service.BucketGrantService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AllBucketGrants;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping(value = "/buckets")
public class BucketGrantController {

    @Autowired
    private BucketGrantService bucketGrantService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update the access type of an existing user bucket grant")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/user", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata updateBucketGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the user that is benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username,
            @ApiParam(value = "The new type of the access grant. It can be either read, write or admin.", required = true, defaultValue = "") @RequestParam(value = "accessType", required = true, defaultValue = "") String accessType)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.updateBucketGrantForASpecificUser(bucketName, username, accessType);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update the access type of an existing group bucket grant")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/group", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata updateBucketGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the group of users that are benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup,
            @ApiParam(value = "The new type of the access grant. It can be either read, write or admin.", required = true, defaultValue = "") @RequestParam(value = "accessType", required = true, defaultValue = "") String accessType)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.updateBucketGrantForASpecificUserGroup(bucketName, userGroup, accessType);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a user grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/user", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata deleteBucketGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName,
            @ApiParam(value = "The name of the user that is benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.deleteBucketGrantForAUser(bucketName, username);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a group grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/group", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public BucketGrantMetadata deleteBucketGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName,
            @ApiParam(value = "The name of the group of users that are benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.deleteBucketGrantForAGroup(bucketName, userGroup);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all grants associated with a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketGrantMetadata> getAllGrantsForABucket(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName) {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.getAllCreatedBucketGrantsForABucket(bucketName);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new user grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/user", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketGrantMetadata createBucketGrantForAUser(
            @ApiParam(value = "The the session id used to access ProActive REST server", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName,
            @ApiParam(value = "The type of the access grant. It can be either read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user = null;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        if (user == null) {
            user = AuthenticatedUser.EMPTY;
        }
        return bucketGrantService.createBucketGrantForAUSer(bucketName, user.getName(), accessType, username);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new user group grant access for a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/group", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketGrantMetadata createBucketGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName,
            @ApiParam(value = "The type of the access grant. It can be either read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the group of users that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user = null;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
        }
        if (user == null) {
            user = AuthenticatedUser.EMPTY;
        }
        return bucketGrantService.createBucketGrantForAGroup(bucketName, user.getName(), accessType, userGroup);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete all grants assigned to a bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketGrantMetadata> deleteAllBucketGrantsAssignedToABucket(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.deleteAllGrantsAssignedToABucket(bucketName);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all grants associated with a bucket and all objects contained in this bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/all", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public AllBucketGrants getAllGrantsForABucketAndItsObjects(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName) {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.getAllBucketAndObjectGrants(bucketName);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete all grants associated with a bucket and all objects contained in this bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}/grant/all", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public AllBucketGrants deleteAllGrantsForABucketAndItsObjects(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable(value = "bucketName") String bucketName) {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new DataIntegrityViolationException("Bucket: " + bucketName +
                                                          " is public. You can not assign a grant to it");
            }
            // Check if user is admin or has admin Grants over the bucket
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!bucketGrantService.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return bucketGrantService.deleteAllBucketAndItsObjectsGrants(bucketName);
    }
}
