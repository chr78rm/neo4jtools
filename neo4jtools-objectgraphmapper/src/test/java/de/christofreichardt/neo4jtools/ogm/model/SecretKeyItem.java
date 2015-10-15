package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "SECRET_KEY_ITEMS")
public class SecretKeyItem extends KeyItem {

  public SecretKeyItem(Long id) {
    super(id);
  }
}
