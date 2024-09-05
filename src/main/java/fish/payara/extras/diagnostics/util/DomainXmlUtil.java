/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2024 Payara Foundation and/or its affiliates. All rights reserved.
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

package fish.payara.extras.diagnostics.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomainXmlUtil {
    private static final Logger LOGGER = Logger.getLogger(DomainXmlUtil.class.getName());
    private static final String PASSWORD_CHANGE = "PASSWORD_HIDDEN";
    private static final String OBFUSCATED_CHANGE = "OBFUSCATED";
    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private static final String ADDRESS_KEYWORD = "address";
    private static final String DEFAULT_HOST = "localhost";
    private static final String HOST_KEYWORD = "host";
    private static final String PASSWORD_KEYWORD = "password";
    private static final String ADMIN_PASSWORD_KEYWORD = "admin-password";
    private static final String NAME_KEYWORD = "name";
    private static final String VALUE_KEYWORD = "value";
    private static final String URL_KEYWORD = "URL";

    int obfuscatedItem = 1;

    public void obfuscateDomainXml (File xmlFile) {
        try {
            LOGGER.info("Obfuscating " + xmlFile.getAbsolutePath());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            traverseNodes(doc.getDocumentElement());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            LOGGER.info("Successfully obfuscated " + xmlFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error obfuscating XML: " + e.getMessage(), e);
        }
    }

    private void traverseNodes(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element tempNode = (Element) node;
            boolean hasPasswordAttribute = tempNode.hasAttribute(PASSWORD_KEYWORD);
            boolean hasAdminPasswordAttribute = tempNode.hasAttribute(ADMIN_PASSWORD_KEYWORD);
            if (hasPasswordAttribute) {
                tempNode.setAttribute(PASSWORD_KEYWORD, PASSWORD_CHANGE);
            }
            if (hasAdminPasswordAttribute) {
                tempNode.setAttribute(ADMIN_PASSWORD_KEYWORD, PASSWORD_CHANGE);
            }

            String nameAttribute = tempNode.getAttribute(NAME_KEYWORD);
            boolean hasValueAttribute = tempNode.hasAttribute(VALUE_KEYWORD);
            if (nameAttribute.toLowerCase().contains(PASSWORD_KEYWORD)) {
                if (hasValueAttribute) {
                    tempNode.setAttribute(VALUE_KEYWORD, PASSWORD_CHANGE);
                }
            }
            if (URL_KEYWORD.equalsIgnoreCase(nameAttribute)) {
                String obfuscatedUrl = "";
                String urlAttribute = tempNode.getAttribute(VALUE_KEYWORD);
                if (urlAttribute.startsWith("jdbc:")) {
                    //Keep database type e.g. jdbc:h2:xxx
                    String[] splitUrl = urlAttribute.split(":", 3);
                    obfuscatedUrl = splitUrl[0] + ":";
                    if (splitUrl.length == 3){
                        obfuscatedUrl+= splitUrl[1] + ":";
                    }
                }
                obfuscatedUrl += "database-obfuscated";
                tempNode.setAttribute(VALUE_KEYWORD, obfuscatedUrl);
            }
            obfuscateAddressAndHost(tempNode);
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                traverseNodes(childNodes.item(i));
            }
        }
    }

    private void obfuscateAddressAndHost (Node node) {
        Element tempNode = (Element) node;
        boolean hasAddressAttribute = tempNode.hasAttribute(ADDRESS_KEYWORD);
        String addressAttribute = tempNode.getAttribute(ADDRESS_KEYWORD);
        if (hasAddressAttribute) {
            if (!addressAttribute.toLowerCase().contains(DEFAULT_ADDRESS)) {
                tempNode.setAttribute(ADDRESS_KEYWORD, OBFUSCATED_CHANGE + obfuscatedItem);
                obfuscatedItem++;
            }
        }

        boolean hasHostAttribute = tempNode.hasAttribute(HOST_KEYWORD);
        String hostAttribute = tempNode.getAttribute(HOST_KEYWORD);
        if (hasHostAttribute) {
            if (!hostAttribute.toLowerCase().contains(DEFAULT_HOST)) {
                tempNode.setAttribute(HOST_KEYWORD, OBFUSCATED_CHANGE + obfuscatedItem);
                obfuscatedItem++;
            }
        }
    }

}
