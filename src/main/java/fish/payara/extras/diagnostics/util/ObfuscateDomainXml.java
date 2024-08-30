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

public class ObfuscateDomainXml {
    private static final Logger LOGGER = Logger.getLogger(ObfuscateDomainXml.class.getName());
    private static final String PASSWORD_CHANGE = "PASSWORD_HIDDEN";
    private static final String PASSWORD_KEYWORD = "password";
    private static final String NAME_KEYWORD = "name";
    private static final String VALUE_KEYWORD = "value";

    public void obfuscateDomainXml(File xmlFile) {
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
            if (hasPasswordAttribute) {
                tempNode.setAttribute(PASSWORD_KEYWORD, PASSWORD_CHANGE);
            }

            String nameAttribute = tempNode.getAttribute(NAME_KEYWORD);
            boolean hasValueAttribute = tempNode.hasAttribute(VALUE_KEYWORD);
            if (PASSWORD_KEYWORD.equalsIgnoreCase(nameAttribute)) {
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
