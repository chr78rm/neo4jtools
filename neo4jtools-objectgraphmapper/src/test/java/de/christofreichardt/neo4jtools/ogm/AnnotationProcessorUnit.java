package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.apt.AptNamespaceContext;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Christof Reichardt
 */
public class AnnotationProcessorUnit implements Traceable {
  final private Properties properties;
  private final XPath xPath = XPathFactory.newInstance().newXPath();
  private Document mappingDocument;

  public AnnotationProcessorUnit(Properties properties) {
    this.properties = properties;
  }
  
  @Before
  public void init() throws SAXException, IOException, ParserConfigurationException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "init()");

    try {
      Path path = FileSystems
          .getDefault()
          .getPath(".", "target", "test-classes", "de", "christofreichardt", "neo4jtools", "ogm", "object-graph-mapping.xml");
      
      Assert.assertTrue("Missing mapping file.", Files.exists(path));
      
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      this.mappingDocument = documentBuilder.parse(path.toFile());
      this.xPath.setNamespaceContext(new AptNamespaceContext());
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void mappedEntities() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "mappedEntities()");
    
    try {
      NodeList nodes = (NodeList) xPath.evaluate("/:Mapping/:NodeEntity/@className"
          , this.mappingDocument.getDocumentElement()
          , XPathConstants.NODESET);
      
      tracer.out().printfIndentln("nodes.getLength() = %d", nodes.getLength());
      for (int i=0; i<nodes.getLength(); i++) {
        tracer.out().printfIndentln("className = %s", nodes.item(i).getNodeValue());
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void mappedProperties() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "mappedProperties()");
    
    try {
      NodeList classNameNodes = (NodeList) xPath.evaluate("/:Mapping/:NodeEntity/@className"
          , this.mappingDocument.getDocumentElement()
          , XPathConstants.NODESET);
      String[] classNames = new String[classNameNodes.getLength()];
      for (int i=0; i<classNameNodes.getLength(); i++) {
        classNames[i] = classNameNodes.item(i).getNodeValue();
      }
      
      for (String className : classNames) {
        tracer.out().printfIndentln("className = %s", className);
        
        NodeList propertyNameNodes = (NodeList) xPath.evaluate("/:Mapping/:NodeEntity[@className='" + className + "']/:Property/@name"
            , this.mappingDocument.getDocumentElement()
            , XPathConstants.NODESET);

        tracer.out().printfIndentln("nodes.getLength() = %d", propertyNameNodes.getLength());
        for (int i=0; i<propertyNameNodes.getLength(); i++) {
          tracer.out().printfIndentln("propertyName = %s", propertyNameNodes.item(i).getNodeValue());
        }
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void indices() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "indices()");
    
    try {
      NodeList nodes = (NodeList) xPath.evaluate("/:Mapping/:NodeEntity/:Property/:Index/@label"
          , this.mappingDocument.getDocumentElement()
          , XPathConstants.NODESET);
      
      tracer.out().printfIndentln("nodes.getLength() = %d", nodes.getLength());
      for (int i=0; i<nodes.getLength(); i++) {
        tracer.out().printfIndentln("label = %s", nodes.item(i).getNodeValue());
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @After
  public void exit() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "exit()");
    
    try {
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }

}
