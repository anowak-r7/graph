package com.rapid7.aces;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

/**
 * This class provides static methods to parse an ACE XML file into its logical representation.
 */
class LoadAces
{
    static AcesData parse(File fXmlFile)
    {
        AcesData ace = new AcesData();

        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("ace:source");

            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) node;

                    ace.setId(element.getElementsByTagName("id").item(0).getTextContent());
                    ace.setName(element.getElementsByTagName("name").item(0).getTextContent());
                    ace.setVersion(element.getElementsByTagName("version").item(0).getTextContent());
                    ace.setHost(element.getElementsByTagName("host").item(0).getTextContent());
                    ace.setIpAddress(element.getElementsByTagName("ip_address").item(0).getTextContent());
                    ace.setPort(element.getElementsByTagName("port").item(0).getTextContent());
                    ace.setTimeStamp(element.getElementsByTagName("timestamp").item(0).getTextContent());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ace;
    }
}