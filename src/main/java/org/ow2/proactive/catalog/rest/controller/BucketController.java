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
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketAlreadyExistingException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private GrantRightsService grantRightsService;

    @Autowired
    private BucketGrantService bucketGrantService;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Creates a new bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketMetadata create(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The unique name of the Bucket. /n " +
                              "The name of bucket can be between 3 and 63 characters long, and can contain only lower-case characters, numbers, and dashes. /n" +
                              "A bucket's name must start with a lowercase letter and cannot terminate with a dash") @RequestParam(value = "name", required = true) String bucketName,
            @ApiParam(value = "The name of the user that will own the Bucket", defaultValue = BucketService.DEFAULT_BUCKET_OWNER) @RequestParam(value = "owner", required = false, defaultValue = BucketService.DEFAULT_BUCKET_OWNER) String ownerName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForOwnerOrGroupAndThrowIfDeclined(sessionId, ownerName);
        }
        try {
            return bucketService.createBucket(bucketName, ownerName);
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException(bucketName, ownerName);
        }
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Update bucket owner")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata updateBucketOwner(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @ApiParam(value = "The name of the existing Bucket ", required = true) @PathVariable String bucketName,
            @ApiParam(value = "The new name of the user that will own the Bucket") @RequestParam(value = "owner", required = true) String newOwnerName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getBucketRights(user, bucketName), admin)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }
        try {
            return bucketService.updateOwnerByBucketName(bucketName, newOwnerName);
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException(bucketName, newOwnerName);
        }
    }

    @ApiOperation(value = "Gets a bucket's metadata by ID")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata getMetadata(
            @SuppressWarnings("DefaultAnnotationParam") @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName) throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        BucketMetadata data = bucketService.getBucketMetadata(bucketName);

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            user = restApiAccessService.getUserFromSessionId(sessionId);

            if (!grantRightsService.isBucketAccessible(user, data)) {
                throw new BucketGrantAccessException(bucketName);
            }

            data.setRights(grantRightsService.getBucketRights(user, bucketName));
        }
        return data;
    }

    @ApiOperation(value = "Lists the buckets")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketMetadata> list(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @ApiParam(value = "The kind(s) of objects that buckets must contain. Multiple kinds can be specified using comma separators") @RequestParam(value = "kind", required = false) Optional<String> kind,
            @ApiParam(value = "The Content-Type of objects that buckets must contain") @RequestParam(value = "contentType", required = false) Optional<String> contentType,
            @ApiParam(value = "The name of objects that buckets must contain") @RequestParam(value = "objectName", required = false) Optional<String> objectName)
            throws NotAuthenticatedException, AccessDeniedException {

        List<BucketMetadata> listBucket;
        log.debug("====== Get buckets list request started ======== ");
        long startTime = System.currentTimeMillis();

        //transform empty String into an empty Optional
        kind = kind.filter(s -> !s.isEmpty());
        contentType = contentType.filter(s -> !s.isEmpty());
        objectName = objectName.filter(s -> !s.isEmpty());
        if (sessionIdRequired) {
            AuthenticatedUser user = restApiAccessService.checkAccessBySessionIdForOwnerOrGroupAndThrowIfDeclined(sessionId,
                                                                                                                  ownerName)
                                                         .getAuthenticatedUser();
            log.debug("bucket list timer : validate session : " + (System.currentTimeMillis() - startTime) + " ms");
            listBucket = bucketService.getBucketsByGroups(ownerName, kind, contentType, objectName, user::getGroups);
            listBucket.addAll(grantRightsService.getBucketsByPrioritiedGrants(user));

            List<BucketGrantMetadata> allBucketsGrants = bucketGrantService.getUserAllBucketsGrants(user);
            List<CatalogObjectGrantMetadata> allCatalogObjectsGrants = catalogObjectGrantService.getObjectsGrants(user);

            for (BucketMetadata bucket : listBucket) {
                if (GrantHelper.isPublicBucket(bucket.getOwner())) {
                    bucket.setRights(admin.name());
                } else {
                    List<BucketGrantMetadata> bucketGrants = GrantHelper.filterBucketGrants(allBucketsGrants,
                                                                                            bucket.getName());
                    grantRightsService.addGrantsForBucketOwner(user, bucket.getName(), bucket.getOwner(), bucketGrants);

                    String bucketRights = grantRightsService.getBucketRights(bucketGrants);
                    bucket.setRights(bucketRights);

                    List<CatalogObjectGrantMetadata> objectsInBucketGrants = GrantHelper.filterBucketGrants(allCatalogObjectsGrants,
                                                                                                            bucket.getName());
                    int objectCount = grantRightsService.getTheNumberOfAccessibleObjectsInTheBucket(bucket,
                                                                                                    bucketRights,
                                                                                                    bucketGrants,
                                                                                                    objectsInBucketGrants);
                    bucket.setObjectCount(objectCount);
                }
            }
        } else {
            listBucket = bucketService.listBuckets(ownerName, kind, contentType, objectName);
        }
        Collections.sort(listBucket);
        log.debug("bucket list timer : total : " + (System.currentTimeMillis() - startTime) + " ms");
        log.debug("====== Get buckets list request finished ========");
        return listBucket;
    }

    @ApiOperation(value = "Delete the empty buckets")
    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void cleanEmpty() {
        bucketService.cleanAllEmptyBuckets();
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @ApiOperation(value = "Delete an empty bucket", notes = "It's forbidden to delete a non-empty bucket. You need to delete manually all workflows in the bucket before.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata delete(
            @ApiParam(value = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @PathVariable String bucketName) throws NotAuthenticatedException, AccessDeniedException {
        // Check session validation
        if (sessionIdRequired) {
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!grantRightsService.getBucketRights(user, bucketName).equals(admin.toString())) {
                throw new BucketGrantAccessException(bucketName);
            }
        }
        return bucketService.deleteEmptyBucket(bucketName);
    }
}
