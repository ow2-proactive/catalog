/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */

package org.ow2.proactive.workflow_catalog.rest.util;

import org.junit.Test;

import javax.xml.stream.XMLStreamException;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author ActiveEon Team
 */
public class WorkflowParserTest {

    @Test
    public void testParseWorkflow() throws Exception {
        WorkflowParser parser = parseWorkflow("workflow.xml");

        assertThat(parser.getJobName().isPresent()).isTrue();
        assertThat(parser.getJobName().get()).isEqualTo("Valid Workflow");

        assertThat(parser.getProjectName().isPresent()).isTrue();
        assertThat(parser.getProjectName().get()).isEqualTo("Project Name");

        assertThat(parser.getGenericInformation())
                .containsExactly("genericInfo1", "genericInfo1Value", "genericInfo2", "genericInfo2Value");
        assertThat(parser.getVariables())
                .containsExactly("var1", "var1Value", "var2", "var2Value");
    }


    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        WorkflowParser parser = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(parser.getJobName().isPresent()).isTrue();
        assertThat(parser.getJobName().get()).isEqualTo("Valid Workflow");

        assertThat(parser.getProjectName().isPresent()).isTrue();
        assertThat(parser.getProjectName().get()).isEqualTo("Project Name");

        assertThat(parser.getGenericInformation()).isEmpty();
        assertThat(parser.getVariables()).isEmpty();
    }

    private WorkflowParser parseWorkflow(String xmlFilename) throws XMLStreamException {
        WorkflowParser parser =
                new WorkflowParser(
                        WorkflowParserTest.class.getResourceAsStream("/workflows/" + xmlFilename));

        parser.parse();

        return parser;
    }

}