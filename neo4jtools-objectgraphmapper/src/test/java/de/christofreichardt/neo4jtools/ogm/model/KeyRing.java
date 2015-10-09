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
@NodeEntity(label = "KEY_RINGS")
public class KeyRing {
  @Id
  @Property
  private Integer id;

  @Property
  private String path;

  @Property(nullable = true)
  private String password;
  
  @Links(direction = Direction.OUTGOING, type = "CONTAINS")
  private Collection<KeyItem> keyItems;
  
  @SingleLink(direction = Direction.INCOMING, type = "OWNS")
  private Cell<Account> account;
  
  public KeyRing(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Collection<KeyItem> getKeyItems() {
    return keyItems;
  }

  public void setKeyItems(Collection<KeyItem> keyItems) {
    this.keyItems = keyItems;
  }

  public Account getAccount() {
    return this.account != null ? this.account.getEntity() : null;
  }

  public void setAccount(Account account) {
    this.account = new Wrapper<>(account);
  }

}
