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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.report.HeadersBuilder;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
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
import be.quodlibet.boxable.utils.FontUtils;


/**
 * @author ActiveEon Team
 * @since 2019-03-25
 */

@Component
public class CatalogObjectCallGraphPDFGenerator {

    private static final float MARGIN = 10;

    private static final float HEADER_HEIGHT = 100;

    private static final String MAIN_TITLE = "ProActive Call Graph Report";

    private static final String LIGHT_GRAY = "#D3D3D3";

    private static final String BLACK = "#000000";

    private static final int BIG_FONT = 12;

    private static final int NODES_LIMIT_NUMBER = 30;

    private static final String MISSING_CATALOG_OBJECT_STYLE = "fillColor=#C0C0C0";

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
            Optional<String> contentType, CatalogObjectService catalogObjectService) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument()) {

            CallGraphHolder callGraph = buildCatalogCallGraph(catalogObjectMetadataList, catalogObjectService);
            List<BufferedImage> subGraphBufferedImages = new ArrayList<>();

            callGraphPartitioning(callGraph).stream()
                                            .forEach(partition -> subGraphBufferedImages.add(generateBufferedImage(callGraph,
                                                                                                                   partition)));

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

            if (callGraph.order() == 0) {
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
                int pageIndex = 0;
                for (BufferedImage partition : subGraphBufferedImages) {
                    drawPartitionImageOnSinglePage(partition, doc, pageIndex);
                    pageIndex++;
                }
            }

            table.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new PDFGenerationException(e);
        }
    }

    private void drawPartitionImageOnSinglePage(BufferedImage partition, PDDocument doc, int pageIndex)
            throws IOException {
        PDPage page = pageIndex != 0 ? addNewPage(doc) : doc.getPage(0);

        final PDImageXObject pdImage = LosslessFactory.createFromImage(doc, partition);
        PDRectangle mediaBox = page.getMediaBox();
        float fX = pdImage.getWidth() / mediaBox.getWidth();
        float fY = pdImage.getHeight() / mediaBox.getHeight();
        float startY = pageIndex != 0 ? mediaBox.getHeight() - pdImage.getHeight()
                                      : mediaBox.getHeight() - (HEADER_HEIGHT + pdImage.getHeight());
        float factor = Math.max(fX, fY);
        if (pageIndex == 0) {
            factor = factor * 1.10f;
        }

        try (final PDPageContentStream contentStream = new PDPageContentStream(doc,
                                                                               doc.getPage(pageIndex),
                                                                               PDPageContentStream.AppendMode.APPEND,
                                                                               true)) {
            contentStream.drawImage(pdImage,
                                    0,
                                    startY > 0 ? startY : 0,
                                    pdImage.getWidth() / factor,
                                    pdImage.getHeight() / factor);
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

    private BufferedImage generateBufferedImage(CallGraphHolder callGraphHolder, Set<GraphNode> subGraphNodes) {

        JGraphXAdapter<GraphNode, DefaultEdge> callGraphAdapter = new JGraphXAdapter(new AsSubgraph(callGraphHolder.getDependencyGraph(),
                                                                                                    subGraphNodes,
                                                                                                    callGraphHolder.getDependencyGraph()
                                                                                                                   .edgeSet()));
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
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_FONTSTYLE,
                                                                     mxConstants.FONT_ITALIC);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_ROUNDED, true);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_OPACITY, 50);
        callGraphAdapter.getStylesheet()
                        .getDefaultVertexStyle()
                        .put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(new Color(255, 255, 255)));
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKEWIDTH, 1.5);
        callGraphAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_STROKECOLOR,
                                                                     mxUtils.getHexColorString(new Color(0, 0, 255)));

        //personalize the style of missing catalog objects
        List<mxICell> missingCatalogObjects = callGraphAdapter.getVertexToCellMap()
                                                              .entrySet()
                                                              .stream()
                                                              .filter(map -> !map.getKey().isInCatalog())
                                                              .map(map -> map.getValue())
                                                              .collect(Collectors.toList());
        callGraphAdapter.setCellStyle(MISSING_CATALOG_OBJECT_STYLE, missingCatalogObjects.toArray());



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

    private CallGraphHolder buildCatalogCallGraph(List<CatalogObjectMetadata> catalogObjectMetadataList,
            CatalogObjectService catalogObjectService) {

        CallGraphHolder callGraphHolder = new CallGraphHolder();
        for (CatalogObjectMetadata catalogObjectMetadata : catalogObjectMetadataList) {
            List<String> dependsOnCatalogObjects = collectDependsOnCatalogObjects(catalogObjectMetadata);
            if (!dependsOnCatalogObjects.isEmpty()) {
                GraphNode callingCatalogObject = callGraphHolder.addNode(catalogObjectMetadata.getBucketName(),
                                                                         catalogObjectMetadata.getName(),
                                                                         catalogObjectMetadata.getKind(),
                                                                         true);

                for (String dependsOnCatalogObject : dependsOnCatalogObjects) {
                    String bucketName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(0);
                    String objectName = separatorUtility.getSplitBySeparator(dependsOnCatalogObject).get(1);
                    boolean isCatalogObjectExist = catalogObjectService.isDependsOnObjectExistInCatalog(bucketName,
                                                                                                        objectName,
                                                                                                        WorkflowParser.LATEST_VERSION);
                    String objectKind = isCatalogObjectExist ? catalogObjectService.getCatalogObjectMetadata(bucketName,
                                                                                                             objectName)
                                                                                   .getKind()
                                                             : "N/A";
                    GraphNode calledCatalogObject = callGraphHolder.addNode(bucketName,
                                                                            objectName,
                                                                            objectKind,
                                                                            isCatalogObjectExist);
                    callGraphHolder.addDependsOnEdge(callingCatalogObject, calledCatalogObject);
                }
            }
        }
        return callGraphHolder;
    }

    private List<Set<GraphNode>> callGraphPartitioning(CallGraphHolder callGraphHolder) {
        Set<GraphNode> nodes = new HashSet(callGraphHolder.nodeSet());
        Set<DefaultEdge> edges = new HashSet(callGraphHolder.dependencySet());
        List<Set<GraphNode>> nodesPartition = new ArrayList<>();
        ConnectivityInspector<GraphNode, DefaultEdge> inspector = new ConnectivityInspector(new AsSubgraph(callGraphHolder.getDependencyGraph(),
                                                                                                           nodes,
                                                                                                           edges));
        List<Set<GraphNode>> connectedVertices = inspector.connectedSets();
        final Set<GraphNode> partition = new HashSet();
        connectedVertices.forEach(subGraph -> {
            if (subGraph.size() > NODES_LIMIT_NUMBER) {
                nodesPartition.add(subGraph);
            } else {
                partition.addAll(subGraph);
                if (partition.size() > NODES_LIMIT_NUMBER) {
                    nodesPartition.add(new HashSet<>(partition));
                    partition.clear();
                }
            }
        });
        if (!partition.isEmpty()) {
            nodesPartition.add(new HashSet<>(partition));
        }
        return nodesPartition;
    }

    private List<String> collectDependsOnCatalogObjects(CatalogObjectMetadata catalogObjectMetadata) {
        return catalogObjectMetadata.getMetadataList()
                                    .stream()
                                    .filter(metadata -> metadata.getLabel()
                                                                .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                    .map(Metadata::getKey)
                                    .collect(Collectors.toList());

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
