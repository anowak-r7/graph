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

            NodeList nList = doc.getElementsByTagName("ace:source");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    ace.setId(eElement.getElementsByTagName("id").item(0).getTextContent());
                    ace.setName(eElement.getElementsByTagName("name").item(0).getTextContent());
                    ace.setVersion(eElement.getElementsByTagName("version").item(0).getTextContent());
                    ace.setHost(eElement.getElementsByTagName("host").item(0).getTextContent());
                    ace.setIpAddress(eElement.getElementsByTagName("ip_address").item(0).getTextContent());
                    ace.setIpAddress(eElement.getElementsByTagName("port").item(0).getTextContent());
                    ace.setIpAddress(eElement.getElementsByTagName("timestamp").item(0).getTextContent());
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