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
import org.ow2.proactive.catalog.service.exception.*;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

    private static final String ANONYMOUS = "anonymous";

    private static final String ACTION = "[Action] ";

    public static final String PUBLIC_CANNOT_ASSIGN_A_GRANT = " is public. You cannot assign a grant to it or to any of its object";

    public static final String PUBLIC_NO_GRANTS_ARE_ASSIGNED = " is public. No grants are assigned to it or to its objects";

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
    @Transactional
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            checkAccess(sessionId, user, bucketName, PUBLIC_CANNOT_ASSIGN_A_GRANT, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata createdUserObjectGrant = catalogObjectGrantService.createCatalogObjectGrantForAUser(bucketName,
                                                                                                                       catalogObjectName,
                                                                                                                       user.getName(),
                                                                                                                       accessType,
                                                                                                                       username);
        if (sessionIdRequired) {
            if (user.getName().equals(username)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By creating this grant for yourself, you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }

        log.info(ACTION + initiator + " created " + accessType + " grant access for user " + username + " on object " +
                 catalogObjectName + " in bucket " + bucketName);

        return createdUserObjectGrant;
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Create a new group grant for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The new priority of the access grant. It can be a value from 1 (lowest) to 10 (highest), with 5 as default.\n" +
                              "Priorities are used to compute the final access rights of a user belonging to multiple groups. Group grants with the same priority will resolve with the default accessType order (admin > write > read > noAccess).\n" +
                              "Finally, please note that a user grant has always more priority than a group grant.", required = true, defaultValue = "5") @RequestParam(value = "priority", required = true, defaultValue = "5") int priority,
            @ApiParam(value = "The name of the group of users that will benefit of the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            checkAccess(sessionId, user, bucketName, PUBLIC_CANNOT_ASSIGN_A_GRANT, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata createdUserGroupObjectGrant = catalogObjectGrantService.createCatalogObjectGrantForAGroup(bucketName,
                                                                                                                             catalogObjectName,
                                                                                                                             user.getName(),
                                                                                                                             accessType,
                                                                                                                             priority,
                                                                                                                             userGroup);
        if (sessionIdRequired) {
            if (user.getGroups().contains(userGroup)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By creating this grant for your group: " + userGroup +
                                                             ", you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }
        log.info(ACTION + initiator + " created " + accessType + " grant access for group " + userGroup +
                 " on object " + catalogObjectName + " in bucket " + bucketName);
        return createdUserGroupObjectGrant;
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a user grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/user", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The name of the user that is benefiting of the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata deletedUserObjectGrant = catalogObjectGrantService.deleteCatalogObjectGrantForAUser(bucketName,
                                                                                                                       catalogObjectName,
                                                                                                                       username);
        if (sessionIdRequired) {
            if (user.getName().equals(username)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By deleting this grant assigned to yourself, you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }
        log.info(ACTION + initiator + " deleted grant access for user " + username + " on object " + catalogObjectName +
                 " in bucket " + bucketName);
        return deletedUserObjectGrant;
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The name of the group of users that are benefiting of the access grant.", required = true, defaultValue = "") @RequestParam(value = "userGroup", required = true, defaultValue = "") String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata deletedUserGroupObjectGrant = catalogObjectGrantService.deleteCatalogObjectGrantForAGroup(bucketName,
                                                                                                                             catalogObjectName,
                                                                                                                             userGroup);
        if (sessionIdRequired) {
            if (user.getGroups().contains(userGroup)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By deleting this grant assigned to your group: " +
                                                             userGroup +
                                                             ", you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }
        log.info(ACTION + initiator + " deleted grant access for group " + userGroup + " on object " +
                 catalogObjectName + " in bucket " + bucketName);
        return deletedUserGroupObjectGrant;
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update a user grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/user", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAUser(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The new type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The name of the user that is benefiting from the access grant.", required = true, defaultValue = "") @RequestParam(value = "username", required = true, defaultValue = "") String username)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata updatedUserObjectGrant = catalogObjectGrantService.updateCatalogObjectGrantForAUser(user,
                                                                                                                       username,
                                                                                                                       catalogObjectName,
                                                                                                                       bucketName,
                                                                                                                       accessType);
        if (sessionIdRequired) {
            if (user.getName().equals(username)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By updating this grant assigned to yourself, you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }
        log.info(ACTION + initiator + " updated grant access for user " + username + " on object " + catalogObjectName +
                 " in bucket " + bucketName + " to " + accessType);
        return updatedUserObjectGrant;
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update a user group grant access for a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY + "/group", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAGroup(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog object is stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName,
            @ApiParam(value = "The new type of the access grant. It can be either noAccess, read, write or admin.", required = true) @RequestParam(value = "accessType", required = true) String accessType,
            @ApiParam(value = "The new priority of the access grant. It can be a value from 1 (lowest) to 10 (highest), with 5 as default.\n" +
                              "Priorities are used to compute the final access rights of a user belonging to multiple groups. Group grants with the same priority will resolve with the default accessType order (admin > write > read > noAccess).\n" +
                              "Finally, please note that a user grant has always more priority than a group grant.", required = true) @RequestParam(value = "priority", required = true) int priority,
            @ApiParam(value = "The name of the group of users that are benefiting of the access grant.", required = true) @RequestParam(value = "userGroup", required = true) String userGroup)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check session validation
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectGrantMetadata updatedUserGroupObjectGrant = catalogObjectGrantService.updateCatalogObjectGrantForAGroup(user,
                                                                                                                             userGroup,
                                                                                                                             catalogObjectName,
                                                                                                                             bucketName,
                                                                                                                             accessType,
                                                                                                                             priority);
        if (sessionIdRequired) {
            if (user.getGroups().contains(userGroup)) {
                if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                       .equals(admin.toString())) {
                    throw new LostOfAdminGrantRightException("By updating this grant assigned to your group: " +
                                                             userGroup +
                                                             ", you will lose your admin rights over the object: " +
                                                             catalogObjectName + ".");
                }
            }
        }
        log.info(ACTION + initiator + " updated grant access for group " + userGroup + " on object " +
                 catalogObjectName + " in bucket " + bucketName + " to " + accessType);
        return updatedUserGroupObjectGrant;
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
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
        }
        return catalogObjectGrantService.getObjectGrants(bucketName, catalogObjectName);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete all grant associated with a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = REQUEST_API_QUERY, method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public List<CatalogObjectGrantMetadata> deleteAllCatalogObjectGrants(
            @ApiParam(value = "The session id used to access ProActive REST server.", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the bucket where the catalog objects are stored.", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The name of the object in the bucket, which is the subject of the grant.", required = true) @PathVariable String catalogObjectName)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
            checkAccess(sessionId, user, bucketName, PUBLIC_NO_GRANTS_ARE_ASSIGNED, catalogObjectName);
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        if (sessionIdRequired) {
            if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName)
                                   .equals(admin.toString())) {
                throw new LostOfAdminGrantRightException("By deleting all grants assigned to the object: " +
                                                         catalogObjectName +
                                                         ", you will lose your admin rights over it.");
            }
        }
        List<CatalogObjectGrantMetadata> catalogObjectGrantMetadataList = catalogObjectGrantService.deleteAllCatalogObjectGrantsAssignedToAnObjectInABucket(bucketName,
                                                                                                                                                            catalogObjectName);
        log.info(ACTION + initiator + " deleted all grant accesses on object " + catalogObjectName + " in bucket " +
                 bucketName);
        return catalogObjectGrantMetadataList;
    }

    private void checkAccess(String sessionId, AuthenticatedUser user, String bucketName,
            String publicGrantErrorMessage, String catalogObjectName) {
        // Check session validation
        if (!restApiAccessService.isUserSessionActive(sessionId, user.getName())) {
            throw new AccessDeniedException("Session id is not active. Please login.");
        }
        if (restApiAccessService.isAPublicBucket(bucketName)) {
            throw new PublicBucketGrantAccessException("Bucket: " + bucketName + publicGrantErrorMessage);
        }
        // Check Grants
        if (!grantRightsService.getCatalogObjectRights(user, bucketName, catalogObjectName).equals(admin.toString())) {
            throw new CatalogObjectGrantAccessException(bucketName, catalogObjectName);
        }
    }

}
