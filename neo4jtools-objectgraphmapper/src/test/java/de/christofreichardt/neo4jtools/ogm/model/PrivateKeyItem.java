package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity
public class PrivateKeyItem extends KeyItem {

  public PrivateKeyItem(Integer id) {
    super(id);
  }
}
