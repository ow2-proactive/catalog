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

import static org.ow2.proactive.catalog.util.AccessType.read;
import static org.ow2.proactive.catalog.util.AccessType.write;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
import org.ow2.proactive.catalog.util.LinkUtil;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.catalog.util.name.validator.ObjectNameValidator;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@RestController
@RequestMapping("/buckets/{bucketName}/resources/{name:.+}/revisions")
@Log4j2
public class CatalogObjectRevisionController {

    private static final String ANONYMOUS = "anonymous";

    private static final String ACTION = "[Action] ";

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private RawObjectResponseCreator rawObjectResponseCreator;

    @Autowired
    private GrantRightsService grantRightsService;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private BucketGrantService bucketGrantService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @Operation(summary = "Creates a new catalog object revision")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket not found"),
                            @ApiResponse(responseCode = "422", description = "Invalid catalog object JSON content supplied"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadata create(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name,
            @Parameter(description = "The commit message of the CatalogRawObject Revision", required = true) @RequestParam String commitMessage,
            @Parameter(description = "Project of the object") @RequestParam(value = "projectName", required = false, defaultValue = "") Optional<String> projectName,
            @Parameter(description = "Tags of the object") @RequestParam(value = "tags", required = false, defaultValue = "") Optional<String> tags,
            @RequestPart(value = "file") MultipartFile file)
            throws IOException, NotAuthenticatedException, AccessDeniedException {
        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), write)) {
                throw new BucketGrantAccessException(bucketName);
            }
            initiator = user.getName();
        } else {
            user = AuthenticatedUser.EMPTY;
        }
        CatalogObjectMetadata catalogObjectRevision = catalogObjectService.createCatalogObjectRevision(bucketName,
                                                                                                       name,
                                                                                                       projectName.orElse(""),
                                                                                                       tags.orElse(""),
                                                                                                       commitMessage,
                                                                                                       user.getName(),
                                                                                                       file.getBytes());
        if (sessionIdRequired) {
            catalogObjectRevision.setRights(grantRightsService.getCatalogObjectRights(user, bucketName, name));
        }
        catalogObjectRevision.add(LinkUtil.createLink(bucketName,
                                                      catalogObjectRevision.getName(),
                                                      catalogObjectRevision.getCommitDateTime()));
        catalogObjectRevision.add(LinkUtil.createRelativeLink(bucketName,
                                                              catalogObjectRevision.getName(),
                                                              catalogObjectRevision.getCommitDateTime()));
        log.info(ACTION + initiator + " created a new revision with message '" +
                 catalogObjectRevision.getCommitMessage() + "' on object " + name + " in bucket " + bucketName);
        return catalogObjectRevision;
    }

    @Operation(summary = "Gets a specific revision")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket, catalog object or catalog object revision not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(value = "/{commitTimeRaw}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata get(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name,
            @PathVariable long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), read)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectRevision(bucketName, name, commitTimeRaw);
        metadata.add(LinkUtil.createLink(bucketName, metadata.getName(), metadata.getCommitDateTime()));
        metadata.add(LinkUtil.createRelativeLink(bucketName, metadata.getName(), metadata.getCommitDateTime()));
        return metadata;

    }

    @Operation(summary = "Gets the raw content of a specific revision")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ok"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"),
                            @ApiResponse(responseCode = "404", description = "Bucket, catalog object or catalog object revision not found") })
    @RequestMapping(value = "/{commitTimeRaw}/raw", method = GET, produces = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getRaw(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name,
            @PathVariable long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), read)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        CatalogRawObject objectRevisionRaw = catalogObjectService.getCatalogObjectRevisionRaw(bucketName,
                                                                                              name,
                                                                                              commitTimeRaw);

        return rawObjectResponseCreator.createRawObjectResponse(objectRevisionRaw);
    }

    @Operation(summary = "Lists a catalog object revisions")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket or catalog object not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectMetadata> list(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        // Check Grants
        AuthenticatedUser user;
        List<CatalogObjectGrantMetadata> catalogObjectGrants = new LinkedList<>();
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), read)) {
                throw new BucketGrantAccessException(bucketName);
            } else {
                catalogObjectGrants = catalogObjectGrantService.getObjectsGrantsInABucket(bucketName);
            }

        } else {
            user = AuthenticatedUser.EMPTY;
        }
        List<CatalogObjectMetadata> catalogObjectMetadataList = catalogObjectService.listCatalogObjectRevisions(bucketName,
                                                                                                                name);

        for (CatalogObjectMetadata catalogObjectMetadata : catalogObjectMetadataList) {
            if (sessionIdRequired) {
                catalogObjectMetadata.setRights(grantRightsService.getCatalogObjectRights(user,
                                                                                          bucketName,
                                                                                          catalogObjectMetadata.getName()));
            }
            catalogObjectMetadata.add(LinkUtil.createLink(bucketName,
                                                          catalogObjectMetadata.getName(),
                                                          catalogObjectMetadata.getCommitDateTime()));
            catalogObjectMetadata.add(LinkUtil.createRelativeLink(bucketName,
                                                                  catalogObjectMetadata.getName(),
                                                                  catalogObjectMetadata.getCommitDateTime()));
        }

        return catalogObjectMetadataList;
    }

    @Operation(summary = "Restore a catalog object revision")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket, object or revision not found"),
                            @ApiResponse(responseCode = "422", description = "Invalid catalog object JSON content supplied"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(value = "/{commitTimeRaw}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata restore(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name,
            @PathVariable Long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), write)) {
                throw new BucketGrantAccessException(bucketName);
            }
            initiator = user.getName();
        }
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.restoreCatalogObject(bucketName,
                                                                                                name,
                                                                                                commitTimeRaw);
        log.info(ACTION + initiator + " restored revision " + catalogObjectMetadata.getCommitTimeRaw() +
                 " with message '" + catalogObjectMetadata.getCommitMessage() + "' on object " + name + " in bucket " +
                 bucketName);
        return catalogObjectMetadata;
    }

}
