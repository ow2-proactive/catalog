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
import java.util.List;
import java.util.function.BiConsumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;

import com.google.common.collect.ImmutableList;

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
public final class WorkflowParser implements CatalogObjectParserInterface {

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

    private static final String ELEMENT_GENERIC_INFORMATION = "genericInformation";

    private static final String ELEMENT_GENERIC_INFORMATION_INFO = "info";

    private static final String ELEMENT_JOB = "job";

    private static final String ELEMENT_TASK_FLOW = "taskFlow";

    private static final String ELEMENT_VARIABLE = "variable";

    private static final String ELEMENT_VARIABLES = "variables";

    private static final String GENERAL_LABEL = "General";

    private static final String ELEMENT_JOB_DESCRIPTION = "description";

    private static final String JOB_DESCRIPTION_KEY = "description";

    /*
     * Variables indicating which parts of the document have been parsed. Thanks to these
     * information, parsing can be stopped once required information have been extracted.
     */

    private boolean jobHandled = false;

    private boolean variablesHandled = false;

    private boolean genericInformationHandled = false;

    private boolean descriptionHandled = false;

    /* Below are instance variables containing values which are extracted */

    private ImmutableList<KeyValueLabelMetadataEntity> keyValueMap;

    private static final class XmlInputFactoryLazyHolder {

        private static final XMLInputFactory INSTANCE = XMLInputFactory.newInstance();

    }

    public WorkflowParser() {
    }

    public List<KeyValueLabelMetadataEntity> parse(InputStream inputStream) throws XMLStreamException {

        XMLStreamReader xmlStreamReader = XmlInputFactoryLazyHolder.INSTANCE.createXMLStreamReader(inputStream);
        int eventType;

        ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder = ImmutableList.builder();
        boolean isTaskFlow = false;
        try {
            while (xmlStreamReader.hasNext() && !allElementHandled()) {
                eventType = xmlStreamReader.next();

                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        switch (elementLocalPart) {
                            case ELEMENT_JOB:
                                handleJobElement(keyValueMapBuilder, xmlStreamReader);
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
                                }
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
                                    this.genericInformationHandled = true;
                                }
                                break;
                            case ELEMENT_VARIABLES:
                                if (!isTaskFlow) {
                                    this.variablesHandled = true;
                                }
                                break;

                        }
                    default:
                        break;
                }
            }

            this.keyValueMap = keyValueMapBuilder.build();

            return getKeyValueMap();
        } finally {
            xmlStreamReader.close();
        }
    }

    private void handleGenericInformationElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_GENERIC_INFORMATION_LABEL,
                                        ATTRIBUTE_GENERIC_INFORMATION_NAME,
                                        ATTRIBUTE_GENERIC_INFORMATION_VALUE,
                                        xmlStreamReader);
    }

    private void handleJobElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        iterateOverAttributes((attributeName, attributeValue) -> {
            if (attributeName.equals(ATTRIBUTE_JOB_NAME)) {
                keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_NAME_KEY,
                                                                       attributeValue,
                                                                       JOB_AND_PROJECT_LABEL));
            } else if (attributeName.equals(ATTRIBUTE_JOB_PROJECT_NAME)) {
                keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(PROJECT_NAME_KEY,
                                                                       attributeValue,
                                                                       JOB_AND_PROJECT_LABEL));
            }
        }, xmlStreamReader);
        jobHandled = true;
    }

    private void handleDescriptionElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {

        String descriptionContent = new String();
        try {
            descriptionContent = xmlStreamReader.getElementText(); //returns the text content of CDATA for description tag in our case
        } catch (XMLStreamException e) {
            log.error("Unable to parse the workflow description", e);
        }
        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_DESCRIPTION_KEY, descriptionContent, GENERAL_LABEL));
        descriptionHandled = true;
    }

    private void handleVariableElement(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_VARIABLE_LABEL,
                                        ATTRIBUTE_VARIABLE_NAME,
                                        ATTRIBUTE_VARIABLE_VALUE,
                                        xmlStreamReader);
    }

    private void handleElementWithMultipleValues(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            String attributeLabel, String attributeNameForKey, String attributeNameForValue,
            XMLStreamReader xmlStreamReader) {
        String[] key = new String[1];
        String[] value = new String[1];

        iterateOverAttributes((attributeName, attributeValue) -> {
            if (attributeName.equals(attributeNameForKey)) {
                key[0] = attributeValue;
            } else if (attributeName.equals(attributeNameForValue)) {
                value[0] = attributeValue;
            }
        }, xmlStreamReader);

        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(key[0], value[0], attributeLabel));
    }

    private void iterateOverAttributes(BiConsumer<String, String> attribute, XMLStreamReader xmlStreamReader) {
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);

            attribute.accept(attributeName, attributeValue);
        }
    }

    private boolean allElementHandled() {
        return this.jobHandled && this.genericInformationHandled && this.variablesHandled && this.descriptionHandled;
    }

    private ImmutableList<KeyValueLabelMetadataEntity> getKeyValueMap() {
        return keyValueMap;
    }

}
