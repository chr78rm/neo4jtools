/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import java.util.Iterator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 *
 * @author Christof Reichardt
 */
public class RichNode {
  private final Node node;

  public RichNode(Node node) {
    this.node = node;
  }
  
  public void trace(AbstractTracer tracer) {
    tracer.out().printIndentString();
    tracer.out().print("(");
    Iterator<Label> iter = this.node.getLabels().iterator();
    while(iter.hasNext()) {
      tracer.out().print(iter.next().name());
      if (iter.hasNext())
        tracer.out().print(", ");
    }
    tracer.out().print(")");
    tracer.out().printf(": nodeId = %d%n", this.node.getId());
    
    Iterable<String> propertyKeys = this.node.getPropertyKeys();
    propertyKeys.forEach(propertyKey -> {
      tracer.out().printfIndentln("node[%s] = %s", propertyKey, this.node.getProperty(propertyKey));
    });
    
    tracer.out().printfIndentln("this.node.getDegree() = %d", this.node.getDegree());
    
    this.node.getRelationships(Direction.OUTGOING).forEach(relationship -> {
      tracer.out().printfIndentln("node[%d] -- %s[%d] --> node[%d]", 
          this.node.getId(), 
          relationship.getType().name(), 
          relationship.getId(), 
          relationship.getOtherNode(this.node).getId());
    });
    
    this.node.getRelationships(Direction.INCOMING).forEach(relationship -> {
      tracer.out().printfIndentln("node[%d] <-- %s[%d] -- node[%d]", 
          this.node.getId(), 
          relationship.getType().name(), 
          relationship.getId(), 
          relationship.getOtherNode(this.node).getId());
    });
  }
}
