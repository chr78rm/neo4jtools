package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "DOCUMENTS")
public class PlaintextDocument extends Document {

  public PlaintextDocument(Integer id) {
    super(id);
  }
}
