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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.CatalogObjectCallGraphService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.model.RestApiAccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */
@RestController
@Log4j2
@RequestMapping(value = "/buckets/call-graph")
public class CatalogObjectCallGraphController {

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectCallGraphService catalogObjectCallGraphService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @ApiOperation(value = "Get the call graph of all catalog objects in a JPG image")
    @ApiResponses(value = { @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied"), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void getCallGraph(HttpServletResponse response,
            @ApiParam(value = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @ApiParam(value = "The kind of objects that buckets must contain") @RequestParam(value = "kind", required = false) Optional<String> kind,
            @ApiParam(value = "The content type of objects that buckets must contain") @RequestParam(value = "contentType", required = false) Optional<String> contentType)
            throws NotAuthenticatedException, AccessDeniedException, IOException {

        List<String> authorisedBucketsNames = getListOfAuthorizedBuckets(sessionId, ownerName, kind, contentType);

        byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(authorisedBucketsNames,
                                                                              kind,
                                                                              contentType);

        flushResponse(response, content);

    }

    @ApiOperation(value = "Get the call graph of selected catalog objects in a bucket")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 401, message = "User not authenticated"),
                            @ApiResponse(code = 403, message = "Permission denied") })
    @RequestMapping(value = "/selected/{bucketName}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public void getCallGraphForSelectedObjects(HttpServletResponse response,
            @ApiParam(value = "sessionID") @RequestHeader(value = "sessionID", required = false) String sessionId,
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @PathVariable String bucketName,
            @ApiParam(value = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind,
            @ApiParam(value = "Filter according to content type.") @RequestParam(required = false) Optional<String> contentType,
            @ApiParam(value = "Give a list of name separated by comma to get them in the report", allowMultiple = true, type = "string") @RequestParam(value = "name", required = false) Optional<List<String>> catalogObjectsNames)
            throws NotAuthenticatedException, AccessDeniedException, IOException {

        restApiAccessService.checkAccessBySessionIdForBucketAndThrowIfDeclined(sessionIdRequired,
                                                                               sessionId,
                                                                               bucketName);

        if (catalogObjectsNames.isPresent()) {

            byte[] content = catalogObjectCallGraphService.generateBytesCallGraphForSelectedObjects(bucketName,
                                                                                                    catalogObjectsNames.get(),
                                                                                                    kind,
                                                                                                    contentType);
            flushResponse(response, content);

        } else {

            byte[] content = catalogObjectCallGraphService.generateBytesCallGraph(Collections.singletonList(bucketName),
                                                                                  kind,
                                                                                  contentType);
            flushResponse(response, content);

        }

    }

    private void flushResponse(HttpServletResponse response, byte[] content) throws IOException {
        response.addHeader("Content-size", Integer.toString(content.length));
        response.setCharacterEncoding("UTF-8");

        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    private List<String> getListOfAuthorizedBuckets(String sessionId, String ownerName, Optional<String> kind,
            Optional<String> contentType) throws NotAuthenticatedException, AccessDeniedException {
        List<BucketMetadata> authorisedBuckets;
        if (sessionIdRequired) {
            RestApiAccessResponse restApiAccessResponse = restApiAccessService.checkAccessBySessionIdForOwnerOrGroupAndThrowIfDeclined(sessionId,
                                                                                                                                       ownerName);

            authorisedBuckets = bucketService.getBucketsByGroups(ownerName,
                                                                 kind,
                                                                 contentType,
                                                                 () -> restApiAccessResponse.getAuthenticatedUser()
                                                                                            .getGroups());

        } else {
            authorisedBuckets = bucketService.listBuckets(ownerName, kind, contentType);
        }

        return authorisedBuckets.stream().map(BucketMetadata::getName).collect(Collectors.toList());
    }

}
