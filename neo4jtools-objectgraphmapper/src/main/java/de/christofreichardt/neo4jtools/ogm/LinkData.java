/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Developer
 */
public class LinkData implements Traceable {
  final Direction direction;
  final String type;
  final String linkedEntityClassName;
  final Class<?> linkedEntityClass;

  public LinkData(Direction direction, String type, String linkedEntityClassName) throws ClassNotFoundException {
    this.direction = direction;
    this.type = type;
    this.linkedEntityClassName = linkedEntityClassName;
    this.linkedEntityClass = Class.forName(this.linkedEntityClassName);
  }

  public Direction getDirection() {
    return direction;
  }

  public String getType() {
    return type;
  }

  public String getEntityClassName() {
    return linkedEntityClassName;
  }
  
  public boolean matches(Class<?> startClass, Relationship relationship, Direction direction) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("boolean", this, "matches(Relationship relationship)");
    
    try {
      tracer.out().printfIndentln("(%s == %s) = %b", this.direction, direction, this.direction == direction);
      tracer.out().printfIndentln("relationship.getType().name() = %s", relationship.getType().name());
      relationship.getStartNode().getLabels().forEach(startNodeLabel -> tracer.out().printfIndentln("startNodeLabel = %s", startNodeLabel.name()));
      relationship.getEndNode().getLabels().forEach(startNodeLabel -> tracer.out().printfIndentln("endNodeLabel = %s", startNodeLabel.name()));
      tracer.out().printfIndentln("startClass.getAnnotation(NodeEntity.class).label() = %s", startClass.getAnnotation(NodeEntity.class).label());
      tracer.out().printfIndentln("endClass.getAnnotation(NodeEntity.class).label() = %s", this.linkedEntityClass.getAnnotation(NodeEntity.class).label());
      
      boolean matched;
      if (this.direction == direction) {
        boolean startLabelFlag = false;
        for (Label startLabel : relationship.getStartNode().getLabels()) {
          if (direction == Direction.OUTGOING) {
            if (startLabel.name().equals(startClass.getAnnotation(NodeEntity.class).label())) {
              startLabelFlag = true;
              break;
            }
          }
          else if (direction == Direction.INCOMING) {
            if (startLabel.name().equals(this.linkedEntityClass.getAnnotation(NodeEntity.class).label())) {
              startLabelFlag = true;
              break;
            }
          }
          else
            throw new UnsupportedOperationException(Direction.BOTH + " isn't supported.");
        }

        boolean endLabelFlag = false;
        for (Label endLabel : relationship.getEndNode().getLabels()) {
          if (direction == Direction.OUTGOING) {
            if (endLabel.name().equals(this.linkedEntityClass.getAnnotation(NodeEntity.class).label())) {
              endLabelFlag = true;
              break;
            }
          }
          else if (direction == Direction.INCOMING) {
           if (endLabel.name().equals(startClass.getAnnotation(NodeEntity.class).label())) {
              endLabelFlag = true;
              break;
            }
          }
          else
            throw new UnsupportedOperationException(Direction.BOTH + " isn't supported.");
        }
        
        matched = startLabelFlag && endLabelFlag;
      }
      else
        matched = false;
      
      return matched;
    }
    finally {
      tracer.wayout();
    }
  } 
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("LinkData[");
    builder.append("direction=").append(this.direction).append(", ");
    builder.append("type=").append(this.type).append(", ");
    builder.append("linkedEntityClassName=").append(this.linkedEntityClassName);
    builder.append(")").append("]");
    
    return builder.toString();
  }

  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
