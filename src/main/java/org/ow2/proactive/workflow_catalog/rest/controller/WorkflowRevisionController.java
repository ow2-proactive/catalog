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

import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowParser;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowParser workflowParser;

    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions", method = POST)
    public WorkflowMetadata create(
            @PathVariable Long bucketId,
            @PathVariable Long workflowId,
            @RequestParam(value = "file") MultipartFile file) {
        return null;
    }

    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions", method = GET)
    public Page<WorkflowMetadata> list(@PathVariable Long bucketId,
                                       @PathVariable Long workflowId,
                                       Pageable pageable,
                                       PagedResourcesAssembler assembler) {
        return null;
    }

    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}/revisions/{revisionId}", method = GET)
    public WorkflowMetadata getMetadata(@PathVariable Long bucketId,
                                        @PathVariable Long workflowId,
                                        @PathVariable Long revisionId,
                                        @RequestParam(required = false) String alt,
                                        Pageable pageable,
                                        PagedResourcesAssembler assembler) {
        return null;
    }

}
