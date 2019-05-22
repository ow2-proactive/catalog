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
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.image.Image;


/**
 * @author ActiveEon Team
 * @since 2019-05-15
 */

@Component
public class TableGraphPathBuilder {

    private static final String MISSING_CATALOG_OBJECT_STYLE = "fillColor=#C0C0C0;strokeColor=#FF0000;fontSize=8";

    private static final String LIGHT_GRAY = "#D3D3D3";

    private static final String BLACK = "#000000";

    private static final int BIG_FONT = 12;

    private static final int MAX_DIAMETER = 3;

    @Autowired
    private CellFactory cellFactory;

    public void buildGraphPathTable(CallGraphHolder globalCallGraph, BaseTable table) {

        if (globalCallGraph.order() == 0) {
            Row<PDPage> dataRow = table.createRow(10f);
            createDataCell(dataRow,
                           100,
                           "No identified Dependencies among selected Catalog Objects",
                           BIG_FONT,
                           HorizontalAlignment.CENTER,
                           VerticalAlignment.MIDDLE,
                           LIGHT_GRAY,
                           BLACK);
        } else {

            List<GraphPath<GraphNode, DefaultEdge>> graphPathList = computeAllGraphPaths(globalCallGraph);
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupingGraphPathsHavingSameRoot = graphPathList.stream()
                                                                                                                    .collect(Collectors.groupingBy(GraphPath::getStartVertex,
                                                                                                                                                   Collectors.toList()));

            Map<GraphNode, Graph<GraphNode, DefaultEdge>> callGraphHashMap = computeCallGraphForAllRoots(groupingGraphPathsHavingSameRoot,
                                                                                                         globalCallGraph);
            Map<GraphNode, Integer> callGraphDiameterHashMap = computeCallGraphsDiameter(groupingGraphPathsHavingSameRoot);
            TreeMap<GraphNode, Graph<GraphNode, DefaultEdge>> orderedCallGraphsPerBucket = sortCallGraphsPerBucket(callGraphHashMap);
            String currentBucketName = "";

            for (Map.Entry<GraphNode, Graph<GraphNode, DefaultEdge>> mapEntry : orderedCallGraphsPerBucket.entrySet()) {

                if (!currentBucketName.equals(mapEntry.getKey().getBucketName())) {
                    currentBucketName = mapEntry.getKey().getBucketName();
                    Row<PDPage> dataRow = table.createRow(15f);
                    cellFactory.createDataCellBucketName(dataRow, (100), currentBucketName);
                }
                Row<PDPage> dataRow = table.createRow(10f);
                dataRow.createImageCell(100,
                                        new Image(generateBufferedImage(orderedCallGraphsPerBucket.get(mapEntry.getKey()),
                                                                        callGraphDiameterHashMap.get((mapEntry.getKey())))));
            }
        }

    }

    private Map<GraphNode, Integer> computeCallGraphsDiameter(
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupingGraphPathsHavingSameRoot) {
        Map<GraphNode, Integer> callGraphDiameterHashMap = new HashMap<>();
        groupingGraphPathsHavingSameRoot.entrySet().stream().forEach(entry -> {
            GraphNode rootNode = entry.getKey();
            int diameter = Collections.max(entry.getValue(), Comparator.comparingInt(GraphPath::getLength)).getLength();
            callGraphDiameterHashMap.put(rootNode, diameter);
        });

        return callGraphDiameterHashMap;
    }

    private Map<GraphNode, Graph<GraphNode, DefaultEdge>> computeCallGraphForAllRoots(
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> mergeGraphPathsHavingSameRoot,
            CallGraphHolder globalCallGraph) {

        Map<GraphNode, Graph<GraphNode, DefaultEdge>> callGraphsHashMap = new HashMap<>();
        mergeGraphPathsHavingSameRoot.entrySet().stream().forEach(entry -> {
            GraphNode rootNode = entry.getKey();
            Set<GraphNode> callGraphNodes = new HashSet<>();
            entry.getValue().stream().forEach(graphPath -> callGraphNodes.addAll(graphPath.getVertexList()));
            Set<DefaultEdge> callGraphEdges = new HashSet<>();
            entry.getValue().stream().forEach(graphPath -> callGraphEdges.addAll(graphPath.getEdgeList()));
            callGraphsHashMap.put(rootNode,
                                  new AsSubgraph(globalCallGraph.getDependencyGraph(), callGraphNodes, callGraphEdges));
        });
        return callGraphsHashMap;
    }

