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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.apache.pdfbox.pdmodel.PDPage;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.Cycles;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.ow2.proactive.catalog.report.CellFactory;
import org.ow2.proactive.catalog.report.TableDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.image.Image;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 2019-05-15
 */

@Component
@Log4j2
public class TableCallGraphsBuilder {

    private static final String MISSING_CATALOG_OBJECT_STYLE = "fillColor=#C0C0C0;strokeColor=#FF0000;fontSize=8";

    private static final int MAX_DIAMETER = 3;

    @Autowired
    private CellFactory cellFactory;

    /**
     * This method builds a table of call Graphs which are ordered and grouped by bucket name. The build is composed of three steps
     * 1. Compute all graph paths of the callGraphHolder. A graph path is a chain of dependencies starting from a root until a leaf
     * 2. Grouping all graph paths having the same root in a single graph
     * 3. Compute call graphs for all roots
     * In case the oder of the callGraphHolder is zero, an appropriate message is displayed.
     *
     * @param callGraphHolder
     * @param table
     */
    public void buildCallGraphsTable(CallGraphHolder callGraphHolder, BaseTable table) {

        if (callGraphHolder.order() == 0) {
            Row<PDPage> dataRow = table.createRow(10f);
            cellFactory.createDataHeaderCell(dataRow,
                                             100,
                                             "No identified Dependencies in the Catalog or among the selected Catalog Objects");
        } else {

            // We first compute all graph paths of the callGraphHolder. A graph path is a chain of dependencies starting from a root until a leaf
            List<GraphPath<GraphNode, DefaultEdge>> graphPathList = computeAllGraphPaths(callGraphHolder);

            //Grouping all graph paths having the same root in a single graph
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupedGraphPathsHavingSameRoot = graphPathList.stream()
                                                                                                                   .collect(Collectors.groupingBy(GraphPath::getStartVertex,
                                                                                                                                                  Collectors.toList()));
            // Compute call graphs for all roots
            Map<GraphNode, Graph<GraphNode, DefaultEdge>> callGraphHashMap = computeCallGraphForAllRoots(groupedGraphPathsHavingSameRoot,
                                                                                                         callGraphHolder);
            Map<GraphNode, Integer> callGraphDiameterHashMap = computeCallGraphsDiameter(groupedGraphPathsHavingSameRoot);
            TreeMap<GraphNode, Graph<GraphNode, DefaultEdge>> orderedCallGraphsPerBucket = sortCallGraphsPerBucket(callGraphHashMap);
            String currentBucketName = "";

            for (Map.Entry<GraphNode, Graph<GraphNode, DefaultEdge>> mapEntry : orderedCallGraphsPerBucket.entrySet()) {

                if (!currentBucketName.equals(mapEntry.getKey().getBucketName())) {
                    currentBucketName = mapEntry.getKey().getBucketName();
                    Row<PDPage> dataRow = table.createRow(10f);
                    cellFactory.createDataCellBucketName(dataRow, 100, currentBucketName);
                }
                Row<PDPage> callGraphRow = table.createRow(10f);
                Image image;
                try {
                    image = new Image(generateBufferedImage(orderedCallGraphsPerBucket.get(mapEntry.getKey()),
                                                            callGraphDiameterHashMap.get((mapEntry.getKey())))).scale(350,
                                                                                                                      250);
                } catch (Throwable e) {
                    image = new Image(createBufferedImageFromString("Graph generation is not supported by OpenJDK"));
                    log.warn("Unable to generate graph. Usually this issue is due to the usage of OpenJDK instead of OracleJDK",
                             e);
                }
                callGraphRow.createImageCell(100, image).scaleToFit();
            }
        }

    }

