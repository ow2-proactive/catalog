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

import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


/**
 * @author ActiveEon Team
 * @since 2019-03-22
 */
public class CallGraphHolder {

    private CallGraphHolder() {
    }

    private static final CallGraphHolder INSTANCE = new CallGraphHolder();

    private final DefaultDirectedGraph<GraphNode, DefaultEdge> dependencyGraph = new DefaultDirectedGraph(DefaultEdge.class);

    public static CallGraphHolder getInstance() {
        return INSTANCE;
    }

    public GraphNode addNode(String bucketName, String objectName) {
        GraphNode graphNode = new GraphNode.Builder().bucketName(bucketName).objectName(objectName).build();
        dependencyGraph.addVertex(graphNode);
        return graphNode;

    }

    public boolean removeNode(String bucketName, String objectName) {
        return dependencyGraph.removeVertex(new GraphNode.Builder().bucketName(bucketName)
                                                                   .objectName(objectName)
                                                                   .build());
    }

    public DefaultEdge addDependsOnDependency(GraphNode graphNode1, GraphNode graphNode2) {
        return dependencyGraph.addEdge(graphNode1, graphNode2);
    }

    public DefaultEdge removeDependsOnEdge(GraphNode graphNode1, GraphNode graphNode2) {
        return dependencyGraph.removeEdge(graphNode1, graphNode2);
    }

    public Set<GraphNode> NodeSet() {
        return dependencyGraph.vertexSet();
    }

    public Set<DefaultEdge> DependencySet() {
        return dependencyGraph.edgeSet();
    }

    /**
     * The number of vertices, the cardinality of V, is called the order of graph and devoted by |V|. We usually use n to denote the order of G.
     * @return
     */
    public int order() {
        return dependencyGraph.vertexSet().size();
    }

    /**
     * The number of edges, the cardinality of E, is called the size of graph and denoted by |E|. We usually use m to denote the size of G.
     * @return
     */
    public int size() {
        return dependencyGraph.edgeSet().size();
    }

    public DefaultDirectedGraph<GraphNode, DefaultEdge> getDependencyGraph() {
        return dependencyGraph;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Dependency Graph=").append(dependencyGraph);
        return sb.toString();
    }
}
