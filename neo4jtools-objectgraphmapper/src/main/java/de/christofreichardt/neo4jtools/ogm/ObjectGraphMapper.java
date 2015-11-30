package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.lang.reflect.Field;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Christof Reichardt
 * @param <S>
 * @param <T>
 */
public class ObjectGraphMapper<S extends Enum<S> & Label, T extends Enum<T> & RelationshipType> implements Traceable {
  static private MappingInfo mappingInfo;
  static {
    try {
      ObjectGraphMapper.mappingInfo = new MappingInfo();
    }
    catch (MappingInfo.Exception ex) {
      throw new Error(ex);
    }
  }
  
  final private GraphDatabaseService graphDatabaseService;
  final private Class<S> labels;
  final private Class<T> relationshipTypes;

  public ObjectGraphMapper(GraphDatabaseService graphDatabaseService, Class<S> labels, Class<T> relationshipTypes) {
    this.graphDatabaseService = graphDatabaseService;
    this.labels = labels;
    this.relationshipTypes = relationshipTypes;
  }

  public <U> U load(Class<U> entityClass, Object primaryKeyValue) throws Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "load(Class<U> entityClass, Object primaryKeyValue)");
    
    try {
      tracer.out().printfIndentln("entityClass = %s", entityClass.getName());
      tracer.out().printfIndentln("key = %s", primaryKeyValue);
      
      PrimaryKeyData primaryKeyData = ObjectGraphMapper.mappingInfo.getPrimaryKeyMapping(entityClass);
      PropertyData propertyData = ObjectGraphMapper.mappingInfo.getPropertyMappingForField(primaryKeyData.getFieldName(), entityClass);
      if (!propertyData.getClazz().isAssignableFrom(primaryKeyValue.getClass()))
        throw new IllegalArgumentException("Invalid type for the primary key. Need a '" + propertyData.getClazz().getName() + "' instance.");
      S specificLabel = ObjectGraphMapper.mappingInfo.getSpecificLabel(entityClass, this.labels);
      Node entityNode = this.graphDatabaseService.findNode(specificLabel, propertyData.getName(), primaryKeyValue);
      Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(entityNode, ObjectGraphMapper.mappingInfo);
      if (!entityClass.isAssignableFrom(node2ObjectMapper.getMostSpecificClass()))
        throw new IllegalArgumentException("Inappropriate specified class '" + entityClass.getName() + "'.");
      Object object = node2ObjectMapper.map(this.relationshipTypes);
      
      return entityClass.cast(object);
    }
    finally {
      tracer.wayout();
    }
  }
  
  public Node save(Object entity) throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "save(Object entity)");
    
    try {
      tracer.out().printfIndentln("entity = %s", entity);
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(entity, ObjectGraphMapper.mappingInfo, this.graphDatabaseService);
      return object2NodeMapper.map(this.labels, this.relationshipTypes);
    }
    finally {
      tracer.wayout();
    }
  }
  
  public void remove(Object entity) throws GraphPersistenceException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "remove(Object entity)");
    
    try {
      tracer.out().printfIndentln("entity = %s", entity);
      
      PrimaryKeyData primaryKeyData = ObjectGraphMapper.mappingInfo.getPrimaryKeyMapping(entity.getClass());
      PropertyData propertyData = ObjectGraphMapper.mappingInfo.getPropertyMappingForField(primaryKeyData.getFieldName(), entity.getClass());
      ReflectedClass reflectedClass = new ReflectedClass(entity.getClass());
      try {
        Field field = reflectedClass.getDeclaredField(propertyData.getName());
        field.setAccessible(true);
        Object primaryKeyValue = field.get(entity);
        S specificLabel = ObjectGraphMapper.mappingInfo.getSpecificLabel(entity.getClass(), this.labels);
        Node entityNode = this.graphDatabaseService.findNode(specificLabel, propertyData.getName(), primaryKeyValue);
        Iterable<Relationship> relationships = entityNode.getRelationships();
        for (Relationship relationship : relationships) {
          Node otherNode = relationship.getOtherNode(entityNode);
          Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(otherNode, mappingInfo);
          int preViolations = node2ObjectMapper.nonNullableSingleLinkViolations(this.relationshipTypes).size();
          
          tracer.out().printfIndentln("preViolations = %d", preViolations);
          
          relationship.delete();
          int postViolations = node2ObjectMapper.nonNullableSingleLinkViolations(this.relationshipTypes).size();
          
          tracer.out().printfIndentln("postViolations = %d", postViolations);
          
          if (preViolations != postViolations)
            throw new GraphPersistenceException("SingleLink constraint violation.");
        }
        entityNode.delete();
      }
      catch (NoSuchFieldException ex) {
        throw new GraphPersistenceException("Invalid mapping definition.", ex);
      }
      catch (IllegalAccessException ex) {
        throw new Error(ex); // should be impossible since the field has been made accessible
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
