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

import java.util.AbstractMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 09/08/2017
 */
@SuppressWarnings("WeakerAccess")
@Component
public class WorkflowXmlManipulator {

    private final static String TASK_FLOW_START_TAG = "<taskFlow>";

    private final static String GENERIC_INFORMATION_START_TAG = "<genericInformation>";

    private final static String GENERIC_INFORMATION_END_TAG = "<\\/genericInformation>";

    private final static String ONE_INTEND = " ";

    private final static String TWO_INTEND = ONE_INTEND + ONE_INTEND;

    private final static String NEW_LINE = "\n";

    private final static String GENERIC_INFORMATION_ENTRY_FORMAT_STRING = TWO_INTEND +
                                                                          "<info name=\"%s\" value=\"%s\"/>" + NEW_LINE;

    private final static Pattern genericInformationPattern = Pattern.compile(GENERIC_INFORMATION_START_TAG +
                                                                             "[\\D\\d]*" + GENERIC_INFORMATION_END_TAG +
                                                                             "[\\r\\n]");

    private final static Pattern taskFlowStartTagPattern = Pattern.compile(TASK_FLOW_START_TAG);

    private byte[] removeGenericInformation(final byte[] xmlWorkflow) {
        String workflow = new String(xmlWorkflow);

        Matcher genericInfoMatcher = genericInformationPattern.matcher(workflow);
        return genericInfoMatcher.replaceAll("").getBytes();
    }

    public byte[] replaceGenericInformation(final byte[] xmlWorkflow,
            List<AbstractMap.SimpleImmutableEntry<String, String>> genericInfoEntries) {
        if (xmlWorkflow == null || genericInfoEntries == null) {
            return new byte[] {};
        }

        String workflowWithoutGenericInfo = new String(removeGenericInformation(xmlWorkflow));

        return taskFlowStartTagPattern.matcher(workflowWithoutGenericInfo)
                                      .replaceFirst(ONE_INTEND + GENERIC_INFORMATION_START_TAG + NEW_LINE +
                                                    createGenericInfoString(genericInfoEntries) + ONE_INTEND +
                                                    GENERIC_INFORMATION_END_TAG + NEW_LINE + TASK_FLOW_START_TAG)
                                      .getBytes();
    }

    private String
            createGenericInfoString(List<AbstractMap.SimpleImmutableEntry<String, String>> keyValueMetadataEntities) {
        return keyValueMetadataEntities.stream()
                                       .map(entry -> String.format(GENERIC_INFORMATION_ENTRY_FORMAT_STRING,
                                                                   entry.getKey(),
                                                                   entry.getValue()))
                                       .collect(Collectors.joining());
    }

}
