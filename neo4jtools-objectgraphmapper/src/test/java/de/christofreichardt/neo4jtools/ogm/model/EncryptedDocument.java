package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "DOCUMENTS")
public class EncryptedDocument extends Document {

  public EncryptedDocument(Integer id) {
    super(id);
  }
}
