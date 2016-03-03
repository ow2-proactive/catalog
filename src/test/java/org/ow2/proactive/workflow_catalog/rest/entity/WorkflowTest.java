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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author ActiveEon Team
 */
public class WorkflowTest {

    private Bucket bucket;

    private Workflow workflow;

    private WorkflowRevision workflowRevision;

    @Before
    public void setUp() {
        bucket = new Bucket("test");
        workflowRevision =
                new WorkflowRevision(
                        1L, 1L, "Test", "Test Project",
                        LocalDateTime.now(),
                        Lists.<GenericInformation>newArrayList(),
                        Lists.<Variable>newArrayList(),
                        new byte[0]);
        workflow = new Workflow(bucket);
    }

    @Test
    public void testAddRevision() throws Exception {

        assertThat(workflow.getLastRevisionId()).isEqualTo(0L);
        assertThat(workflow.getRevisions()).hasSize(0);

        workflow.addRevision(workflowRevision);

        assertThat(workflow.getLastRevisionId()).isEqualTo(1L);
        assertThat(workflow.getRevisions()).hasSize(1);
    }

    @Test
    public void testSetRevisions() throws Exception {
        List<WorkflowRevision> revisions = ImmutableList.of(workflowRevision);
        workflow.setRevisions(revisions);
        assertEquals(revisions, workflow.getRevisions());
    }

}