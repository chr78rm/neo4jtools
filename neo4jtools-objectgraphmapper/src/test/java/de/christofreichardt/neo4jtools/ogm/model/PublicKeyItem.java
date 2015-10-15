package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "PUBLIC_KEY_ITEMS")
public class PublicKeyItem extends KeyItem {

  public PublicKeyItem(Long id) {
    super(id);
  }
}
