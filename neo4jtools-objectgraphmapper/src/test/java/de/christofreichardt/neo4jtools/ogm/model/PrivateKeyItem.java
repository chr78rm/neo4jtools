package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "KEY_ITEMS")
public class PrivateKeyItem extends KeyItem {

  public PrivateKeyItem(Integer id) {
    super(id);
  }
}
