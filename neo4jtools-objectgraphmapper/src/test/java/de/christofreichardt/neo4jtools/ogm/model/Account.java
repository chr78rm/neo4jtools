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
import java.util.Collection;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id
  @Property
  private final String commonName;

  @Property
  private String localityName;

  @Property
  private String stateName;

  @Property
  private String countryCode;
  
  @Links(direction = Direction.OUTGOING, type = "OWNS")
  private Collection<KeyItem> keyItems;
  
  @Links(direction = Direction.OUTGOING, type = "FULFILLS")
  private Collection<Role> roles;
  
  @Links(direction = Direction.OUTGOING, type = "HAS")
  private Collection<Document> documents;

  public Account(String commonName) {
    this.commonName = commonName;
  }

  public String getCommonName() {
    return commonName;
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

  public Collection<KeyItem> getKeyItems() {
    return keyItems;
  }

  public void setKeyItems(Collection<KeyItem> keyItems) {
    this.keyItems = keyItems;
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
  
}
