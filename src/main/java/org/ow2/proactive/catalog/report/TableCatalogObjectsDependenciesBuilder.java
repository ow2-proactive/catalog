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
package org.ow2.proactive.catalog.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.Cycles;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.ow2.proactive.catalog.callgraph.CallGraphHolder;
import org.ow2.proactive.catalog.callgraph.GraphNode;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;


/**
 * @author ActiveEon Team
 * @since 2019-06-14
 */

@Component
public class TableCatalogObjectsDependenciesBuilder {

    @Autowired
    private CellFactory cellFactory;

    private static final String FONT_NAME = "arial-unicode-ms.ttf";

    private static final String MARGIN = "                  ";

    private static final float CELL_WIDTH = 100f;

    private static final float CELL_HEIGHT = 10f;

    @Autowired
    private SeparatorUtility separatorUtility;

    /**
     * This method builds a table of catalog objects dependencies ordered and grouped by bucket and object name.
     *  In case the oder of the callGraphHolder is zero, an appropriate message is displayed.
     * @param doc
     * @param callGraphHolder
     * @param catalogObjectMetadataList
     * @param catalogObjectService
     * @param table
     * @throws IOException
     */
    public void buildCatalogObjectsDependenciesTable(PDDocument doc, CallGraphHolder callGraphHolder,
            List<CatalogObjectMetadata> catalogObjectMetadataList, CatalogObjectService catalogObjectService,
            BaseTable table) throws IOException {

        if (callGraphHolder.order() == 0) {
            Row<PDPage> dataRow = table.createRow(CELL_HEIGHT);
            cellFactory.createDataHeaderCell(dataRow,
                                             CELL_WIDTH,
                                             "No identified Dependencies in the Catalog or among the selected Catalog Objects");
        } else {
            List<GraphPath<GraphNode, DefaultEdge>> graphPathsList = computeAllGraphPaths(callGraphHolder);

            //Grouping all graph paths having the same root
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> groupingGraphPathsHavingSameRoot = graphPathsList.stream()
                                                                                                                     .collect(Collectors.groupingBy(GraphPath::getStartVertex,
                                                                                                                                                    Collectors.toList()));
            Set<GraphNode> leafNodes = getLeafNodes(callGraphHolder);
            leafNodes.stream()
                     .filter(leafNode -> !leafNode.getObjectKind().equals("N/A"))
                     .collect(Collectors.toSet())
                     .forEach(leafNode -> {
                         groupingGraphPathsHavingSameRoot.put(leafNode, new ArrayList<>());
                         catalogObjectMetadataList.add(catalogObjectService.getCatalogObjectMetadata(leafNode.getBucketName(),
                                                                                                     leafNode.getObjectName()));
                     });

            TreeMap<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> sortGraphPathsPerBucketAndObjectName = sortGraphPathsPerBucketAndObjectName(groupingGraphPathsHavingSameRoot);

            //collect Catalog Objects Dependencies
            Map<String, CatalogObjectDependencies> catalogObjectDependenciesMap = computeCatalogObjectsDependencies(catalogObjectMetadataList,
                                                                                                                    catalogObjectService);

            String currentBucketName = "";
            String bucketAndNameAndKindCatalogObject = "";
            for (Map.Entry<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> mapEntry : sortGraphPathsPerBucketAndObjectName.entrySet()) {
                if (!currentBucketName.equals(mapEntry.getKey().getBucketName())) {
                    currentBucketName = mapEntry.getKey().getBucketName();
                    Row<PDPage> dataRow = table.createRow(CELL_HEIGHT);
                    cellFactory.createDataHeaderCell(dataRow, CELL_WIDTH, currentBucketName);
                }

                bucketAndNameAndKindCatalogObject = separatorUtility.getConcatWithSeparator(mapEntry.getKey()
                                                                                                    .getBucketName(),
                                                                                            mapEntry.getKey()
                                                                                                    .getObjectName(),
                                                                                            mapEntry.getKey()
                                                                                                    .getObjectKind());
                Row<PDPage> tableRow = table.createRow(CELL_HEIGHT);
                Cell<PDPage> cell = tableRow.createCell(CELL_WIDTH,
                                                        dataCell(mapEntry.getKey(),
                                                                 mapEntry.getValue(),
                                                                 catalogObjectDependenciesMap.get(bucketAndNameAndKindCatalogObject)));
                cell.setFont(PDType0Font.load(doc,
                                              new File(getClass().getClassLoader().getResource(FONT_NAME).getFile())));
                cell.setFontSize(5);
            }
        }
    }

    /**
     * This methods computes all graph paths of the callGraphHolder. A graph path is a chain of dependencies starting from a node until a leaf
     *
     * @param callGraphHolder
     * @return
     */
    private List<GraphPath<GraphNode, DefaultEdge>> computeAllGraphPaths(CallGraphHolder callGraphHolder) {

        Graph<GraphNode, DefaultEdge> callGraph = callGraphHolder.getCallGraph();
        List<GraphPath<GraphNode, DefaultEdge>> graphPathList = new ArrayList<>();

        Set<GraphNode> leafNodes = getLeafNodes(callGraphHolder);

        AllDirectedPaths allDirectedPaths = new AllDirectedPaths(callGraph);
        graphPathList.addAll(allDirectedPaths.getAllPaths(callGraph.vertexSet(), leafNodes, true, null));

        //Detect simple cycles and add them to the graph paths list
        CycleDetector<GraphNode, DefaultEdge> cycleDetector = new CycleDetector(callGraph);

        if (cycleDetector.detectCycles()) {
            Set<GraphNode> cycleVertices = cycleDetector.findCycles();
            Graph<GraphNode, DefaultEdge> subGraphWithCycles = new AsSubgraph(callGraph, cycleVertices);
            graphPathList.add(Cycles.simpleCycleToGraphPath(subGraphWithCycles,
                                                            new ArrayList(subGraphWithCycles.edgeSet())));

        }

        return graphPathList.stream().filter(graphPath -> graphPath.getLength() >= 1).collect(Collectors.toList());
    }

