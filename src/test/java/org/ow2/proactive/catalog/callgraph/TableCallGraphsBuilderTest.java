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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.report.CellFactory;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;


@RunWith(MockitoJUnitRunner.class)
public class TableCallGraphsBuilderTest {

    @InjectMocks
    private TableCallGraphsBuilder tableCallGraphsBuilder;

    @Mock
    private CellFactory cellFactory;

    @Test
    public void buildCallGraphsTableWithCallGraphHolder0Order() throws IOException {

        CallGraphHolder callGraphHolder = new CallGraphHolder();

        assertThat(callGraphHolder.order()).isEqualTo(0);

        BaseTable baseTable = new ReportGeneratorHelper().initializeTable(new PDDocument(), 1f, new PDPage());

        tableCallGraphsBuilder.buildCallGraphsTable(callGraphHolder, baseTable);

        verify(cellFactory, times(1)).createDataHeaderCell(any(Row.class), eq(100f), anyString());
    }

    @Test
    public void buildCallGraphsTableWithoutCallGraphHolder0Order() throws IOException {

        final String bucketName = "bucket";

        CallGraphHolder callGraphHolder = new CallGraphHolder();
        callGraphHolder.addNode(bucketName, "object", "kind", true);

        assertThat(callGraphHolder.order()).isNotEqualTo(0);

        BaseTable baseTable = new ReportGeneratorHelper().initializeTable(new PDDocument(), 1f, new PDPage());

        tableCallGraphsBuilder.buildCallGraphsTable(callGraphHolder, baseTable);

        assertThat(baseTable.getRows()).hasSize(2);

        verify(cellFactory, times(1)).createDataCellBucketName(any(Row.class), eq(100f), eq(bucketName));

    }

}
