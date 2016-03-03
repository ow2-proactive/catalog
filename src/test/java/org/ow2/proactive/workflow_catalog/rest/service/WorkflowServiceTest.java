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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;

import java.util.*;
import static org.mockito.Mockito.*;

/**
 * @author ActiveEon Team
 */
public class WorkflowServiceTest {

    @InjectMocks
    private WorkflowService workflowService;

    @Mock
    private WorkflowRevisionService workflowRevisionService;

    private final static Long DUMMY_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateWorkflow() {
        workflowService.createWorkflow(DUMMY_ID, null);
        verify(workflowRevisionService, times(1)).createWorkflowRevision(DUMMY_ID, Optional.empty(), null);
    }

    @Test
    public void testGetWorkflowMetadata() {
        workflowService.getWorkflowMetadata(DUMMY_ID, DUMMY_ID, Optional.empty());
        verify(workflowRevisionService, times(1)).getWorkflow(DUMMY_ID, DUMMY_ID, Optional.empty(), Optional.empty());
    }

    @Test
    public void testListWorkflows() throws QueryExpressionBuilderException {
        workflowService.listWorkflows(DUMMY_ID, Optional.empty(), null, null);
        verify(workflowRevisionService, times(1)).listWorkflows(DUMMY_ID, Optional.empty(), Optional.empty(), null, null);
    }

}
