/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
package org.ow2.proactive.workflow_catalog.rest.service;

import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author ActiveEon Team
 */
@Service
public class WorkflowService {

    @Autowired
    private WorkflowRevisionService workflowRevisionService;

    public WorkflowMetadata createWorkflow(Long bucketId, ProActiveWorkflowParserResult proActiveWorkflowParserResult,
                                           byte[] proActiveWorkflowXmlContent) {
        return workflowRevisionService.createWorkflowRevision(bucketId, Optional.empty(),
                proActiveWorkflowParserResult, proActiveWorkflowXmlContent);
    }

    public WorkflowMetadata createWorkflow(Long bucketId, byte[] proActiveWorkflowXmlContent) {
        return workflowRevisionService.createWorkflowRevision(bucketId, Optional.empty(), proActiveWorkflowXmlContent);
    }

    public ResponseEntity<?> getWorkflowMetadata(long bucketId, long workflowId, Optional<String> alt) {
        return workflowRevisionService.getWorkflow(bucketId, workflowId, Optional.empty(), alt);
    }

    public PagedResources listWorkflows(Long bucketId, Optional<String> query, Pageable pageable, PagedResourcesAssembler assembler) throws QueryExpressionBuilderException {
        return workflowRevisionService.listWorkflows(bucketId, Optional.empty(), query, pageable, assembler);
    }

}
