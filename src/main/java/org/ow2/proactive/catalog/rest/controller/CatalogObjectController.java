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
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadataList;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.LinkUtil;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
@RequestMapping(value = "/buckets/{bucketId}/resources")
public class CatalogObjectController {

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
    @RequestMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadataList create(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId,
            @ApiParam(value = "Name of the object or empty when a ZIP archive is uploaded (All objects inside the archive are stored inside the catalog).") @RequestParam(required = false) Optional<String> name,
            @ApiParam(value = "Kind of the new object") @RequestParam String kind,
            @ApiParam(value = "Commit message") @RequestParam String commitMessage,
            @ApiParam(value = "The content type of CatalogRawObject") @RequestParam String objectContentType,
            @RequestPart(value = "file") MultipartFile file)
            throws IOException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }
        if (name.isPresent()) {
            CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucketId,
                                                                                           name.get(),
                                                                                           kind,
                                                                                           commitMessage,
                                                                                           objectContentType,
                                                                                           file.getBytes());
            catalogObject.add(LinkUtil.createLink(catalogObject.getBucketId(), catalogObject.getName()));

            return new CatalogObjectMetadataList(catalogObject);
        } else {
            List<CatalogObjectMetadata> catalogObjects = catalogObjectService.createCatalogObjects(bucketId,
                                                                                                   kind,
                                                                                                   commitMessage,
                                                                                                   objectContentType,
                                                                                                   file.getBytes());

            for (CatalogObjectMetadata catalogObject : catalogObjects) {
                catalogObject.add(LinkUtil.createLink(catalogObject.getBucketId(), catalogObject.getName()));
            }

            return new CatalogObjectMetadataList(catalogObjects);
        }
    }

    @ApiOperation(value = "Gets a catalog object's metadata by IDs", notes = "Returns metadata associated to the latest revision of the catalog object.")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket or catalog object not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/{name}", method = GET)
    public ResponseEntity<?> get(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId, @PathVariable String name) throws MalformedURLException,
            UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }

        String decodedName = URLDecoder.decode(name, "UTF-8");
        try {
            CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectMetadata(bucketId, decodedName);
            metadata.add(LinkUtil.createLink(metadata.getBucketId(), metadata.getName()));
            metadata.add(LinkUtil.createRelativeLink(metadata.getBucketId(), metadata.getName()));
            return ResponseEntity.ok(metadata);
        } catch (CatalogObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
        }

    }

    @ApiOperation(value = "Gets the raw content of the last revision of a catalog object")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = InputStreamResource.class),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"),
                            @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found") })

    @RequestMapping(value = "/{name}/raw", method = GET, produces = MediaType.ALL_VALUE)
    public ResponseEntity<InputStreamResource> getRaw(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId, @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }

        String decodedName = URLDecoder.decode(name, "UTF-8");

        try {
            CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucketId, decodedName);

            return rawObjectResponseCreator.createRawObjectResponse(rawObject);
        } catch (CatalogObjectNotFoundException e) {
            log.error("CatalogObject not found ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @ApiOperation(value = "Lists catalog objects metadata", notes = "Returns catalog objects metadata associated to the latest revision.")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", dataType = "int", paramType = "query", value = "Results page you want to retrieve (0..N)"),
                         @ApiImplicitParam(name = "size", dataType = "int", paramType = "query", value = "Number of records per page."),
                         @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). " +
                                                                                                                                  "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 206, message = "Missing object"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(method = GET)
    @ResponseBody
    public ResponseEntity<?> list(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId,
            @ApiParam(value = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind,
            @ApiParam(value = "Give a list of name separated by comma to get them in an archive", allowMultiple = true, type = "string") @RequestParam(value = "name", required = false) Optional<List<String>> names,
            HttpServletResponse response)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }

        if (names.isPresent()) {
            List<String> decodedNames = names.get().stream().map(name -> {
                try {
                    return URLDecoder.decode(name, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return name;
                }
            }).collect(Collectors.toList());

            ZipArchiveContent zipArchiveContent = catalogObjectService.getCatalogObjectsAsZipArchive(bucketId,
                                                                                                     decodedNames);

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
            return ResponseEntity.status(status).build();
        } else {
            List<CatalogObjectMetadata> metadataList;
            if (kind.isPresent()) {
                metadataList = catalogObjectService.listCatalogObjectsByKind(bucketId, kind.get());
            } else {
                metadataList = catalogObjectService.listCatalogObjects(bucketId);
            }

            for (CatalogObjectMetadata catalogObject : metadataList) {
                catalogObject.add(LinkUtil.createLink(catalogObject.getBucketId(), catalogObject.getName()));
                catalogObject.add(LinkUtil.createRelativeLink(catalogObject.getBucketId(), catalogObject.getName()));
            }

            return ResponseEntity.ok(metadataList);
        }
    }

    @ApiOperation(value = "Delete a catalog object", notes = "Delete the entire catalog object as well as its revisions. Returns the deleted CatalogRawObject's metadata")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket or object not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/{name}", method = DELETE)
    public ResponseEntity<?> delete(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId, @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }

        String decodedName = URLDecoder.decode(name, "UTF-8");
        try {
            catalogObjectService.delete(bucketId, decodedName);
        } catch (CatalogObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Restore a catalog object revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket, object or revision not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/{name}", method = PUT)
    public ResponseEntity<?> restore(
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable Long bucketId, @PathVariable String name, @RequestParam(required = true) Long commitTime)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {
        if (sessionIdRequired) {
            restApiAccessService.checkAccessBySessionIdAndThrowIfDeclined(sessionId, bucketId);
        }
        String decodedName = URLDecoder.decode(name, "UTF-8");
        try {
            CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.restore(bucketId,
                                                                                       decodedName,
                                                                                       commitTime);
            return ResponseEntity.ok(catalogObjectMetadata);
        } catch (RevisionNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
