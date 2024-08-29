/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023-2024 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fish.payara.extras.diagnostics.collection.collectors;

import fish.payara.extras.diagnostics.util.ParamConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * FIXME
 *
 */
public class DomainXmlCollector extends FileCollector {

    private Path path;
    private String dirSuffix;
    private Logger LOGGER = Logger.getLogger(DomainXmlCollector.class.getName());
    private final String PASSWORD_CHANGE = "PASSWORD_HIDDEN";
    private final String PASSWORD_KEYWORD = "password";
    private final int COLLECTED_OKAY = 0;

    public DomainXmlCollector(Path path) {
        this.path = path;
    }

    public DomainXmlCollector(Path path, String instanceName, String dirSuffix) {
        this.path = path;
        super.setInstanceName(instanceName);
        this.dirSuffix = dirSuffix;
    }


    @Override
    public int collect() {
        int domainXmlCollected = COLLECTED_OKAY;
        Map<String, Object> params = getParams();
        if (params != null) {
            Path outputPath = getPathFromParams(ParamConstants.DIR_PARAM, params);
            if (path != null && outputPath != null) {
                setFilePath(path);
                setDestination(Paths.get(outputPath.toString(), dirSuffix != null ? dirSuffix : ""));
                LOGGER.info("Collecting domain.xml from " + (getInstanceName() != null ? getInstanceName() : "server"));
                domainXmlCollected = super.collect();
                if (domainXmlCollected == COLLECTED_OKAY) {
                    modifyXMLFile(resolveDestinationFile().toFile());
                }
            }
        }
        return domainXmlCollected;
    }

    private Path getPathFromParams(String key, Map<String, Object> parameterMap) {
        Map<String, Object> params = parameterMap;
        if (params != null) {
            String valueString = (String) params.get(key);
            if (valueString != null) {
                return Paths.get(valueString);
            }
        }
        return null;
    }

    private void modifyXMLFile(File xmlFile) {
        LOGGER.info("Modifying " + xmlFile.getAbsolutePath());
        modifyXMLPassword(xmlFile);
    }

    private void modifyXMLPassword(File xmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            traverseNodes(doc.getDocumentElement());


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            LOGGER.info("Successfully modified " + xmlFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.severe("Error Modifying XML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void traverseNodes(Node node) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element tempNode = (Element) node;
            boolean hasPasswordAttribute = tempNode.hasAttribute("password");
            if (hasPasswordAttribute) {
                tempNode.setAttribute("password", PASSWORD_CHANGE);
            }
            String nameAttribute = tempNode.getAttribute("name");
            boolean hasValueAttribute = tempNode.hasAttribute("value");
            if ("Password".equalsIgnoreCase(nameAttribute)) {
                if (hasValueAttribute) {
                    tempNode.setAttribute("value", PASSWORD_CHANGE);
                }
            }

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                traverseNodes(childNodes.item(i));
            }
        }
    }


}
