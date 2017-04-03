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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.util.ArchiveManagerHelper;


/**
 * @author ActiveEon Team
 */
public class WorkflowServiceTest {

    @InjectMocks
    private WorkflowService workflowService;

    @Mock
    private WorkflowRevisionService workflowRevisionService;

    @Mock
    private ArchiveManagerHelper archiveManagerHelper;

    private final static Long DUMMY_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateWorkflow() {
        workflowService.createWorkflow(DUMMY_ID, Optional.empty(), null);
        verify(workflowRevisionService, times(1)).createWorkflowRevision(DUMMY_ID,
                                                                         Optional.empty(),
                                                                         null,
                                                                         Optional.empty());
    }

    @Test
    public void testGetWorkflowMetadata() {
        workflowService.getWorkflowMetadata(DUMMY_ID, DUMMY_ID, Optional.empty());
        verify(workflowRevisionService, times(1)).getWorkflow(DUMMY_ID, DUMMY_ID, Optional.empty(), Optional.empty());
    }

    @Test
    public void testListWorkflows() throws QueryExpressionBuilderException {
        workflowService.listWorkflows(DUMMY_ID, Optional.empty(), null, null);
        verify(workflowRevisionService, times(1)).listWorkflows(DUMMY_ID,
                                                                Optional.empty(),
                                                                Optional.empty(),
                                                                null,
                                                                null);
    }

    @Test
    public void testDelete() throws Exception {
        workflowService.delete(1L, 2L);
        verify(workflowRevisionService, times(1)).delete(1L, 2L, Optional.empty());
    }

    @Test
    public void testGetWorkflowsAsArchive() throws Exception {
        List<Long> idList = new ArrayList<>();
        workflowService.getWorkflowsAsArchive(1L, idList);
        verify(workflowRevisionService, times(1)).getWorkflowsRevisions(1L, idList);
        verify(archiveManagerHelper, times(1)).compressZIP(Mockito.anyListOf(WorkflowRevision.class));
    }

    @Test
    public void testCreateWorkflows() throws Exception {
        byte[] archive = Files.readAllBytes(Paths.get(WorkflowServiceTest.class.getResource("/archives/archive.zip")
                                                                               .toURI()));
        List<byte[]> workflows = new ArrayList<>(2);
        workflows.add(Files.readAllBytes(Paths.get(WorkflowServiceTest.class.getResource("/archives/workflow_0.xml")
                                                                            .toURI())));
        workflows.add(Files.readAllBytes(Paths.get(WorkflowServiceTest.class.getResource("/archives/workflow_1.xml")
                                                                            .toURI())));
        when(archiveManagerHelper.extractZIP(archive)).thenReturn(workflows);

        workflowService.createWorkflows(1L, Optional.empty(), archive);
        verify(archiveManagerHelper, times(1)).extractZIP(archive);
        verify(workflowRevisionService, times(1)).createWorkflowRevision(1L,
                                                                         Optional.empty(),
                                                                         workflows.get(0),
                                                                         Optional.empty());
        verify(workflowRevisionService, times(1)).createWorkflowRevision(1L,
                                                                         Optional.empty(),
                                                                         workflows.get(1),
                                                                         Optional.empty());
    }
}
