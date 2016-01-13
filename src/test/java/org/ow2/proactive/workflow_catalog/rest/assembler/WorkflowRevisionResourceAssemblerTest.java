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
package org.ow2.proactive.workflow_catalog.rest.assembler;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Workflow;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ActiveEon Team
 */
public class WorkflowRevisionResourceAssemblerTest {

    @InjectMocks
    private WorkflowRevisionResourceAssembler workflowRevisionResourceAssembler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testToResource() throws Exception {
        WorkflowRevision workflowRevision = new WorkflowRevision(1L, 1L, "WR-TEST", "WR-PROJ-TEST",
                LocalDateTime.now(), Lists.newArrayList(), Lists.newArrayList(),
                getWorkflowAsByteArray("workflow.xml"));
        Workflow mockedWorkflow = mock(Workflow.class);
        when(mockedWorkflow.getId()).thenReturn(1L);
        workflowRevision.setWorkflow(mockedWorkflow);
        WorkflowMetadata workflowMetadata = workflowRevisionResourceAssembler.toResource(workflowRevision);
        assertEquals(workflowRevision.getName(), workflowMetadata.name);
        assertEquals(workflowRevision.getProjectName(), workflowMetadata.projectName);
    }

    private static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        return ByteStreams.toByteArray(
                new FileInputStream(new File(WorkflowRevisionResourceAssemblerTest
                        .class.getResource("/workflows/" + filename).getFile())));
    }
}