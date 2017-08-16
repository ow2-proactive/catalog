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

import static com.google.common.truth.Truth.assertThat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * @author ActiveEon Team
 * @since 16/08/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowXmlManipulatorTest {

    private final byte[] simpleWorkflowWithoutGenericInfo = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<job\n" +
                                                             "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                             "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                             "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                             "    name=\"TestGenericInfo\" \n" +
                                                             "    priority=\"normal\"\n" +
                                                             "    onTaskError=\"continueJobExecution\"\n" +
                                                             "     maxNumberOfExecution=\"2\"\n" + ">\n" +
                                                             "  <taskFlow>\n" + "    <task name=\"Task1\">\n" +
                                                             "      <scriptExecutable>\n" + "        <script>\n" +
                                                             "          <code language=\"javascript\">\n" +
                                                             "            <![CDATA[\n" +
                                                             "print(java.lang.System.getProperty('pas.task.name'))\n" +
                                                             "]]>\n" + "          </code>\n" + "        </script>\n" +
                                                             "      </scriptExecutable>\n" + "    </task>\n" +
                                                             "  </taskFlow>\n" + "</job>").getBytes();

    private final byte[] simpleWorkflowWithGenericInfo = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<job\n" +
                                                          "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                          "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                          "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                          "    name=\"TestGenericInfo\" \n" +
                                                          "    priority=\"normal\"\n" +
                                                          "    onTaskError=\"continueJobExecution\"\n" +
                                                          "     maxNumberOfExecution=\"2\"\n" + ">\n" +
                                                          "  <genericInformation>\n" +
                                                          "    <info name=\"first\" value=\"value1\"/>\n" +
                                                          "    <info name=\"second\" value=\"value2\"/>\n" +
                                                          "  </genericInformation>\n" + "  <taskFlow>\n" +
                                                          "    <task name=\"Task1\">\n" + "      <scriptExecutable>\n" +
                                                          "        <script>\n" +
                                                          "          <code language=\"javascript\">\n" +
                                                          "            <![CDATA[\n" +
                                                          "print(java.lang.System.getProperty('pas.task.name'))\n" +
                                                          "]]>\n" + "          </code>\n" + "        </script>\n" +
                                                          "      </scriptExecutable>\n" + "    </task>\n" +
                                                          "  </taskFlow>\n" + "</job>").getBytes();

    @Spy
    private WorkflowXmlManipulator workflowXmlManipulator;

    @Test
    public void testThatWorkflowHasGenericInfoTagAdded() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformation(simpleWorkflowWithoutGenericInfo,
                                                                                              Collections.emptyList()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).doesNotContain("<info name="); // Generic Info has no Entry
    }

    @Test
    public void testThatWorkflowHasGenericInfoRemovedIfAlreadyThere() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformation(simpleWorkflowWithGenericInfo,
                                                                                              Collections.emptyList()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).doesNotContain("<info name="); // Generic Info has no Entry
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedIfAlreadyThere() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformation(simpleWorkflowWithGenericInfo,
                                                                                              this.getTwoSimpleEntries()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).contains("<info name=\"firstTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"firstTestValue\"");
        assertThat(emptyGenericInfo).contains("<info name=\"secondTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericAllGenericInfoAddedIfItWasNotThereBefore() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformation(simpleWorkflowWithoutGenericInfo,
                                                                                              this.getTwoSimpleEntries()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).contains("<info name=\"firstTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"firstTestValue\"");
        assertThat(emptyGenericInfo).contains("<info name=\"secondTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfAnyParameterIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformation(null, null);
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfXmlWorkflowIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformation(null, Collections.emptyList());
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfGenericInfoIsEntriesNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformation(new byte[] {}, null);
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    private List<AbstractMap.SimpleImmutableEntry<String, String>> getTwoSimpleEntries() {
        List<AbstractMap.SimpleImmutableEntry<String, String>> returnList = new ArrayList<>();

        returnList.add(new AbstractMap.SimpleImmutableEntry<>("firstTestKey", "firstTestValue"));
        returnList.add(new AbstractMap.SimpleImmutableEntry<>("secondTestKey", "secondTestValue"));

        return returnList;
    }

}
