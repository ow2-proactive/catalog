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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.report.HeadersBuilder;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.image.Image;
import be.quodlibet.boxable.utils.FontUtils;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Component
public class CatalogObjectCallGraphPDFGenerator {

    private static final float MARGIN = 10;

    private static final String MAIN_TITLE = "ProActive Call Graph Report";

    @Autowired
    private SeparatorUtility separatorUtility;

    @Value("${pa.catalog.pdf.report.ttf.font.path}")
    private String ttfFontPath;

    @Value("${pa.catalog.pdf.report.ttf.font.bold.path}")
    private String ttfFontBoldPath;

    @Value("${pa.catalog.pdf.report.ttf.font.italic.path}")
    private String ttfFontItalicPath;

    @Value("${pa.catalog.pdf.report.ttf.font.bold.italic.path}")
    private String ttfFontBoldItalicPath;

    @Autowired
    private HeadersBuilder headersBuilder;

    public byte[] generatePdfImage(List<CatalogObjectMetadata> catalogObjectMetadataList, Optional<String> kind,
            Optional<String> contentType) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument();) {

            CallGraphHolder callGraph = buildCatalogCallGraph(catalogObjectMetadataList);

            BufferedImage callGraphBufferedImage = generateBufferedImage(callGraph);

            //Load font for all languages
            setFontToUse(doc);

            // Initialize Document
            PDPage page = addNewPage(doc);

            // Initialize table
            BaseTable table = initializeTable(doc, MARGIN, page);

            // Create Header row
            headersBuilder.createMainHeader(table, MAIN_TITLE);

            // Create Header row
            headersBuilder.createInfoHeader(table,
                                            extractBucketSet(callGraph),
                                            extractObjectSet(callGraph),
                                            kind,
                                            contentType);

            Row<PDPage> dataRow = table.createRow(100);
            dataRow.createImageCell(100, new Image(callGraphBufferedImage));

            table.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new PDFGenerationException(e);
        }
    }

    private void setFontToUse(PDDocument doc) throws IOException {
        FontUtils.setSansFontsAsDefault(doc);
        addFontTypeIfFileExists(doc, ttfFontPath, "font");
        addFontTypeIfFileExists(doc, ttfFontBoldPath, "fontBold");
        addFontTypeIfFileExists(doc, ttfFontItalicPath, "fontItalic");
        addFontTypeIfFileExists(doc, ttfFontBoldItalicPath, "fontBoldItalic");
    }

    private void addFontTypeIfFileExists(PDDocument doc, String path, String fontType) throws IOException {
        if (!StringUtils.isEmpty(path) && new File(path).exists()) {
            FontUtils.getDefaultfonts().put(fontType, PDType0Font.load(doc, new File(path)));
        }
    }

    private BaseTable initializeTable(PDDocument doc, float margin, PDPage page) throws IOException {
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        boolean drawContent = true;
        boolean drawLines = true;
        float yStart = yStartNewPage;
        float bottomMargin = 70;
        return new BaseTable(yStart,
                             yStartNewPage,
                             bottomMargin,
                             tableWidth,
                             margin,
                             doc,
                             page,
                             drawLines,
                             drawContent);
    }

    private PDPage addNewPage(PDDocument doc) {
        PDPage page = new PDPage();
        doc.addPage(page);
        return page;
    }

    private BufferedImage generateBufferedImage(CallGraphHolder callGraphHolder) {

        JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter = new JGraphXAdapter(callGraphHolder.getDependencyGraph());
        callGraphStyle(callGraphAdapter);
        mxGraphLayout layout = new mxHierarchicalLayout(callGraphAdapter, SwingConstants.WEST);
        layout.execute(callGraphAdapter.getDefaultParent());
        return mxCellRenderer.createBufferedImage(callGraphAdapter, null, 2, null, true, null);

    }

    private Set<String> extractBucketSet(CallGraphHolder callGraphHolder) {
        Set<String> bucketSet = new HashSet<>();
        callGraphHolder.nodeSet().forEach(graphNode -> bucketSet.add(graphNode.getBucketName()));
        return bucketSet;

    }

    private Set<String> extractObjectSet(CallGraphHolder callGraphHolder) {
        Set<String> objectSet = new HashSet<>();
        callGraphHolder.nodeSet().forEach(graphNode -> objectSet.add(graphNode.getObjectName()));
        return objectSet;

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

        mxGraphComponentWithoutDragAndDrop graphComponent = new mxGraphComponentWithoutDragAndDrop(callGraphAdapter);
        graphComponent.setWheelScrollingEnabled(false);
    }

    private CallGraphHolder buildCatalogCallGraph(List<CatalogObjectMetadata> catalogObjectMetadataList) {

        CallGraphHolder callGraphHolder = new CallGraphHolder();
        for (CatalogObjectMetadata catalogObjectMetadata : catalogObjectMetadataList) {
            List<String> dependsOnCatalogObjects = collectDependsOnCatalogObjects(catalogObjectMetadata);
            if (!dependsOnCatalogObjects.isEmpty()) {
                GraphNode callingWorkflow = callGraphHolder.addNode(catalogObjectMetadata.getBucketName(),
                                                                    catalogObjectMetadata.getName());

                for (String dependsOnCatalogObject : dependsOnCatalogObjects) {
                    String bucketName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(0);
                    String workflowName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(1);
                    GraphNode calledWorkflow = callGraphHolder.addNode(bucketName, workflowName);
                    callGraphHolder.addDependsOnEdge(callingWorkflow, calledWorkflow);
                }
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

    /**
     * The aim of these inner private classes is to disable the Drag and Drop functionality which throws HeadlessException in a Headless Environment.
     */

    private class mxGraphComponentWithoutDragAndDrop extends mxGraphComponent {

        public mxGraphComponentWithoutDragAndDrop(mxGraph graph) {
            super(graph);
        }

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
