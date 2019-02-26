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
package org.ow2.proactive.catalog.service;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.catalog.service.dependencygraph.DependencyGraphHolder;
import org.ow2.proactive.catalog.service.dependencygraph.GraphNode;

/**
 * @author ActiveEon Team
 * @since 2019-02-25
 */

public class DependencyGraphTest {

     DependencyGraphHolder dependencyGraphHolder = DependencyGraphHolder.getInstance();

    static int a = 10;

    @Test
    public void testAddNodeOK(){
        GraphNode graphNode1 = new GraphNode("bucket1", "wf1");
        GraphNode graphNode2 = new GraphNode("bucket2", "wf2");
        dependencyGraphHolder.addNode(graphNode1);
        dependencyGraphHolder.addNode(graphNode2);
        assertThat(dependencyGraphHolder.size()).isEqualTo(2);
        a++;
        System.out.println("a= " + a);
    }

    @Test
    public void testAddNodeKO(){
        GraphNode graphNode1 = new GraphNode("bucket1", "wf1");
        GraphNode graphNode2 = new GraphNode("bucket1", "wf1");
        dependencyGraphHolder.addNode(graphNode1);
        dependencyGraphHolder.addNode(graphNode2);
        assertThat(dependencyGraphHolder.size()).isEqualTo(1);
        a++;
        System.out.println("a= " + a);

    }

    @Test
    public void testAddEdgeOK(){

        System.out.println(dependencyGraphHolder.toString());
    }
}
