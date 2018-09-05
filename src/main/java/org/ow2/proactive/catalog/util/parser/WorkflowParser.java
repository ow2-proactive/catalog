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
package org.ow2.proactive.catalog.util.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * ProActiveWorkflowParser aims to parse a ProActive XML workflow (whatever the schema version is)
 * in order to extract some specific values (job name, project name, generic
 * information and variables).
 * <p>
 * No validation is applied for now. Besides parsing stop once required information have
 * been extracted, mainly for performance reasons.
 *
 * @author ActiveEon Team
 */
@Log4j2
@NoArgsConstructor
@Component
public final class WorkflowParser extends AbstractCatalogObjectParser {

    private static final String JOB_NAME_KEY = "name";

    private static final String PROJECT_NAME_KEY = "project_name";

    private static final String JOB_AND_PROJECT_LABEL = "job_information";

    private static final String ATTRIBUTE_JOB_NAME = "name";

    private static final String ATTRIBUTE_JOB_PROJECT_NAME = "projectName";

    public static final String ATTRIBUTE_GENERIC_INFORMATION_LABEL = "generic_information";

    private static final String ATTRIBUTE_GENERIC_INFORMATION_NAME = "name";

    private static final String ATTRIBUTE_GENERIC_INFORMATION_VALUE = "value";

    private static final String ATTRIBUTE_VARIABLE_LABEL = "variable";

    private static final String ATTRIBUTE_VARIABLE_NAME = "name";

    private static final String ATTRIBUTE_VARIABLE_VALUE = "value";

    private static final String ATTRIBUTE_VARIABLE_MODEL_LABEL = "variable_model";

    private static final String ATTRIBUTE_VARIABLE_MODEL = "model";

    private static final String ELEMENT_GENERIC_INFORMATION = "genericInformation";

    private static final String ELEMENT_GENERIC_INFORMATION_INFO = "info";

    private static final String ELEMENT_JOB = "job";

    private static final String ELEMENT_TASK_FLOW = "taskFlow";

    private static final String ELEMENT_VARIABLE = "variable";

    private static final String ELEMENT_VARIABLES = "variables";

    private static final String ELEMENT_JOB_DESCRIPTION = "description";

    private static final String JOB_DESCRIPTION_KEY = "description";

    /* Below are instance variables containing values which are extracted */

    private static final class XmlInputFactoryLazyHolder {

        private static final XMLInputFactory INSTANCE = XMLInputFactory.newInstance();

    }