    private List<GraphPath<GraphNode, DefaultEdge>> computeAllGraphPaths(CallGraphHolder globalCallGraph) {

        Graph<GraphNode, DefaultEdge> callGraph = globalCallGraph.getDependencyGraph();
        List<GraphPath<GraphNode, DefaultEdge>> graphPathList = new ArrayList<>();
        ConnectivityInspector<GraphNode, DefaultEdge> inspector = new ConnectivityInspector(new AsSubgraph(callGraph,
                                                                                                           globalCallGraph.nodeSet(),
                                                                                                           globalCallGraph.dependencySet()));
        inspector.connectedSets().forEach(connectedComponent -> {
            BreadthFirstIterator breadthFirstIterator = new BreadthFirstIterator(new AsSubgraph(callGraph,
                                                                                                connectedComponent,
                                                                                                globalCallGraph.dependencySet()));
            Set<GraphNode> rootNodes = new HashSet<>();
            Set<GraphNode> leafNodes = new HashSet<>();
            while (breadthFirstIterator.hasNext()) {
                GraphNode graphNode = ((GraphNode) breadthFirstIterator.next());
                if (callGraph.inDegreeOf(graphNode) == 0) {
                    rootNodes.add(graphNode);
                }
                if (callGraph.outDegreeOf(graphNode) == 0) {
                    leafNodes.add(graphNode);
                }
            }
            AllDirectedPaths allDirectedPaths = new AllDirectedPaths(callGraph);
            graphPathList.addAll(allDirectedPaths.getAllPaths(rootNodes, leafNodes, true, null));

        });

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

        //Edit Vertex style
        editVertexStyle(callGraphAdapter);
        //Edit Edge style
        editEdgeStyle(callGraphAdapter);

        TableGraphPathBuilder.mxGraphComponentWithoutDragAndDrop graphComponent = new TableGraphPathBuilder.mxGraphComponentWithoutDragAndDrop(callGraphAdapter);
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

    private String intToARGB(int i) {
        return "#" + (Integer.toHexString(((i >> 24) & 0xFF)) + Integer.toHexString(((i >> 16) & 0xFF)) +
                      Integer.toHexString(((i >> 8) & 0xFF)) + Integer.toHexString((i & 0xFF))).substring(0, 6);
    }

    private BufferedImage generateBufferedImage(Graph<GraphNode, DefaultEdge> callGraph, int diameter) {

        JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter = new JGraphXAdapter(callGraph);
        editCallGraphStyle(callGraphAdapter);
        mxHierarchicalLayout layout = new mxHierarchicalLayout(callGraphAdapter, SwingConstants.WEST);
        layout.setFineTuning(true);
        layout.setResizeParent(true);

        if (diameter >= MAX_DIAMETER) {
            layout.setOrientation(SwingConstants.NORTH);
        }

        layout.execute(callGraphAdapter.getDefaultParent());
        return mxCellRenderer.createBufferedImage(callGraphAdapter, null, 2, null, true, null);

    }

    private void createDataCell(Row<PDPage> row, float width, String data, int fontSize, HorizontalAlignment align,
            VerticalAlignment valign, String fillColor, String textColor) {
        Cell<PDPage> cell = row.createCell(width, data);
        cell.setFontSize(fontSize);
        cell.setAlign(align);
        cell.setValign(valign);
        cell.setFillColor(java.awt.Color.decode(fillColor));
        cell.setTextColor(java.awt.Color.decode(textColor));

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
