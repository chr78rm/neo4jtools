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
public class SingleLinkData extends LinkData {
  
  final private boolean nullable;

  public SingleLinkData(Direction direction, String type, String entityClassName, boolean nullable) {
    super(direction, type, entityClassName);
    this.nullable = nullable;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("SingleLinkData[");
    builder.append("direction=").append(this.direction).append(", ");
    builder.append("type=").append(this.type).append(", ");
    builder.append("entityClassName=").append(this.entityClassName).append(", ");
    builder.append("nullable=").append(this.nullable);
    builder.append(")");
    builder.append("]");
    
    return builder.toString();
  }
  
}