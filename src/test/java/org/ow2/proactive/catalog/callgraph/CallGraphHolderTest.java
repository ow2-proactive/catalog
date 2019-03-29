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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.IntStream;

import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 2019-03-26
 */
public class CallGraphHolderTest {

    CallGraphHolder callGraphHolder = CallGraphHolder.getInstance();

    @Test
    public void testCallGraphHolder() {

        //TEST1: Adding/Removing Nodes

        //Adding three nodes
        GraphNode graphNode1 = callGraphHolder.addNode("bucket1", "workflow1");
        GraphNode graphNode2 = callGraphHolder.addNode("bucket2", "workflow2");
        GraphNode graphNode3 = callGraphHolder.addNode("bucket1", "workflow2");
        assertEquals(3, callGraphHolder.order());

        //Checking that we cannot add a node having same bucket name and workflow name as an existing node and therefore the order of the graph remains the same.
        GraphNode graphNode22 = callGraphHolder.addNode("bucket2", "workflow2");
        assertTrue(callGraphHolder.NodeSet().contains(graphNode22));
        assertEquals(3, callGraphHolder.order());

        //Adding 10 more nodes
        IntStream.range(0, 10).forEach(nbr -> callGraphHolder.addNode(String.valueOf(nbr), String.valueOf(nbr)));
        assertEquals(13, callGraphHolder.order());

        //Removing those nodes and check that the order of the graph is correct
        IntStream.range(0, 10).forEach(nbr -> callGraphHolder.removeNode(String.valueOf(nbr), String.valueOf(nbr)));
        assertEquals(3, callGraphHolder.order());

        //Removing a non existing node
        assertFalse(callGraphHolder.removeNode("nonexistingbucket", "nonexistingworkflow"));
        assertEquals(3, callGraphHolder.order());

        //TEST2: Adding/Removing Edges

        //Adding three edges
        callGraphHolder.addDependsOnDependency(graphNode1, graphNode2);
        callGraphHolder.addDependsOnDependency(graphNode1, graphNode3);
        callGraphHolder.addDependsOnDependency(graphNode2, graphNode3);
        assertEquals(3, callGraphHolder.size());

        //Checking that we cannot add an edge where one of its node is not in the graph and therefore the the size of the graph remains the same.
        DefaultEdge defaultEdge = callGraphHolder.addDependsOnDependency(graphNode1, graphNode22);
        assertThat(defaultEdge).isEqualTo(null);
        assertEquals(3, callGraphHolder.size());

        //Checking that we cannot have duplicated directed edges between two nodes.
        callGraphHolder.addDependsOnDependency(graphNode1, graphNode2);
        assertEquals(3, callGraphHolder.size());

        //Removing an existing edge
        DefaultEdge defaultEdge1 = callGraphHolder.removeDependsOnEdge(graphNode1, graphNode2);
        assertEquals(2, callGraphHolder.size());

        //Removing a non existing edge
        assertFalse(callGraphHolder.DependencySet().contains(defaultEdge1));
        assertThat(callGraphHolder.removeDependsOnEdge(graphNode1, graphNode2)).isEqualTo(null);
        assertEquals(2, callGraphHolder.size());

        //TEST3: Removing Nodes and check the consistency of the Edges

        callGraphHolder.removeNode("bucket1", "workflow1");
        assertEquals(1, callGraphHolder.size());

    }
}
