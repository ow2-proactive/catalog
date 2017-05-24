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
import java.util.Optional;

import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.catalog.rest.service.CatalogObjectRevisionService;
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
public class CatalogObjectRevisionController {

    @Autowired
    private CatalogObjectRevisionService catalogObjectRevisionService;

    @ApiOperation(value = "Creates a new catalog object revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid catalog object JSON content supplied") })
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/revisions", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadata create(@PathVariable Long bucketId, @PathVariable Long objectId,
            @ApiParam(value = "Layout describing the tasks position in the CatalogObject Revision") @RequestParam String kind,
            @ApiParam(value = "Layout describing the tasks position in the CatalogObject Revision") @RequestParam String name,
            @ApiParam(value = "Layout describing the tasks position in the CatalogObject Revision") @RequestParam String commitMessage,
            @ApiParam(value = "Layout describing the tasks position in the CatalogObject Revision") @RequestParam(required = false) Optional<String> contentType,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        return catalogObjectRevisionService.createCatalogObjectRevision(bucketId,
                                                                        kind,
                                                                        name,
                                                                        commitMessage,
                                                                        Optional.of(objectId),
                                                                        contentType,
                                                                        file.getBytes());
    }

    @ApiOperation(value = "Gets a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/revisions/{revisionId}", method = GET)
    public ResponseEntity<CatalogObjectMetadata> get(@PathVariable Long bucketId, @PathVariable Long objectId,
            @PathVariable Long revisionId) {
        return catalogObjectRevisionService.getCatalogObject(bucketId, objectId, Optional.ofNullable(revisionId));
    }

    @ApiOperation(value = "Gets the raw content of a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/raw", method = GET)
    public ResponseEntity<InputStreamResource> getRaw(@PathVariable Long bucketId, @PathVariable Long objectId) {
        return catalogObjectRevisionService.getCatalogObjectRaw(bucketId, objectId, Optional.empty());
    }

    @ApiOperation(value = "Gets a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/revisions/{revisionId}/raw", method = GET)
    public ResponseEntity<InputStreamResource> getRaw(@PathVariable Long bucketId, @PathVariable Long objectId,
            @PathVariable Long revisionId) {
        return catalogObjectRevisionService.getCatalogObjectRaw(bucketId, objectId, Optional.ofNullable(revisionId));
    }

    @ApiOperation(value = "Delete a catalog object's revision", notes = "If the revisionId references the latest revision, it is deleted and the catalog object then points to the previous revision. If the revisionId doesn't references the latest revision, it is simply deleted without any impact on the current catalog object. Returns the deleted CatalogObjectRevision metadata.")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or catalog object not found"))
    @RequestMapping(value = "/buckets/{bucketId}/resources/{objectId}/revisions/{revisionId}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long bucketId, @PathVariable Long objectId,
            @PathVariable Long revisionId) {
        return catalogObjectRevisionService.delete(bucketId, objectId, Optional.of(revisionId));
    }

}
