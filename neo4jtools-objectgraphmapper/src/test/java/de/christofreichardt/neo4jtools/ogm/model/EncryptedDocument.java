package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ENCRYPTED_DOCUMENTS")
public class EncryptedDocument extends Document {

  public EncryptedDocument(Long id) {
    super(id);
  }
}
