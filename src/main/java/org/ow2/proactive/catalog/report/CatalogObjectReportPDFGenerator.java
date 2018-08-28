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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.service.exception.PDFGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.utils.FontUtils;


@Component
public class CatalogObjectReportPDFGenerator {

    @Value("${pa.scheduler.url}")
    private String schedulerUrl;

    @Value("${pa.catalog.pdf.report.ttf.font.path}")
    private String ttfFontPath;

    @Value("${pa.catalog.pdf.report.ttf.font.bold.path}")
    private String ttfFontBoldPath;

    @Value("${pa.catalog.pdf.report.ttf.font.italic.path}")
    private String ttfFontItalicPath;

    @Value("${pa.catalog.pdf.report.ttf.font.bold.italic.path}")
    private String ttfFontBoldItalicPath;

    @Autowired
    private TableDataBuilder tableDataBuilder;

    @Autowired
    private HeadersBuilder headersBuilder;

    public byte[] generatePDF(Set<CatalogObjectMetadata> orderedObjectsPerBucket, Optional<String> kind,
            Optional<String> contentType) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PDDocument doc = new PDDocument();) {

            // Set margins
            float margin = 10;

            //Load font for all languages
            setFontToUse(doc);

            // Initialize Document
            PDPage page = addNewPage(doc);

            // Initialize table
            BaseTable table = initializeTable(doc, margin, page);

            // Create Header row
            headersBuilder.createMainHeader(table);

            // Create Header row
            headersBuilder.createInfoHeader(table, orderedObjectsPerBucket, kind, contentType);

            // Create table data
            tableDataBuilder.buildTableData(orderedObjectsPerBucket, table);

            table.draw();

            doc.save(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
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

}
