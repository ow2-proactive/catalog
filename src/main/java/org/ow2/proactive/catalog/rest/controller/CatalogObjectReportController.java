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

import static org.ow2.proactive.catalog.util.ReportHelper.getListOfAuthorizedBuckets;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.ow2.proactive.catalog.util.ReportHelper;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@RestController
@Log4j2
@RequestMapping(value = "/buckets/report")
public class CatalogObjectReportController {

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectReportService catalogObjectReportService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private GrantRightsService grantRightsService;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private BucketGrantService bucketGrantService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @Operation(summary = "Get a ZIP file containing the PDF reports for each catalog objects matching the provided filters")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "byte"))),
                            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                            @ApiResponse(responseCode = "403", description = "Permission denied", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)), })
    @RequestMapping(method = GET)
    @ResponseStatus(HttpStatus.OK)
    @Produces({ MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public void getReport(HttpServletResponse response,
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) String ownerName,
            @Parameter(description = "The kind of objects that buckets must contain") @RequestParam(value = "kind", required = false) Optional<String> kind,
            @Parameter(description = "The Content-Type of objects that buckets must contain") @RequestParam(value = "contentType", required = false) Optional<String> contentType,
            @Parameter(description = "The project name of objects containing this name") @RequestParam(value = "projectName", required = false) Optional<String> projectName,
            @Parameter(description = "The object name of objects containing this name") @RequestParam(value = "objectName", required = false) Optional<String> objectName,
            @Parameter(description = "The bucket name of catalog objects") @RequestParam(value = "bucketName", required = false) Optional<String> bucketName,
            @Parameter(description = "The tag of catalog objects") @RequestParam(value = "tag", required = false) Optional<String> tag,
            @Parameter(description = "The user who last committed the catalog object") @RequestParam(value = "lastCommitBy", required = false) Optional<String> lastCommitBy,
            @Parameter(description = "The user who committed at least once in the catalog object") @RequestParam(value = "committedAtLeastOnceBy", required = false) Optional<String> committedAtLeastOnceBy,
            @Parameter(description = "The maximum time the object was last committed") @RequestParam(value = "lastCommitTimeLessThan", required = false) Optional<Long> lastCommitTimeLessThan,
            @Parameter(description = "The minimum time the object was last committed") @RequestParam(value = "lastCommitTimeGreater", required = false) Optional<Long> lastCommitTimeGreaterThan)
            throws NotAuthenticatedException, AccessDeniedException, IOException {

        List<String> authorisedBucketsNames = getListOfAuthorizedBuckets(bucketGrantService,
                                                                         bucketService,
                                                                         catalogObjectGrantService,
                                                                         grantRightsService,
                                                                         restApiAccessService,
                                                                         sessionIdRequired,
                                                                         sessionId,
                                                                         ownerName,
                                                                         kind,
                                                                         contentType);
        bucketName.ifPresent(bucketNameFilter -> authorisedBucketsNames.removeIf(bName -> !bName.contains(bucketNameFilter)));

        byte[] content = catalogObjectReportService.generateBytesReportZip(authorisedBucketsNames,
                                                                           kind,
                                                                           contentType,
                                                                           objectName,
                                                                           tag,
                                                                           projectName,
                                                                           lastCommitBy,
                                                                           committedAtLeastOnceBy,
                                                                           lastCommitTimeGreaterThan,
                                                                           lastCommitTimeLessThan);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(ZIP_CONTENT_TYPE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"catalog_report.zip\"");
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    @Operation(summary = "Get a PDF report file for the selected catalog items")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "byte"))),
                            @ApiResponse(responseCode = "404", description = "Bucket not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                            @ApiResponse(responseCode = "403", description = "Permission denied", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @RequestMapping(value = "/selected/{bucketName}", method = POST)
    @ResponseStatus(HttpStatus.OK)
    @Produces({ MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public void getReportForSelectedObjects(HttpServletResponse response,
            @Parameter(description = "sessionID") @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName,
            @Parameter(description = "Filter according to kind.") @RequestParam(required = false) Optional<String> kind,
            @Parameter(description = "Filter according to Content-Type.") @RequestParam(required = false) Optional<String> contentType,
            @Parameter(description = "Give a list of name separated by comma to get them in the report") @RequestBody Optional<List<String>> catalogObjectsNames)
            throws NotAuthenticatedException, AccessDeniedException, IOException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            BucketMetadata bucket = bucketService.getBucketMetadata(bucketName);
            if (!grantRightsService.isBucketAccessible(user, bucket)) {
                throw new BucketGrantAccessException(bucketName);
            }

        }

        byte[] content;

        if (catalogObjectsNames.isPresent()) {
            content = catalogObjectReportService.generateBytesReportForSelectedObjects(bucketName,
                                                                                       catalogObjectsNames.get(),
                                                                                       kind,
                                                                                       contentType);
        } else {
            content = catalogObjectReportService.generateBytesReport(Collections.singletonList(bucketName),
                                                                     kind,
                                                                     contentType);
        }
        flushResponse(response, content);
    }

    private void flushResponse(HttpServletResponse response, byte[] content) throws IOException {
        ReportHelper.flushResponse(response, content, "report.pdf");
    }
}
