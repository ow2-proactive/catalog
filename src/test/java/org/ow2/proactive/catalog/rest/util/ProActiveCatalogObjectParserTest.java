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
package org.ow2.proactive.catalog.rest.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.ow2.proactive.catalog.rest.entity.KeyValueMetadata;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParser;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserFactory;


/**
 * Unit tests associated to {@link CatalogObjectParser}.
 *
 * @author ActiveEon Team
 */
public class ProActiveCatalogObjectParserTest {

    @Test
    public void testParseWorkflow() throws Exception {
        List<KeyValueMetadata> result = parseWorkflow("workflow.xml");

        assertThat(result).hasSize(4);
        assertKeyValueDataAre(result.get(0), "var1", "var1Value", "variable");
        assertKeyValueDataAre(result.get(1), "var2", "var2Value", "variable");
        assertKeyValueDataAre(result.get(2), "genericInfo1", "genericInfo1Value", "generic_information");
        assertKeyValueDataAre(result.get(3), "genericInfo2", "genericInfo2Value", "generic_information");

    }

    private static void assertKeyValueDataAre(KeyValueMetadata data, String key, String value, String type) {
        assertTrue(data.getKey().equals(key) && data.getValue().equals(value) && data.getLabel().equals(type));
    }

    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        List<KeyValueMetadata> result = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(result).isEmpty();
    }

    private List<KeyValueMetadata> parseWorkflow(String xmlFilename) throws XMLStreamException {
        CatalogObjectParser parser = CatalogObjectParserFactory.get().getParser("workflow");

        return parser.parse(ProActiveCatalogObjectParserTest.class.getResourceAsStream("/workflows/" + xmlFilename));
    }

}
