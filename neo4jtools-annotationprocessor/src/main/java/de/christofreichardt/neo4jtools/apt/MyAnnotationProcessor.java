/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.apt;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Developer
 */
@SupportedAnnotationTypes({"de.christofreichardt.neo4jtools.apt.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyAnnotationProcessor extends AbstractProcessor {
  
  private int pass = 0;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    System.out.printf("apt-init(ProcessingEnvironment processingEnv)%n");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    System.out.printf("apt-process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)%n");
    System.out.printf("apt-annotations.size() = %d%n", annotations.size());
    System.out.printf("apt-this.pass = %d%n", this.pass);
    
    try {
      if (this.pass == 0) {
        
        Map<TypeMirror, List<Element>> entity2Properties = buildEntity2Elements(roundEnv, Property.class);
        Map<TypeMirror, List<Element>> entity2Links = buildEntity2Elements(roundEnv, Links.class);
        Map<TypeMirror, List<Element>> entity2SingleLinks = buildEntity2Elements(roundEnv, SingleLink.class);
        
        FileObject mappingResource = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                                        "de.christofreichardt.neo4jtools.ogm", "object-graph-mapping.xml");
        try (OutputStream outputStream = mappingResource.openOutputStream()) {
          XMLStreamWriter xmlStreamWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream, "UTF-8"));
          
          try {
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            xmlStreamWriter.writeStartElement("Mapping");
            xmlStreamWriter.writeDefaultNamespace("http://www.christofreichardt.de/neo4jtools/apt");
            xmlStreamWriter.writeAttribute("timestamp", new Date().toString());
            
            Set<? extends Element> nodeEntityElements = roundEnv.getElementsAnnotatedWith(NodeEntity.class);
            for (Element element : nodeEntityElements) {
              if (!(element.getKind() == ElementKind.CLASS))
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Expected a class element.", element);
              TypeElement typeElement = (TypeElement) element;
              
              xmlStreamWriter.writeStartElement("NodeEntity");
              xmlStreamWriter.writeAttribute("className", typeElement.getQualifiedName().toString());
              
              NodeEntity nodeEntity = typeElement.getAnnotation(NodeEntity.class);
              xmlStreamWriter.writeAttribute("label", nodeEntity.label());
              
              for (Element otherElement : nodeEntityElements) {
                if (!(otherElement.getKind() == ElementKind.CLASS))
                  this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Expected a class element.", otherElement);
                TypeElement otherTypeElement = (TypeElement) otherElement;
                
                if (this.processingEnv.getTypeUtils().isSameType(otherElement.asType(), typeElement.getSuperclass()))
                  xmlStreamWriter.writeAttribute("isSubClassOf", otherTypeElement.getQualifiedName().toString());
              }
              
              processProperties(xmlStreamWriter, entity2Properties.get(element.asType()));
              processLinks(xmlStreamWriter, entity2Links.get(element.asType()));
              processSingleLinks(xmlStreamWriter, entity2SingleLinks.get(element.asType()));
              
              xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();
          }
          finally {
            xmlStreamWriter.close();
          }
        }
      }
    }
    catch (IOException | FactoryConfigurationError | XMLStreamException ex) {
      this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage());
    }
    finally {
      this.pass++;
    }
    
    return true;
  }
  
  private Map<TypeMirror, List<Element>> buildEntity2Elements(RoundEnvironment roundEnv, Class<? extends Annotation> annotationClass) {
    System.out.printf("apt-Building entity multimap for %s...%n", annotationClass.getSimpleName());
    
    Map<TypeMirror, List<Element>> entity2Elements = new HashMap<>();
    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);
    for (Element element : elements) {
      TypeMirror enclosingType = element.getEnclosingElement().asType();

      System.out.printf("apt-%s[%s]%n", element, enclosingType);
      
      if (element.getEnclosingElement().getAnnotation(NodeEntity.class) == null)
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Expected a '" + NodeEntity.class.getSimpleName() + "' annotation.", element.getEnclosingElement());

      if (!entity2Elements.containsKey(enclosingType)) {
        entity2Elements.put(enclosingType, new ArrayList<>());
      }
      entity2Elements.get(enclosingType).add(element);
    }
    
    return entity2Elements;
  }
  
  private void processLinks(XMLStreamWriter xmlStreamWriter, List<Element> linkElements) throws XMLStreamException {
    System.out.printf("apt-Processing links ...%n");
    
    if (linkElements != null) {
      for (Element linkElement : linkElements) {
        xmlStreamWriter.writeStartElement("Link");
        Links link = linkElement.getAnnotation(Links.class);

        System.out.printf("apt-link[direction=%s,type=%s)]%n", link.direction(), link.type());

        xmlStreamWriter.writeAttribute("direction", link.direction().name());
        xmlStreamWriter.writeAttribute("type", link.type());

        System.out.printf("apt-linkElement = %s%n", linkElement);

        xmlStreamWriter.writeStartElement("Field");
        xmlStreamWriter.writeAttribute("name", linkElement.toString());

        System.out.printf("apt-erasure(%s) = %s%n", linkElement.asType(), this.processingEnv.getTypeUtils().erasure(linkElement.asType()));

        xmlStreamWriter.writeStartElement(this.processingEnv.getTypeUtils().erasure(linkElement.asType()).toString());
        DeclaredType declaredType = (DeclaredType) linkElement.asType();
        if (declaredType.getTypeArguments().size() == 1) {
          xmlStreamWriter.writeCharacters(declaredType.getTypeArguments().get(0).toString());
        }
        else {
          this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Expected a generic collection.");
        }

        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeEndElement();
      }
    }
  }
  
  private void processProperties(XMLStreamWriter xmlStreamWriter, List<Element> propertyElements) throws XMLStreamException {
    System.out.printf("apt-Processing properties ...%n");
    
    if (propertyElements != null) {
      for (Element propertyElement : propertyElements) {
        xmlStreamWriter.writeStartElement("Property");
        Property property = propertyElement.getAnnotation(Property.class);

        System.out.printf("apt-property[name=%s]%n", property.name());
        System.out.printf("apt-propertyElement = %s%n", propertyElement);

        if ("".equals(property.name()))
          xmlStreamWriter.writeAttribute("name", propertyElement.toString());
        else
          xmlStreamWriter.writeAttribute("name", property.name());
        xmlStreamWriter.writeAttribute("nullable", Boolean.toString(property.nullable()));
        
        if (propertyElement.getAnnotation(Version.class) != null)
          xmlStreamWriter.writeAttribute("version", "true");
        
        xmlStreamWriter.writeStartElement("Field");
        xmlStreamWriter.writeAttribute("name", propertyElement.toString());
        xmlStreamWriter.writeAttribute("class", propertyElement.asType().toString());
        xmlStreamWriter.writeEndElement();
        
        Id id = propertyElement.getAnnotation(Id.class);
        NodeEntity nodeEntity = propertyElement.getEnclosingElement().getAnnotation(NodeEntity.class);
        if (id != null) {
          xmlStreamWriter.writeEmptyElement("Index");
          xmlStreamWriter.writeAttribute("label", nodeEntity.label());
          xmlStreamWriter.writeAttribute("primary", "true");
        }
        xmlStreamWriter.writeEndElement();
      }
    }
  }
  
  private void processSingleLinks(XMLStreamWriter xmlStreamWriter, List<Element> singleLinkElements) throws XMLStreamException {
    System.out.printf("apt-Processing singleLinks ...%n");
    
    if (singleLinkElements != null) {
      for (Element singleLinkElement : singleLinkElements) {
        xmlStreamWriter.writeStartElement("SingleLink");
        SingleLink singleLink = singleLinkElement.getAnnotation(SingleLink.class);

        System.out.printf("apt-singleLink[direction=%s, type=%s, nullable=%b)]%n", singleLink.direction(), singleLink.type(), singleLink.nullable());
        System.out.printf("apt-singleLinkElement = %s%n", singleLinkElement);

        xmlStreamWriter.writeAttribute("direction", singleLink.direction().name());
        xmlStreamWriter.writeAttribute("type", singleLink.type());
        xmlStreamWriter.writeAttribute("nullable", Boolean.toString(singleLink.nullable()));
        
        xmlStreamWriter.writeStartElement("Field");
        xmlStreamWriter.writeAttribute("name", singleLinkElement.toString());

        System.out.printf("apt-erasure(%s) = %s%n", singleLinkElement.asType(), this.processingEnv.getTypeUtils().erasure(singleLinkElement.asType()));
        
        if("de.christofreichardt.neo4jtools.ogm.Cell".equals(this.processingEnv.getTypeUtils().erasure(singleLinkElement.asType()).toString())) {
          xmlStreamWriter.writeStartElement(this.processingEnv.getTypeUtils().erasure(singleLinkElement.asType()).toString()); // e.g. Cell
          DeclaredType declaredType = (DeclaredType) singleLinkElement.asType();
          xmlStreamWriter.writeCharacters(declaredType.getTypeArguments().get(0).toString());
          xmlStreamWriter.writeEndElement(); // e.g. Cell
        }
        else
          this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Expected a Cell<T>.");

        xmlStreamWriter.writeEndElement(); // Field
        xmlStreamWriter.writeEndElement(); // SingleLink
      }
    }
  }
  
//  private void processVersions(XMLStreamWriter xmlStreamWriter, List<Element> versionElements) {
//    System.out.printf("apt-Processing versions ...%n");
//    
//    if (versionElements != null) {
//      if (versionElements.size() == 1) {
//        
//      }
//    }
//  }
}
