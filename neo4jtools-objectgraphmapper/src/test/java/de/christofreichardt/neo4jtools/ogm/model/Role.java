package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.GeneratedValue;
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
@NodeEntity(label = "ROLES")
public class Role {
  @Id
  @Property
  @GeneratedValue
  private Long id;
  
  @Property
  private String name;

  @Links(direction = Direction.INCOMING, type = "FULFILLS")
  private Collection<Account> accounts;

  public Role() {
  }

  public Role(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<Account> getAccounts() {
    return accounts;
  }

  public void setAccounts(Collection<Account> accounts) {
    this.accounts = accounts;
  }

  @Override
  public String toString() {
    return "Role[" + "id=" + id + "]";
  }
}
