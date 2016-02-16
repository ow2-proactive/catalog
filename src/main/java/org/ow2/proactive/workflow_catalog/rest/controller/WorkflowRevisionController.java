/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.controller;

import java.io.IOException;
import java.util.Optional;

import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.query.QueryPredicateBuilderException;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRevisionService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author ActiveEon Team
 */
@RestController
public class WorkflowRevisionController {

    @Autowired
    private WorkflowRevisionService workflowRevisionService;

    @ApiOperation(value = "Creates a new workflow revision")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Bucket not found"),
            @ApiResponse(code = 422, message = "Invalid XML workflow content supplied")
    })
    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowMetadata create(
            @PathVariable Long bucketId,
            @PathVariable Long workflowId,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        return workflowRevisionService.createWorkflowRevision(
                bucketId, Optional.of(workflowId), file.getBytes());
    }

    @ApiOperation(value = "Gets a specific revision")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket, workflow or workflow revision not found"))
    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions/{revisionId}", method = GET)
    public ResponseEntity<?> get(@PathVariable Long bucketId,
            @PathVariable Long workflowId,
            @PathVariable Long revisionId,
            @ApiParam(value = "Force response to return workflow XML content when set to 'xml'.")
            @RequestParam(required = false)
            Optional<String> alt) {
        return workflowRevisionService.getWorkflow(
                bucketId, workflowId, Optional.ofNullable(revisionId), alt);
    }

    @ApiOperation(value = "Lists a workflow's revisions")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. " +
                            "Multiple sort criteria are supported.")
    })
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or workflow not found"))
    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions", method = GET)
    public PagedResources list(@PathVariable Long bucketId,
            @PathVariable Long workflowId,
            @ApiParam("Query string for searching workflows. See <a href=\"http://doc.activeeon.com/latest/user/ProActiveUserGuide.html#_searching_for_workflows\">Searching for workflows</a> for more information about supported fields and operations.")
            @RequestParam(required = false)
            Optional<String> query,
            Pageable pageable,
            PagedResourcesAssembler assembler) throws QueryPredicateBuilderException {
        return workflowRevisionService.listWorkflows(
                bucketId, Optional.ofNullable(workflowId), query, pageable, assembler);
    }

}
