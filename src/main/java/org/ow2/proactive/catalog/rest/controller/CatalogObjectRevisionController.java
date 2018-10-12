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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.util.LinkUtil;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@RestController
@RequestMapping("/buckets/{bucketName}/resources/{name}/revisions")
@Log4j2
public class CatalogObjectRevisionController {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private RawObjectResponseCreator rawObjectResponseCreator;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @ApiOperation(value = "Creates a new catalog object revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid catalog object JSON content supplied"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadata create(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name,
            @ApiParam(value = "The commit message of the CatalogRawObject Revision", required = true) @RequestParam String commitMessage,
            @RequestPart(value = "file") MultipartFile file)
            throws IOException, NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);
        CatalogObjectMetadata catalogObjectRevision = catalogObjectService.createCatalogObjectRevision(bucketName,
                                                                                                       name,
                                                                                                       commitMessage,
                                                                                                       file.getBytes());
        catalogObjectRevision.add(LinkUtil.createLink(bucketName,
                                                      catalogObjectRevision.getName(),
                                                      catalogObjectRevision.getCommitDateTime()));
        return catalogObjectRevision;
    }

    @ApiOperation(value = "Gets a specific revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/{commitTimeRaw}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata get(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name, @PathVariable long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectRevision(bucketName, name, commitTimeRaw);
        metadata.add(LinkUtil.createLink(bucketName, metadata.getName(), metadata.getCommitDateTime()));
        metadata.add(LinkUtil.createRelativeLink(bucketName, metadata.getName(), metadata.getCommitDateTime()));
        return metadata;

    }

    @ApiOperation(value = "Gets the raw content of a specific revision")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"),
                            @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found") })
    @RequestMapping(value = "/{commitTimeRaw}/raw", method = GET, produces = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getRaw(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name, @PathVariable long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);

        CatalogRawObject objectRevisionRaw = catalogObjectService.getCatalogObjectRevisionRaw(bucketName,
                                                                                              name,
                                                                                              commitTimeRaw);

        return rawObjectResponseCreator.createRawObjectResponse(objectRevisionRaw);
    }

    @ApiOperation(value = "Lists a catalog object revisions")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket or catalog object not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectMetadata> list(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);
        List<CatalogObjectMetadata> catalogObjectMetadataList = catalogObjectService.listCatalogObjectRevisions(bucketName,
                                                                                                                name);

        for (CatalogObjectMetadata catalogObjectMetadata : catalogObjectMetadataList) {
            catalogObjectMetadata.add(LinkUtil.createLink(bucketName,
                                                          catalogObjectMetadata.getName(),
                                                          catalogObjectMetadata.getCommitDateTime()));
            catalogObjectMetadata.add(LinkUtil.createRelativeLink(bucketName,
                                                                  catalogObjectMetadata.getName(),
                                                                  catalogObjectMetadata.getCommitDateTime()));
        }

        return catalogObjectMetadataList;
    }

    @ApiOperation(value = "Restore a catalog object revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket, object or revision not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/{commitTimeRaw}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata restore(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name, @PathVariable Long commitTimeRaw)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);
        return catalogObjectService.restore(bucketName, name, commitTimeRaw);
    }

}
