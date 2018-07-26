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
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadataList;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.LinkUtil;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
@Log4j2
@RequestMapping(value = "/buckets")
public class CatalogObjectController {

    private static final String REQUEST_API_QUERY = "/{bucketName}/resources";

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private RawObjectResponseCreator rawObjectResponseCreator;

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @ApiOperation(value = "Creates a new catalog object")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid file content supplied") })
    @RequestMapping(value = REQUEST_API_QUERY, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadataList create(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName,
            @ApiParam(value = "Name of the object or empty when a ZIP archive is uploaded (All objects inside the archive are stored inside the catalog).") @RequestParam(required = false) Optional<String> name,
            @ApiParam(value = "Kind of the new object", required = true) @RequestParam String kind,
            @ApiParam(value = "Commit message", required = true) @RequestParam String commitMessage,
            @ApiParam(value = "The content type of CatalogRawObject - MIME type", required = true) @RequestParam String objectContentType,
            @ApiParam(value = "The content of CatalogRawObject", required = true) @RequestPart(value = "file") MultipartFile file)
            throws IOException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }
        if (name.isPresent()) {
            CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucketName,
                                                                                           name.get(),
                                                                                           kind,
                                                                                           commitMessage,
                                                                                           objectContentType,
                                                                                           file.getBytes());
            catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));

            return new CatalogObjectMetadataList(catalogObject);
        } else {
            List<CatalogObjectMetadata> catalogObjects = catalogObjectService.createCatalogObjects(bucketName,
                                                                                                   kind,
                                                                                                   commitMessage,
                                                                                                   file.getBytes());

            for (CatalogObjectMetadata catalogObject : catalogObjects) {
                catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));
            }

            return new CatalogObjectMetadataList(catalogObjects);
        }
    }

    @ApiOperation(value = "Lists all kinds for all objects")
    @RequestMapping(value = "/kinds", method = GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> listKinds() {
        return catalogObjectService.getKinds();
    }

    @ApiOperation(value = "Update a catalog object metadata, like kind and content type")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket, object or revision not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"),
                            @ApiResponse(code = 400, message = "Wrong specified parameters: at least one should be present") })
    @RequestMapping(value = REQUEST_API_QUERY + "/{name}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata updateObjectMetadata(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name,
            @ApiParam(value = "The new kind of an object", required = false) @RequestParam(value = "kind", required = false) Optional<String> kind,
            @ApiParam(value = "The new content type of an object - MIME type", required = false) @RequestParam(value = "contentType", required = false) Optional<String> contentType)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }
        return catalogObjectService.updateObjectMetadata(bucketName, name, kind, contentType);
    }

    @ApiOperation(value = "Gets a catalog object's metadata by IDs", notes = "Returns metadata associated to the latest revision of the catalog object.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket or catalog object not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = REQUEST_API_QUERY + "/{name}", method = GET)
    public CatalogObjectMetadata get(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name) throws MalformedURLException,
            UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectMetadata(bucketName, name);
        metadata.add(LinkUtil.createLink(bucketName, metadata.getName()));
        metadata.add(LinkUtil.createRelativeLink(bucketName, metadata.getName()));
        return metadata;
    }

    @ApiOperation(value = "Gets the raw content of the last revision of a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"),
                            @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found") })

    @RequestMapping(value = REQUEST_API_QUERY + "/{name}/raw", method = GET, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getRaw(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }

        CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucketName, name);
        return rawObjectResponseCreator.createRawObjectResponse(rawObject);

    }

    @ApiOperation(value = "Lists catalog objects metadata", notes = "Returns catalog objects metadata associated to the latest revision.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 206, message = "Missing object"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = REQUEST_API_QUERY, method = GET)
    public ResponseEntity<List<CatalogObjectMetadata>> list(
            @ApiParam(value = "sessionID") @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName,
            @ApiParam(value = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind,
            @ApiParam(value = "Filter according to content type.") @RequestParam(required = false) Optional<String> contentType,
            @ApiParam(value = "Give a list of name separated by comma to get them in an archive", allowMultiple = true, type = "string") @RequestParam(value = "name", required = false) Optional<List<String>> names,
            HttpServletResponse response)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }

        if (names.isPresent()) {

            ZipArchiveContent zipArchiveContent = catalogObjectService.getCatalogObjectsAsZipArchive(bucketName,
                                                                                                     names.get());

            HttpStatus status;
            if (zipArchiveContent.isPartial()) {
                status = HttpStatus.PARTIAL_CONTENT;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } else {
                status = HttpStatus.OK;
                response.setStatus(HttpServletResponse.SC_OK);
            }

            response.setContentType(ZIP_CONTENT_TYPE);
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
            try {
                response.getOutputStream().write(zipArchiveContent.getContent());
                response.getOutputStream().flush();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            return new ResponseEntity<>(status);
        } else {
            List<CatalogObjectMetadata> metadataList;
            if (kind.isPresent() && contentType.isPresent()) {
                metadataList = catalogObjectService.listCatalogObjectsByKindAndContentType(bucketName,
                                                                                           kind.get(),
                                                                                           contentType.get());
            } else if (!kind.isPresent() && contentType.isPresent()) {
                metadataList = catalogObjectService.listCatalogObjectsByContentType(bucketName, contentType.get());
            } else if (kind.isPresent() && !contentType.isPresent()) {
                metadataList = catalogObjectService.listCatalogObjectsByKind(bucketName, kind.get());
            }

            else {
                metadataList = catalogObjectService.listCatalogObjects(bucketName);
            }

            for (CatalogObjectMetadata catalogObject : metadataList) {
                catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));
                catalogObject.add(LinkUtil.createRelativeLink(bucketName, catalogObject.getName()));
            }

            return ResponseEntity.ok(metadataList);
        }
    }

    @ApiOperation(value = "Delete a catalog object", notes = "Delete the entire catalog object as well as its revisions. Returns the deleted CatalogObject's metadata.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket or object not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = REQUEST_API_QUERY + "/{name}", method = DELETE)
    public CatalogObjectMetadata delete(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName, @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionId, bucketName);
        }

        return catalogObjectService.delete(bucketName, name);
    }

}
