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
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRevisionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author ActiveEon Team
 */
public class WorkflowRevisionControllerTest {

    @InjectMocks
    private WorkflowRevisionController workflowRevisionController;

    @Mock
    private WorkflowRevisionService workflowRevisionService;

    private static final Long BUCKET_ID = 1L;
    private static final Long WF_ID = 2L;
    private static final Long REV_ID = 3L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        MultipartFile mockedFile = mock(MultipartFile.class);
        when(mockedFile.getBytes()).thenReturn(null);
        workflowRevisionController.create(BUCKET_ID, WF_ID, mockedFile);
        verify(workflowRevisionService, times(1))
                .createWorkflowRevision(BUCKET_ID, Optional.of(WF_ID), null);
    }

    @Test
    public void testList() throws Exception {
        Pageable mockedPageable = mock(Pageable.class);
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        workflowRevisionController.list(BUCKET_ID, WF_ID, Optional.empty(), mockedPageable, mockedAssembler);
        verify(workflowRevisionService, times(1))
                .listWorkflows(BUCKET_ID, Optional.of(WF_ID), Optional.empty(), mockedPageable, mockedAssembler);
    }

    @Test
    public void testGet() throws Exception {
        workflowRevisionController.get(BUCKET_ID, WF_ID, REV_ID, Optional.empty());
        verify(workflowRevisionService, times(1))
                .getWorkflow(BUCKET_ID, WF_ID, Optional.of(REV_ID), Optional.empty());
    }
}