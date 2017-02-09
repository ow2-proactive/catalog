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
package org.ow2.proactive.workflow_catalog.rest.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.util.ArchiveManagerHelper;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * @author ActiveEon Team
 */
@Service
public class WorkflowService {

    @Autowired
    private WorkflowRevisionService workflowRevisionService;

    @Autowired
    private ArchiveManagerHelper archiveManagerHelper;

    public WorkflowMetadata createWorkflow(Long bucketId, ProActiveWorkflowParserResult proActiveWorkflowParserResult,
            byte[] proActiveWorkflowXmlContent) {
        return workflowRevisionService.createWorkflowRevision(bucketId,
                                                              Optional.empty(),
                                                              proActiveWorkflowParserResult,
                                                              Optional.empty(),
                                                              proActiveWorkflowXmlContent);
    }

    public WorkflowMetadata createWorkflow(Long bucketId, Optional<String> layout, byte[] proActiveWorkflowXmlContent) {
        return workflowRevisionService.createWorkflowRevision(bucketId,
                                                              Optional.empty(),
                                                              proActiveWorkflowXmlContent,
                                                              layout);
    }

    public List<WorkflowMetadata> createWorkflows(Long bucketId, Optional<String> layout,
            byte[] proActiveWorkflowsArchive) {

        List<byte[]> extractedWorkflows = archiveManagerHelper.extractZIP(proActiveWorkflowsArchive);
        if (extractedWorkflows.isEmpty()) {
            throw new UnprocessableEntityException("Malformed archive");
        } else {
            return extractedWorkflows.stream()
                                     .map(workflowFile -> createWorkflow(bucketId, layout, workflowFile))
                                     .collect(Collectors.toList());
        }
    }

    public ResponseEntity<?> getWorkflowMetadata(long bucketId, long workflowId, Optional<String> alt) {
        return workflowRevisionService.getWorkflow(bucketId, workflowId, Optional.empty(), alt);
    }

    public PagedResources listWorkflows(Long bucketId, Optional<String> query, Pageable pageable,
            PagedResourcesAssembler assembler) throws QueryExpressionBuilderException {
        return workflowRevisionService.listWorkflows(bucketId, Optional.empty(), query, pageable, assembler);
    }

    public ResponseEntity<?> delete(Long bucketId, Long workflowId) {
        return workflowRevisionService.delete(bucketId, workflowId, Optional.empty());
    }

    public byte[] getWorkflowsAsArchive(Long bucketId, List<Long> idList) {
        return archiveManagerHelper.compressZIP(workflowRevisionService.getWorkflowsRevisions(bucketId, idList));
    }

}
