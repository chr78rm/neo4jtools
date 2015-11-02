/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.Serializable;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Christof Reichardt
 */
public class ProxyObject<T extends Enum<T> & RelationshipType> implements Cell<Object>, Serializable, Traceable {
  transient final private Node node;
  transient final private SingleLinkData singleLinkData;
  transient final private MappingInfo mappingInfo;
  transient final private Class<T> relationshipTypes;
  transient final private Class<?> startClass;
  
  private Object entity;

  public ProxyObject(Node node, SingleLinkData singleLinkData, MappingInfo mappingInfo, Class<T> relationshipTypes, Class<?> startClass) {
    this.node = node;
    this.singleLinkData = singleLinkData;
    this.mappingInfo = mappingInfo;
    this.relationshipTypes = relationshipTypes;
    this.startClass = startClass;
  }

  @Override
  public Object getEntity() {
    if (this.entity == null)
      loadEntity();
    
    return this.entity;
  }
  
  private void loadEntity() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "loadEntity()");
    
    try {
      tracer.out().printfIndentln("this.singleLinkData = %s", this.singleLinkData);
      
      RelationshipType relationshipType = Enum.valueOf(this.relationshipTypes, this.singleLinkData.getType());
      Iterable<Relationship> relationships = this.node.getRelationships(this.singleLinkData.getDirection(), relationshipType);
      try {
        for (Relationship relationship : relationships) {
          if (this.singleLinkData.matches(this.startClass, relationship, this.singleLinkData.getDirection())) {
            if (this.entity != null) {
              throw new RuntimeException("More than one link satisfies " + this.singleLinkData + ".");
            }
            
            Node endNode;
            if (this.singleLinkData.getDirection() == Direction.OUTGOING)
              endNode = relationship.getEndNode();
            else if (this.singleLinkData.getDirection() == Direction.INCOMING)
              endNode = relationship.getStartNode();
            else
              throw new UnsupportedOperationException(Direction.BOTH + " isn't supported.");
            Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(endNode, this.mappingInfo);
            this.entity = node2ObjectMapper.map(this.relationshipTypes);
          }
        }
        
        if (!this.singleLinkData.isNullable()  &&  this.entity == null)
          throw new RuntimeException("Value required for " + this.singleLinkData + ".");
      }
      catch (Node2ObjectMapper.Exception ex) {
        throw new RuntimeException("Problems when loading the entity.", ex);
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
