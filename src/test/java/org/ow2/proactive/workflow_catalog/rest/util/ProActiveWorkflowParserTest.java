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

        assertThat(result.getGenericInformation())
                .containsExactly("genericInfo1", "genericInfo1Value", "genericInfo2", "genericInfo2Value");
        assertThat(result.getVariables())
                .containsExactly("var1", "var1Value", "var2", "var2Value");
    }

    @Test
    public void testParseWorkflowContainingNoGenericInformationAndNoVariable() throws Exception {
        ProActiveWorkflowParserResult result = parseWorkflow("workflow-no-generic-information-no-variable.xml");

        assertThat(result.getJobName()).isEqualTo("Valid Workflow");

        assertThat(result.getProjectName()).isEqualTo("Project Name");

        assertThat(result.getGenericInformation()).isEmpty();
        assertThat(result.getVariables()).isEmpty();
    }

    private ProActiveWorkflowParserResult parseWorkflow(String xmlFilename) throws XMLStreamException {
        ProActiveWorkflowParser parser =
                new ProActiveWorkflowParser(
                        ProActiveWorkflowParserTest.class.getResourceAsStream("/workflows/" + xmlFilename));

        return parser.parse();
    }

}