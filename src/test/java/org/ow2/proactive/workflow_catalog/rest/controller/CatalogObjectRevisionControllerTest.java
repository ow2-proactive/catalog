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
package org.ow2.proactive.workflow_catalog.rest.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRevisionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectRevisionControllerTest {

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
        workflowRevisionController.create(BUCKET_ID, WF_ID, Optional.empty(), mockedFile);
        verify(workflowRevisionService, times(1)).createWorkflowRevision(BUCKET_ID,
                                                                         Optional.of(WF_ID),
                                                                         null,
                                                                         Optional.empty());
    }

    @Test
    public void testList() throws Exception {
        Pageable mockedPageable = mock(Pageable.class);
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        workflowRevisionController.list(BUCKET_ID, WF_ID, Optional.empty(), mockedPageable, mockedAssembler);
        verify(workflowRevisionService, times(1)).listWorkflows(BUCKET_ID,
                                                                Optional.of(WF_ID),
                                                                Optional.empty(),
                                                                mockedPageable,
                                                                mockedAssembler);
    }

    @Test
    public void testGet() throws Exception {
        workflowRevisionController.get(BUCKET_ID, WF_ID, REV_ID, Optional.empty());
        verify(workflowRevisionService, times(1)).getWorkflow(BUCKET_ID, WF_ID, Optional.of(REV_ID), Optional.empty());
    }

    @Test
    public void testDelete() throws Exception {
        workflowRevisionController.delete(BUCKET_ID, WF_ID, REV_ID);
        verify(workflowRevisionService, times(1)).delete(BUCKET_ID, WF_ID, Optional.of(REV_ID));
    }
}
