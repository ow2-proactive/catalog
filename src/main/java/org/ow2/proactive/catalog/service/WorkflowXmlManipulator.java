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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 09/08/2017
 */
@SuppressWarnings("WeakerAccess")
@Component
@Log4j2
public class WorkflowXmlManipulator {

    public byte[] replaceGenericInformationHiddenVariablesAndJobNameOnJobLevel(final byte[] xmlWorkflow,
            Map<String, String> genericInfoMap, String jobName) {
        if (xmlWorkflow == null || xmlWorkflow.length == 0) {
            return new byte[] {};
        }
        if (genericInfoMap == null) {
            return xmlWorkflow;
        }

        Document doc = null;
        try {
            doc = parseWorkflow(xmlWorkflow);

            Element rootElement = doc.getDocumentElement();
            replaceJobName(rootElement, jobName);
            replaceOrAddGenericInfoElement(genericInfoMap, doc, rootElement);
            replaceHiddenVariables(rootElement);

            ByteArrayOutputStream answer = transform(doc);
            return answer.toByteArray();
        } catch (Exception e) {
            throw new ParsingObjectException(e);
        }
    }

    private Document parseWorkflow(byte[] xmlWorkflow) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance()
                                     .newDocumentBuilder()
                                     .parse(new InputSource(new StringReader(new String(xmlWorkflow))));
    }

    private ByteArrayOutputStream transform(Document doc) throws TransformerException {
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream answer = new ByteArrayOutputStream();
        xformer.transform(new DOMSource(doc), new StreamResult(answer));
        return answer;
    }

    public byte[] replaceOrAddOrRemoveProjectNameOnJobLevel(final byte[] xmlWorkflow, String projectName) {
        if (xmlWorkflow == null || xmlWorkflow.length == 0) {
            return new byte[] {};
        }
        Document doc = null;
        try {
            doc = parseWorkflow(xmlWorkflow);

            Element rootElement = doc.getDocumentElement();
            if (projectName.trim().isEmpty()) {
                removeProjectName(rootElement);
            } else {
                replaceOrAddProjectName(rootElement, projectName);
            }
            ByteArrayOutputStream answer = transform(doc);
            return answer.toByteArray();
        } catch (Exception e) {
            throw new ParsingObjectException(e);
        }
    }

    private void replaceOrAddGenericInfoElement(Map<String, String> genericInfoMap, Document doc, Element rootElement) {
        NodeList nodes = rootElement.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            if ("genericInformation".equals(nodes.item(idx).getNodeName())) {
                Element oldGenericInfoElement = (Element) nodes.item(idx);
                rootElement.replaceChild(createGenericInfoElement(doc, genericInfoMap), oldGenericInfoElement);
                break;
            }
            switch (nodes.item(idx).getNodeName()) {
                case "genericInformation":
                    Element oldGenericInfoElement = (Element) nodes.item(idx);
                    rootElement.replaceChild(createGenericInfoElement(doc, genericInfoMap), oldGenericInfoElement);
                    return;
                case "inputSpace":
                case "outputSpace":
                case "globalSpace":
                case "userSpace":
                case "taskFlow":
                    Element elementAfterGenericInfo = (Element) nodes.item(idx);
                    rootElement.insertBefore(createGenericInfoElement(doc, genericInfoMap), elementAfterGenericInfo);
                    return;
            }
        }
    }

    private void replaceHiddenVariables(Element rootElement) {
        NodeList nodes = rootElement.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            if ("variables".equals(nodes.item(idx).getNodeName())) {
                Element variablesElement = (Element) nodes.item(idx);
                NodeList variableNodes = variablesElement.getChildNodes();
                for (int varIdx = 0; varIdx < variableNodes.getLength(); varIdx++) {
                    Node variableNode = variableNodes.item(varIdx);
                    if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element variableElement = (Element) variableNode;
                        if (checkHiddenVariableReplacementNeeded(variableElement)) {
                            String value = variableElement.getAttribute("value");
                            try {
                                value = PropertyDecrypter.encryptData(value);
                            } catch (Exception e) {
                                String name = variableElement.getAttribute("name");
                                log.warn("Could not encrypt hidden variable " + name, e);
                            }
                            variableElement.setAttribute("value", value);
                        }
                    }
                }
            }
        }
    }

    private Element replaceJobName(Element element, String jobName) {
        return replaceAttributeValue(element, "name", jobName);
    }

    private boolean checkHiddenVariableReplacementNeeded(Element element) {
        String model = element.getAttribute("model");
        if ("PA:HIDDEN".equalsIgnoreCase(model)) {
            String value = element.getAttribute("value");
            if (!value.startsWith("ENC(")) {
                return true;
            }
        }
        return false;
    }

    private Element replaceOrAddProjectName(Element rootElement, String projectName) {
        return replaceAttributeValue(rootElement, "projectName", projectName);
    }

    private Element removeProjectName(Element rootElement) {
        return removeAttributeValue(rootElement, "projectName");
    }

    private Element replaceAttributeValue(Element element, String attrName, String attrValue) {
        element.setAttribute(attrName, attrValue);
        return element;
    }

    private Element removeAttributeValue(Element element, String attrName) {
        element.removeAttribute(attrName);
        return element;
    }

    private Element createInfoElement(Document doc, String name, String value) {
        Element infoElement = doc.createElement("info");
        infoElement.setAttribute("name", name);
        infoElement.setAttribute("value", value);
        return infoElement;
    }

    private Node createGenericInfoElement(Document doc, Map<String, String> keyValueMetadataEntities) {
        Element genericInfoElement = doc.createElement("genericInformation");
        for (Map.Entry<String, String> entry : keyValueMetadataEntities.entrySet()) {
            Element child = createInfoElement(doc, entry.getKey(), entry.getValue());
            genericInfoElement.appendChild(child);
        }
        return genericInfoElement;
    }

}
