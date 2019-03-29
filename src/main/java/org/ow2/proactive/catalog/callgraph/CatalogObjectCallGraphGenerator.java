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
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Component
public class CatalogObjectCallGraphGenerator {

    @Autowired
    private SeparatorUtility separatorUtility;

    public File buildImageOfCatalogCallGraph(List<CatalogObjectMetadata> catalogObjectMetadataList) throws IOException {

        CallGraphHolder callGraphHolder = buildCatalogCallGraph(catalogObjectMetadataList);

        JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter = new JGraphXAdapter(callGraphHolder.getDependencyGraph());

        callGraphStyle(callGraphAdapter);

        mxGraphLayout layout = new mxHierarchicalLayout(callGraphAdapter, SwingConstants.NORTH);
        layout.execute(callGraphAdapter.getDefaultParent());

        BufferedImage image = mxCellRenderer.createBufferedImage(callGraphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File("src/test/resources/graph.png");
        ImageIO.write(image, "PNG", imgFile);

        return imgFile;

    }

    private void callGraphStyle(JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter) {
        callGraphAdapter.setAutoSizeCells(true);
        callGraphAdapter.setCellsResizable(true);
        //Vertex style
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_SHAPE,
                                                                     mxConstants.SHAPE_RECTANGLE);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTCOLOR,
                                                                     mxUtils.getHexColorString(new Color(0, 0, 0)));
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_ROUNDED, true);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_OPACITY, 50);
        callGraphAdapter.getStylesheet()
                        .getDefaultVertexStyle()
                        .put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKECOLOR,
                                                                     mxUtils.getHexColorString(new Color(0, 0, 255)));

        //Edge style
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_SHAPE,
                                                                   mxConstants.SHAPE_CONNECTOR);
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_EDGE,
                                                                   mxConstants.EDGESTYLE_ORTHOGONAL);
        callGraphAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_STROKECOLOR,
                                                                   mxUtils.getHexColorString(new Color(0, 0, 255)));

        mxGraphComponent graphComponent = new mxGraphComponent(callGraphAdapter);
        graphComponent.setWheelScrollingEnabled(false);
    }

    private CallGraphHolder buildCatalogCallGraph(List<CatalogObjectMetadata> catalogObjectMetadataList) {

        CallGraphHolder callGraphHolder = CallGraphHolder.getInstance();
        for (CatalogObjectMetadata catalogObjectMetadata : catalogObjectMetadataList) {
            GraphNode callingWorkflow = callGraphHolder.addNode(catalogObjectMetadata.getBucketName(),
                                                                catalogObjectMetadata.getName());
            List<String> dependsOnCatalogObjects = collectDependsOnCatalogObjects(catalogObjectMetadata);
            for (String dependsOnCatalogObject : dependsOnCatalogObjects) {
                String bucketName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(0);
                String workflowName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(1);
                GraphNode calledWorkflow = callGraphHolder.addNode(bucketName, workflowName);
                callGraphHolder.addDependsOnDependency(callingWorkflow, calledWorkflow);
            }
        }
        return callGraphHolder;
    }

    private List<String> collectDependsOnCatalogObjects(CatalogObjectMetadata catalogObjectMetadata) {
        return catalogObjectMetadata.getMetadataList()
                                    .stream()
                                    .filter(metadata -> metadata.getLabel()
                                                                .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                    .map(Metadata::getKey)
                                    .collect(Collectors.toList());

    }

}
