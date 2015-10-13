/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.idgen.IdGeneratorService;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Christof Reichardt
 */
public class Object2NodeMapper implements Traceable {
  
  public static class Exception extends GraphPersistenceException {
    public Exception(String message) {
      super(message);
    }
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }
  
  final private Object entity;
  final private MappingInfo mappingInfo;
  final private GraphDatabaseService graphDatabaseService;
  final private Class<?> entityClass;
  final private Map<Class<?>, Map<Object, Node>> processingEntityIds2NodeMap;
  final private String label;

  public Object2NodeMapper(Object entity, GraphDatabaseService graphDatabaseService) throws MappingInfo.Exception {
    this(entity, new MappingInfo(), graphDatabaseService);
  }

  public Object2NodeMapper(Object entity, MappingInfo mappingInfo, GraphDatabaseService graphDatabaseService) {
    this(entity, mappingInfo, graphDatabaseService, new HashMap<>());
  }

  public Object2NodeMapper(Object entity, MappingInfo mappingInfo, GraphDatabaseService graphDatabaseService, Map<Class<?>, Map<Object, Node>> processedEntityIds2NodeMap) {
    this.entity = entity;
    this.mappingInfo = mappingInfo;
    this.graphDatabaseService = graphDatabaseService;
    this.entityClass = entity.getClass();
    this.processingEntityIds2NodeMap = processedEntityIds2NodeMap;
    if (!this.processingEntityIds2NodeMap.containsKey(this.entityClass))
      this.processingEntityIds2NodeMap.put(this.entityClass, new HashMap<>());
    this.label = this.mappingInfo.getPrimaryKeyMapping(this.entityClass).getLabel();
  }

