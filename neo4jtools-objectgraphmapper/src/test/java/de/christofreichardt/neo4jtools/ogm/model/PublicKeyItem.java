package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity
public class PublicKeyItem extends KeyItem {

  public PublicKeyItem(Integer id) {
    super(id);
  }
}
