/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Christof Reichardt
 */
public class Node2ObjectMapper implements Traceable {
  
  public static class Exception extends GraphPersistenceException {
    public Exception(String message) {
      super(message);
    }
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }
  
  final private RichNode node;
  final private MappingInfo mappingInfo;
  final private Class<?> mostSpecificClass;

  public Node2ObjectMapper(Node node) throws MappingInfo.Exception, Node2ObjectMapper.Exception {
    this(node, new MappingInfo());
  }

  public Node2ObjectMapper(Node node, MappingInfo mappingInfo) throws Node2ObjectMapper.Exception {
    if (node == null)
      throw new NullPointerException("Need a Node.");
    
    this.node = new RichNode(node);
    this.mappingInfo = mappingInfo;
    this.mostSpecificClass = detectMostSpecificClass();
  }

  public Class<?> getMostSpecificClass() {
    return this.mostSpecificClass;
  }
  
  public <S extends Enum<S> & Label, T extends Enum<T> & RelationshipType> Object map(Class<T> relationshipTypes) throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "map(Class<T> relationshipTypes)");
    
    try {
      tracer.out().printfIndentln("mostSpecificClass.getName() = %s", this.mostSpecificClass.getName());
      
      try {
        Object entity = this.mostSpecificClass.newInstance();
        entity = coverFields(entity);
        entity = coverLinks(entity, relationshipTypes);
        entity = coverSingleLinks(entity, relationshipTypes);
      
        return entity;
      }
      catch (InstantiationException | IllegalAccessException ex) {
        throw new Node2ObjectMapper.Exception("Problems when mapping the entity.", ex);
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private Class<?> detectMostSpecificClass() throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Class<?>", this, "detectMostSpecificClass()");
    
    try {
      int numberOfLabels = 0;
      for (Label label : this.node.getLabels()) {
        numberOfLabels++;
      }
      
      Class<?> mostSpecificClass = null;
      for (Label label : this.node.getLabels()) {
        Class<?> mappedClass = this.mappingInfo.getMappedClass(label);
        
        tracer.out().printfIndentln("mappedClass.getName() = %s", mappedClass.getName());
        
        if (this.mappingInfo.getLabels(mappedClass).size() == numberOfLabels) {
          mostSpecificClass = mappedClass;
          break;
        }
      }
      
      if (mostSpecificClass == null)
        throw new Node2ObjectMapper.Exception("No most specific class found.");
      
      return mostSpecificClass;
    }
    finally {
      tracer.wayout();
    }
  }
  
  private Object coverFields(Object entity) throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "coverFields(Object entity)");
    
    try {
      ReflectedClass reflectedClass = new ReflectedClass(entity.getClass());
      Set<Map.Entry<String, PropertyData>> propertyMappings = this.mappingInfo.getPropertyMappings(entity.getClass());
      for (Map.Entry<String, PropertyData> propertyMapping : propertyMappings) {
        try {
          String fieldName = propertyMapping.getKey();
          PropertyData propertyData = propertyMapping.getValue();
          Object property = this.node.getProperty(propertyData.getName(), null);
          
          tracer.out().printfIndentln("propertyMapping[%s] = %s, property = %s", fieldName, propertyData, property);
          
          Field field = reflectedClass.getDeclaredField(fieldName);
          if (Modifier.isFinal(field.getModifiers()))
            throw new Node2ObjectMapper.Exception("Field '" + fieldName + "' is declared final.");
          field.setAccessible(true);
          if (!propertyData.isNullable()  &&  !this.node.hasProperty(propertyData.getName())) 
            throw new Node2ObjectMapper.Exception("Value expected for property '" + propertyData.getName() + "'.");
          field.set(entity, property);
        }
        catch (NoSuchFieldException ex) {
          throw new Node2ObjectMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the field has been made accessible
        }
      }
      
      return entity;
    }
    finally {
      tracer.wayout();
    }
  }
  
  private <T extends Enum<T> & RelationshipType> Object coverLinks(Object entity, Class<T> relationshipTypes) throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "coverLinks(Object entity, Class<T> relationshipTypes)");
    
    try {
      ReflectedClass reflectedClass = new ReflectedClass(entity.getClass());
      Set<Map.Entry<String, LinkData>> linkMappings = this.mappingInfo.getLinkMappings(entity.getClass());
      for (Map.Entry<String, LinkData> linkMapping : linkMappings) {
        String fieldName = linkMapping.getKey();
        LinkData linkData = linkMapping.getValue();
        
        tracer.out().printfIndentln("linkData[%s] = %s", fieldName, linkData);
        
        try {
          Field field = reflectedClass.getDeclaredField(fieldName);
          if (Modifier.isFinal(field.getModifiers()))
            throw new Node2ObjectMapper.Exception("Field '" + fieldName + "' is declared final.");
          field.setAccessible(true);
          Collection<Object> entities = new ProxyList<>(this.node, this.mostSpecificClass, linkData, this.mappingInfo, relationshipTypes);
          field.set(entity, entities);
        }
        catch (NoSuchFieldException ex) {
          throw new Node2ObjectMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the field has been made accessible
        }
      }
      
      return entity;
    }
    finally {
      tracer.wayout();
    }
  }
  
  private <T extends Enum<T> & RelationshipType> Object coverSingleLinks(Object entity, Class<T> relationshipTypes) throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "coverSingleLinks(Object entity, Class<T> relationshipTypes)");
    
    try {
      ReflectedClass reflectedClass = new ReflectedClass(entity.getClass());
      Set<Map.Entry<String, SingleLinkData>> singleLinkMappings = this.mappingInfo.getSingleLinkMappings(entity.getClass());
      for (Map.Entry<String, SingleLinkData> singleLinkMapping : singleLinkMappings) {
        String fieldName = singleLinkMapping.getKey();
        SingleLinkData singleLinkData = singleLinkMapping.getValue();
        
        tracer.out().printfIndentln("singleLinkData[%s] = %s", fieldName, singleLinkData);
        
        try {
          Field field = reflectedClass.getDeclaredField(fieldName);
          if (Modifier.isFinal(field.getModifiers()))
            throw new Node2ObjectMapper.Exception("Field '" + fieldName + "' is declared final.");
          field.setAccessible(true);
          Cell<Object> proxy = new ProxyObject<>(this.node, singleLinkData, this.mappingInfo, relationshipTypes, this.mostSpecificClass);
          field.set(entity, proxy);
        }
        catch (NoSuchFieldException ex) {
          throw new Node2ObjectMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the field has been made accessible
        }
      }
      
      return entity;
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
