package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity
public class SecretKeyItem extends KeyItem {

  public SecretKeyItem(Integer id) {
    super(id);
  }
}
