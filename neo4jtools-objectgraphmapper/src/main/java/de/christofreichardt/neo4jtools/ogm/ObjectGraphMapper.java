package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
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

  public <U> U load(Class<U> entityClass, Object primaryKeyValue) throws Node2ObjectMapper.Exception{
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "load(Class<U> entityClass, Object primaryKeyValue)");
    
    try {
      tracer.out().printfIndentln("entityClass = %s", entityClass.getName());
      tracer.out().printfIndentln("key = %s", primaryKeyValue);
      
      PrimaryKeyData primaryKeyData = ObjectGraphMapper.mappingInfo.getPrimaryKeyMapping(entityClass);
      S specificLabel = ObjectGraphMapper.mappingInfo.getSpecificLabel(entityClass, this.labels);
      PropertyData propertyData = ObjectGraphMapper.mappingInfo.getPropertyMappingForField(primaryKeyData.getFieldName(), entityClass);
      Node entityNode = this.graphDatabaseService.findNode(specificLabel, propertyData.getName(), primaryKeyValue);
      Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(entityNode, ObjectGraphMapper.mappingInfo);
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

  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
