/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
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

  public Node2ObjectMapper(Node node) throws MappingInfo.Exception {
    this.node = new RichNode(node);
    this.mappingInfo = new MappingInfo();
  }
  
  public <S extends Enum<S> & Label, T extends Enum<T> & RelationshipType, U> U map(Class<S> labels, Class<T> relationshipTypes) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Object", this, "map(Class<S> labels, Class<T> relationshipTypes)");
    
    try {
      return null;
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
