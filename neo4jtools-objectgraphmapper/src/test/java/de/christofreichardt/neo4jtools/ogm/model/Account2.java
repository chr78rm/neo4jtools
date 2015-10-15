package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ACCOUNT2S")
public class Account2 extends Account {

  public Account2(String commonName) {
    super(commonName);
  }

}
