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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainXmlUtil {
    private static final Logger LOGGER = Logger.getLogger(DomainXmlUtil.class.getName());
    private static final String PASSWORD_CHANGE = "PASSWORD_HIDDEN";
    private static final String PASSWORD_KEYWORD = "password";
    private static final String ADMIN_PASSWORD_KEYWORD = "admin-password";
    private static final String NAME_KEYWORD = "name";
    private static final String VALUE_KEYWORD = "value";

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
            LOGGER.severe("Error obfuscating XML: " + e.getMessage());
            e.printStackTrace();
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

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                traverseNodes(childNodes.item(i));
            }
        }
    }
}
