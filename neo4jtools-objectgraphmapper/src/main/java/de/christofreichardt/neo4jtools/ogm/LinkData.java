/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import org.neo4j.graphdb.Direction;

/**
 *
 * @author Developer
 */
public class LinkData {
  
  final Direction direction;
  final String type;
  final String entityClassName;

  public LinkData(Direction direction, String type, String entityClassName) {
    this.direction = direction;
    this.type = type;
    this.entityClassName = entityClassName;
  }

  public Direction getDirection() {
    return direction;
  }

  public String getType() {
    return type;
  }

  public String getEntityClassName() {
    return entityClassName;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("LinkData[");
    builder.append("direction=").append(this.direction).append(", ");
    builder.append("type=").append(this.type).append(", ");
    builder.append("entityClassName=").append(this.entityClassName);
    builder.append(")").append("]");
    
    return builder.toString();
  }
}
