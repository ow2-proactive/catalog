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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.exceptions.BucketNotFoundException;
import org.ow2.proactive.workflow_catalog.rest.service.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRepository;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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
    WorkflowResourceAssembler workflowResourceAssembler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testList() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        when(pagedResourcesAssembler.toResource(any(Page.class),
                any(WorkflowResourceAssembler.class))).thenReturn(mock(PagedResources.class));
        workflowController.list(1L, null, pagedResourcesAssembler);
        verify(workflowService, times(1))
                .listWorkflows(anyLong(), any(Pageable.class), any(PagedResourcesAssembler.class));
    }

    @Ignore
    public void testGet() throws Exception {
        assertTrue(false);
    }
}