/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import java.util.Iterator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;

/**
 *
 * @author Christof Reichardt
 */
public class RichNode implements Node {
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

  @Override
  public long getId() {
    return this.node.getId();
  }

  @Override
  public void delete() {
    this.node.delete();
  }

  @Override
  public Iterable<Relationship> getRelationships() {
    return this.node.getRelationships();
  }

  @Override
  public boolean hasRelationship() {
    return this.node.hasRelationship();
  }

  @Override
  public Iterable<Relationship> getRelationships(RelationshipType... types) {
    return this.node.getRelationships(types);
  }

  @Override
  public Iterable<Relationship> getRelationships(Direction arg0, RelationshipType... arg1) {
    return this.node.getRelationships(arg0, arg1);
  }

  @Override
  public boolean hasRelationship(RelationshipType... types) {
    return this.node.hasRelationship(types);
  }

  @Override
  public boolean hasRelationship(Direction arg0, RelationshipType... arg1) {
    return this.node.hasRelationship(arg0, arg1);
  }

  @Override
  public Iterable<Relationship> getRelationships(Direction dir) {
    return this.node.getRelationships(dir);
  }

  @Override
  public boolean hasRelationship(Direction dir) {
    return this.node.hasRelationship(dir);
  }

  @Override
  public Iterable<Relationship> getRelationships(RelationshipType arg0, Direction arg1) {
    return this.node.getRelationships(arg0, arg1);
  }

  @Override
  public boolean hasRelationship(RelationshipType arg0, Direction arg1) {
    return this.node.hasRelationship(arg0, arg1);
  }

  @Override
  public Relationship getSingleRelationship(RelationshipType arg0, Direction arg1) {
    return this.node.getSingleRelationship(arg0, arg1);
  }

  @Override
  public Relationship createRelationshipTo(Node arg0, RelationshipType arg1) {
    return this.node.createRelationshipTo(arg0, arg1);
  }

  @Override
  public Iterable<RelationshipType> getRelationshipTypes() {
    return this.node.getRelationshipTypes();
  }

  @Override
  public int getDegree() {
    return this.node.getDegree();
  }

  @Override
  public int getDegree(RelationshipType type) {
    return this.node.getDegree(type);
  }

  @Override
  public int getDegree(Direction direction) {
    return this.getDegree(direction);
  }

  @Override
  public int getDegree(RelationshipType arg0, Direction arg1) {
    return this.node.getDegree(arg0, arg1);
  }

  @Override
  public Traverser traverse(Traverser.Order arg0, StopEvaluator arg1, ReturnableEvaluator arg2, RelationshipType arg3, Direction arg4) {
    throw new UnsupportedOperationException("Deprecated."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Traverser traverse(Traverser.Order arg0, StopEvaluator arg1, ReturnableEvaluator arg2, RelationshipType arg3, Direction arg4, RelationshipType arg5, Direction arg6) {
    throw new UnsupportedOperationException("Deprecated."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Traverser traverse(Traverser.Order arg0, StopEvaluator arg1, ReturnableEvaluator arg2, Object... arg3) {
    throw new UnsupportedOperationException("Deprecated."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addLabel(Label label) {
    this.node.addLabel(label);
  }

  @Override
  public void removeLabel(Label label) {
    this.node.removeLabel(label);
  }

  @Override
  public boolean hasLabel(Label label) {
    return this.node.hasLabel(label);
  }

  @Override
  public Iterable<Label> getLabels() {
    return this.node.getLabels();
  }

  @Override
  public GraphDatabaseService getGraphDatabase() {
    return this.node.getGraphDatabase();
  }

  @Override
  public boolean hasProperty(String key) {
    return this.node.hasProperty(key);
  }

  @Override
  public Object getProperty(String key) {
    return this.node.getProperty(key);
  }

  @Override
  public Object getProperty(String arg0, Object arg1) {
    return this.node.getProperty(arg0, arg1);
  }

  @Override
  public void setProperty(String arg0, Object arg1) {
    this.node.setProperty(arg0, arg1);
  }

  @Override
  public Object removeProperty(String key) {
    return this.node.removeProperty(key);
  }

  @Override
  public Iterable<String> getPropertyKeys() {
    return this.node.getPropertyKeys();
  }
}
