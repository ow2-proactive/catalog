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
package org.ow2.proactive.catalog.util;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;

import org.junit.Test;
import org.ow2.proactive.catalog.util.CatalogObjectJSONParser.CatalogObjectData;


/**
 * Unit tests associated to {@link CatalogObjectJSONParser}.
 *
 * @author ActiveEon Team
 */
public class CatalogObjectJSONParserTest {

    @Test
    public void testParseJSON() throws Exception {

        File file = new File(CatalogObjectJSONParserTest.class.getResource("/objects/workflow.json").getPath());

        CatalogObjectData data = CatalogObjectJSONParser.parseJSONFile(file);

        assertThat(data.getKind()).isEqualTo("workflow");
        assertThat(data.getName()).isEqualTo("workflow name");
        assertThat(data.getCommitMessage()).isEqualTo("First commit");
        assertThat(data.getUsername()).isEqualTo("username");
        assertThat(data.getObjectFileName()).isEqualTo("myFileName.xml");
        assertThat(data.getContentType()).isEqualTo("application/xml");
    }

}
