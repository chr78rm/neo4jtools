package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "PRIVATE_KEY_ITEMS")
public class PrivateKeyItem extends KeyItem {

  public PrivateKeyItem(Long id) {
    super(id);
  }
}
