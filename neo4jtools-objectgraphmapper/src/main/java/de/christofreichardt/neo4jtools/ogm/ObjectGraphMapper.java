package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;

/**
 *
 * @author Christof Reichardt
 * @param <S>
 * @param <T>
 */
public class ObjectGraphMapper<S extends Enum<S> & Label, T extends Enum<T> & RelationshipType> implements Traceable {
  final private MappingInfo mappingInfo;
  final private GraphDatabaseService graphDatabaseService;
  final private Class<S> labels;
  final private Class<T> relationshipTypes;

  public ObjectGraphMapper(MappingInfo mappingInfo, GraphDatabaseService graphDatabaseService, Class<S> labels, Class<T> relationshipTypes) {
    this.mappingInfo = mappingInfo;
    this.graphDatabaseService = graphDatabaseService;
    this.labels = labels;
    this.relationshipTypes = relationshipTypes;
  }

  public <U> U load(Class<U> entityClass, Object keyValue) throws Node2ObjectMapper.Exception{
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "load(Class<U> entityClass, Object key)");
    
    try {
      tracer.out().printfIndentln("entityClass = %s", entityClass.getName());
      tracer.out().printfIndentln("key = %s", keyValue);
      
      PrimaryKeyData primaryKeyData = this.mappingInfo.getPrimaryKeyMapping(entityClass);
      S specificLabel = this.mappingInfo.getSpecificLabel(entityClass, this.labels);
      PropertyData propertyData = this.mappingInfo.getPropertyMappingForField(primaryKeyData.getFieldName(), entityClass);
      Node entityNode = this.graphDatabaseService.findNode(specificLabel, propertyData.getName(), keyValue);
      Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(entityNode, this.mappingInfo);
      Object object = node2ObjectMapper.map(this.relationshipTypes);
      
      return entityClass.cast(object);
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
