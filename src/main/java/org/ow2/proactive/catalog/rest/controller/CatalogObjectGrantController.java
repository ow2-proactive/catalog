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

import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetaData;
import org.ow2.proactive.catalog.service.CatalogObjectGrantService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectGrantAccessException;
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
@RequestMapping(value = "/buckets/{bucketName}/grant")
public class CatalogObjectGrantController {

    private static final String REQUEST_API_QUERY = "/resources/{catalogObjectName}/grant";

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new username grant or a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectGrantMetaData createCatalogObjectGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName, @PathVariable String catalogObjectName,
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

        // Check Grants
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!catalogObjectGrantService.checkInCatalogObjectGrantsIfTheUserOrUserGroupHasAdminRightsOverTheCatalogObject(user,
                                                                                                                           bucketName,
                                                                                                                           catalogObjectName)) {
                throw new CatalogObjectGrantAccessException(bucketName, catalogObjectName);
            }
        }

        return catalogObjectGrantService.createCatalogObjectGrant(bucketName,
                                                                  catalogObjectName,
                                                                  currentUser,
                                                                  accessType,
                                                                  username,
                                                                  userGroup);

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a username grant or a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectGrantMetaData deleteCatalogObjectGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName, @PathVariable String catalogObjectName,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The name of the user that will have grant access", defaultValue = "") @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @ApiParam(value = "The name of the user group that will have grant access", defaultValue = "") @RequestParam(value = "userGroup", required = false, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        // Check Grants
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!catalogObjectGrantService.checkInCatalogObjectGrantsIfTheUserOrUserGroupHasAdminRightsOverTheCatalogObject(user,
                                                                                                                           bucketName,
                                                                                                                           catalogObjectName)) {
                throw new CatalogObjectGrantAccessException(bucketName, catalogObjectName);
            }
        }
        return catalogObjectGrantService.deleteCatalogObjectGrant(bucketName, catalogObjectName, username, userGroup);

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update a username grant or a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = PUT)
    @ResponseStatus(HttpStatus.OK)
    private CatalogObjectGrantMetaData updateCatalogObjectGrant(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName, @PathVariable String catalogObjectName,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The new access type") @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that have the grant access", defaultValue = "") @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @ApiParam(value = "The name of the user group that have the grant access", defaultValue = "") @RequestParam(value = "userGroup", required = false, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        // Check Grants
        if (!restApiAccessService.isBucketAccessibleByUser(sessionIdRequired, sessionId, bucketName)) {
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!catalogObjectGrantService.checkInCatalogObjectGrantsIfTheUserOrUserGroupHasAdminRightsOverTheCatalogObject(user,
                                                                                                                           bucketName,
                                                                                                                           catalogObjectName)) {
                throw new CatalogObjectGrantAccessException(bucketName, catalogObjectName);
            }
        }

        return catalogObjectGrantService.updateCatalogObjectGrant(username,
                                                                  userGroup,
                                                                  catalogObjectName,
                                                                  bucketName,
                                                                  accessType);

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all assigned grants for the user and his group on a specific catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectGrantMetaData> getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName, @PathVariable String catalogObjectName,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser,
            @ApiParam(value = "The current userGroup") @RequestParam(value = "userGroup", required = true) String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        return catalogObjectGrantService.getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(currentUser,
                                                                                                       userGroup,
                                                                                                       catalogObjectName,
                                                                                                       bucketName);

    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Get all created grants by the current user on all bucket's object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectGrantMetaData> getAllCreatedCatalogObjectGrantsByTheCurrentUSerForTheCurrentUserBucket(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName,
            @ApiParam(value = "The current user") @RequestParam(value = "currentUser", required = true) String currentUser)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isUserSessionActive(sessionId, currentUser)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
        }

        return catalogObjectGrantService.getAllCreatedCatalogObjectGrantsForThisBucket(currentUser, bucketName);
        //getAllCreatedCatalogObjectGrantsForTheCBucket
    }

}
