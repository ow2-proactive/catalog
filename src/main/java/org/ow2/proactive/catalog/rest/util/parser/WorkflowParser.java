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
package org.ow2.proactive.catalog.rest.util.parser;

import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.ow2.proactive.catalog.rest.entity.KeyValueMetadata;

import com.google.common.collect.ImmutableList;


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
public final class WorkflowParser implements CatalogObjectParserInterface {

    private static final String ATTRIBUTE_GENERIC_INFORMATION_LABEL = "generic_information";

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

    /*
     * Variables indicating which parts of the document have been parsed. Thanks to these
     * information, parsing can be stopped once required information have been extracted.
     */

    private boolean jobHandled = false;

    private boolean variablesHandled = false;

    private boolean genericInformationHandled = false;

    /* Below are instance variables containing values which are extracted */

    private ImmutableList<KeyValueMetadata> keyValueMap;

    private static final class XmlInputFactoryLazyHolder {

        private static final XMLInputFactory INSTANCE = XMLInputFactory.newInstance();

    }

    public WorkflowParser() {
    }

    public List<KeyValueMetadata> parse(InputStream inputStream) throws XMLStreamException {

        XMLStreamReader xmlStreamReader = XmlInputFactoryLazyHolder.INSTANCE.createXMLStreamReader(inputStream);
        int eventType;

        ImmutableList.Builder<KeyValueMetadata> keyValueMapBuilder = ImmutableList.builder();
        boolean isTaskFlow = false;
        try {
            while (xmlStreamReader.hasNext() && !allElementHandled()) {
                eventType = xmlStreamReader.next();

                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        switch (elementLocalPart) {
                            case ELEMENT_JOB:
                                handleJobElement(xmlStreamReader);
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

    private void handleGenericInformationElement(ImmutableList.Builder<KeyValueMetadata> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_GENERIC_INFORMATION_LABEL,
                                        ATTRIBUTE_GENERIC_INFORMATION_NAME,
                                        ATTRIBUTE_GENERIC_INFORMATION_VALUE,
                                        xmlStreamReader);
    }

    private void handleJobElement(XMLStreamReader xmlStreamReader) {
        jobHandled = true;
    }

    private void handleVariableElement(ImmutableList.Builder<KeyValueMetadata> keyValueMapBuilder,
            XMLStreamReader xmlStreamReader) {
        handleElementWithMultipleValues(keyValueMapBuilder,
                                        ATTRIBUTE_VARIABLE_LABEL,
                                        ATTRIBUTE_VARIABLE_NAME,
                                        ATTRIBUTE_VARIABLE_VALUE,
                                        xmlStreamReader);
    }

    private void handleElementWithMultipleValues(ImmutableList.Builder<KeyValueMetadata> keyValueMapBuilder,
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

        keyValueMapBuilder.add(new KeyValueMetadata(key[0], value[0], attributeLabel));
    }

    private void iterateOverAttributes(BiConsumer<String, String> attribute, XMLStreamReader xmlStreamReader) {
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);

            attribute.accept(attributeName, attributeValue);
        }
    }

    private boolean allElementHandled() {
        return this.jobHandled && this.genericInformationHandled && this.variablesHandled;
    }

    private ImmutableList<KeyValueMetadata> getKeyValueMap() {
        return keyValueMap;
    }

}
