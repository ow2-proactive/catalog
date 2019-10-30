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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;


@RunWith(value = MockitoJUnitRunner.class)
public class CellFactoryTest {

    @InjectMocks
    private CellFactory cellFactory;

    @Mock
    private ReportGeneratorHelper reportGeneratorHelper;

    private Row<PDPage> row;

    private final String cellContent = "cellContent";

    private final float cellWidth = 10f;

    @Before
    public void setUp() throws IOException {

        when(reportGeneratorHelper.initializeTable(anyObject(), anyFloat(), anyObject())).thenCallRealMethod();

        BaseTable table = reportGeneratorHelper.initializeTable(new PDDocument(), 1f, new PDPage());

        row = table.createRow(15f);

        assertThat(row.getCells()).isEmpty();
    }

    @Test
    public void addMainTitleCellTest() throws IOException {
        cellFactory.addMainTitleCell(row, cellContent);
        assertThatCellHasGoodValues(cellContent);
    }

    @Test
    public void addSecondaryHeaderCellTest() {
        cellFactory.addSecondaryHeaderCell(row, cellContent);
        assertThatCellHasGoodValues(cellContent);
    }

    @Test
    public void createDataCellTest() {
        cellFactory.createDataCell(row, cellWidth, cellContent);
        assertThatCellHasGoodValues(cellContent, cellWidth);
    }

    @Test
    public void createDataCellWithAlignmentTest() {
        cellFactory.createDataCell(row, cellWidth, cellContent, HorizontalAlignment.RIGHT);
        assertThatCellHasGoodValues(cellContent, cellWidth);
        assertThat(row.getCells().get(0).getAlign()).isEquivalentAccordingToCompareTo(HorizontalAlignment.RIGHT);
    }

    @Test
    public void createDataHeaderCellTest() {
        cellFactory.createDataHeaderCell(row, cellWidth, cellContent);
        assertThatCellHasGoodValues(cellContent, cellWidth);
    }

    @Test
    public void createDataCellBucketNameTest() {
        cellFactory.createDataCellBucketName(row, cellWidth, cellContent);
        assertThatCellHasGoodValues(cellContent, cellWidth);
    }

    @Test
    public void createKeyValueContentDataCellTest() {
        cellFactory.createKeyValueContentDataCell(row, cellWidth, cellContent);
        assertThatCellHasGoodValues(cellContent, cellWidth);
    }

    @Test
    public void testThatCreateIconCellThrowExceptionWithBadPath() {
        String path = "badPath";
        cellFactory.createIconCell(row, cellWidth, path);
        assertThat(row.getCells().get(0).getWidth()).isWithin(0.001f).of((row.getWidth() * cellWidth) / 100);
        assertThat(row.getCells().get(0).getText()).isEqualTo(path);
    }

    private void assertThatCellHasGoodValues(String content) {
        assertThat(row.getCells()).hasSize(1);
        assertThat(row.getCells().get(0).getText()).isEqualTo(content);
    }

    private void assertThatCellHasGoodValues(String content, float width) {
        assertThatCellHasGoodValues(content);
        assertThat(row.getCells().get(0).getWidth()).isWithin(0.001f).of((row.getWidth() * width) / 100);
    }
}
