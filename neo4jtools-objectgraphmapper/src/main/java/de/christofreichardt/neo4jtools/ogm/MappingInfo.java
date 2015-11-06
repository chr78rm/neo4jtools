/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.Links;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.apt.Version;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Developer
 */
public class MappingInfo implements Traceable {
  
  public static class Exception extends GraphPersistenceException {
    public Exception(String message) {
      super(message);
    }
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }
  
  final private Map<String, Element> entityMap = new HashMap<>();
  final private Map<Element, Map<String, PropertyData>> fieldMap = new HashMap<>();
  final private Map<Element, Map<String, LinkData>> linkMap = new HashMap<>();
  final private Map<Element, Map<String, SingleLinkData>> singleLinkMap = new HashMap<>();
  final private Map<Element, PrimaryKeyData> primaryKeyMap = new HashMap<>();
  final private Map<Element, String> versionFieldMap = new HashMap<>();
  final private Map<String, Class<?>> labelMap = new HashMap<>();

  public MappingInfo() throws MappingInfo.Exception {
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream resourceAsStream = MappingInfo.class.getClassLoader().getResourceAsStream("de/christofreichardt/neo4jtools/ogm/object-graph-mapping.xml");
      org.w3c.dom.Document mappingDocument = documentBuilder.parse(resourceAsStream);
      readMapping(mappingDocument);
    }
    catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | ClassNotFoundException ex) {
      throw new MappingInfo.Exception("Cannot create MappingInfos.", ex);
    }
  }
  
  public MappingInfo(Class<?>... entityClasses) throws MappingInfo.Exception {
    try {
      org.w3c.dom.Document mappingDocument = evaluateAnnotations(entityClasses);
      readMapping(mappingDocument);
    }
    catch (IOException | XMLStreamException | ParserConfigurationException | SAXException | XPathExpressionException | ClassNotFoundException ex) {
      throw new MappingInfo.Exception("Cannot create MappingInfos.", ex);
    }
  }
  
  private void readMapping(org.w3c.dom.Document mappingDocument) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ClassNotFoundException, MappingInfo.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "readMapping()");

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      NodeList nodeEntityNodes = (NodeList) xPath.evaluate("/Mapping/NodeEntity", mappingDocument.getDocumentElement(), XPathConstants.NODESET);
      for (int i=0; i<nodeEntityNodes.getLength(); i++) {
        Element entityElement = (Element) nodeEntityNodes.item(i);
        String className = entityElement.getAttribute("className");
        
        tracer.out().printfIndentln("className = %s", className);
        
        Class.forName(className);
        this.entityMap.put(className, entityElement);
        String label = entityElement.getAttribute("label");
        
        tracer.out().printfIndentln("label = %s", label);
        
        if (!this.labelMap.containsKey(label))
          this.labelMap.put(label, Class.forName(className));
        else
          throw new MappingInfo.Exception("Duplicate label: '" + label + "'.");
      }
      
      for (Element entityElement : this.entityMap.values()) {
        {
          this.fieldMap.put(entityElement, new HashMap<>());
          NodeList propertyNodes = (NodeList) xPath.evaluate("Property", entityElement, XPathConstants.NODESET);
          for (int i=0; i<propertyNodes.getLength(); i++) {
            String propertyName = ((Element) propertyNodes.item(i)).getAttribute("name");
            Boolean propertyNullable = Boolean.valueOf(((Element) propertyNodes.item(i)).getAttribute("nullable"));
            Boolean propertyVersion = Boolean.valueOf(((Element) propertyNodes.item(i)).getAttribute("version"));
            PropertyData propertyData = new PropertyData(propertyName, propertyNullable, propertyVersion);
            org.w3c.dom.Node fieldNode = (org.w3c.dom.Node) xPath.evaluate("Field", propertyNodes.item(i), XPathConstants.NODE);
            String fieldName = ((Element) fieldNode).getAttribute("name");
            this.fieldMap.get(entityElement).put(fieldName, propertyData);
          }
        }
        
        {
          this.linkMap.put(entityElement, new HashMap<>());
          NodeList linkNodes = (NodeList) xPath.evaluate("Link", entityElement, XPathConstants.NODESET);
          for (int i=0; i<linkNodes.getLength(); i++) {
            Direction direction = Enum.valueOf(Direction.class, ((Element) linkNodes.item(i)).getAttribute("direction"));
            String type = ((Element) linkNodes.item(i)).getAttribute("type");
            org.w3c.dom.Node fieldNode = (org.w3c.dom.Node) xPath.evaluate("Field", linkNodes.item(i), XPathConstants.NODE);
            String className = (String) xPath.evaluate("*/text()", fieldNode, XPathConstants.STRING);
            LinkData linkData = new LinkData(direction, type, className);
            String fieldName = ((Element) fieldNode).getAttribute("name");
            this.linkMap.get(entityElement).put(fieldName, linkData);
          }
        }
        
        {
          this.singleLinkMap.put(entityElement, new HashMap<>());
          NodeList singleLinkNodes = (NodeList) xPath.evaluate("SingleLink", entityElement, XPathConstants.NODESET);
          for (int i=0; i<singleLinkNodes.getLength(); i++) {
            Direction direction = Enum.valueOf(Direction.class, ((Element) singleLinkNodes.item(i)).getAttribute("direction"));
            String type = ((Element) singleLinkNodes.item(i)).getAttribute("type");
            boolean nullable = Boolean.valueOf(((Element) singleLinkNodes.item(i)).getAttribute("nullable"));
            org.w3c.dom.Node fieldNode = (org.w3c.dom.Node) xPath.evaluate("Field", singleLinkNodes.item(i), XPathConstants.NODE);
            String className = (String) xPath.evaluate("*/text()", fieldNode, XPathConstants.STRING);
            SingleLinkData singleLinkData = new SingleLinkData(direction, type, className, nullable);
            String fieldName = ((Element) fieldNode).getAttribute("name");
            this.singleLinkMap.get(entityElement).put(fieldName, singleLinkData);
          }
        }
        
        // TODO: sanity checks 
        org.w3c.dom.Node indexNode = (org.w3c.dom.Node) xPath.evaluate("Property/PrimaryKey", entityElement, XPathConstants.NODE);
        if (indexNode != null) {
          String label = ((Element) indexNode).getAttribute("label");
          String fieldName = (String) xPath.evaluate("../Field/@name", indexNode, XPathConstants.STRING);
          boolean generated = "true".equals(((Element) indexNode).getAttribute("generated"));
          PrimaryKeyData primaryKeyData = new PrimaryKeyData(label, fieldName, generated);
          this.primaryKeyMap.put(entityElement, primaryKeyData);
        }
        
        org.w3c.dom.Node versionFieldNode = (org.w3c.dom.Node) xPath.evaluate("Property[@version='true']/Field", entityElement, XPathConstants.NODE);
        if (versionFieldNode != null)
          this.versionFieldMap.put(entityElement, ((Element) versionFieldNode).getAttribute("name"));
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private org.w3c.dom.Document evaluateAnnotations(Class<?>[] entityClasses) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "evaluateAnnotations(Class<?>[] entityClasses)");

    try {
      tracer.out().printfIndentln("entityClasses.length = %d", entityClasses.length);
      
      File file = new File("." + File.separator + "object-graph-mapping.xml");
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        IndentingXMLStreamWriter xmlStreamWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8"));
        xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
        xmlStreamWriter.writeStartElement("Mapping");
        
        for (Class<?> entityClass : entityClasses) {
          tracer.out().printfIndentln("entityClass.getName() = %s", entityClass.getName());
          
          NodeEntity nodeEntity = entityClass.getAnnotation(NodeEntity.class);
          xmlStreamWriter.writeStartElement("NodeEntity");
          xmlStreamWriter.writeAttribute("className", entityClass.getName());
          if (entityClass.isAnnotationPresent(NodeEntity.class)) {
            for (Field field : entityClass.getDeclaredFields()) {
              if (field.isAnnotationPresent(Property.class)) {
                tracer.out().printfIndentln("'%s' field annotated with '%s'", field.getName(), Property.class.getSimpleName());
                
                xmlStreamWriter.writeStartElement("Property");
                if ("".equals(field.getAnnotation(Property.class).name()))
                  xmlStreamWriter.writeAttribute("name", field.getName());
                else
                  xmlStreamWriter.writeAttribute("name", field.getAnnotation(Property.class).name());
                xmlStreamWriter.writeAttribute("nullable", Boolean.toString(field.getAnnotation(Property.class).nullable()));
                if (field.isAnnotationPresent(Version.class))
                  xmlStreamWriter.writeAttribute("version", "true");
                xmlStreamWriter.writeStartElement("Field");
                xmlStreamWriter.writeAttribute("name", field.getName());
                xmlStreamWriter.writeAttribute("class", field.getType().getName());
                xmlStreamWriter.writeEndElement();
                if (field.isAnnotationPresent(Id.class)) {
                  xmlStreamWriter.writeEmptyElement("Index");
                  xmlStreamWriter.writeAttribute("label", nodeEntity.label());
                  xmlStreamWriter.writeAttribute("primary", "true");
                }
                xmlStreamWriter.writeEndElement();
              }
              else if (field.isAnnotationPresent(Links.class)) {
                tracer.out().printfIndentln("'%s' field annotated with '%s'", field.getName(), Links.class.getSimpleName());
                
                xmlStreamWriter.writeStartElement("Link");
                xmlStreamWriter.writeAttribute("direction", field.getAnnotation(Links.class).direction().name());
                xmlStreamWriter.writeAttribute("type", field.getAnnotation(Links.class).type());
                xmlStreamWriter.writeStartElement("Field");
                xmlStreamWriter.writeAttribute("name", field.getName());
                xmlStreamWriter.writeStartElement(field.getType().getName());

                MyField myField = new MyField(field);
                if (!myField.typeImplements(Collection.class))
                  throw new IllegalArgumentException("The type of '" + field.getName() + "' isn't a collection class.");

                Type genericType = field.getGenericType();
                if (!(genericType instanceof ParameterizedType)) 
                  throw new IllegalArgumentException("The type of '" + field.getName() + "' isn't generic.");

                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                Class<?> clazz = (Class<?>) typeArgument;
                if (!clazz.isAnnotationPresent(NodeEntity.class))
                  throw new IllegalArgumentException("'" + clazz.getName() + "' isn't an entity class.");

                xmlStreamWriter.writeCharacters(clazz.getName());
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndElement();
              }
              else if (field.isAnnotationPresent(SingleLink.class)) {
                tracer.out().printfIndentln("'%s' field annotated with '%s'", field.getName(), SingleLink.class.getSimpleName());
                xmlStreamWriter.writeStartElement("SingleLink");
                xmlStreamWriter.writeAttribute("direction", field.getAnnotation(SingleLink.class).direction().name());
                xmlStreamWriter.writeAttribute("type", field.getAnnotation(SingleLink.class).type());
                xmlStreamWriter.writeAttribute("nullable", Boolean.toString(field.getAnnotation(SingleLink.class).nullable()));
                
                xmlStreamWriter.writeStartElement("Field");
                xmlStreamWriter.writeAttribute("name", field.getName());
                
                if (!"de.christofreichardt.neo4jtools.ogm.Cell".equals(field.getType().getName()))
                  throw new IllegalArgumentException("Expected a Cell<T>.");
                
                Type genericType = field.getGenericType();
                if (!(genericType instanceof ParameterizedType)) 
                  throw new IllegalArgumentException("The type of '" + field.getName() + "' isn't generic.");
                
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                Class<?> clazz = (Class<?>) typeArgument;
                if (!clazz.isAnnotationPresent(NodeEntity.class))
                  throw new IllegalArgumentException("'" + clazz.getName() + "' isn't an entity class.");
                
                xmlStreamWriter.writeStartElement(field.getType().getName());
                xmlStreamWriter.writeCharacters(clazz.getName());
                xmlStreamWriter.writeEndElement();
                
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndElement();
              }
            }
            xmlStreamWriter.writeEndElement();
          }
          else
            tracer.logMessage(LogLevel.WARNING, entityClass.getName() + " isn't annotated with '" + NodeEntity.class.getSimpleName() + "'", getClass(), "evaluateAnnotations(Class<?>[] entityClasses)");
        }
        
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndDocument();
      }
      
      try (FileInputStream inputStream = new FileInputStream(file)) {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(inputStream);
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  public Set<Class<?>> getMappedEntityClasses() {
    Set<Class<?>> mappedEntities = this.entityMap
        .keySet()
        .stream()
        .map((String className) -> {
          try {
            return Class.forName(className);
          }
          catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
          }
        })
        .collect(Collectors.toSet());

    return mappedEntities;
  }
  
  public Set<Map.Entry<String,PropertyData>> getPropertyMappings(Class<?> entityClass) {
    if (!this.entityMap.containsKey(entityClass.getName()))
      throw new IllegalArgumentException("Unknown class: '" + entityClass.getName() + "'.");
    
    Element entityElement = this.entityMap.get(entityClass.getName());
    Set<Map.Entry<String,PropertyData>> combinedFieldSet = new HashSet<>(this.fieldMap.get(entityElement).entrySet());
    while (entityElement.hasAttribute("isSubClassOf")) {
      String superClassName = entityElement.getAttribute("isSubClassOf");
      entityElement = this.entityMap.get(superClassName);
      combinedFieldSet.addAll(this.fieldMap.get(entityElement).entrySet());
    }

    return combinedFieldSet;
  }
  
  public PropertyData getPropertyMappingForField(String fieldName, Class<?> entityClass) {
    if (!this.entityMap.containsKey(entityClass.getName()))
      throw new IllegalArgumentException("Unknown class: '" + entityClass.getName() + "'.");

    Element entityElement = this.entityMap.get(entityClass.getName());
    while (!this.fieldMap.get(entityElement).containsKey(fieldName) && entityElement.hasAttribute("isSubClassOf")) {
      String superClassName = entityElement.getAttribute("isSubClassOf");
      entityElement = this.entityMap.get(superClassName);
    }
    
    return this.fieldMap.get(entityElement).get(fieldName);
  }
  
  public Set<Map.Entry<String, LinkData>> getLinkMappings(Class<?> entityClass) {
    if (!this.entityMap.containsKey(entityClass.getName()))
      throw new IllegalArgumentException("Unknown class: '" + entityClass.getName() + "'.");
    
    Element entityElement = this.entityMap.get(entityClass.getName());
    Set<Map.Entry<String,LinkData>> combinedLinkSet = new HashSet<>(this.linkMap.get(entityElement).entrySet());
    while (entityElement.hasAttribute("isSubClassOf")) {
      String superClassName = entityElement.getAttribute("isSubClassOf");
      entityElement = this.entityMap.get(superClassName);
      combinedLinkSet.addAll(this.linkMap.get(entityElement).entrySet());
    }

    return combinedLinkSet;
  }
  
  public Set<Map.Entry<String, SingleLinkData>> getSingleLinkMappings(Class<?> entityClass) {
    if (!this.entityMap.containsKey(entityClass.getName()))
      throw new IllegalArgumentException("Unknown class: '" + entityClass.getName() + "'.");
    
    Element entityElement = this.entityMap.get(entityClass.getName());
    Set<Map.Entry<String,SingleLinkData>> combinedSingleLinkSet = new HashSet<>(this.singleLinkMap.get(entityElement).entrySet());
    while (entityElement.hasAttribute("isSubClassOf")) {
      String superClassName = entityElement.getAttribute("isSubClassOf");
      entityElement = this.entityMap.get(superClassName);
      combinedSingleLinkSet.addAll(this.singleLinkMap.get(entityElement).entrySet());
    }
    
    return combinedSingleLinkSet;
  }
  
  public PrimaryKeyData getPrimaryKeyMapping(Class<?> entityClass) {
    if (!this.entityMap.containsKey(entityClass.getName()))
      throw new IllegalArgumentException("Unknown class: '" + entityClass.getName() + "'.");

    Element entityElement = this.entityMap.get(entityClass.getName());
    while (entityElement.hasAttribute("isSubClassOf")) {
      String superClassName = entityElement.getAttribute("isSubClassOf");
      entityElement = this.entityMap.get(superClassName);
    }
    
    if (!this.primaryKeyMap.containsKey(entityElement))
      throw new IllegalArgumentException("No primary index available for '" + entityClass.getName() + "'");

    return this.primaryKeyMap.get(entityElement);
  }
  
  public String getVersionFieldName(Class<?> entityClass) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("String", this, "getVersionFieldName(Class<?> entityClass)");

    try {
      tracer.out().printfIndentln("entityClass.getName() = %s", entityClass.getName());
      
      if (!entityClass.isAnnotationPresent(NodeEntity.class))
        throw new IllegalArgumentException("No entity class: '" + entityClass.getName() + "'");
      
      Element entityElement = this.entityMap.get(entityClass.getName());
      
      String versionFieldName = null;
      if (this.versionFieldMap.containsKey(entityElement))
        versionFieldName = this.versionFieldMap.get(entityElement);
      
      return versionFieldName;
    }
    finally {
      tracer.wayout();
    }
  }
  
  public Set<String> getLabels(Class<?> entityClass) {
    String className = entityClass.getName();
    if (!this.entityMap.containsKey(className))
      throw new IllegalArgumentException("Unknown class: '" + className + "'.");

    Set<String> labels = new HashSet<>();
    Element entityElement;
    do {
      entityElement = this.entityMap.get(className);
      labels.add(entityElement.getAttribute("label"));
      if (entityElement.hasAttribute("isSubClassOf"))
        className = entityElement.getAttribute("isSubClassOf");
      else
        break;
    } while (true);

    return labels;
  }
  
  public Class<?> getMappedClass(Label label) {
    if (!this.labelMap.containsKey(label.name()))
      throw new IllegalArgumentException("Unknown label: '" + label + "'.");
    
    return this.labelMap.get(label.name());
  }

  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