    @Override
    List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) throws XMLStreamException {

        /*
         * Variables indicating which parts of the document have been parsed. Thanks to these
         * information, parsing can be stopped once required information have been extracted.
         */

        boolean jobHandled = false;

        boolean variablesHandled = false;

        boolean genericInformationHandled = false;

        boolean descriptionHandled = false;

        XMLStreamReader xmlStreamReader = XmlInputFactoryLazyHolder.INSTANCE.createXMLStreamReader(inputStream);
        int eventType;

        ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder = ImmutableList.builder();
        boolean isTaskFlow = false;
        try {
            while (xmlStreamReader.hasNext() &&
                   !(jobHandled && variablesHandled && genericInformationHandled && descriptionHandled)) {
                eventType = xmlStreamReader.next();

                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        switch (elementLocalPart) {
                            case ELEMENT_JOB:
                                handleJobElement(keyValueMapBuilder, xmlStreamReader);
                                jobHandled = true;
                                break;
                            case ELEMENT_TASK_FLOW:
                                isTaskFlow = true;
                                break;
                            case ELEMENT_GENERIC_INFORMATION_INFO:
                                if (!isTaskFlow) {
                                    handleGenericInformationElement(keyValueMapBuilder, xmlStreamReader);
                                }
                                break;
                            case ELEMENT_VARIABLE:
                                if (!isTaskFlow) {
                                    handleVariableElement(keyValueMapBuilder, xmlStreamReader);
                                }
                                break;
                            case ELEMENT_JOB_DESCRIPTION:
                                if (!isTaskFlow) {
                                    handleDescriptionElement(keyValueMapBuilder, xmlStreamReader);
                                    descriptionHandled = true;
                                }
                                break;
                            default: // all the other workflow tags should be ignored for parsing
                                break;
                        }
                        break;

                    case XMLEvent.END_ELEMENT:
                        elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        switch (elementLocalPart) {
                            case ELEMENT_TASK_FLOW:
                                isTaskFlow = false;
                                break;
                            case ELEMENT_GENERIC_INFORMATION:
                                if (!isTaskFlow) {
                                    genericInformationHandled = true;
                                }
                                break;
                            case ELEMENT_VARIABLES:
                                if (!isTaskFlow) {
                                    variablesHandled = true;
                                }
                                break;
                            default: // all the other workflow tags should be ignored for parsing
                                break;
                        }
                        break;

                    default:
                        break;
                }
            }

            return keyValueMapBuilder.build();

        } finally {
            xmlStreamReader.close();
        }
    }

    @Override
    public boolean isMyKind(String kind) {
        return kind.toLowerCase().startsWith(SupportedParserKinds.WORKFLOW.toString().toLowerCase());
    }

    private void handleGenericInformationElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_GENERIC_INFORMATION_LABEL,
                                        ATTRIBUTE_GENERIC_INFORMATION_NAME,
                                        ATTRIBUTE_GENERIC_INFORMATION_VALUE,
                                        xmlStreamReader,
                                        true);
    }

    private void handleJobElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {

        List<String> strings = orderedSearch(xmlStreamReader, ATTRIBUTE_JOB_NAME, ATTRIBUTE_JOB_PROJECT_NAME);
        String jobNameValue = strings.get(0);
        String projectNameValue = strings.get(1);

        if (checkIfNotNull(projectNameValue)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(PROJECT_NAME_KEY,
                                                                   projectNameValue,
                                                                   JOB_AND_PROJECT_LABEL));
        }
        if (checkIfNotNull(jobNameValue)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_NAME_KEY, jobNameValue, JOB_AND_PROJECT_LABEL));
        }
    }

    private void handleDescriptionElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {

        String descriptionContent = "";
        try {
            descriptionContent = xmlStreamReader.getElementText(); //returns the text content of CDATA for description tag in our case
        } catch (XMLStreamException e) {
            log.error("Unable to parse the workflow description", e);
            throw new RuntimeException("Unable to parse the workflow description", e);
        }
        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_DESCRIPTION_KEY, descriptionContent, GENERAL_LABEL));

    }

    private void handleVariableElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {

        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_VARIABLE_LABEL,
                                        ATTRIBUTE_VARIABLE_NAME,
                                        ATTRIBUTE_VARIABLE_VALUE,
                                        xmlStreamReader,
                                        true);
        //for variables model
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_VARIABLE_MODEL_LABEL,
                                        ATTRIBUTE_VARIABLE_NAME,
                                        ATTRIBUTE_VARIABLE_MODEL,
                                        xmlStreamReader,
                                        false);

    }

    private void handleElementWithMultipleValues(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            String attributeLabel, String attributeNameForKey, String attributeNameForValue,
            XMLStreamReader xmlStreamReader, boolean allowEmptyValues) {
        List<String> strings = orderedSearch(xmlStreamReader, attributeNameForKey, attributeNameForValue);

        String key = strings.get(0);
        String value = strings.get(1);

        if (checkIfNotNull(key, value) && (allowEmptyValues || checkIfNotEmpty(key, value))) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(key, value, attributeLabel));
        }
    }

    private boolean checkIfNotNull(String... values) {
        return Arrays.stream(values).noneMatch(Objects::isNull);

    }

    private boolean checkIfNotEmpty(String... values) {
        return Arrays.stream(values).noneMatch(String::isEmpty);

    }

    private List<String> orderedSearch(XMLStreamReader xmlStreamReader, String... names) {
        List<String> listNames = Arrays.asList(names);
        String[] result = new String[names.length];

        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            int index = listNames.indexOf(attributeName);
            if (index != -1) {
                result[index] = xmlStreamReader.getAttributeValue(i);
            }
        }

        return Arrays.asList(result);
    }

    @Override
    public String getIconPath(List<KeyValueLabelMetadataEntity> keyValueMetadataEntities) {
        return keyValueMetadataEntities.stream()
                                       .filter(keyValue -> keyValue.getLabel()
                                                                   .equals(ATTRIBUTE_GENERIC_INFORMATION_LABEL) &&
                                                           keyValue.getKey().equals("workflow.icon"))
                                       .map(keyValue -> keyValue.getValue())
                                       .findAny()
                                       .orElse(SupportedParserKinds.WORKFLOW.getDefaultIcon());

    }

}
