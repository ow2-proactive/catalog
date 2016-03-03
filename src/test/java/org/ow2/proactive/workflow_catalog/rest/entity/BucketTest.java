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
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author ActiveEon Team
 */
public class BucketTest {

    private final String DEFAULT_BUCKET_NAME = "test";
    private Bucket bucket;

    @Before
    public void setUp() {
        bucket = new Bucket(DEFAULT_BUCKET_NAME);
    }

    @Test
    public void testAddWorkflow() throws Exception {
        Workflow workflow = new Workflow(bucket);

        bucket.addWorkflow(workflow);

        assertThat(bucket.getWorkflows()).hasSize(1);
        assertThat(workflow.getBucket()).isEqualTo(bucket);
    }

    @Test
    public void testSetName() {
        final String expectedName = "EXPECTED_BUCKET_NAME";
        bucket.setName(expectedName);
        assertEquals(expectedName, bucket.name);
    }

    @Test
    public void testSetWorkflows() throws Exception {
        List<Workflow> workflowList = ImmutableList.of();
        bucket.setWorkflows(workflowList);
        assertEquals(workflowList, bucket.getWorkflows());
    }


    @Test
    public void testGetId() throws Exception {
        Long expectedId = 42L;
        bucket.id = expectedId;
        assertEquals(expectedId, bucket.getId());
    }

    @Test
    public void testGetCreatedAt() throws Exception {
        LocalDateTime expectedDate = LocalDateTime.now();
        bucket.createdAt = expectedDate;
        assertEquals(expectedDate, bucket.getCreatedAt());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(DEFAULT_BUCKET_NAME, bucket.getName());
    }

    @Test
    public void testGetWorkflows() throws Exception {
        List<Workflow> workflowList = ImmutableList.of();
        bucket.setWorkflows(workflowList);
        assertEquals(workflowList, bucket.getWorkflows());
    }
}