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

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketAlreadyExistingException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.model.RestApiAccessResponse;
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


/**
 * @author ActiveEon Team
 */
@RestController
@RequestMapping(value = "/buckets")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @Autowired
    private RestApiAccessService restApiAccessService;

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

    @ApiOperation(value = "Gets a bucket's metadata by ID")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketName}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public BucketMetadata getMetadata(
            @SuppressWarnings("DefaultAnnotationParam") @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName) throws NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);
        return bucketService.getBucketMetadata(bucketName);
    }

    @ApiOperation(value = "Lists the buckets")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<BucketMetadata> list(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @ApiParam(value = "The kind of objects that buckets must contain") @RequestParam(value = "kind", required = false) Optional<String> kind,
            @ApiParam(value = "The content type of objects that buckets must contain") @RequestParam(value = "contentType", required = false) Optional<String> contentType)
            throws NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            RestApiAccessResponse restApiAccessResponse = restApiAccessService.checkAccessBySessionIdForOwnerOrGroupAndThrowIfDeclined(sessionId,
                                                                                                                                       ownerName);

            return bucketService.getBucketsByGroups(ownerName,
                                                    kind,
                                                    contentType,
                                                    () -> restApiAccessResponse.getAuthenticatedUser().getGroups());

        } else {
            return bucketService.listBuckets(ownerName, kind, contentType);
        }
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
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);
        return bucketService.deleteEmptyBucket(bucketName);
    }
}
