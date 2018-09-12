/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wscliente.xmlutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Manuel.Castillo
 */
public class XmlProcess {

    private Document xml;

    public XmlProcess(InputStream in) {
        loadXml(in);
    }
    
    public XmlProcess(Document xml) {
        this.xml = xml;
    }

    public void loadXml(InputStream in) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            xml = builder.parse(new InputSource(in));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String generaTextoXML() throws TransformerConfigurationException, TransformerException {
        OutputStream os = new ByteArrayOutputStream();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(xml), new StreamResult(os));
        return os.toString();
    }
}
