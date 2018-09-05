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
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;


/**
 * Unit tests associated to {@link WorkflowParser}.
 *
 * @author ActiveEon Team
 */
public class ProActiveCatalogObjectParserTest {

    @Test
    public void testParseWorkflow() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow.xml");

        assertThat(result).hasSize(7);
        assertKeyValueDataAre(result.get(0), "project_name", "Project Name", "job_information");
        assertKeyValueDataAre(result.get(1), "name", "Valid Workflow", "job_information");
        assertKeyValueDataAre(result.get(2), "var1", "var1Value", "variable");
        assertKeyValueDataAre(result.get(3), "var2", "var2Value", "variable");
        assertKeyValueDataAre(result.get(4),
                              "description",
                              "\n" + "         A catalogObject that executes cmd in JVM. \n" + "    ",
                              "General");
        assertKeyValueDataAre(result.get(5), "genericInfo1", "genericInfo1Value", "generic_information");
        assertKeyValueDataAre(result.get(6), "genericInfo2", "genericInfo2Value", "generic_information");

    }

    @Test
    public void testParseWorkflowWithModelVariables() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow_variables_with_model.xml");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "test_variables", "job_information");
        assertKeyValueDataAre(result.get(1), "keyWithoutModel", "valueWithoutModel", "variable");
        assertKeyValueDataAre(result.get(2), "keyInteger", "1", "variable");
        assertKeyValueDataAre(result.get(3), "keyInteger", "PA:Integer", "variable_model");
        assertKeyValueDataAre(result.get(4), "keyGeneral", "valueGeneral", "variable");
        assertKeyValueDataAre(result.get(5), "emptyValue", "", "variable");
    }

    private static void assertKeyValueDataAre(KeyValueLabelMetadataEntity data, String key, String value,
            String label) {
        assertTrue(data.getKey().equals(key) && data.getValue().equals(value) && data.getLabel().equals(label));
    }

    @Test
    public void testParseWorkflowContainingNoName() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-no-name.xml");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "project_name", "Project Name", "job_information");
        assertKeyValueDataAre(result.get(1), "var1", "var1Value", "variable");
        assertKeyValueDataAre(result.get(2), "var2", "var2Value", "variable");
        assertKeyValueDataAre(result.get(3),
                              "description",
                              "\n" + "         A catalogObject that executes cmd in JVM. \n" + "    ",
                              "General");
        assertKeyValueDataAre(result.get(4), "genericInfo1", "genericInfo1Value", "generic_information");
        assertKeyValueDataAre(result.get(5), "genericInfo2", "genericInfo2Value", "generic_information");
    }

    @Test
    public void testParseWorkflowContainingNoProjectName() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-no-project-name.xml");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "Valid Workflow", "job_information");
        assertKeyValueDataAre(result.get(1), "var1", "var1Value", "variable");
        assertKeyValueDataAre(result.get(2), "var2", "var2Value", "variable");
        assertKeyValueDataAre(result.get(3),
                              "description",
                              "\n" + "         A catalogObject that executes cmd in JVM. \n" + "    ",
                              "General");
        assertKeyValueDataAre(result.get(4), "genericInfo1", "genericInfo1Value", "generic_information");
        assertKeyValueDataAre(result.get(5), "genericInfo2", "genericInfo2Value", "generic_information");
    }

    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(result).hasSize(3);
        assertKeyValueDataAre(result.get(0), "project_name", "Project Name", "job_information");
        assertKeyValueDataAre(result.get(1), "name", "Valid Workflow", "job_information");
        assertKeyValueDataAre(result.get(2),
                              "description",
                              "\n" + "         A catalogObject that executes cmd in JVM. \n" + "    ",
                              "General");
    }

    private List<KeyValueLabelMetadataEntity> parseWorkflow(String xmlFilename) throws XMLStreamException {
        AbstractCatalogObjectParser parser = new WorkflowParser();

        return parser.parse(ProActiveCatalogObjectParserTest.class.getResourceAsStream("/workflows/" + xmlFilename));
    }

}
