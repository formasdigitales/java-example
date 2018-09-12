/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package forsedi.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;

/**
 *
 * @author Manuel.Castillo
 */
public class Xml {

    private static Document xml;

    public Xml(){
        
    }


    public String getSerializationXML(String rfc, String fecha, List<String> folios) throws IOException {

        org.jdom.Document xmlDoc = new org.jdom.Document();
        org.jdom.Element root = new org.jdom.Element("Cancelacion");
        root.addNamespaceDeclaration(org.jdom.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema"));
        root.addNamespaceDeclaration(org.jdom.Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
        root.setAttribute("RfcEmisor", rfc);
        root.setAttribute("Fecha", fecha.replaceAll(" ", "T"));

        for (String folio : folios) {
            org.jdom.Element efolios = new org.jdom.Element("Folios");
            org.jdom.Element eUUID = new org.jdom.Element("UUID");
            eUUID.setText(folio);
            efolios.addContent(eUUID);
            root.addContent(efolios);
        }

        xmlDoc.setRootElement(root);

        Format xmlFormat = Format.getCompactFormat(); //getPrettyFormat();
        xmlFormat.setEncoding("UTF-8");
        XMLOutputter xmlOutputter = new XMLOutputter(xmlFormat);
        StringWriter strOutput = new StringWriter();
        xmlOutputter.output(xmlDoc, strOutput);

        String strTmp = strOutput.toString().replaceAll("'", "&apos;").trim();
        strTmp = strTmp.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\"?>");
        int index = strTmp.indexOf("Fecha=\"");
        index = strTmp.indexOf((int)'>', index);
        String firstPart = strTmp.substring(0, index);
        String secondPart = strTmp.substring(index);
        strTmp = firstPart + " xmlns=\"http://cancelacfd.sat.gob.mx\"" + secondPart;

        return strTmp;
    }
}
