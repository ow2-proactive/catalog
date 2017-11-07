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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                                                             "    bucketName=\"TestGenericInfo\" \n" +
                                                             "    priority=\"normal\"\n" +
                                                             "    onTaskError=\"continueJobExecution\"\n" +
                                                             "     maxNumberOfExecution=\"2\"\n" + ">\n" +
                                                             "  <taskFlow>\n" + "    <task bucketName=\"Task1\">\n" +
                                                             "      <scriptExecutable>\n" + "        <script>\n" +
                                                             "          <code language=\"javascript\">\n" +
                                                             "            <![CDATA[\n" +
                                                             "print(java.lang.System.getProperty('pas.task.bucketName'))\n" +
                                                             "]]>\n" + "          </code>\n" + "        </script>\n" +
                                                             "      </scriptExecutable>\n" + "    </task>\n" +
                                                             "  </taskFlow>\n" + "</job>").getBytes();

    private final byte[] simpleWorkflowWithGenericInfo = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<job\n" +
                                                          "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                          "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                          "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                          "    bucketName=\"TestGenericInfo\" \n" +
                                                          "    priority=\"normal\"\n" +
                                                          "    onTaskError=\"continueJobExecution\"\n" +
                                                          "     maxNumberOfExecution=\"2\"\n" + ">\n" +
                                                          "  <genericInformation>\n" +
                                                          "    <info bucketName=\"first\" value=\"value1\"/>\n" +
                                                          "    <info bucketName=\"second\" value=\"value2\"/>\n" +
                                                          "  </genericInformation>\n" + "  <taskFlow>\n" +
                                                          "    <task bucketName=\"Task1\">\n" +
                                                          "      <scriptExecutable>\n" + "        <script>\n" +
                                                          "          <code language=\"javascript\">\n" +
                                                          "            <![CDATA[\n" +
                                                          "print(java.lang.System.getProperty('pas.task.bucketName'))\n" +
                                                          "]]>\n" + "          </code>\n" + "        </script>\n" +
                                                          "      </scriptExecutable>\n" + "    </task>\n" +
                                                          "  </taskFlow>\n" + "</job>").getBytes();

    private final byte[] workflowWithGenericInfoAtJobAndTaskLevel_BeforeTaskFlow = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                                    "<job\n" +
                                                                                    "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                                    "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                                                    "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                                                    "    bucketName=\"TestGenericInfo\" \n" +
                                                                                    "    priority=\"normal\"\n" +
                                                                                    "    onTaskError=\"continueJobExecution\"\n" +
                                                                                    "     maxNumberOfExecution=\"2\"\n" +
                                                                                    ">\n" + "    <genericInformation>" +
                                                                                    "      <info bucketName=\"first\" value=\"value1\"/>\n" +
                                                                                    "      <info bucketName=\"second\" value=\"value2\"/>\n" +
                                                                                    "    </genericInformation>\n" +
                                                                                    "     <description>\n" +
                                                                                    "        <![CDATA[ Perform anomaly detection of an input image (only pedestrians are supposed to be present) ]]>\n" +
                                                                                    "     </description>\n" +
                                                                                    "    <taskFlow>\n" +
                                                                                    "    <task bucketName=\"Task1\">\n" +
                                                                                    "      <genericInformation>\n" +
                                                                                    "         <info bucketName=\"insideTaskGenInfo\" value=\"TaskGenInfoValue\"/>\n" +
                                                                                    "       </genericInformation>\n" +
                                                                                    "      <scriptExecutable>\n" +
                                                                                    "        <script>\n" +
                                                                                    "          <code language=\"javascript\">\n" +
                                                                                    "            <![CDATA[\n" +
                                                                                    "print(java.lang.System.getProperty('pas.task.bucketName'))\n" +
                                                                                    "]]>\n" + "          </code>\n" +
                                                                                    "        </script>\n" +
                                                                                    "      </scriptExecutable>\n" +
                                                                                    "    </task>\n" +
                                                                                    "  </taskFlow>\n" +
                                                                                    "</job>").getBytes();

    private final byte[] workflowWithGenericInfoAtJobAndTaskLevel_AfterTaskFlow = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                                   "<job\n" +
                                                                                   "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                                   "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                                                   "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                                                   "    bucketName=\"TestGenericInfo\" \n" +
                                                                                   "    priority=\"normal\"\n" +
                                                                                   "    onTaskError=\"continueJobExecution\"\n" +
                                                                                   "     maxNumberOfExecution=\"2\"\n" +
                                                                                   ">\n" + "    <taskFlow>\n" +
                                                                                   "    <task bucketName=\"Task1\">\n" +
                                                                                   "      <genericInformation>\n" +
                                                                                   "         <info bucketName=\"insideTaskGenInfo\" value=\"TaskGenInfoValue\"/>\n" +
                                                                                   "       </genericInformation>\n" +
                                                                                   "      <scriptExecutable>\n" +
                                                                                   "        <script>\n" +
                                                                                   "          <code language=\"javascript\">\n" +
                                                                                   "            <![CDATA[\n" +
                                                                                   "print(java.lang.System.getProperty('pas.task.bucketName'))\n" +
                                                                                   "]]>\n" + "          </code>\n" +
                                                                                   "        </script>\n" +
                                                                                   "      </scriptExecutable>\n" +
                                                                                   "    </task>\n" + "  </taskFlow>\n" +
                                                                                   "     <description>\n" +
                                                                                   "        <![CDATA[ Perform anomaly detection of an input image (only pedestrians are supposed to be present) ]]>\n" +
                                                                                   "     </description>\n" +
                                                                                   "    <genericInformation>" +
                                                                                   "      <info bucketName=\"first\" value=\"value1\"/>\n" +
                                                                                   "      <info bucketName=\"second\" value=\"value2\"/>\n" +
                                                                                   "    </genericInformation>\n" +
                                                                                   "</job>").getBytes();

    @Spy
    private WorkflowXmlManipulator workflowXmlManipulator;

    private final static Pattern genericInformationPatternInsideTaskFlow = Pattern.compile(WorkflowXmlManipulator.ANY_CHARACTER_OR_NEW_LINE +
                                                                                           WorkflowXmlManipulator.TASK_FLOW_START_TAG +
                                                                                           WorkflowXmlManipulator.ANY_CHARACTER_OR_NEW_LINE +
                                                                                           "(" +
                                                                                           WorkflowXmlManipulator.GENERIC_INFO_TAG_ENTITY +
                                                                                           ")" +
                                                                                           WorkflowXmlManipulator.ANY_CHARACTER_OR_NEW_LINE +
                                                                                           WorkflowXmlManipulator.TASK_FLOW_END_TAG +
                                                                                           WorkflowXmlManipulator.ANY_CHARACTER_OR_NEW_LINE);

    @Test
    public void testThatWorkflowHasGenericInfoTagAdded() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(simpleWorkflowWithoutGenericInfo,
                                                                                                      Collections.emptyMap()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).doesNotContain("<info bucketName="); // Generic Info has no Entry
    }

    @Test
    public void testThatWorkflowHasGenericInfoRemovedIfAlreadyThere() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                      Collections.emptyMap()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).doesNotContain("<info bucketName="); // Generic Info has no Entry
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedIfAlreadyThere() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                      this.getTwoSimpleEntries()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).contains("<info bucketName=\"firstTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"firstTestValue\"");
        assertThat(emptyGenericInfo).contains("<info bucketName=\"secondTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedOnlyOnJobLevelBeforeTaskFlowButNotInTask() {
        String genericInfoAdded = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(workflowWithGenericInfoAtJobAndTaskLevel_BeforeTaskFlow,
                                                                                                      this.getTwoSimpleEntries()));

        Matcher matcherGenericInfoInsideTask = genericInformationPatternInsideTaskFlow.matcher(genericInfoAdded);
        String genericInfoInTaskFlow = null;

        boolean matcherGenericInfoInsideTaskFound = matcherGenericInfoInsideTask.find();
        if (matcherGenericInfoInsideTaskFound) {
            genericInfoInTaskFlow = matcherGenericInfoInsideTask.group(1);
        }

        assertThat(genericInfoAdded).contains("<genericInformation>");
        assertThat(genericInfoAdded).contains("</genericInformation>");
        assertThat(genericInfoAdded).contains("<info bucketName=\"firstTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"firstTestValue\"");
        assertThat(genericInfoAdded).contains("<info bucketName=\"secondTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"secondTestValue\"");

        //check that genericInfo are not modified inside on task level
        assertThat(matcherGenericInfoInsideTaskFound).isTrue();
        assertThat(genericInfoInTaskFlow).contains("<info bucketName=\"insideTaskGenInfo\"");
        assertThat(genericInfoInTaskFlow).contains("value=\"TaskGenInfoValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"firstTestValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedOnlyOnJobLevelAfterTaskFlowButNotInTask() {
        String genericInfoAdded = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(workflowWithGenericInfoAtJobAndTaskLevel_AfterTaskFlow,
                                                                                                      this.getTwoSimpleEntries()));

        Matcher matcherGenericInfoInsideTask = genericInformationPatternInsideTaskFlow.matcher(genericInfoAdded);
        String genericInfoInTaskFlow = null;

        boolean matcherGenericInfoInsideTaskFound = matcherGenericInfoInsideTask.find();
        if (matcherGenericInfoInsideTaskFound) {
            genericInfoInTaskFlow = matcherGenericInfoInsideTask.group(1);
        }

        assertThat(genericInfoAdded).contains("<genericInformation>");
        assertThat(genericInfoAdded).contains("</genericInformation>");
        assertThat(genericInfoAdded).contains("<info bucketName=\"firstTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"firstTestValue\"");
        assertThat(genericInfoAdded).contains("<info bucketName=\"secondTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"secondTestValue\"");

        assertThat(matcherGenericInfoInsideTaskFound).isTrue();
        assertThat(genericInfoInTaskFlow).contains("<info bucketName=\"insideTaskGenInfo\"");
        assertThat(genericInfoInTaskFlow).contains("value=\"TaskGenInfoValue\"");

        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"firstTestValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericAllGenericInfoAddedIfItWasNotThereBefore() {
        String emptyGenericInfo = new String(workflowXmlManipulator.replaceGenericInformationJobLevel(simpleWorkflowWithoutGenericInfo,
                                                                                                      this.getTwoSimpleEntries()));
        assertThat(emptyGenericInfo).contains("<genericInformation>");
        assertThat(emptyGenericInfo).contains("</genericInformation>");
        assertThat(emptyGenericInfo).contains("<info bucketName=\"firstTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"firstTestValue\"");
        assertThat(emptyGenericInfo).contains("<info bucketName=\"secondTestKey\"");
        assertThat(emptyGenericInfo).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfAnyParameterIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationJobLevel(null, null);
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfXmlWorkflowIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationJobLevel(null, Collections.emptyMap());
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfGenericInfoIsEntriesNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationJobLevel(new byte[] {}, null);
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    private Map<String, String> getTwoSimpleEntries() {
        Map<String, String> returnList = new HashMap<>();

        returnList.put("firstTestKey", "firstTestValue");
        returnList.put("secondTestKey", "secondTestValue");

        return returnList;
    }

}
