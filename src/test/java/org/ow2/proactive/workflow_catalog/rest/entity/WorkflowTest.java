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
package org.ow2.proactive.workflow_catalog.rest.entity;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
public class WorkflowTest {

    private Bucket bucket;

    private Workflow workflow;

    private WorkflowRevision workflowRevision;

    @Before
    public void setUp() {
        bucket = new Bucket("test", "WorkflowTestUser");
        workflowRevision = newWorkflowRevision(1L, LocalDateTime.now());
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
        SortedSet<WorkflowRevision> revisions = ImmutableSortedSet.of(workflowRevision);
        workflow.setRevisions(revisions);
        assertEquals(revisions, workflow.getRevisions());
    }

    @Test
    public void testGetRevisions() throws Exception {
        SortedSet<WorkflowRevision> revisions = new TreeSet<>();
        revisions.add(workflowRevision);
        revisions.add(newWorkflowRevision(10L, LocalDateTime.now().plusHours(1)));
        revisions.add(newWorkflowRevision(2L, LocalDateTime.now().plusHours(2)));
        workflow.setRevisions(revisions);
        assertEquals(revisions, workflow.getRevisions());
        Iterator iterator = workflow.getRevisions().iterator();
        assertEquals(2L, ((WorkflowRevision) iterator.next()).getRevisionId().longValue());
        assertEquals(10L, ((WorkflowRevision) iterator.next()).getRevisionId().longValue());
        assertEquals(1L, ((WorkflowRevision) iterator.next()).getRevisionId().longValue());
    }

    private WorkflowRevision newWorkflowRevision(Long revisionId, LocalDateTime date) {
        return new WorkflowRevision(1L,
                                    revisionId,
                                    "Test",
                                    "Test Project",
                                    date,
                                    null,
                                    Lists.<GenericInformation> newArrayList(),
                                    Lists.<Variable> newArrayList(),
                                    new byte[0]);
    }
}
