/*
 *  ProActive Parallel Suite(TM): The Java(TM) library for
 *     Parallel, Distributed, Multi-Core Computing for
 *     Enterprise Grids & Clouds
 *
 *  Copyright (C) 1997-2016 INRIA/University of
 *                  Nice-Sophia Antipolis/ActiveEon
 *  Contact: proactive@ow2.org or contact@activeeon.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; version 3 of
 *  the License.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *  USA
 *
 *  If needed, contact us to obtain a release under GPL Version 2 or 3
 *  or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                          http://proactive.inria.fr/team_members.htm
 */

package org.ow2.proactive.workflow_catalog.rest.entity;

import java.time.LocalDateTime;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author ActiveEon Team
 */
public class WorkflowRevisionTest {

    @Test
    public void testAddGenericInformation() throws Exception {
        WorkflowRevision workflowRevision = createWorkflowRevision();

        assertThat(workflowRevision.getGenericInformation()).hasSize(1);

        workflowRevision.addGenericInformation(Mockito.mock(GenericInformation.class));

        assertThat(workflowRevision.getGenericInformation()).hasSize(2);
    }

    @Test
    public void testAddVariable() throws Exception {
        WorkflowRevision workflowRevision = createWorkflowRevision();

        assertThat(workflowRevision.getVariables()).hasSize(1);

        workflowRevision.addVariable(Mockito.mock(Variable.class));

        assertThat(workflowRevision.getVariables()).hasSize(2);
    }

    private WorkflowRevision createWorkflowRevision() {
        GenericInformation genericInformationMock = Mockito.mock(GenericInformation.class);
        Variable variableMock = Mockito.mock(Variable.class);

        WorkflowRevision workflowRevision = new WorkflowRevision(
                1L, 1L, "test", "test", LocalDateTime.now(),
                null, Lists.newArrayList(genericInformationMock), Lists.newArrayList(variableMock), new byte[0]);

        return workflowRevision;
    }

}