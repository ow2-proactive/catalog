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
package org.ow2.proactive.catalog.callgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ow2.proactive.catalog.callgraph.GraphNode;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */
public class GraphNodeTest {

    @Test
    public void testEqualsAndHashCodeOK1() {
        GraphNode graphNode1 = new GraphNode.Builder().bucketName("bucket1").objectName("workflow1").build();

        GraphNode graphNode2 = new GraphNode.Builder().bucketName("bucket1").objectName("workflow1").build();
        assertTrue(graphNode1.equals(graphNode2));
        assertEquals(graphNode1.hashCode(), graphNode2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeKO1() {
        GraphNode graphNode1 = new GraphNode.Builder().bucketName("bucket1").objectName("workflow1").build();

        GraphNode graphNode2 = new GraphNode.Builder().bucketName("bucket2").objectName("workflow1").build();
        assertFalse(graphNode1.equals(graphNode2));
        assertNotEquals(graphNode1.hashCode(), graphNode2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeKO2() {
        GraphNode graphNode1 = new GraphNode.Builder().bucketName("bucket1").objectName("workflow1").build();

        GraphNode graphNode2 = new GraphNode.Builder().bucketName("bucket1").objectName("workflow2").build();
        assertFalse(graphNode1.equals(graphNode2));
        assertNotEquals(graphNode1.hashCode(), graphNode2.hashCode());
    }
}
