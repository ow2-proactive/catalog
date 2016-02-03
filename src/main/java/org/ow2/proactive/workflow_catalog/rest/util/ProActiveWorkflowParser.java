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

import com.google.common.collect.ImmutableMap;
import org.ow2.proactive.workflow_catalog.rest.exceptions.UnprocessableEntityException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
public final class ProActiveWorkflowParser {

    private static final String ATTRIBUTE_JOB_NAME = "name";

    private static final String ATTRIBUTE_JOB_PROJECT_NAME = "projectName";

    private static final String ATTRIBUTE_GENERIC_INFORMATION_NAME = "name";

    private static final String ATTRIBUTE_GENERIC_INFORMATION_VALUE = "value";

    private static final String ATTRIBUTE_VARIABLE_NAME = "name";

    private static final String ATTRIBUTE_VARIABLE_VALUE = "value";

    private static final String ELEMENT_GENERIC_INFORMATION = "genericInformation";

    private static final String ELEMENT_GENERIC_INFORMATION_INFO = "info";

    private static final String ELEMENT_JOB = "job";

    private static final String ELEMENT_VARIABLE = "variable";

    private static final String ELEMENT_VARIABLES = "variables";

    private final XMLStreamReader xmlStreamReader;

    /*
     * Variables indicating which parts of the document have been parsed.
     * Thanks to these information, parsing can be stopped once required
     * information have been extracted.
     */

    private boolean jobHandled = false;

    private boolean variablesHandled = false;

    private boolean genericInformationHandled = false;

    /* Below are instance variables containing values which are extracted */

    private String jobName;

    private String projectName;

    private ImmutableMap<String, String> genericInformation;

    private ImmutableMap<String, String> variables;

    private static final class XmlInputFactoryLazyHolder {

        private static final XMLInputFactory INSTANCE = XMLInputFactory.newInstance();

    }

    public ProActiveWorkflowParser(InputStream inputStream) throws XMLStreamException {
        this.xmlStreamReader = XmlInputFactoryLazyHolder.INSTANCE.createXMLStreamReader(inputStream);
        this.genericInformation = ImmutableMap.of();
        this.variables = ImmutableMap.of();
    }

    public ProActiveWorkflowParserResult parse() throws XMLStreamException {
        int eventType;

        ImmutableMap.Builder<String, String> genericInformation = ImmutableMap.builder();
        ImmutableMap.Builder<String, String> variables = ImmutableMap.builder();

        try {
            while (xmlStreamReader.hasNext() && !allElementHandled()) {
                eventType = xmlStreamReader.next();

                switch (eventType) {
                    case XMLEvent.START_ELEMENT:
                        String elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        switch (elementLocalPart) {
                            case ELEMENT_JOB:
                                handleJobElement();
                                break;
                            case ELEMENT_GENERIC_INFORMATION_INFO:
                                handleGenericInformationElement(genericInformation);
                                break;
                            case ELEMENT_VARIABLE:
                                handleVariableElement(variables);
                                break;
                        }

                        break;
                    case XMLEvent.END_ELEMENT:
                        elementLocalPart = xmlStreamReader.getName().getLocalPart();

                        if (elementLocalPart.equals(ELEMENT_GENERIC_INFORMATION)) {
                            this.genericInformationHandled = true;
                        } else if (elementLocalPart.equals(ELEMENT_VARIABLES)) {
                            this.variablesHandled = true;
                        }

                        break;
                    default:
                        break;
                }
            }

            this.genericInformation = genericInformation.build();
            this.variables = variables.build();

            return createResult();
        } finally {
            this.xmlStreamReader.close();
        }
    }

    private ProActiveWorkflowParserResult createResult() {
        String projectName = getProjectName().orElseThrow(
                getMissingElementException("No project name defined.")
        );

        String name = getJobName().orElseThrow(
                getMissingElementException("No job name defined.")
        );

        return new ProActiveWorkflowParserResult(
                projectName, name, getGenericInformation(), getVariables());
    }

    private Supplier<UnprocessableEntityException> getMissingElementException(String message) {
        return () -> new UnprocessableEntityException("XML does not validate against Schema. " + message);
    }

    private void handleGenericInformationElement(ImmutableMap.Builder<String, String> genericInformation) {
        handleElementWithMultipleValues(
                genericInformation,
                ATTRIBUTE_GENERIC_INFORMATION_NAME,
                ATTRIBUTE_GENERIC_INFORMATION_VALUE);
    }

    private void handleJobElement() {
        iterateOverAttributes(
                (attributeName, attributeValue) -> {
                    if (attributeName.equals(ATTRIBUTE_JOB_NAME)) {
                        this.jobName = attributeValue;
                    } else if (attributeName.equals(ATTRIBUTE_JOB_PROJECT_NAME)) {
                        this.projectName = attributeValue;
                    }
                }
        );

        jobHandled = true;
    }

    private void handleVariableElement(ImmutableMap.Builder<String, String> variables) {
        handleElementWithMultipleValues(
                variables,
                ATTRIBUTE_VARIABLE_NAME,
                ATTRIBUTE_VARIABLE_VALUE);
    }

    private void handleElementWithMultipleValues(ImmutableMap.Builder<String, String> store, String attributeNameForKey, String attributeNameForValue) {
        String[] key = new String[1];
        String[] value = new String[1];

        iterateOverAttributes(
                (attributeName, attributeValue) -> {
                    if (attributeName.equals(attributeNameForKey)) {
                        key[0] = attributeValue;
                    } else if (attributeName.equals(attributeNameForValue)) {
                        value[0] = attributeValue;
                    }
                }
        );

        store.put(key[0], value[0]);
    }

    private void iterateOverAttributes(BiConsumer<String, String> attribute) {
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);

            attribute.accept(attributeName, attributeValue);
        }
    }

    private boolean allElementHandled() {
        return this.jobHandled && this.genericInformationHandled && this.variablesHandled;
    }

    private Optional<String> getJobName() {
        return Optional.ofNullable(jobName);
    }

    private Optional<String> getProjectName() {
        return Optional.ofNullable(projectName);
    }

    private ImmutableMap<String, String> getGenericInformation() {
        return genericInformation;
    }

    private ImmutableMap<String, String> getVariables() {
        return variables;
    }

}