  public <S extends Enum<S> & Label, T extends Enum<T> & RelationshipType> 
    Node map(Class<S> labels, Class<T> relationshipTypes) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "map(Class<S> labels, Class<T> relationshipTypes)");
    
    try {
      try {
        String idFieldName = this.mappingInfo.getPrimaryKeyMapping(this.entityClass).getFieldName();
        Field idField = this.entityClass.getDeclaredField(idFieldName);
        idField.setAccessible(true);
        Object primaryKeyValue = handleIdField(this.entityClass, this.entity, idField);
        String idPropertyKey = this.mappingInfo.getPropertyMappingForField(idFieldName, this.entityClass).getName();
        
        tracer.out().printfIndentln("idFieldName[%s] = %s", idFieldName, primaryKeyValue);
        
        Node entityNode = this.graphDatabaseService.findNode(Enum.valueOf(labels, this.label), idPropertyKey, primaryKeyValue);
        if (entityNode == null) {
          tracer.out().printfIndentln("Saving ...");
          entityNode = this.graphDatabaseService.createNode(Enum.valueOf(labels, this.label));
        }
        else
          tracer.out().printfIndentln("Merging ...");
        
        // TODO: check for staleness
        
        this.processingEntityIds2NodeMap.get(this.entityClass).put(primaryKeyValue, entityNode);
        entityNode = coverProperties(entityNode);
        entityNode = coverLinks(entityNode, labels, relationshipTypes);
        entityNode = coverSingleLinks(entityNode, labels, relationshipTypes);
        
        return entityNode;
      }
      catch (NoSuchFieldException | SecurityException | IllegalArgumentException ex) {
        throw new Object2NodeMapper.Exception("Problems when mapping the entity.", ex);
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private Node coverProperties(final Node entityNode) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "coverProperties(final Node entityNode)");
    
    try {
      Set<Map.Entry<String, PropertyData>> propertyMappings = this.mappingInfo.getPropertyMappings(this.entityClass);
      for (Map.Entry<String, PropertyData> propertyMapping : propertyMappings) {
        try {
          String fieldName = propertyMapping.getKey();
          PropertyData propertyData = propertyMapping.getValue();
          Field propertyField = this.entityClass.getDeclaredField(fieldName);
          propertyField.setAccessible(true);
          Object property = propertyField.get(this.entity);
          
          tracer.out().printfIndentln("propertyMapping[%s] = %s, property = %s", fieldName, propertyData, property);
        
          if (property == null  &&  !propertyData.isNullable()) 
            throw new Object2NodeMapper.Exception("Value required for property '" + fieldName + "'.");
          
          if (property == null  &&  entityNode.hasProperty(propertyMapping.getKey()))
            entityNode.removeProperty(propertyMapping.getKey());
          else if (property != null )
            entityNode.setProperty(propertyMapping.getKey(), property);
        }
        catch (NoSuchFieldException ex) {
          tracer.logException(LogLevel.ERROR, ex, getClass(), "coverProperties(final Node entityNode)");
          throw new Object2NodeMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the field has been made accessible
        }
      }
      
      return entityNode;
    }
    finally {
      tracer.wayout();
    }
  }
  
  private <S extends Enum<S> & Label, T extends Enum<T> & RelationshipType>
      Node coverLinks(final Node entityNode, Class<S> labels, Class<T> relationshipTypes) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "coverLinks(final Node entityNode, Class<S> labels, Class<T> relationshipTypes)");
    
    try {
      Set<Map.Entry<String, LinkData>> linkMappings = this.mappingInfo.getLinkMappings(this.entityClass);
      for (Map.Entry<String, LinkData> linkMapping : linkMappings) {
        String fieldName = linkMapping.getKey();
        LinkData linkData = linkMapping.getValue();
        
        tracer.out().printfIndentln("linkMapping[%s] = %s", fieldName, linkData);
        
        try {
          Field linkField = this.entityClass.getDeclaredField(fieldName);
          linkField.setAccessible(true);
          if (followLinks(linkField, linkData)) {
            RelationshipType relationshipType = Enum.valueOf(relationshipTypes, linkData.getType());
            entityNode.getRelationships(Direction.OUTGOING, relationshipType).forEach(relationShip -> {
              boolean matched = linkData.matches(this.entityClass, relationShip);
              tracer.out().printfIndentln("%s matched: %b", relationShip, matched);
              if (matched)
                relationShip.delete();
            });
            
            Class<?> linkedEntityClass = Class.forName(linkData.getEntityClassName());
            if (!this.processingEntityIds2NodeMap.containsKey(linkedEntityClass))
              this.processingEntityIds2NodeMap.put(linkedEntityClass, new HashMap<>());
            
            Collection<?> linkedEntities = (Collection<?>) linkField.get(this.entity);
            if (linkedEntities != null) {
              String idFieldName = this.mappingInfo.getPrimaryKeyMapping(linkedEntityClass).getFieldName();
              Field idField = linkedEntityClass.getDeclaredField(idFieldName);
              idField.setAccessible(true);
              
              for (Object linkedEntity : linkedEntities) {
                tracer.out().printfIndentln("linkedEntity = %s", linkedEntity);

                Object primaryKeyValue = handleIdField(linkedEntityClass, linkedEntity, idField);
                if (!this.processingEntityIds2NodeMap.get(linkedEntityClass).containsKey(primaryKeyValue)) {
                  Object2NodeMapper object2NodeMapper = new Object2NodeMapper(linkedEntity, this.mappingInfo, this.graphDatabaseService, this.processingEntityIds2NodeMap);
                  Node linkedEntityNode = object2NodeMapper.map(labels, relationshipTypes);
                  entityNode.createRelationshipTo(linkedEntityNode, relationshipType);
                }
                else {
                  Node linkedEntityNode = this.processingEntityIds2NodeMap.get(linkedEntityClass).get(primaryKeyValue);
                  entityNode.createRelationshipTo(linkedEntityNode, relationshipType);
                }
              }
            }
            else {
              // TODO: Should the referenced end nodes of the matched relationships (recursively?!) deleted? Think about it.
            }
          }
        }
        catch (NoSuchFieldException | ClassNotFoundException ex) {
          tracer.logException(LogLevel.ERROR, ex, getClass(), "coverLinks(final Node entityNode, Class<T> relationshipTypes)");
          throw new Object2NodeMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the fields have been made accessible
        }
      }
      
      return entityNode;
    }
    finally {
      tracer.wayout();
    }
  }
    
  private boolean followLinks(Field linkField, LinkData linkData) {
    return linkData.getDirection() == Direction.OUTGOING;
  }
  
  private <S extends Enum<S> & Label, T extends Enum<T> & RelationshipType>
      Node coverSingleLinks(final Node entityNode, Class<S> labels, Class<T> relationshipTypes) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "coverSingleLinks(final Node entityNode, Class<S> labels, Class<T> relationshipTypes)");

    try {
      Set<Map.Entry<String, SingleLinkData>> singleLinkMappings = this.mappingInfo.getSingleLinkMappings(this.entityClass);
      for (Map.Entry<String, SingleLinkData> singleLinkMapping : singleLinkMappings) {
        String fieldName = singleLinkMapping.getKey();
        SingleLinkData singleLinkData = singleLinkMapping.getValue();
        
        tracer.out().printfIndentln("singleLinkMapping[%s] = %s", fieldName, singleLinkData);
        
        try {
          Field singleLinkField = this.entityClass.getDeclaredField(fieldName);
          singleLinkField.setAccessible(true);
          
          if (followSingleLink(singleLinkField, singleLinkData)) {
            Cell<?> cell = (Cell<?>) singleLinkField.get(this.entity);
            if (!singleLinkData.isNullable() && (cell == null || cell.getEntity() == null))
              throw new Object2NodeMapper.Exception("Entity required for " + singleLinkData);
            
            RelationshipType relationshipType = Enum.valueOf(relationshipTypes, singleLinkData.getType());
            Relationship singleRelationship = entityNode.getSingleRelationship(relationshipType, Direction.OUTGOING);
            if (singleRelationship != null) {
              boolean matched = singleLinkData.matches(this.entityClass, singleRelationship);
              tracer.out().printfIndentln("%s matched: %b", singleRelationship, matched);
              if (matched) {
                singleRelationship.delete();
              }
            }
            
            Class<?> linkedEntityClass = Class.forName(singleLinkData.getEntityClassName());
            if (!this.processingEntityIds2NodeMap.containsKey(linkedEntityClass))
              this.processingEntityIds2NodeMap.put(linkedEntityClass, new HashMap<>());
            
            if (cell != null  &&  cell.getEntity() != null) {
              tracer.out().printfIndentln("linkedEntity = %s", cell.getEntity());
              
              String idFieldName = this.mappingInfo.getPrimaryKeyMapping(linkedEntityClass).getFieldName();
              Field idField = linkedEntityClass.getDeclaredField(idFieldName);
              idField.setAccessible(true);
              Object primaryKeyValue = handleIdField(linkedEntityClass, cell.getEntity(), idField);
              if (!this.processingEntityIds2NodeMap.get(linkedEntityClass).containsKey(primaryKeyValue)) {
                Object2NodeMapper object2NodeMapper = new Object2NodeMapper(cell.getEntity(), this.mappingInfo, this.graphDatabaseService, this.processingEntityIds2NodeMap);
                Node linkedEntityNode = object2NodeMapper.map(labels, relationshipTypes);
                entityNode.createRelationshipTo(linkedEntityNode, relationshipType);
              }
              else {
                Node linkedEntityNode = this.processingEntityIds2NodeMap.get(linkedEntityClass).get(primaryKeyValue);
                entityNode.createRelationshipTo(linkedEntityNode, relationshipType);
              }
            }
            else {
              // TODO: Should the referenced end nodes of the matched relationships (recursively?!) deleted? Think about it.
            }
          }
        }
        catch (NoSuchFieldException | ClassNotFoundException ex) {
          tracer.logException(LogLevel.ERROR, ex, getClass(), "coverSingleLinks(final Node entityNode, Class<S> labels, Class<T> relationshipTypes)");
          throw new Object2NodeMapper.Exception("Invalid mapping definition.", ex);
        }
        catch (IllegalAccessException ex) {
          throw new Error(ex); // should be impossible since the field has been made accessible
        }
      }
      
      return entityNode;
    }
    finally {
      tracer.wayout();
    }
  }
      
  private boolean followSingleLink(Field singleLinkField, SingleLinkData singleLinkData) {
    return singleLinkData.getDirection() == Direction.OUTGOING;
  }
  
  private Object handleIdField(Class<?> entityClass, Object entity, Field idField) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "handleIdField(Class<?> linkedEntityClass, Object linkedEntity, Field idField)");
    
    try {
      try {
        Object primaryKey = idField.get(entity);
        if (primaryKey == null) {
          if (this.mappingInfo.getPrimaryKeyMapping(entityClass).isGenerated() == false) {
            throw new Object2NodeMapper.Exception("Primary key is null.");
          }
          
          primaryKey = IdGeneratorService.getInstance().getNextId(entityClass.getName());
          idField.set(entity, primaryKey);
        }
        
        return primaryKey;
      }
      catch (InterruptedException ex) {
        throw new Object2NodeMapper.Exception("Interrupted when accessing generated id.", ex);
      }
      catch (IllegalAccessException ex) {
        throw new RuntimeException(ex); // should be impossible since the field has been made accessible
      }
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
