/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.GeneratedValue;
import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import de.christofreichardt.neo4jtools.ogm.Wrapper;
import org.neo4j.graphdb.Direction;

@NodeEntity(label = "KEY_ITEMS")
public class KeyItem {
  @Id
  @Property
  @GeneratedValue
  private Integer id;

  @Property
  private String algorithm;
  
  @Property
  private String creationDate;
  
  @SingleLink(direction = Direction.INCOMING, type = "CONTAINS")
  private Cell<KeyRing> keyRing;

  public KeyItem(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public KeyRing getKeyRing() {
    return this.keyRing != null ? this.keyRing.getEntity() : null;
  }

  public void setKeyRing(KeyRing keyRing) {
    this.keyRing = new Wrapper<>(keyRing);
  }

  @Override
  public String toString() {
    return "KeyItem[" + "id=" + id + "]";
  }
}
