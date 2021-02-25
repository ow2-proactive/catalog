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
package org.ow2.proactive.catalog.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.util.parser.SupportedParserKinds;


/**
 * @author ActiveEon Team
 * @since 14/08/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowInfoAdderTest {

    @InjectMocks
    private WorkflowInfoAdder genericInformationAdder;

    @Mock
    private WorkflowXmlManipulator workflowXmlManipulator;

    @Test
    public void testThatWorkflowParserKindTriggersXmlManipulation() {
        genericInformationAdder.addGenericInformationJobNameToRawObjectIfWorkflow(new byte[] {},
                                                                                  SupportedParserKinds.WORKFLOW.toString() +
                                                                                                 "specific-workflow-kind",
                                                                                  Collections.emptyMap(),
                                                                                  "");

        verify(workflowXmlManipulator).replaceGenericInformationAndNameOnJobLevel(Mockito.any(),
                                                                                  Mockito.any(),
                                                                                  Mockito.any());
    }

    @Test
    public void testThatWorkflowParserKindTriggersXmlManipulation2() {
        genericInformationAdder.addProjectNameToRawObjectIfWorkflow(new byte[] {},
                                                                    SupportedParserKinds.WORKFLOW.toString() +
                                                                                   "specific-workflow-kind",
                                                                    "");

        verify(workflowXmlManipulator).replaceOrAddOrRemoveProjectNameOnJobLevel(Mockito.any(), Mockito.any());
    }

    @Test
    public void testThatOtherKindNotTriggersXmlManipulation() {
        genericInformationAdder.addGenericInformationJobNameToRawObjectIfWorkflow(new byte[] {},
                                                                                  SupportedParserKinds.PCW_RULE.toString(),
                                                                                  Collections.emptyMap(),
                                                                                  "");

        verify(workflowXmlManipulator, times(0)).replaceGenericInformationAndNameOnJobLevel(Mockito.any(),
                                                                                            Mockito.any(),
                                                                                            Mockito.any());
    }

    @Test
    public void testThatOtherKindNotTriggersXmlManipulation2() {
        genericInformationAdder.addProjectNameToRawObjectIfWorkflow(new byte[] {},
                                                                    SupportedParserKinds.PCW_RULE.toString(),
                                                                    "");

        verify(workflowXmlManipulator, times(0)).replaceOrAddOrRemoveProjectNameOnJobLevel(Mockito.any(),
                                                                                           Mockito.any());
    }

}
