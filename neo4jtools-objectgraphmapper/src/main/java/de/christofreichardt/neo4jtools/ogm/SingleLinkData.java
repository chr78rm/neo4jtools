/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import java.util.Set;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Developer
 */
public class SingleLinkData extends LinkData {
  
  final private boolean nullable;

  public SingleLinkData(Direction direction, String type, String linkedEntityClassName, boolean nullable) throws ClassNotFoundException {
    super(direction, type, linkedEntityClassName);
    this.nullable = nullable;
  }

  public boolean isNullable() {
    return nullable;
  }
  
  <T extends Enum<T> & RelationshipType> boolean checkConstraint(Node entityNode, Class<T> relationshipTypes, MappingInfo mappingInfo) throws ClassNotFoundException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("boolean", this, "checkConstraint(Node entityNode, Class<T> relationshipTypes, MappingInfo mappingInfo)");
    
    try {
      tracer.out().printfIndentln("this = %s", this);
      
      Set<String> labels = mappingInfo.getLabels(this.linkedEntityClass);
      RelationshipType relationshipType = Enum.valueOf(relationshipTypes, this.type);
      Iterable<Relationship> relationships = entityNode.getRelationships(relationshipType, this.direction);
      int counter = 0;
      for (Relationship relationship : relationships) {
        Node otherNode = relationship.getOtherNode(entityNode);
        boolean labelFlag = false;
        for (Label label : otherNode.getLabels()) {
          if (labels.contains(label.name())) {
            labelFlag = true;
            break;
          }
        }
        if (labelFlag)
          counter++;
      }
      
      tracer.out().printfIndentln("counter = %d", counter);
      
      return counter <= 1;
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("SingleLinkData[");
    builder.append("direction=").append(this.direction).append(", ");
    builder.append("type=").append(this.type).append(", ");
    builder.append("linkedEntityClassName=").append(this.linkedEntityClassName).append(", ");
    builder.append("nullable=").append(this.nullable);
    builder.append(")");
    builder.append("]");
    
    return builder.toString();
  }
  
}
