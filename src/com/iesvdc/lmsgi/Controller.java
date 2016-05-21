/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iesvdc.lmsgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Rafael Vargas del Moral
 * @author Manuel Quesada Segura
 * @author Javier García Fdez-Medina
 * @author David Martínez Tuñón
 * @version 0.0
 * 
 */


public class Controller {

    private File file;
    private File fileDTD;
    private File fileXSL; //para hojas de transformacion
    private File fileHTML; // para salida html en hojas de transformacion

    public Controller() {
        this.fileHTML=null;
        this.file=null;
        this.fileDTD=null;
        this.fileXSL=null;
    }
/**
 * Constructor al que le pasamos el fichero file
 * @param file
 */
    public Controller(File file) {
        this.file = file;
        this.fileHTML=null;
        //this.file=null;
        this.fileDTD=null;
        this.fileXSL=null;
    }

    /**
     * 
     * GETTERS Y SETTERS 
     */
    public File getFileDTD() {
        return fileDTD;
    }

    public File getFileHTML() {
        return fileHTML;
    }

    public File getFileXSL() {
        return fileXSL;
    }

    public void setFileDTD(File fileDTD) {
        this.fileDTD = fileDTD;
    }

    public void setFileHTML(File FileHTML) {
        this.fileHTML = FileHTML;
    }

    public void setFileXSL(File fileXSL) {
        this.fileXSL = fileXSL;
    }
    

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
  
    /**
     * método para evaluar expresiones xPath
     * @param stringXpath
     * @return resultado
     */
    
    public String xPathEvaluate(String stringXpath) {
        String resultado = "";

        try {

            Processor proc = new Processor(false);
            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);

            XdmNode documentoXML = builder.build(file);
            XPathCompiler xpath = proc.newXPathCompiler();
            XPathSelector selector = xpath.compile(stringXpath).load();

            selector.setContextItem(documentoXML);
            XdmValue evaluate = selector.evaluate();
            for (XdmItem item : evaluate) {
                resultado += item.getStringValue() + "\n";
            }

        } catch (SaxonApiException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultado;
    }

    /**
     * Método para validar DTD
     * @return resultado
     */
    public String validar() {
        
        String resultado = "procesando fichero " + file.getPath().toString() + "\n";
        try {
            DomUtil.parse(file, true);
            resultado += "\n fichero procesado";
            return resultado;
        } catch (ParserConfigurationException | IOException | SAXException ex) {

            resultado += ex.getLocalizedMessage() + "\n";
        }
        return resultado;
    }
   /**
    * método para transformar XSLT
    * @return resultado 
    */

    String validateXSD() {
        String resultado = "validacion xsd correcta";
        try {
            Document doc = DomUtil.parseXSD(this.file/*, null*/);

        } catch (ParserConfigurationException | IOException | SAXException ex) {
            resultado = ex.getLocalizedMessage();
        }
        return resultado;
    }
/**
 * método para transfrmar XSLT
 * @return resultado 
 */
    public  String xslTransform() {
        String resultado = "Transformacion completada correctamente";
        File xmlFile=this.file; File xslFile=this.fileXSL; File htmlOut=this.fileHTML;
        if (xmlFile != null && xslFile != null && htmlOut != null) {
            try {

                Processor proc = new Processor(false);
                XsltCompiler comp = proc.newXsltCompiler();
                XsltExecutable exp = comp.compile(new StreamSource(xslFile));
                XdmNode source = proc.newDocumentBuilder().build(new StreamSource(xmlFile));
                Serializer out = proc.newSerializer(htmlOut);
                out.setOutputProperty(Serializer.Property.METHOD, "html");
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                XsltTransformer trans = exp.load();
                trans.setInitialContextNode(source);
                trans.setDestination(out);
                trans.transform();
                
            } catch (SaxonApiException ex) {
                //Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                resultado = ex.getLocalizedMessage();
            }
        } else {
            resultado = "fichero no valido";
        }
        return resultado;
    }
    /**
     * 
     * método para guardar el XML
     * @param contenido
     * @return resultado
     */
    public boolean save2xml(String contenido){
        boolean resultado=true;
        try (FileWriter outFileXML_XSLT= new FileWriter(this.file);){
            outFileXML_XSLT.write(contenido);
            outFileXML_XSLT.flush();
        } catch (IOException ex) {
            resultado=false;
        }
        return resultado;
    }
    /**
     * 
     * método para guardar el XSL
     * @param contenido
     * @return resultado
     */
    
    public boolean save2xsl(String contenido){
         boolean resultado=true;
        try (FileWriter outFileXML_XSLT= new FileWriter(this.fileXSL);){
            outFileXML_XSLT.write(contenido);
            outFileXML_XSLT.flush();
        } catch (IOException ex) {
            resultado=false;
        }
        return resultado;
    }
    
     /**
     * 
     * método para guardar el DTD
     * @param contenido
     * @return resultado
     */
    
    public boolean save2DTD(String contenido){
         boolean resultado=true;
        try (FileWriter outFileXML_XSLT= new FileWriter(this.fileDTD);){
            outFileXML_XSLT.write(contenido);
            outFileXML_XSLT.flush();
        } catch (IOException ex) {
            resultado=false;
        }
        return resultado;
    }    
    
    

}
