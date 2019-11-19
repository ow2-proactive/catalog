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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.util.ReportGeneratorHelper;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;


/**
 * Created by mael on 30/10/19.
 */
@RunWith(value = MockitoJUnitRunner.class)
public class HeadersBuilderTest {

    @InjectMocks
    private HeadersBuilder headersBuilder;

    @Mock
    private CellFactory cellFactory;

    @Test
    public void createInfoHeaderTest() throws IOException {
        BaseTable table = new ReportGeneratorHelper().initializeTable(new PDDocument(), 1f, new PDPage());

        headersBuilder.createInfoHeader(table, Collections.EMPTY_SET, Optional.empty(), Optional.empty());
        headersBuilder.createInfoHeader(table,
                                        Collections.EMPTY_SET,
                                        Collections.EMPTY_SET,
                                        Optional.empty(),
                                        Optional.empty());

        verify(cellFactory, times(2)).addSecondaryHeaderCell(any(Row.class), anyString());
    }
}