    /**
     * Generate an image containing a String
     * @param s the string that will be displayed
     * @return a Buffered image containing the string
     */
    private BufferedImage createBufferedImageFromString(String s) {
        int width = 400;
        int height = 100;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try {
            // Create a graphics which can be used to draw into the buffered image
            Graphics2D g2d = bufferedImage.createGraphics();

            // fill all the image with white
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, width, height);

            // create a string with black
            g2d.setColor(Color.black);
            g2d.drawString(s, 10, 50);

            // Disposes of this graphics context and releases any system resources that it is using.
            g2d.dispose();
        } catch (Throwable t) {
            log.warn("The server do not have a Graphical context, therefore it cannot generate image.", t);
        }
        return bufferedImage;
    }

    /**
     * This method computes the diameter of all call graphs. A diameter is the longest shortest path" (i.e., the longest graph geodesic) between any two graph vertices.
     *
     * @param groupedGraphPathsHavingSameRoot
     * @return
     */
    private Map<GraphNode, Integer> computeCallGraphsDiameter(
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupedGraphPathsHavingSameRoot) {
        Map<GraphNode, Integer> callGraphDiameterHashMap = new HashMap<>();
        groupedGraphPathsHavingSameRoot.entrySet().stream().forEach(entry -> {
            GraphNode rootNode = entry.getKey();
            int diameter = Collections.max(entry.getValue(), Comparator.comparingInt(GraphPath::getLength)).getLength();
            callGraphDiameterHashMap.put(rootNode, diameter);
        });

        return callGraphDiameterHashMap;
    }

    /**
     * This methods computes the call graph for all node roots
     *
     * @param groupedGraphPathsHavingSameRoot
     * @param callGraphHolder
     * @return
     */
    private Map<GraphNode, Graph<GraphNode, DefaultEdge>> computeCallGraphForAllRoots(
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupedGraphPathsHavingSameRoot,
            CallGraphHolder callGraphHolder) {

        Map<GraphNode, Graph<GraphNode, DefaultEdge>> callGraphsHashMap = new HashMap<>();
        groupedGraphPathsHavingSameRoot.entrySet().stream().forEach(entry -> {
            GraphNode rootNode = entry.getKey();
            Set<GraphNode> callGraphNodes = new HashSet<>();
            Set<DefaultEdge> callGraphEdges = new HashSet<>();
            entry.getValue().forEach(graphPath -> {
                callGraphNodes.addAll(graphPath.getVertexList());
                callGraphEdges.addAll(graphPath.getEdgeList());
            });
            callGraphsHashMap.put(rootNode,
                                  new AsSubgraph(callGraphHolder.getCallGraph(), callGraphNodes, callGraphEdges));
        });
        return callGraphsHashMap;
    }

    /**
     * This methods computes all graph paths of the callGraphHolder. A graph path is a chain of dependencies starting from a root until a leaf
     *
     * @param callGraphHolder
     * @return
     */
    private List<GraphPath<GraphNode, DefaultEdge>> computeAllGraphPaths(CallGraphHolder callGraphHolder) {

        Graph<GraphNode, DefaultEdge> callGraph = callGraphHolder.getCallGraph();
        List<GraphPath<GraphNode, DefaultEdge>> graphPathList = new ArrayList<>();
        ConnectivityInspector<GraphNode, DefaultEdge> inspector = new ConnectivityInspector(new AsSubgraph(callGraph,
                                                                                                           callGraphHolder.nodeSet(),
                                                                                                           callGraphHolder.dependencySet()));
        Set<GraphNode> rootNodes = new HashSet<>();
        Set<GraphNode> leafNodes = new HashSet<>();
        inspector.connectedSets().forEach(connectedComponent -> {
            BreadthFirstIterator breadthFirstIterator = new BreadthFirstIterator(new AsSubgraph(callGraph,
                                                                                                connectedComponent,
                                                                                                callGraphHolder.dependencySet()));

            GraphNode graphNode;
            while (breadthFirstIterator.hasNext()) {
                graphNode = ((GraphNode) breadthFirstIterator.next());
                if (callGraph.inDegreeOf(graphNode) == 0) {
                    rootNodes.add(graphNode);
                }
                if (callGraph.outDegreeOf(graphNode) == 0) {
                    leafNodes.add(graphNode);
                }
            }
        });

        AllDirectedPaths allDirectedPaths = new AllDirectedPaths(callGraph);
        graphPathList.addAll(allDirectedPaths.getAllPaths(rootNodes, leafNodes, true, null));

        //Detect simple cycles and add them to the graph paths list
        CycleDetector<GraphNode, DefaultEdge> cycleDetector = new CycleDetector(callGraph);

        if (cycleDetector.detectCycles()) {
            Set<GraphNode> cycleVertices = cycleDetector.findCycles();
            Graph<GraphNode, DefaultEdge> subGraphWithCycles = new AsSubgraph(callGraph, cycleVertices);
            graphPathList.add(Cycles.simpleCycleToGraphPath(subGraphWithCycles,
                                                            new ArrayList(subGraphWithCycles.edgeSet())));

        }

        return graphPathList;
    }

    /**
     * This methods sorts call graphs per bucket then object name
     *
     * @param callGraphsHashMap
     * @return
     */
    private TreeMap<GraphNode, Graph<GraphNode, DefaultEdge>>
            sortCallGraphsPerBucket(Map<GraphNode, Graph<GraphNode, DefaultEdge>> callGraphsHashMap) {
        Comparator<GraphNode> sortBasedOnName = Comparator.comparing(GraphNode::getBucketName);
        sortBasedOnName = sortBasedOnName.thenComparing(GraphNode::getObjectName);

        TreeMap<GraphNode, Graph<GraphNode, DefaultEdge>> sortedObjects = new TreeMap(sortBasedOnName);
        sortedObjects.putAll(callGraphsHashMap);
        return sortedObjects;

    }

    private void editCallGraphStyle(JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter) {
        callGraphAdapter.setAutoSizeCells(true);
        callGraphAdapter.setCellsResizable(true);
        callGraphAdapter.setAllowLoops(true);
        callGraphAdapter.setHtmlLabels(true);

        //Edit and personalize Vertex style
        editVertexStyle(callGraphAdapter);
        //Edit and personalize Edge style
        editEdgeStyle(callGraphAdapter);

        TableCallGraphsBuilder.mxGraphComponentWithoutDragAndDrop graphComponent = new TableCallGraphsBuilder.mxGraphComponentWithoutDragAndDrop(callGraphAdapter);
        graphComponent.setWheelScrollingEnabled(false);
    }

    private void editVertexStyle(JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter) {
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_SHAPE,
                                                                     mxConstants.SHAPE_RECTANGLE);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTCOLOR,
                                                                     mxUtils.getHexColorString(new Color(0, 0, 0)));
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTSIZE, 10);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_ROUNDED, true);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_OPACITY, 70);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        callGraphAdapter.getStylesheet()
                        .getDefaultVertexStyle()
                        .put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKEWIDTH, 1.5);

        //personalize the style of missing catalog objects
        List<mxICell> missingCatalogObjects = callGraphAdapter.getVertexToCellMap()
                                                              .entrySet()
                                                              .stream()
                                                              .filter(map -> !map.getKey().isInCatalog())
                                                              .map(Map.Entry::getValue)
                                                              .collect(Collectors.toList());
        callGraphAdapter.setCellStyle(MISSING_CATALOG_OBJECT_STYLE, missingCatalogObjects.toArray());

        //Apply a hashcode color-based on all nodes kind to distinguish them visually
        callGraphAdapter.getModel().beginUpdate();
        try {
            callGraphAdapter.getVertexToCellMap().entrySet().forEach(entry -> {
                entry.getValue()
                     .setStyle("strokeColor=" + intToARGB(entry.getKey().getObjectKind().hashCode()) +
                               (entry.getValue().getStyle() != null ? ";" + entry.getValue().getStyle()
                                                                    : "" + ";fontSize=8"));
                callGraphAdapter.getModel()
                                .setValue(entry.getValue(),
                                          new StringBuilder(entry.getKey().getBucketName()).append("/")
                                                                                           .append(entry.getKey()
                                                                                                        .getObjectName())
                                                                                           .append("<br><b><i>")
                                                                                           .append("&#91;")
                                                                                           .append(entry.getKey()
                                                                                                        .getObjectKind())
                                                                                           .append("&#93;")
                                                                                           .append("</i></b>"));
                callGraphAdapter.updateCellSize(entry.getValue(), true);

            });
        } finally {
            callGraphAdapter.getModel().endUpdate();
        }

    }

    private void editEdgeStyle(JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter) {
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_SHAPE,
                                                                   mxConstants.SHAPE_CONNECTOR);
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_EDGE,
                                                                   mxConstants.EDGESTYLE_ORTHOGONAL);
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_STROKECOLOR,
                                                                   mxUtils.getHexColorString(new Color(0, 0, 255)));
    }

    /**
     * This methods computes an ARGB color from the hashcode of a catalog object kind to distinguish between vertex visually.
     * In this way, we ensure to have as many color as many different kinds in the catalog.
     *
     * @param i
     * @return
     */
    private String intToARGB(int i) {
        return "#" + (Integer.toHexString(((i >> 24) & 0xFF)) + Integer.toHexString(((i >> 16) & 0xFF)) +
                      Integer.toHexString(((i >> 8) & 0xFF)) + Integer.toHexString((i & 0xFF))).substring(0, 6);
    }

    private BufferedImage generateBufferedImage(Graph<GraphNode, DefaultEdge> callGraph, int diameter) {

        JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter = new JGraphXAdapter(callGraph);
        editCallGraphStyle(callGraphAdapter);
        mxHierarchicalLayout layout = new mxHierarchicalLayout(callGraphAdapter, SwingConstants.WEST);
        layout.setFineTuning(true);

        if (diameter >= MAX_DIAMETER) {
            layout.setOrientation(SwingConstants.NORTH);
        }

        layout.execute(callGraphAdapter.getDefaultParent());
        return mxCellRenderer.createBufferedImage(callGraphAdapter, null, 2, null, true, null);

    }

    /**
     * The aim of these inner private classes is to disable the Drag and Drop functionality which throws HeadlessException in a Headless Environment.
     */

    private class mxGraphComponentWithoutDragAndDrop extends mxGraphComponent {

        public mxGraphComponentWithoutDragAndDrop(mxGraph graph) {
            super(graph);
        }

        @Override
        protected mxGraphHandler createGraphHandler() {
            return new mxGraphHandlerWithoutDragAndDrop(this);
        }
    }

    private class mxGraphHandlerWithoutDragAndDrop extends mxGraphHandler {
        public mxGraphHandlerWithoutDragAndDrop(mxGraphComponent graphComponent) {
            super(graphComponent);
        }

        @Override
        protected void installDragGestureHandler() {
            //My Blank implementation for the installDragGestureHandler
        }
    }
}
