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

import java.util.List;

import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.service.CatalogObjectGrantService;
import org.ow2.proactive.catalog.service.GrantRightsService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.exception.PublicBucketGrantAccessException;
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


@Log4j2
@RestController
@RequestMapping(value = "/buckets/{bucketName}/")
public class CatalogObjectGrantController {

    private static final String REQUEST_API_QUERY = "/resources/{catalogObjectName}/grant";

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private GrantRightsService grantRightsService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new user grant for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/user", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. You can not assign a grant to it or to any of its object");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        return catalogObjectGrantService.createCatalogObjectGrantForAUser(bucketName,
                                                                          catalogObjectName,
                                                                          user.getName(),
                                                                          accessType,
                                                                          username);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new group grant for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The priority of the access grant. It can be a value from 1 to 10 and has a default value equals to 5.", required = true, defaultValue = "5") @RequestParam(value = "priority", required = true, defaultValue = "5") int priority,
            @ApiParam(value = "The name of the group of users that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. You can not assign a grant to it or to any of its object");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        return catalogObjectGrantService.createCatalogObjectGrantForAGroup(bucketName,
                                                                           catalogObjectName,
                                                                           user.getName(),
                                                                           accessType,
                                                                           priority,
                                                                           userGroup);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a user grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/user", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The name of the user that is benefiting of the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user = null;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.deleteCatalogObjectGrantForAUser(bucketName, catalogObjectName, username);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The name of the group of users that are benefiting of the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.deleteCatalogObjectGrantForAGroup(bucketName, catalogObjectName, userGroup);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update a user grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/user", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    private CatalogObjectGrantMetadata updateCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The new type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that is benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.updateCatalogObjectGrantForAUser(username,
                                                                          catalogObjectName,
                                                                          bucketName,
                                                                          accessType);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    private CatalogObjectGrantMetadata updateCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The new type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The new priority of the access grant. It can be a value from 1 to 10.", required = true) @RequestParam(value = "priority", required = true) int priority,
            @ApiParam(value = "The name of the group of users that are benefiting of the access grant.", required = true) @RequestParam(value = "userGroup", required = true) String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.updateCatalogObjectGrantForAGroup(userGroup,
                                                                           catalogObjectName,
                                                                           bucketName,
                                                                           accessType,
                                                                           priority);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all grants associated with a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectGrantMetadata> getAllCreatedCatalogObjectGrantsByAdmins(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.getAllCreatedCatalogObjectGrantsForThisBucket(bucketName, catalogObjectName);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete all grant associated with a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectGrantMetadata> deleteAllCatalogObjectGrants(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            if (restApiAccessService.isAPublicBucket(bucketName)) {
                throw new PublicBucketGrantAccessException("Bucket: " + bucketName +
                                                           " is public. No grants are assigned to it or to its objects");
            }
            // Check Grants
            if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
                if (!grantRightsService.getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(user,
                                                                                                       bucketName,
                                                                                                       catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
        }
        return catalogObjectGrantService.deleteAllCatalogObjectGrantsAssignedToABucket(bucketName, catalogObjectName);
    }

}
