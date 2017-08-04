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
import org.springframework.http.ResponseEntity;
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

    @ApiOperation(value = "Creates a new bucket")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketMetadata create(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @RequestParam(value = "name", required = true) String bucketName,
            @ApiParam(value = "The name of the user that will own the Bucket", defaultValue = "GROUP:" +
                                                                                              BucketService.DEFAULT_BUCKET_OWNER) @RequestParam(value = "owner", required = false, defaultValue = "GROUP:" +
                                                                                                                                                                                                  BucketService.DEFAULT_BUCKET_OWNER) String ownerName)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, ownerName);
        }
        try {
            return bucketService.createBucket(bucketName, ownerName);
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException("The bucket named " + bucketName + " owned by " + ownerName +
                                                     " already exist");
        }
    }

    @ApiOperation(value = "Gets a bucket's metadata by ID")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketId}", method = GET)
    public BucketMetadata getMetadata(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable long bucketId) throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }
        return bucketService.getBucketMetadata(bucketId);
    }

    @ApiOperation(value = "Lists the buckets")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    public List<BucketMetadata> list(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @ApiParam(value = "The kind of objects that buckets must contain") @RequestParam(value = "kind", required = false) String kind)
            throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            RestApiAccessResponse restApiAccessResponse = restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId,
                                                                                                                        ownerName);
            List<String> groupsAndUsername = restApiAccessResponse.getAuthenticatedUser().getGroups();

            return bucketService.listBuckets(groupsAndUsername, kind);

        } else {
            return bucketService.listBuckets(ownerName, kind);
        }
    }

    @ApiOperation(value = "Delete the empty buckets")
    @RequestMapping(method = DELETE)
    public void cleanEmpty() {
        bucketService.cleanAllEmptyBuckets();
    }

    @ApiOperation(value = "Delete an empty bucket", notes = "It's forbidden to delete a non-empty bucket. You need to delete manually all workflows in the bucket before.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(value = "/{bucketId}", method = DELETE)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<?> delete(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId) throws NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }
        BucketMetadata deletedBucketMetadata = bucketService.deleteEmptyBucket(bucketId);
        return ResponseEntity.ok(deletedBucketMetadata);
    }
}
