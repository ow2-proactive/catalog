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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadataList;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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

    @ApiOperation(value = "Creates a new catalog object")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid file content supplied") })
    @RequestMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadataList create(@PathVariable Long bucketId,
            @ApiParam(value = "Name of the object") @RequestParam String name,
            @ApiParam(value = "Kind of the new object") @RequestParam String kind,
            @ApiParam(value = "Commit message") @RequestParam String commitMessage,
            @ApiParam(value = "The content type of CatalogRawObject") @RequestParam String contentType,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        return new CatalogObjectMetadataList(catalogObjectService.createCatalogObject(bucketId,
                                                                                      name,
                                                                                      kind,
                                                                                      commitMessage,
                                                                                      contentType,
                                                                                      file.getBytes()));
    }

    @ApiOperation(value = "Gets a catalog object's metadata by IDs", notes = "Returns metadata associated to the latest revision of the catalog object.")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or catalog object not found"))
    @RequestMapping(value = "/{name}", method = GET)
    public ResponseEntity<?> get(@PathVariable Long bucketId, @PathVariable String name)
            throws MalformedURLException, UnsupportedEncodingException {

        String decodedName = URLDecoder.decode(name, "UTF-8");
        try {
            CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectMetadata(bucketId, decodedName);
            return ResponseEntity.ok(metadata);
        } catch (CatalogObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
        }

    }

    @ApiOperation(value = "Gets the raw content of a last revision of a catalog object")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/{name}/raw", method = GET)
    public ResponseEntity<InputStreamResource> getRaw(@PathVariable Long bucketId, @PathVariable String name)
            throws UnsupportedEncodingException {

        String decodedName = URLDecoder.decode(name, "UTF-8");

        try {
            CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucketId, decodedName);

            byte[] bytes = rawObject.getRawObject();

            ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok().contentLength(bytes.length);

            try {
                MediaType mediaType = MediaType.valueOf(rawObject.getContentType());
                responseBodyBuilder = responseBodyBuilder.contentType(mediaType);
            } catch (org.springframework.http.InvalidMediaTypeException mimeEx) {
                log.warn("The wrong content type for object: " + name + ", commitTime:" +
                         rawObject.getCommitDateTime() + ", the contentType: " + rawObject.getContentType(), mimeEx);
            }
            return responseBodyBuilder.body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        } catch (CatalogObjectNotFoundException e) {
            log.error("CatalogObject not found ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @ApiOperation(value = "Lists catalog objects metadata", notes = "Returns catalog objects metadata associated to the latest revision.")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
                         @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page."),
                         @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). " +
                                                                                                                                  "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket not found"))
    @RequestMapping(method = GET)
    public ResponseEntity<List<CatalogObjectMetadata>> list(@PathVariable Long bucketId,
            @ApiParam(value = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind) {
        List<CatalogObjectMetadata> result;

        if (kind.isPresent()) {
            result = catalogObjectService.listCatalogObjectsByKind(bucketId, kind.get());
        } else {
            result = catalogObjectService.listCatalogObjects(bucketId);
        }
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Delete a catalog object", notes = "Delete the entire catalog object as well as its revisions. Returns the deleted CatalogRawObject's metadata")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or object not found"))
    @RequestMapping(value = "/{name}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long bucketId, @PathVariable String name)
            throws UnsupportedEncodingException {
        String decodedName = URLDecoder.decode(name, "UTF-8");
        try {
            catalogObjectService.delete(bucketId, decodedName);
        } catch (CatalogObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
