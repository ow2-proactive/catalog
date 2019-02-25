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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
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

    @Test
    public void testParseWorkflowWithCatalogObjectModelVariables() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow_variables_with_catalog_object_model.xml");

        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("test_variables_with_model");
        assertThat(findUniqueValuesForKeyAndLabel(result,
                                                  "depends_on",
                                                  "dependencies")).contains("basic-examples/Native_Task");
        assertThat(findUniqueValuesForKeyAndLabel(result,
                                                  "depends_on",
                                                  "dependencies")).contains("data-connectors/FTP");
        assertThat(findUniqueValuesForKeyAndLabel(result, "depends_on", "dependencies")).contains("finance/QuantLib");
        assertThat(findUniqueValuesForKeyAndLabel(result,
                                                  "depends_on",
                                                  "dependencies")).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(findUniqueValuesForKeyAndLabel(result, "depends_on", "dependencies")).hasSize(4);
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

    @Test
    public void testParseWorkflowContainingVisualization() throws Exception {
        List<KeyValueLabelMetadataEntity> result = parseWorkflow("workflow-visualization.xml");

        assertThat(result).hasSize(5);

        assertThat(findValueForKeyAndLabel(result, "project_name", "job_information")).isEqualTo("Project Name");
        assertThat(findValueForKeyAndLabel(result, "name", "job_information")).isEqualTo("Valid Workflow");
        assertThat(findValueForKeyAndLabel(result, "description", "General")).isEqualTo("\n" +
                                                                                        "         A catalogObject that executes cmd in JVM. \n" +
                                                                                        "    ");
        assertThat(findValueForKeyAndLabel(result,
                                           "visualization",
                                           "job_information").trim()).isEqualTo(getJobVisualizationExpectedContent());
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

    private List<String> findUniqueValuesForKeyAndLabel(List<KeyValueLabelMetadataEntity> result, String key,
            String label) {
        return new ArrayList<>(new HashSet<>(result.stream()
                                                   .filter(metadata -> metadata.getKey().equals(key) &&
                                                                       metadata.getLabel().equals(label))
                                                   .map(KeyValueLabelMetadataEntity::getValue)
                                                   .collect(Collectors.toList())));

    }

    private String getJobVisualizationExpectedContent() {
        return "<html><head><link rel=\"stylesheet\" href=\"/studio/styles/studio-standalone.css\"><style>\n" +
               "        #workflow-designer {\n" + "            left:0 !important;\n" +
               "            top:0 !important;\n" + "            width:1427px;\n" + "            height:905px;\n" +
               "            }\n" +
               "        </style></head><body><div style=\"position:relative;top:-259px;left:-350.5px\"><div class=\"task _jsPlumb_endpoint_anchor_ ui-draggable active-task\" id=\"jsPlumb_1_1\" style=\"top: 309px; left: 450.5px;\"><a class=\"task-name\"><img src=\"/studio/images/Groovy.png\" width=\"20px\">&nbsp;<span class=\"name\">Groovy_Task</span></a></div><div class=\"_jsPlumb_endpoint source-endpoint dependency-source-endpoint connected _jsPlumb_endpoint_anchor_ ui-draggable ui-droppable\" style=\"position: absolute; height: 20px; width: 20px; left: 491px; top: 339px;\"><svg style=\"position:absolute;left:0px;top:0px\" width=\"20\" height=\"20\" pointer-events=\"all\" position=\"absolute\" version=\"1.1\"\n" +
               "      xmlns=\"http://www.w3.org/1999/xhtml\"><circle cx=\"10\" cy=\"10\" r=\"10\" version=\"1.1\"\n" +
               "      xmlns=\"http://www.w3.org/1999/xhtml\" fill=\"#666\" stroke=\"none\" style=\"\"></circle></svg></div></div></body></html>";
    }

}
