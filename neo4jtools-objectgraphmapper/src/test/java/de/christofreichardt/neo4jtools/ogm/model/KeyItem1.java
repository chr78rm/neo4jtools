/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import de.christofreichardt.neo4jtools.ogm.Wrapper;
import org.neo4j.graphdb.Direction;

@NodeEntity(label = "KEY_ITEMS")
public class KeyItem1 extends KeyItem {
  @SingleLink(direction = Direction.OUTGOING, type = "BELONGS_TO")
  private Cell<Account> account;

  public KeyItem1(Long id) {
    super(id);
  }

  public Account getAccount() {
    return this.account != null ? this.account.getEntity() : null;
  }

  public void setAccount(Account account) {
    this.account = new Wrapper<>(account);
  }
}
