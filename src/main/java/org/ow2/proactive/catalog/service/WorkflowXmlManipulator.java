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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 09/08/2017
 */
@SuppressWarnings("WeakerAccess")
@Component
public class WorkflowXmlManipulator {

    protected final static String TASK_FLOW_START_TAG = "<taskFlow>";

    protected final static String TASK_FLOW_END_TAG = "<\\/taskFlow>";

    protected final static String GENERIC_INFORMATION_START_TAG = "<genericInformation>";

    protected final static String GENERIC_INFORMATION_END_TAG = "<\\/genericInformation>";

    protected final static String ONE_INTEND = "  ";

    private final static String TWO_INTEND = ONE_INTEND + ONE_INTEND;

    private final static String NEW_LINE = "\n";

    protected final static String ANY_CHARACTER_OR_NEW_LINE = "[\\S\\s]*";

    private final static String GENERIC_INFORMATION_ENTRY_FORMAT_STRING = TWO_INTEND +
                                                                          "<info bucketName=\"%s\" value=\"%s\"/>" +
                                                                          NEW_LINE;

    protected final static String GENERIC_INFO_TAG_ENTITY = GENERIC_INFORMATION_START_TAG + "[\\D\\d]*?" +
                                                            GENERIC_INFORMATION_END_TAG + "[\\r\\n]";

    private final static Pattern taskFlowStartTagPattern = Pattern.compile(TASK_FLOW_START_TAG);

    private final static Pattern genericInformationPatternBeforeTaskFlow = Pattern.compile(ANY_CHARACTER_OR_NEW_LINE +
                                                                                           "(" +
                                                                                           GENERIC_INFO_TAG_ENTITY +
                                                                                           ")" +
                                                                                           ANY_CHARACTER_OR_NEW_LINE +
                                                                                           TASK_FLOW_START_TAG +
                                                                                           ANY_CHARACTER_OR_NEW_LINE);

    private final static Pattern genericInformationPatternAfterTaskFlow = Pattern.compile(ANY_CHARACTER_OR_NEW_LINE +
                                                                                          TASK_FLOW_END_TAG +
                                                                                          ANY_CHARACTER_OR_NEW_LINE +
                                                                                          "(" +
                                                                                          GENERIC_INFO_TAG_ENTITY +
                                                                                          ")" +
                                                                                          ANY_CHARACTER_OR_NEW_LINE);

    private byte[] removeGenericInformationJobLevel(final byte[] xmlWorkflow) {
        String workflow = new String(xmlWorkflow);

        Matcher matcherBeforeTask = genericInformationPatternBeforeTaskFlow.matcher(workflow);
        Matcher matcherAfterTask = genericInformationPatternAfterTaskFlow.matcher(workflow);

        //from the definition workflow xsd schema: generic information's block can not be separated in several tags
        if (matcherBeforeTask.matches()) {
            workflow = workflow.replaceFirst(Pattern.quote(matcherBeforeTask.group(1)), "");
        } else if (matcherAfterTask.matches()) {
            workflow = workflow.replaceFirst(Pattern.quote(matcherAfterTask.group(1)), "");
        }
        return workflow.getBytes();
    }

    public byte[] replaceGenericInformationJobLevel(final byte[] xmlWorkflow, Map<String, String> genericInfoMap) {
        if (xmlWorkflow == null) {
            return new byte[] {};
        }
        if (genericInfoMap == null) {
            return xmlWorkflow;
        }

        String workflowWithoutGenericInfo = new String(removeGenericInformationJobLevel(xmlWorkflow));

        return taskFlowStartTagPattern.matcher(workflowWithoutGenericInfo)
                                      .replaceFirst(GENERIC_INFORMATION_START_TAG + NEW_LINE +
                                                    createGenericInfoString(genericInfoMap) + ONE_INTEND +
                                                    GENERIC_INFORMATION_END_TAG + NEW_LINE + ONE_INTEND +
                                                    TASK_FLOW_START_TAG)
                                      .getBytes();
    }

    private String createGenericInfoString(Map<String, String> keyValueMetadataEntities) {
        return keyValueMetadataEntities.entrySet()
                                       .stream()
                                       .map(entry -> String.format(GENERIC_INFORMATION_ENTRY_FORMAT_STRING,
                                                                   entry.getKey(),
                                                                   StringEscapeUtils.escapeXml10(entry.getValue())))
                                       .collect(Collectors.joining());
    }

}
