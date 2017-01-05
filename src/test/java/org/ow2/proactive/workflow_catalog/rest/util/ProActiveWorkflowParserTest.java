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
package org.ow2.proactive.workflow_catalog.rest.util;

import static com.google.common.truth.Truth.assertThat;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;


/**
 * Unit tests associated to {@link ProActiveWorkflowParser}.
 *
 * @author ActiveEon Team
 */
public class ProActiveWorkflowParserTest {

    @Test
    public void testParseWorkflow() throws Exception {
        ProActiveWorkflowParserResult result = parseWorkflow("workflow.xml");

        assertThat(result.getJobName()).isEqualTo("Valid Workflow");

        assertThat(result.getProjectName()).isEqualTo("Project Name");

        assertThat(result.getGenericInformation()).containsExactly("genericInfo1",
                                                                   "genericInfo1Value",
                                                                   "genericInfo2",
                                                                   "genericInfo2Value");
        assertThat(result.getVariables()).containsExactly("var1", "var1Value", "var2", "var2Value");
    }

    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        ProActiveWorkflowParserResult result = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(result.getJobName()).isEqualTo("Valid Workflow");

        assertThat(result.getProjectName()).isEqualTo("Project Name");

        assertThat(result.getGenericInformation()).isEmpty();
        assertThat(result.getVariables()).isEmpty();
    }

    @Test
    public void testParseWorkflowWithNoProjectName() throws XMLStreamException {
        ProActiveWorkflowParserResult result = parseWorkflow("workflow-no-project-name.xml");
        assertThat(result.getProjectName()).isEmpty();
    }

    private ProActiveWorkflowParserResult parseWorkflow(String xmlFilename) throws XMLStreamException {
        ProActiveWorkflowParser parser = new ProActiveWorkflowParser(ProActiveWorkflowParserTest.class.getResourceAsStream("/workflows/" +
                                                                                                                           xmlFilename));

        return parser.parse();
    }

}
