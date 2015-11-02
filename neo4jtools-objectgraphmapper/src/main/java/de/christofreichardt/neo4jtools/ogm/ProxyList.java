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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Christof Reichardt
 * @param <T>
 */
public class ProxyList<T extends Enum<T> & RelationshipType> implements Collection<Object>, Serializable, Traceable {
  transient final private Node node;
  transient final private LinkData linkData;
  transient final private MappingInfo mappingInfo;
  transient final private Class<T> relationshipTypes;
  transient final private Class<?> startClass;
  
  private List<Object> entities;

  public ProxyList(Node node, Class<?> startClass, LinkData linkData, MappingInfo mappingInfo, Class<T> relationshipTypes) {
    this.node = node;
    this.linkData = linkData;
    this.mappingInfo = mappingInfo;
    this.relationshipTypes = relationshipTypes;
    this.startClass = startClass;
  }
  
  private void loadEntities() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "loadEntities()");
    
    try {
      tracer.out().printfIndentln("this.linkData = %s", this.linkData);
      
      this.entities = new ArrayList<>();
      RelationshipType relationshipType = Enum.valueOf(this.relationshipTypes, this.linkData.getType());
      Iterable<Relationship> relationships = this.node.getRelationships(this.linkData.getDirection(), relationshipType);
      try {
        for (Relationship relationship : relationships) {
          if (this.linkData.matches(this.startClass, relationship, this.linkData.getDirection())) {
            Node endNode = relationship.getEndNode();
            Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(endNode, this.mappingInfo);
            Object linkedEntity = node2ObjectMapper.map(this.relationshipTypes);
            this.entities.add(linkedEntity);
          }
        }
      }
      catch (Node2ObjectMapper.Exception ex) {
        throw new RuntimeException("Problems when loading the entities.", ex);
      }
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public int size() {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.size();
  }

  @Override
  public boolean isEmpty() {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.isEmpty();
  }

  @Override
  public boolean contains(Object object) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.contains(object);
  }

  @Override
  public Iterator<Object> iterator() {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.iterator();
  }

  @Override
  public Object[] toArray() {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.toArray(ts);
  }

  @Override
  public boolean add(Object object) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.add(object);
  }

  @Override
  public boolean remove(Object object) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.remove(object);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends Object> collection) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.addAll(collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.removeAll(collection);
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    if (this.entities == null)
      loadEntities();
    
    return this.entities.retainAll(collection);
  }

  @Override
  public void clear() {
    if (this.entities != null)
      this.entities.clear();
  }

  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }

}
