package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.Version;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ENCRYPTED_DOCUMENTS")
public class EncryptedDocument extends Document {
  @Property
  @Version
  private Integer counter = 0;
  
  public EncryptedDocument(Long id) {
    super(id);
  }
}
