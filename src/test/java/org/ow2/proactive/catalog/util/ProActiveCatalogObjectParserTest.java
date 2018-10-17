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

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;


/**
 * Unit tests associated to {@link WorkflowParser}.
 *
 * @author ActiveEon Team
 */
public class ProActiveCatalogObjectParserTest {

    @Test
    public void testParseWorkflow() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow.xml");

        assertThat(result).hasSize(8);

        assertThat(findValueForKeyAndLabel(result, "project_name", "job_information")).isEqualTo("Project Name");
        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("Valid Workflow");
        assertThat(findValueForKeyAndLabel(result, "var1", "variable")).isEqualTo("var1Value");
        assertThat(findValueForKeyAndLabel(result, "var2", "variable")).isEqualTo("var2Value");
        assertThat(findValueForKeyAndLabel(result, "description", "General")).isEqualTo("\n" +
                                                                                        "         A catalogObject that executes cmd in JVM. \n" +
                                                                                        "    ");
        assertThat(findValueForKeyAndLabel(result,
                                           "genericInfo1",
                                           "generic_information")).isEqualTo("genericInfo1Value");
        assertThat(findValueForKeyAndLabel(result,
                                           "genericInfo2",
                                           "generic_information")).isEqualTo("genericInfo2Value");
    }

    @Test
    public void testParseWorkflowWithModelVariables() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow_variables_with_model.xml");

        assertThat(result).hasSize(7);

        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("test_variables");
        assertThat(findValueForKeyAndLabel(result, "keyWithoutModel", "variable")).isEqualTo("valueWithoutModel");
        assertThat(findValueForKeyAndLabel(result, "keyInteger", "variable")).isEqualTo("1");
        assertThat(findValueForKeyAndLabel(result, "keyInteger", "variable_model")).isEqualTo("PA:Integer");
        assertThat(findValueForKeyAndLabel(result, "keyGeneral", "variable")).isEqualTo("valueGeneral");
        assertThat(findValueForKeyAndLabel(result, "emptyValue", "variable")).isEqualTo("");
    }

    @Test(expected = ParsingObjectException.class)
    public void testParseWorkflowContainingNoName() throws Exception {
        parseWorkflow("workflow-no-name.xml");
    }

    @Test
    public void testParseWorkflowContainingNoProjectName() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-no-project-name.xml");

        assertThat(result).hasSize(7);

        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("Valid Workflow");
        assertThat(findValueForKeyAndLabel(result, "var1", "variable")).isEqualTo("var1Value");
        assertThat(findValueForKeyAndLabel(result, "var2", "variable")).isEqualTo("var2Value");
        assertThat(findValueForKeyAndLabel(result, "description", "General")).isEqualTo("\n" +
                                                                                        "         A catalogObject that executes cmd in JVM. \n" +
                                                                                        "    ");
        assertThat(findValueForKeyAndLabel(result,
                                           "genericInfo1",
                                           "generic_information")).isEqualTo("genericInfo1Value");
        assertThat(findValueForKeyAndLabel(result,
                                           "genericInfo2",
                                           "generic_information")).isEqualTo("genericInfo2Value");
        assertThat(findValueForKeyAndLabel(result,
                                           "main.icon",
                                           "General")).isEqualTo("/automation-dashboard/styles/patterns/img/wf-icons/wf-default-icon.png");
    }

    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(result).hasSize(4);

        assertThat(findValueForKeyAndLabel(result, "project_name", "job_information")).isEqualTo("Project Name");
        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("Valid Workflow");
        assertThat(findValueForKeyAndLabel(result, "description", "General")).isEqualTo("\n" +
                                                                                        "         A catalogObject that executes cmd in JVM. \n" +
                                                                                        "    ");
    }

    private List<KeyValueLabelMetadataEntity> parseWorkflow(String xmlFilename) throws XMLStreamException {
        AbstractCatalogObjectParser parser = new WorkflowParser();

        return parser.parse(ProActiveCatalogObjectParserTest.class.getResourceAsStream("/workflows/" + xmlFilename));
    }

    private String findValueForKeyAndLabel(List<KeyValueLabelMetadataEntity> result, String key, String label) {
        return result.stream()
                     .filter(metadata -> metadata.getKey().equals(key) && metadata.getLabel().equals(label))
                     .findAny()
                     .get()
                     .getValue();
    }

}
