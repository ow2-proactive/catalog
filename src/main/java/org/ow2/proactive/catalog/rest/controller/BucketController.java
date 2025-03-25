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

import static org.ow2.proactive.catalog.service.model.AuthenticatedUser.ANONYMOUS;
import static org.ow2.proactive.catalog.util.AccessType.admin;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Strings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@RestController
@RequestMapping(value = "/buckets")
public class BucketController {

    private static final String ACTION = "[Action] ";

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

    @Autowired
    private JobPlannerService jobPlannerService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @Value("${pa.catalog.tenant.filtering}")
    private boolean tenantFiltering;

    @SuppressWarnings("DefaultAnnotationParam")
    @Operation(summary = "Creates a new bucket")
    @ApiResponses(value = { @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketMetadata create(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The unique name of the Bucket.<br />" +
                                     "The name of bucket can be between 3 and 63 characters long, and can contain only lower-case characters, numbers, and dashes.<br />" +
                                     "A bucket's name must start with a lowercase letter and cannot terminate with a dash", schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @RequestParam(value = "name", required = true) String bucketName,
            @Parameter(description = "The name of the user that will own the Bucket. Defaults to " +
                                     BucketService.DEFAULT_BUCKET_OWNER, schema = @Schema(type = "string", defaultValue = BucketService.DEFAULT_BUCKET_OWNER)) @RequestParam(value = "owner", required = false, defaultValue = BucketService.DEFAULT_BUCKET_OWNER) String ownerName)
            throws NotAuthenticatedException, AccessDeniedException {
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            AuthenticatedUser authenticatedUser = restApiAccessService.getUserFromSessionId(sessionId);
            String tenant = authenticatedUser.getTenant();
            if (tenantFiltering && !Strings.isNullOrEmpty(tenant)) {
                restApiAccessService.checkAccessBySessionIdForOwnerOrGroupOrTenantAndThrowIfDeclined(sessionId,
                                                                                                     ownerName,
                                                                                                     tenant);
            } else {
                restApiAccessService.checkAccessBySessionIdForOwnerOrGroupAndThrowIfDeclined(sessionId, ownerName);
            }
            initiator = restApiAccessService.getUserFromSessionId(sessionId).getName();
        }
        try {
            String tenant = null;
            if (tenantFiltering && sessionIdRequired) {
                AuthenticatedUser authenticatedUser = restApiAccessService.getUserFromSessionId(sessionId);
                tenant = authenticatedUser.getTenant();
            }
            BucketMetadata bucketMetadata = bucketService.createBucket(bucketName, ownerName, tenant);
            log.info(ACTION + initiator + " created new bucket " + bucketName + " with owner " + ownerName +
                     " and tenant " + tenant);
            return bucketMetadata;
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException(bucketName, ownerName);
        }
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Operation(summary = "Update bucket owner")
    @ApiResponses(value = { @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata updateBucketOwner(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The new name of the user that will own the Bucket") @RequestParam(value = "owner", required = true) String newOwnerName)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user = getAuthenticatedUser(sessionId, bucketName);
        String initiator = user.getName();
        try {
            BucketMetadata bucketMetadata = bucketService.updateOwnerByBucketName(bucketName, newOwnerName);
            log.info(ACTION + initiator + " changed bucket " + bucketName + " ownership to " + newOwnerName);
            return bucketMetadata;
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException(bucketName, newOwnerName);
        }
    }

    @Operation(summary = "Gets a bucket's metadata by ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata getMetadata(
            @SuppressWarnings("DefaultAnnotationParam") @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName)
            throws NotAuthenticatedException, AccessDeniedException {
        BucketMetadata data = bucketService.getBucketMetadata(bucketName);

        AuthenticatedUser user = AuthenticatedUser.EMPTY;

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

    @Operation(summary = "Lists the buckets")
    @ApiResponses(value = { @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketMetadata> list(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @Parameter(description = "The name of the tenant that has access to the Bucket") @RequestParam(value = "tenant", required = false) String tenant,
            @Parameter(description = "The kind(s) of objects that buckets must contain.<br />Multiple kinds can be specified using comma separators") @RequestParam(value = "kind", required = false) Optional<String> kind,
            @Parameter(description = "The Content-Type of objects that buckets must contain") @RequestParam(value = "contentType", required = false) Optional<String> contentType,
            @Parameter(description = "The tag of objects that buckets must contain") @RequestParam(value = "objectTag", required = false) Optional<String> tag,
            @Parameter(description = "The buckets must contain objects which have the given job-planner association status.<br />Can be ALL, PLANNED, DEACTIVATED, FAILED or UNPLANNED.<br />ALL will filter objects which have an association with any status.<br />UNPLANNED will filter objects without any association.", schema = @Schema(type = "string", allowableValues = { "ALL",
                                                                                                                                                                                                                                                                                                                                                                                    "PLANNED", "DEACTIVATED", "FAILED", "UNPLANNED" })) @RequestParam(value = "associationStatus", required = false) Optional<String> associationStatus,
            @Parameter(description = "The name of objects that buckets must contain") @RequestParam(value = "objectName", required = false) Optional<String> objectName,
            @Parameter(description = "The bucket name contains the value of this parameter (case insensitive)")
            @RequestParam(value = "bucketName", required = false)
            final Optional<String> bucketName, @Parameter(description = "Include only objects whose project name contains the given string.") @RequestParam(value = "projectName", required = false) Optional<String> projectName, @Parameter(description = "Include only objects whose last commit belong to the given user.") @RequestParam(value = "lastCommitBy", required = false) Optional<String> lastCommitBy, @Parameter(description = "Include only objects have been committed at least once by the given user.") @RequestParam(value = "committedAtLeastOnceBy", required = false) Optional<String> committedAtLeastOnceBy, @Parameter(description = "Include only objects whose last commit time is greater than the given EPOCH time.") @RequestParam(value = "lastCommitTimeGreater", required = false, defaultValue = "0") Optional<Long> lastCommitTimeGreater, @Parameter(description = "Include only objects whose last commit time is less than the given EPOCH time.") @RequestParam(value = "lastCommitTimeLessThan", required = false, defaultValue = "0") Optional<Long> lastCommitTimeLessThan, @Parameter(description = "If true, buckets without objects matching the filters will be returned with objectCount=0. Default is false") @RequestParam(value = "allBuckets", required = false, defaultValue = "false") String allBuckets) throws NotAuthenticatedException, AccessDeniedException {

        List<BucketMetadata> listBucket;
        log.debug("====== Get buckets list request started ======== ");
        long startTime = System.currentTimeMillis();

        //transform empty String into an empty Optional
        kind = kind.filter(s -> !s.isEmpty());
        contentType = contentType.filter(s -> !s.isEmpty());
        objectName = objectName.filter(s -> !s.isEmpty());
        tag = tag.filter(s -> !s.isEmpty());
        associationStatus = associationStatus.filter(s -> !s.isEmpty());
        projectName = projectName.filter(s -> !s.isEmpty());
        lastCommitBy = lastCommitBy.filter(s -> !s.isEmpty());
        committedAtLeastOnceBy = committedAtLeastOnceBy.filter(s -> !s.isEmpty());
        boolean allBucketsEnabled = Boolean.parseBoolean(allBuckets);
        if (sessionIdRequired) {
            if (!tenantFiltering) {
                tenant = null;
            }
            AuthenticatedUser user = restApiAccessService.checkAccessBySessionIdForOwnerOrGroupOrTenantAndThrowIfDeclined(sessionId,
                                                                                                                          ownerName,
                                                                                                                          tenant)
                                                         .getAuthenticatedUser();
            log.debug("bucket list timer : validate session : " + (System.currentTimeMillis() - startTime) + " ms");
            listBucket = bucketService.getBucketsByGroups(ownerName,
                                                          tenant,
                                                          kind,
                                                          contentType,
                                                          objectName,
                                                          tag,
                                                          associationStatus,
                                                          projectName,
                                                          lastCommitBy,
                                                          committedAtLeastOnceBy,
                                                          lastCommitTimeGreater,
                                                          lastCommitTimeLessThan,
                                                          sessionId,
                                                          allBucketsEnabled,
                                                          user);
            listBucket.addAll(grantRightsService.getBucketsByPrioritiedGrants(user));
            listBucket = GrantHelper.removeDuplicate(listBucket);

            List<BucketGrantMetadata> allBucketsGrants = bucketGrantService.getUserAllBucketsGrants(user);
            List<CatalogObjectGrantMetadata> allCatalogObjectsGrants = catalogObjectGrantService.getObjectsGrants(user);

            for (BucketMetadata bucket : listBucket) {
                if (GrantHelper.isPublicBucket(bucket.getOwner()) || user.isCatalogAdmin()) {
                    bucket.setRights(admin.name());
                } else {
                    List<BucketGrantMetadata> bucketGrants = GrantHelper.filterBucketGrants(allBucketsGrants,
                                                                                            bucket.getName());
                    grantRightsService.addGrantsForBucketOwner(user, bucket.getName(), bucket.getOwner(), bucketGrants);

                    String bucketRights = grantRightsService.getBucketRights(bucketGrants);
                    bucket.setRights(bucketRights);

                    List<CatalogObjectGrantMetadata> objectsInBucketGrants = GrantHelper.filterBucketGrants(allCatalogObjectsGrants,
                                                                                                            bucket.getName());
                    int objectCount = grantRightsService.getNumberOfAccessibleObjectsInBucket(bucket,
                                                                                              bucketGrants,
                                                                                              objectsInBucketGrants);
                    bucket.setObjectCount(objectCount);
                }
            }
        } else {
            if (!tenantFiltering) {
                tenant = null;
            }
            listBucket = bucketService.listBuckets(ownerName,
                                                   tenant,
                                                   null,
                                                   kind,
                                                   contentType,
                                                   objectName,
                                                   tag,
                                                   associationStatus,
                                                   projectName,
                                                   lastCommitBy,
                                                   committedAtLeastOnceBy,
                                                   lastCommitTimeGreater,
                                                   lastCommitTimeLessThan,
                                                   sessionId,
                                                   allBucketsEnabled);
        }
        // Filter by bucket name
        if (!Strings.isNullOrEmpty(bucketName.orElse(""))) {
            listBucket = listBucket.stream()
                                   .filter(bucketMetadata -> bucketNameMatches(bucketMetadata.getName(),
                                                                               bucketName.orElse("")))
                                   .collect(Collectors.toList());
        }
        Collections.sort(listBucket);
        log.debug("bucket list timer : total : " + (System.currentTimeMillis() - startTime) + " ms");
        log.debug("====== Get buckets list request finished ========");
        return listBucket;
    }

    private boolean bucketNameMatches(String bucketName, String bucketNameFilter) {
        if (bucketNameFilter.isEmpty()) {
            return true;
        }
        if (bucketNameFilter.contains("%")) {
            String bucketNameFilterUpdated = bucketNameFilter.toLowerCase().replace("%", ".*");
            return bucketName.toLowerCase().matches(bucketNameFilterUpdated);
        } else {
            return bucketName.toLowerCase().contains(bucketNameFilter.toLowerCase());
        }
    }

    @Operation(summary = "Delete the empty buckets")
    @RequestMapping(method = DELETE)
    @ApiResponses(value = { @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @ResponseStatus(HttpStatus.OK)
    public void cleanEmpty(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId) {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            List<String> emptyBucketNames = bucketService.getAllEmptyBuckets();
            for (String bucketName : emptyBucketNames) {
                if (!AccessTypeHelper.satisfy(grantRightsService.getBucketRights(user, bucketName), admin)) {
                    throw new BucketGrantAccessException(bucketName);
                }
            }
            bucketService.cleanAllEmptyBuckets();
            log.info(ACTION + user.getName() + " deleted the following empty buckets " + emptyBucketNames);
        } else {
            List<String> emptyBucketNames = bucketService.getAllEmptyBuckets();
            bucketService.cleanAllEmptyBuckets();
            log.info("Deleted empty buckets " + emptyBucketNames + " initiated by " + ANONYMOUS);
        }
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Operation(summary = "Delete an empty bucket", description = "Note: it is forbidden to delete a non-empty bucket. You need to delete manually all workflows in the bucket before.")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata delete(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName)
            throws NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user = getAuthenticatedUser(sessionId, bucketName);
        String initiator = user.getName();
        BucketMetadata bucketMetadata = bucketService.deleteEmptyBucket(bucketName);
        log.info(ACTION + initiator + " deleted empty bucket " + bucketName);
        return bucketMetadata;
    }

    private AuthenticatedUser getAuthenticatedUser(String sessionId, String bucketName) {
        AuthenticatedUser user = AuthenticatedUser.EMPTY;
        // Check session validation
        if (sessionIdRequired) {
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getBucketRights(user, bucketName), admin)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }
        return user;
    }
}
