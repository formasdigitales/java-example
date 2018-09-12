/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wscliente.principal;

import com.wscliente.xmlutils.XmlProcess;
import com.wsdlcliente.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.ssl.PKCS8Key;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Manuel.Castillo
 */
public class Principal {

    // usuario y contraseña de prueba
    private final String usuario = "pruebasWS";
    private final String pass = "pruebasWS";
    
    private final String tempXml = "com/resource/tmpxml.xml";
    private final String certPruebas = "com/resource/CSD_Pruebas_CFDI_LAN7008173R5.cer";
    private final String keyPruebas = "com/resource/CSD_Pruebas_CFDI_LAN7008173R5.key";
    private final String passKey = "12345678a";
    
    

    public static void main(String args[]) {
        Principal principal = new Principal();
        String cfdi = principal.generateXml();
        String cfdiTimbrado = principal.timbrar(cfdi);
        System.out.print(cfdiTimbrado);
      
        //

    }
    
    public void guardarDocumento(String file, String texto){
         try {

            OutputStream fos = new FileOutputStream(file);
            OutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
            osw.write(texto);
            osw.flush();
            osw.close();
        } catch (IOException ioe) {
            System.err.println("Error al guardar el archivo.");
            System.err.println(ioe.getMessage());
        }
    }

    public String timbrar(String cdfi) {
        WSTimbradoCFDIService ws = new WSTimbradoCFDIService();
        WSTimbradoCFDI port = ws.getWSTimbradoCFDIPort();

        Accesos acceso = new Accesos();
        acceso.setUsuario(usuario);
        acceso.setPassword(pass);

         AcuseCFDI timbrarCFDI = port.timbrarCFDI(acceso, cdfi);
         System.out.println(timbrarCFDI.getError());
        return timbrarCFDI.getXmlTimbrado();
    }

    public String generateXml() {
        try {
            
            // Codigo para cancelacion
            InputStream inputFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(tempXml);

            DocumentBuilder builder = getDocumentBuilder();
            Document doc = builder.parse(inputFile);
            doc.getDocumentElement().normalize();

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(certPruebas);
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fis);
           
            doc.getDocumentElement().setAttribute("NoCertificado", getNumeroSerie(cert));
            doc.getDocumentElement().setAttribute("Certificado", getCertificado(cert));
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date hoyx = new Date();
            Calendar cld = Calendar.getInstance();
            cld.setTime(hoyx);
            String fechaHoy = formatter.format(hoyx).replace(" ", "T");
            doc.getDocumentElement().setAttribute("Fecha", fechaHoy);
            
            String cadenaOriginal = generaCadenaOriginalComprobante(doc);
            System.out.println("Cadena original = "+ cadenaOriginal);
            doc = sellar(doc, cadenaOriginal);

            XmlProcess xmlProcess = new XmlProcess(doc);
            return xmlProcess.generaTextoXML();

        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private Document sellar(Document doc, String cadenaOriginal){
        try {
            InputStream keyInputPruebas = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyPruebas);
            PKCS8Key key = new PKCS8Key(keyInputPruebas, passKey.toCharArray());
            Signature sign = Signature.getInstance("SHA256withRSA", "SunRsaSign");
            sign.initSign(key.getPrivateKey());
            sign.update(cadenaOriginal.getBytes("UTF-8"));
            System.out.println("actual::" + doc.getDocumentElement().getAttribute("Sello"));
  
            doc.getDocumentElement().setAttribute("Sello", Base64.getEncoder().encodeToString(sign.sign()));
            System.out.println("new::" + doc.getDocumentElement().getAttribute("Sello"));
            
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); 

            domFactory.setValidating(false); 
            domFactory.setIgnoringElementContentWhitespace(true); 
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
                    InputStream xsd = null;
                    try {
                        if (!isEmpty(systemId)) {
                            int index = systemId.lastIndexOf('/');
                            String archivoXsd = systemId.substring(index + 1, systemId.length());
                            //URL url = this.getClass().getClassLoader().getResource(_resourcesPath + "v3_2/xsds/");
                            URL url = new File("C:\\xslt_cfdi\\xsd\\").toURI().toURL();
                            String ruta = "";
                            if (url != null) {
                                ruta = url.getFile().replaceAll("%20", " ") + archivoXsd;
                            }
                            File xsdFile = new File(ruta);
                            if (!xsdFile.exists()) {
                                OutputStream out = null;
                                InputStream in = null;
                                try {
                                    out = new FileOutputStream(xsdFile);
                                    URLConnection conn = new URL(systemId).openConnection();
                                    conn.connect();
                                    in = conn.getInputStream();
                                    int b = 0;
                                    while (b != -1) {
                                        b = in.read();
                                        if (b != -1) {
                                            out.write(b);
                                        }
                                    }
                                } finally {
                                    if (out != null) {
                                        out.flush();
                                        out.close();
                                    }
                                    if (in != null) {
                                        in.close();
                                    }
                                }
                            }

                            xsd = new FileInputStream(xsdFile);
                        }
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    InputSource input = new InputSource(xsd);
                    input.setSystemId(systemId);
                    return input;
                }
            });
            return builder;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getNumeroSerie(X509Certificate certificado) {
        String numeroSerieHex;
        String numeroSerie;
        String aux;
        int i;
        numeroSerieHex = certificado.getSerialNumber().toString(16);
        //System.out.println(numeroSerieHex);
        numeroSerie = "";
        if ((numeroSerieHex.length() % 2) == 1) {
            numeroSerieHex.concat(" ");
        }
        for (i = 0; i < numeroSerieHex.length() / 2; i++) {
            aux = numeroSerieHex.substring(i * 2, (i * 2) + 2);
            numeroSerie = numeroSerie.concat(aux.substring(1));
        }
        return numeroSerie;
    }

    /**
     * codigo que genera el certificado a partir del archivo y regresa un string
     * con el certificado
     *
     * @return String con el certificado extraido
     */
    private String getCertificado(X509Certificate cert) {
        try {
            String certificado = "";
            byte[] buf = cert.getEncoded();
            certificado = Base64.getEncoder().encodeToString(buf).replaceAll("\n", "");
            return certificado;
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean isEmpty(Object s) {
        return ((s == null) || s.toString().trim().equals(""));
    }
    
     private String generaCadenaOriginalComprobante(Document doc) {
        try {
            String fileXslt = "com/resource/cadenaoriginal_3_3.xslt";
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileXslt);
            TransformerFactory tfFacXML = TransformerFactory.newInstance();
            StreamSource xslt = new StreamSource(input);
            Transformer tFacXML = tfFacXML.newTransformer(xslt);
            StringWriter stringWriter = new StringWriter();
            tFacXML.transform(new DOMSource(doc), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

   

}
