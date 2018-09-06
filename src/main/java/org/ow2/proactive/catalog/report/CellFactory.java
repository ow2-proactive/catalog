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

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.image.Image;


@Component
public class CellFactory {

    private static final int SMALL_FONT = 4;

    private static final int MEDIUM_FONT = 6;

    private static final int BIG_FONT = 8;

    private static final int MAIN_TITLE_FONT_SIZE = 20;

    private static final String ACTIVEEON_BLUE = "#0E2C65";

    private static final String ACTIVEEON_ORANGE = "#EE7939";

    private static final String LIGHT_GRAY = "#D3D3D3";

    private static final String LIGHT_CYAN = "#F3F3F4";

    private static final String WHITE = "#ffffff";

    private static final String BLACK = "#000000";

    public void addMainTitleCell(Row<PDPage> row, String data) {
        createDataCell(row,
                       (100 / 12f) * 9,
                       data,
                       MAIN_TITLE_FONT_SIZE,
                       HorizontalAlignment.CENTER,
                       VerticalAlignment.MIDDLE,
                       ACTIVEEON_ORANGE,
                       ACTIVEEON_BLUE);
    }

    public void addSecondaryHeaderCell(Row<PDPage> row, String data) {
        createDataCell(row, 100, data, HorizontalAlignment.CENTER);
    }

    public void createDataCell(Row<PDPage> row, float width, String data) {
        createDataCell(row, width, data, MEDIUM_FONT);
    }

    public void createDataHeaderCell(Row<PDPage> row, float width, String title) {
        createDataCell(row,
                       width,
                       title,
                       BIG_FONT,
                       HorizontalAlignment.CENTER,
                       VerticalAlignment.TOP,
                       LIGHT_CYAN,
                       BLACK);
    }

    public void createDataCellBucketName(Row<PDPage> row, float width, String data) {
        createDataCell(row, width, data, BIG_FONT, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, WHITE, BLACK);
    }

    public void createKeyValueContentDataCell(Row<PDPage> row, float width, String data) {
        createDataCell(row, width, data, SMALL_FONT, HorizontalAlignment.LEFT, VerticalAlignment.TOP, WHITE, BLACK);
    }

    public void createDataCell(Row<PDPage> row, float width, String data, int fontSize) {
        createDataCell(row, width, data, fontSize, HorizontalAlignment.CENTER, VerticalAlignment.TOP, WHITE, BLACK);
    }

    public void createDataCell(Row<PDPage> row, float width, String data, HorizontalAlignment align) {
        createDataCell(row, width, data, MEDIUM_FONT, align, VerticalAlignment.TOP, WHITE, BLACK);
    }

    public void createIconCell(Row<PDPage> row, float width, String url_path) {
        try {
            URL url = new URL(url_path);
            BufferedImage imageFile = ImageIO.read(url);
            row.createImageCell(width, new Image(imageFile));
        } catch (Exception e) {
            createDataCell(row,
                           width,
                           url_path,
                           MEDIUM_FONT,
                           HorizontalAlignment.LEFT,
                           VerticalAlignment.TOP,
                           LIGHT_GRAY,
                           BLACK);
        }
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
}
