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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.validation.Valid;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.rest.controller.validator.CatalogObjectNameEncodingValidator;
import org.ow2.proactive.catalog.rest.controller.validator.CatalogObjectNamePathParam;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/buckets/{bucketId}/resources/{name}/revisions")
@Log4j2
public class CatalogObjectRevisionController {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private CatalogObjectNameEncodingValidator validator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(validator);
    }

    @ApiOperation(value = "Creates a new catalog object revision")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid catalog object JSON content supplied") })
    @RequestMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadata create(@PathVariable Long bucketId, @PathVariable String name,
            @ApiParam(value = "The commit message of the CatalogRawObject Revision") @RequestParam String commitMessage,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        return catalogObjectService.createCatalogObjectRevision(bucketId, name, commitMessage, file.getBytes());
    }

    @ApiOperation(value = "Gets a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/{commitTime}", method = GET)
    public ResponseEntity<CatalogObjectMetadata> get(@PathVariable Long bucketId,
            @PathVariable @Valid CatalogObjectNamePathParam name, @PathVariable long commitTime)
            throws UnsupportedEncodingException {
        try {
            String decodedName = URLDecoder.decode(name.getName(), "UTF-8");
            CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectRevision(bucketId,
                                                                                           decodedName,
                                                                                           commitTime);
            return ResponseEntity.ok(metadata);
        } catch (RevisionNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Gets the raw content of a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, catalog object or catalog object revision not found"))
    @RequestMapping(value = "/{commitTime}/raw", method = GET)
    public ResponseEntity<InputStreamResource> getRaw(@PathVariable Long bucketId,
            @PathVariable @Valid CatalogObjectNamePathParam name, @PathVariable long commitTime)
            throws UnsupportedEncodingException {

        String decodedName = URLDecoder.decode(name.getName(), "UTF-8");
        CatalogRawObject objectRevisionRaw = catalogObjectService.getCatalogObjectRevisionRaw(bucketId,
                                                                                              decodedName,
                                                                                              commitTime);

        byte[] bytes = objectRevisionRaw.getRawObject();

        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok().contentLength(bytes.length);

        try {
            MediaType mediaType = MediaType.valueOf(objectRevisionRaw.getContentType());
            responseBodyBuilder = responseBodyBuilder.contentType(mediaType);
        } catch (org.springframework.http.InvalidMediaTypeException mimeEx) {
            log.warn("The wrong content type for object: " + decodedName + ", revisionId:" + commitTime +
                     ", the contentType: " + objectRevisionRaw.getContentType(), mimeEx);
            mimeEx.printStackTrace();
        }
        return responseBodyBuilder.body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    @ApiOperation(value = "Lists a catalog object revisions")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or catalog object not found"))
    @RequestMapping(method = GET)
    public ResponseEntity<List<CatalogObjectMetadata>> list(@PathVariable Long bucketId,
            @PathVariable @Valid CatalogObjectNamePathParam name) throws UnsupportedEncodingException {
        String decodedName = URLDecoder.decode(name.getName(), "UTF-8");
        return ResponseEntity.ok(catalogObjectService.listCatalogObjectRevisions(bucketId, decodedName));
    }

}