    private Set<GraphNode> getLeafNodes(CallGraphHolder callGraphHolder) {

        Graph<GraphNode, DefaultEdge> callGraph = callGraphHolder.getCallGraph();
        ConnectivityInspector<GraphNode, DefaultEdge> inspector = new ConnectivityInspector(new AsSubgraph(callGraph,
                                                                                                           callGraphHolder.nodeSet(),
                                                                                                           callGraphHolder.dependencySet()));
        Set<GraphNode> leafNodes = new HashSet<>();
        inspector.connectedSets().forEach(connectedComponent -> {
            BreadthFirstIterator breadthFirstIterator = new BreadthFirstIterator(new AsSubgraph(callGraph,
                                                                                                connectedComponent,
                                                                                                callGraphHolder.dependencySet()));
            GraphNode graphNode;
            while (breadthFirstIterator.hasNext()) {
                graphNode = ((GraphNode) breadthFirstIterator.next());
                if (callGraph.outDegreeOf(graphNode) == 0) {
                    leafNodes.add(graphNode);
                }
            }
        });
        return leafNodes;
    }

    /**
      * This methods sorts graph paths per bucket then object name
      *
      * @param graphPathsHashMap
      * @return
      */

    private TreeMap<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> sortGraphPathsPerBucketAndObjectName(
            Map<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> graphPathsHashMap) {
        Comparator<GraphNode> sortBasedOnName = Comparator.comparing(GraphNode::getBucketName);
        sortBasedOnName = sortBasedOnName.thenComparing(GraphNode::getObjectName);

        TreeMap<GraphNode, List<GraphPath<GraphNode, DefaultEdge>>> sortedObjects = new TreeMap(sortBasedOnName);
        sortedObjects.putAll(graphPathsHashMap);
        return sortedObjects;
    }

    private String dataCell(GraphNode catalogObjectRoot,
            List<GraphPath<GraphNode, DefaultEdge>> catalogObjectGraphPathList,
            CatalogObjectDependencies catalogObjectDependencies) {
        StringBuilder dataCell = new StringBuilder();
        String root = nodeBeautify(catalogObjectRoot);
        dataCell.append(root).append("<br>");
        dataCell.append("<i>").append("Calls: ").append("</i>").append("<br>");

        for (GraphPath<GraphNode, DefaultEdge> catalogObjectGraphPath : catalogObjectGraphPathList) {
            dataCell.append(MARGIN);
            List<GraphNode> dependencyList = catalogObjectGraphPath.getVertexList();
            dependencyList.remove(0);
            for (GraphNode catalogObjectDependency : dependencyList) {
                dataCell.append("\u21E2").append(" ").append(nodeBeautify(catalogObjectDependency)).append(" ");
            }
            dataCell.append("<br>");
        }

        dataCell.append("<i>").append("Called: ").append("</i>").append("<br>");
        for (String objectCalledBy : catalogObjectDependencies.getCalledByList()) {
            dataCell.append(MARGIN).append("\u21E0").append(" ").append(parentBeautify(objectCalledBy));
            dataCell.append("<br>");
        }
        return dataCell.toString();
    }

    private Map<String, CatalogObjectDependencies> computeCatalogObjectsDependencies(
            List<CatalogObjectMetadata> catalogObjectMetadataList, CatalogObjectService catalogObjectService) {
        Map<String, CatalogObjectDependencies> catalogObjectDependenciesMap = new HashMap();
        catalogObjectMetadataList.forEach(catalogObjectMetadata -> {
            String bucketName = catalogObjectMetadata.getBucketName();
            String objectName = catalogObjectMetadata.getName();
            String kind = catalogObjectMetadata.getKind();
            String bucketAndNameAndKindCatalogObject = separatorUtility.getConcatWithSeparator(bucketName,
                                                                                               objectName,
                                                                                               kind);
            CatalogObjectDependencies catalogObjectDependenciesList = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                 objectName);
            catalogObjectDependenciesMap.put(bucketAndNameAndKindCatalogObject, catalogObjectDependenciesList);

        });
        return catalogObjectDependenciesMap;

    }

    private String nodeBeautify(GraphNode graphNode) {
        final StringBuilder sb = new StringBuilder();
        sb.append(" ")
          .append(("<b>"))
          .append(graphNode.getObjectName())
          .append(("</b>"))
          .append("   ")
          .append("(Bucket: ")
          .append(graphNode.getBucketName())
          .append(", Kind: ")
          .append(graphNode.getObjectKind())
          .append(")");
        return sb.toString();
    }

    private String parentBeautify(String objectCalledBy) {
        String bucketName = separatorUtility.getSplitBySeparator(objectCalledBy).get(0);
        String objectName = separatorUtility.getSplitBySeparator(objectCalledBy).get(1);
        final StringBuilder sb = new StringBuilder();
        sb.append(" ")
          .append(("<b>"))
          .append(objectName)
          .append(("</b>"))
          .append("   ")
          .append("(Bucket: ")
          .append(bucketName)
          .append(", Kind: ")
          .append("Workflow/standard")
          .append(")");
        return sb.toString();
    }

}
