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
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * @author ActiveEon Team
 * @since 09/08/2017
 */
@SuppressWarnings("WeakerAccess")
@Component
public class WorkflowXmlManipulator {

    public byte[] replaceGenericInformationAndNameOnJobLevel(final byte[] xmlWorkflow,
            Map<String, String> genericInfoMap, String jobName) {
        if (xmlWorkflow == null) {
            return new byte[] {};
        }
        if (genericInfoMap == null) {
            return xmlWorkflow;
        }

        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder()
                                        .parse(new InputSource(new StringReader(new String(xmlWorkflow))));

            Element rootElement = doc.getDocumentElement();
            replaceJobName(rootElement, jobName);
            replaceOrAddGenericInfoElement(genericInfoMap, doc, rootElement);

            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream answer = new ByteArrayOutputStream();
            xformer.transform(new DOMSource(doc), new StreamResult(answer));
            return answer.toByteArray();
        } catch (Exception e) {
            throw new ParsingObjectException(e);
        }
    }

    public byte[] replaceOrAddOrRemoveProjectNameOnJobLevel(final byte[] xmlWorkflow, String projectName) {
        if (xmlWorkflow == null) {
            return new byte[] {};
        }
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder()
                                        .parse(new InputSource(new StringReader(new String(xmlWorkflow))));

            Element rootElement = doc.getDocumentElement();
            if (projectName.trim().isEmpty()) {
                removeProjectName(rootElement);
            } else {
                replaceOrAddProjectName(rootElement, projectName);
            }
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream answer = new ByteArrayOutputStream();
            xformer.transform(new DOMSource(doc), new StreamResult(answer));
            return answer.toByteArray();
        } catch (Exception e) {
            throw new ParsingObjectException(e);
        }
    }

    private void replaceOrAddGenericInfoElement(Map<String, String> genericInfoMap, Document doc, Element rootElement) {
        NodeList nodes = rootElement.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            if (nodes.item(idx).getNodeName() == "genericInformation") {
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

    private Element replaceJobName(Element element, String jobName) {
        return replaceAttributeValue(element, "name", jobName);
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
