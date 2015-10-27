package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ACCOUNT2S")
public class Account2 extends Account {
  @Property(nullable = true)
  private String firstName;
  
  @Property
  private String lastName;  

  public Account2() {
  }

  public Account2(String commonName) {
    super(commonName);
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

}
