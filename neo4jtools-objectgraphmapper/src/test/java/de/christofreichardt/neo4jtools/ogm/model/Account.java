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
import java.util.Objects;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id
  @Property(name = "commonName")
  String userId;

  @Property
  String localityName;

  @Property
  String stateName;

  @Property
  String countryCode;
  
  @SingleLink(direction = Direction.OUTGOING, type = "OWNS", nullable = true)
  Cell<KeyRing> keyRing;
  
  @Links(direction = Direction.OUTGOING, type = "FULFILLS")
  Collection<Role> roles;
  
  @Links(direction = Direction.OUTGOING, type = "HAS")
  Collection<Document> documents;

  public Account() {
  }

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

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + Objects.hashCode(this.userId);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    boolean flag;
    if (this == object) {
      flag = true;
    }
    else if (object == null) {
      flag = false;
    }
    else if (getClass() != object.getClass()) {
      flag = false;
    }
    else  {
      final Account other = (Account) object;
      flag = Objects.equals(this.userId, other.userId);
    }
    
    return flag;
  }
  
}
