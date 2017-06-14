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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadataList;
import org.ow2.proactive.catalog.service.CatalogObjectRevisionService;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
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


/**
 * @author ActiveEon Team
 */
@RestController
public class CatalogObjectController {

    @Autowired
    private CatalogObjectService catalogService;

    @Autowired
    private CatalogObjectRevisionService catalogObjectRevisionService;

    @ApiOperation(value = "Creates a new catalog object")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid file content supplied") })
    @RequestMapping(value = "/buckets/{bucketId}/resources", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadataList create(@PathVariable Long bucketId,
            @ApiParam(value = "Kind of the new object") @RequestParam String kind,
            @ApiParam(value = "Name of the object") @RequestParam String name,
            @ApiParam(value = "Commit message") @RequestParam String commitMessage,
            @ApiParam(value = "The content type of CatalogObject") @RequestParam String contentType,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        return new CatalogObjectMetadataList(catalogService.createCatalogObject(bucketId,
                                                                                kind,
                                                                                name,
                                                                                commitMessage,
                                                                                contentType,
                                                                                file.getBytes()));
    }

    @ApiOperation(value = "Gets a catalog object's metadata by IDs", notes = "Returns metadata associated to the latest revision of the catalog object.")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or catalog object not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}", method = GET)
    public ResponseEntity<?> get(@PathVariable Long bucketId, @PathVariable Long objectId, HttpServletResponse response)
            throws MalformedURLException {
        return catalogService.getCatalogObjectMetadata(bucketId, objectId);
    }

    @ApiOperation(value = "Gets the raw content of a last revision of a catalog object")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/raw", method = GET)
    public ResponseEntity<InputStreamResource> getRaw(@PathVariable Long bucketId, @PathVariable Long objectId) {
        return catalogObjectRevisionService.getCatalogObjectRaw(bucketId, objectId, Optional.empty());
    }

    @ApiOperation(value = "Lists catalog objects metadata", notes = "Returns catalog objects metadata associated to the latest revision.")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
                         @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page."),
                         @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). " +
                                                                                                                                  "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources", method = GET)
    public PagedResources list(@PathVariable Long bucketId, @ApiParam(hidden = true) Pageable pageable,
            @ApiParam(hidden = true) PagedResourcesAssembler assembler,
            @ApiParam(value = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind) {
        return catalogService.listCatalogObjects(bucketId, kind, pageable, assembler);
    }

    @ApiOperation(value = "Delete a catalog object", notes = "Delete the entire catalog object as well as its revisions. Returns the deleted CatalogObject's metadata")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or object not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long bucketId, @PathVariable Long objectId) {
        return catalogService.delete(bucketId, objectId);
    }

}
