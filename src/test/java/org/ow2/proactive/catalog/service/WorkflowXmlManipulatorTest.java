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
import static org.junit.Assert.assertTrue;

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

    protected final static String TASK_FLOW_START_TAG = "<taskFlow>";

    protected final static String TASK_FLOW_END_TAG = "<\\/taskFlow>";

    protected final static String GENERIC_INFORMATION_START_TAG = "<genericInformation>";

    protected final static String GENERIC_INFORMATION_END_TAG = "<\\/genericInformation>";

    protected final static String ANY_CHARACTER_OR_NEW_LINE = "[\\S\\s]*";

    protected final static String PYTHON_CODE_REGEXP = "for x in range\\(1, 11\\):[\\r\\n]+    print x[\\r\\n]+";

    protected final static String GENERIC_INFO_TAG_ENTITY = GENERIC_INFORMATION_START_TAG + "[\\D\\d]*?" +
                                                            GENERIC_INFORMATION_END_TAG + "[\\r\\n]";

    private final byte[] simpleWorkflowWithoutGenericInfo = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<job\n" +
                                                             "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                             "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                             "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                             "    name=\"TestGenericInfo\" \n" +
                                                             "    priority=\"normal\"\n" +
                                                             "    onTaskError=\"continueJobExecution\"\n" +
                                                             "     maxNumberOfExecution=\"2\"\n" + ">\n" +
                                                             "  <taskFlow>\n" + "   " + " <task name=\"Task1\">\n" +
                                                             "      <scriptExecutable>\n" + "      " + "  <script>\n" +
                                                             "          <code language=\"python\">\n" +
                                                             "            <![CDATA[\n" + "for x in range(1, 11):\n" +
                                                             "    print x\n" + "]]>\n" + "      " + "    </code>\n" +
                                                             "       " + " </script>\n" +
                                                             "      </scriptExecutable>\n" + "   " + " </task>\n" +
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
                                                          "          <code language=\"python\">\n" +
                                                          "            <![CDATA[\n" + "for x in range(1, 11):\n" +
                                                          "    print x\n" + "]]>\n" + "          </code>\n" +
                                                          "        </script>\n" + "      </scriptExecutable>\n" +
                                                          "    </task>\n" + "  </taskFlow>\n" + "</job>").getBytes();

    private final byte[] workflowWithGenericInfoAtJobAndTaskLevel_BeforeTaskFlow = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                                    "<job\n" +
                                                                                    "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                                    "  xmlns=\"urn:proactive:jobdescriptor:3.8\"\n" +
                                                                                    "     xsi:schemaLocation=\"urn:proactive:jobdescriptor:3.8 http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/3.8/schedulerjob.xsd\"\n" +
                                                                                    "    name=\"TestGenericInfo\" \n" +
                                                                                    "    priority=\"normal\"\n" +
                                                                                    "    onTaskError=\"continueJobExecution\"\n" +
                                                                                    "     maxNumberOfExecution=\"2\"\n" +
                                                                                    ">\n" + "    <genericInformation>" +
                                                                                    "      <info name=\"first\" value=\"value1\"/>\n" +
                                                                                    "      <info name=\"second\" value=\"value2\"/>\n" +
                                                                                    "    </genericInformation>\n" +
                                                                                    "     <description>\n" +
                                                                                    "        <![CDATA[ Perform anomaly detection of an input image (only pedestrians are supposed to be present) ]]>\n" +
                                                                                    "     </description>\n" +
                                                                                    "    <taskFlow>\n" +
                                                                                    "    <task name=\"Task1\">\n" +
                                                                                    "      <genericInformation>\n" +
                                                                                    "         <info name=\"insideTaskGenInfo\" value=\"TaskGenInfoValue\"/>\n" +
                                                                                    "       </genericInformation>\n" +
                                                                                    "      <scriptExecutable>\n" +
                                                                                    "        <script>\n" +
                                                                                    "          <code language=\"python\">\n" +
                                                                                    "            <![CDATA[\n" +
                                                                                    "for x in range(1, 11):\n" +
                                                                                    "    print x\n" + "]]>\n" +
                                                                                    "          </code>\n" +
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
                                                                                   "    name=\"TestGenericInfo\" \n" +
                                                                                   "    priority=\"normal\"\n" +
                                                                                   "    onTaskError=\"continueJobExecution\"\n" +
                                                                                   "     maxNumberOfExecution=\"2\"\n" +
                                                                                   ">\n" + "    <taskFlow>\n" +
                                                                                   "    <task name=\"Task1\">\n" +
                                                                                   "      <genericInformation>\n" +
                                                                                   "         <info name=\"insideTaskGenInfo\" value=\"TaskGenInfoValue\"/>\n" +
                                                                                   "       </genericInformation>\n" +
                                                                                   "      <scriptExecutable>\n" +
                                                                                   "        <script>\n" +
                                                                                   "          <code language=\"python\">\n" +
                                                                                   "            <![CDATA[\n" +
                                                                                   "for x in range(1, 11):\n" +
                                                                                   "    print x\n" + "]]>\n" +
                                                                                   "          </code>\n" +
                                                                                   "        </script>\n" +
                                                                                   "      </scriptExecutable>\n" +
                                                                                   "    </task>\n" + "  </taskFlow>\n" +
                                                                                   "     <description>\n" +
                                                                                   "        <![CDATA[ Perform anomaly detection of an input image (only pedestrians are supposed to be present) ]]>\n" +
                                                                                   "     </description>\n" +
                                                                                   "    <genericInformation>" +
                                                                                   "      <info name=\"first\" value=\"value1\"/>\n" +
                                                                                   "      <info name=\"second\" value=\"value2\"/>\n" +
                                                                                   "    </genericInformation>\n" +
                                                                                   "</job>").getBytes();

    @Spy
    private WorkflowXmlManipulator workflowXmlManipulator;

    private final static Pattern genericInformationPatternInsideTaskFlow = Pattern.compile(ANY_CHARACTER_OR_NEW_LINE +
                                                                                           TASK_FLOW_START_TAG +
                                                                                           ANY_CHARACTER_OR_NEW_LINE +
                                                                                           "(" +
                                                                                           GENERIC_INFO_TAG_ENTITY +
                                                                                           ")" +
                                                                                           ANY_CHARACTER_OR_NEW_LINE +
                                                                                           TASK_FLOW_END_TAG +
                                                                                           ANY_CHARACTER_OR_NEW_LINE);

    private static Pattern pythonCodePattern = Pattern.compile(PYTHON_CODE_REGEXP);

    @Test
    public void testThatWorkflowHasGenericInfoTagAdded() {
        String modifiedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithoutGenericInfo,
                                                                                                               Collections.emptyMap(),
                                                                                                               ""));
        assertThat(modifiedWorkflow).contains("<genericInformation/>");
        assertThat(modifiedWorkflow).doesNotContain("<info name="); // Generic Info has no Entry
    }

    @Test
    public void testThatFormattingDoesNotChangePythonIdentation() {
        String formattedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithoutGenericInfo,
                                                                                                                Collections.emptyMap(),
                                                                                                                ""));
        Matcher pythonCodeMatcher = pythonCodePattern.matcher(formattedWorkflow);
        assertTrue("Unmodified python code should be found in " + formattedWorkflow, pythonCodeMatcher.find());

    }

    @Test
    public void testThatWorkflowHasGenericInfoRemovedIfAlreadyThere() {
        String modifiedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               Collections.emptyMap(),
                                                                                                               ""));
        assertThat(modifiedWorkflow).contains("<genericInformation/>");
        assertThat(modifiedWorkflow).doesNotContain("<info name="); // Generic Info has no Entry
    }

    @Test
    public void testThatWorkflowHasJobNameReplaced() {
        String modifiedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               Collections.emptyMap(),
                                                                                                               "newJobName"));
        assertThat(modifiedWorkflow).contains("name=\"newJobName\"");
        assertThat(modifiedWorkflow).doesNotContain("name=\"TestGenericInfo\"");
    }

    @Test
    public void testThatWorkflowHasProjectNameReplaced() {
        String modifiedWorkflow1 = new String(workflowXmlManipulator.replaceOrAddOrRemoveProjectNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               "newProjectName"));
        assertThat(modifiedWorkflow1).contains("projectName=\"newProjectName\"");

        String modifiedWorkflow2 = new String(workflowXmlManipulator.replaceOrAddOrRemoveProjectNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               ""));
        assertThat(modifiedWorkflow2).doesNotContain("projectName");

        String modifiedWorkflow3 = new String(workflowXmlManipulator.replaceOrAddOrRemoveProjectNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               "newProjectName2"));
        assertThat(modifiedWorkflow3).contains("projectName=\"newProjectName2\"");

        String modifiedWorkflow4 = new String(workflowXmlManipulator.replaceOrAddOrRemoveProjectNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               "   "));
        assertThat(modifiedWorkflow4).doesNotContain("projectName");

    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedIfAlreadyThere() {
        String modifiedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithGenericInfo,
                                                                                                               this.getTwoSimpleEntries(),
                                                                                                               ""));
        assertThat(modifiedWorkflow).contains("<genericInformation>");
        assertThat(modifiedWorkflow).contains("</genericInformation>");
        assertThat(modifiedWorkflow).contains("<info name=\"firstTestKey\"");
        assertThat(modifiedWorkflow).contains("value=\"firstTestValue\"");
        assertThat(modifiedWorkflow).contains("<info name=\"secondTestKey\"");
        assertThat(modifiedWorkflow).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedOnlyOnJobLevelBeforeTaskFlowButNotInTask() {
        String genericInfoAdded = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(workflowWithGenericInfoAtJobAndTaskLevel_BeforeTaskFlow,
                                                                                                               this.getTwoSimpleEntries(),
                                                                                                               ""));

        Matcher matcherGenericInfoInsideTask = genericInformationPatternInsideTaskFlow.matcher(genericInfoAdded);
        String genericInfoInTaskFlow = null;

        boolean matcherGenericInfoInsideTaskFound = matcherGenericInfoInsideTask.find();
        if (matcherGenericInfoInsideTaskFound) {
            genericInfoInTaskFlow = matcherGenericInfoInsideTask.group(1);
        }

        assertThat(genericInfoAdded).contains("<genericInformation>");
        assertThat(genericInfoAdded).contains("</genericInformation>");
        assertThat(genericInfoAdded).contains("<info name=\"firstTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"firstTestValue\"");
        assertThat(genericInfoAdded).contains("<info name=\"secondTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"secondTestValue\"");

        //check that genericInfo are not modified inside on task level
        assertThat(matcherGenericInfoInsideTaskFound).isTrue();
        assertThat(genericInfoInTaskFlow).contains("<info name=\"insideTaskGenInfo\"");
        assertThat(genericInfoInTaskFlow).contains("value=\"TaskGenInfoValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"firstTestValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericInfoReplacedOnlyOnJobLevelAfterTaskFlowButNotInTask() {
        String genericInfoAdded = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(workflowWithGenericInfoAtJobAndTaskLevel_AfterTaskFlow,
                                                                                                               this.getTwoSimpleEntries(),
                                                                                                               ""));

        Matcher matcherGenericInfoInsideTask = genericInformationPatternInsideTaskFlow.matcher(genericInfoAdded);
        String genericInfoInTaskFlow = null;

        boolean matcherGenericInfoInsideTaskFound = matcherGenericInfoInsideTask.find();
        if (matcherGenericInfoInsideTaskFound) {
            genericInfoInTaskFlow = matcherGenericInfoInsideTask.group(1);
        }

        assertThat(genericInfoAdded).contains("<genericInformation>");
        assertThat(genericInfoAdded).contains("</genericInformation>");
        assertThat(genericInfoAdded).contains("<info name=\"firstTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"firstTestValue\"");
        assertThat(genericInfoAdded).contains("<info name=\"secondTestKey\"");
        assertThat(genericInfoAdded).contains("value=\"secondTestValue\"");

        assertThat(matcherGenericInfoInsideTaskFound).isTrue();
        assertThat(genericInfoInTaskFlow).contains("<info name=\"insideTaskGenInfo\"");
        assertThat(genericInfoInTaskFlow).contains("value=\"TaskGenInfoValue\"");

        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"firstTestValue\"");
        assertThat(genericInfoInTaskFlow).doesNotContain("value=\"secondTestValue\"");
    }

    @Test
    public void testThatWorkflowHasGenericAllGenericInfoAddedIfItWasNotThereBefore() {
        String modifiedWorkflow = new String(workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(simpleWorkflowWithoutGenericInfo,
                                                                                                               this.getTwoSimpleEntries(),
                                                                                                               ""));
        assertThat(modifiedWorkflow).contains("<genericInformation>");
        assertThat(modifiedWorkflow).contains("</genericInformation>");
        assertThat(modifiedWorkflow).contains("<info name=\"firstTestKey\"");
        assertThat(modifiedWorkflow).contains("value=\"firstTestValue\"");
        assertThat(modifiedWorkflow).contains("<info name=\"secondTestKey\"");
        assertThat(modifiedWorkflow).contains("value=\"secondTestValue\"");
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfAnyParameterIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(null, null, "");
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfXmlWorkflowIsNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(null,
                                                                                                 Collections.emptyMap(),
                                                                                                 "");
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    @Test
    public void testThatEmptyByteArrayIsReturnedIfGenericInfoIsEntriesNull() {
        byte[] nullByteArray = workflowXmlManipulator.replaceGenericInformationAndNameOnJobLevel(new byte[] {},
                                                                                                 null,
                                                                                                 "");
        assertThat(nullByteArray.length).isEqualTo(0);
    }

    private Map<String, String> getTwoSimpleEntries() {
        Map<String, String> returnList = new HashMap<>();

        returnList.put("firstTestKey", "firstTestValue");
        returnList.put("secondTestKey", "secondTestValue");

        return returnList;
    }

}
