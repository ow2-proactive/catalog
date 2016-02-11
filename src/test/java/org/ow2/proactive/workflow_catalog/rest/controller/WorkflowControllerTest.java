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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * @author ActiveEon Team
 */
public class WorkflowControllerTest {

    @InjectMocks
    private WorkflowController workflowController;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Mock
    WorkflowRevisionResourceAssembler workflowResourceAssembler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(null);
        workflowController.create(1L, file);
        verify(workflowService, times(1)).createWorkflow(1L, null);
    }

    @Test
    public void testList() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        when(pagedResourcesAssembler.toResource(any(Page.class),
                any(WorkflowRevisionResourceAssembler.class))).thenReturn(mock(PagedResources.class));
        workflowController.list(1L, Optional.empty(), null, pagedResourcesAssembler);
        verify(workflowService, times(1))
                .listWorkflows(anyLong(), any(Optional.class), any(Pageable.class), any(PagedResourcesAssembler.class));
    }

    @Test
    public void testGet() throws Exception {
        workflowController.get(1L, 2L, Optional.empty());
        verify(workflowService, times(1)).getWorkflowMetadata(1L, 2L, Optional.empty());
    }

}