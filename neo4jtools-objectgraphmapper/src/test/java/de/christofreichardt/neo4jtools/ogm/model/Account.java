/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.Links;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import de.christofreichardt.neo4jtools.ogm.Wrapper;
import java.util.Collection;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id
  @Property(name = "commonName")
  private final String userId;

  @Property
  private String localityName;

  @Property
  private String stateName;

  @Property
  private String countryCode;
  
  @SingleLink(direction = Direction.OUTGOING, type = "OWNS", nullable = true)
  private Cell<KeyRing> keyRing;
  
  @Links(direction = Direction.OUTGOING, type = "FULFILLS")
  private Collection<Role> roles;
  
  @Links(direction = Direction.OUTGOING, type = "HAS")
  private Collection<Document> documents;

  public Account(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public String getLocalityName() {
    return localityName;
  }

  public void setLocalityName(String localityName) {
    this.localityName = localityName;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public KeyRing getKeyRing() {
    return this.keyRing != null ? this.keyRing.getEntity() : null;
  }

  public void setKeyRing(KeyRing keyRing) {
    this.keyRing = new Wrapper<>(keyRing);
  }

  public Collection<Role> getRoles() {
    return roles;
  }

  public void setRoles(Collection<Role> roles) {
    this.roles = roles;
  }

  public Collection<Document> getDocuments() {
    return documents;
  }

  public void setDocuments(Collection<Document> documents) {
    this.documents = documents;
  }

  @Override
  public String toString() {
    return "Account[" + "userId=" + userId + "]";
  }
  
}
