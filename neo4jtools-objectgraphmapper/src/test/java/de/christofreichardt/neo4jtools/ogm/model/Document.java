package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "DOCUMENTS")
public class Document {
  @Id
  @Property
  private Integer id;
  
  @Property
  private String creationDate;
  
  @Property
  private String type;
  
  @SingleLink(direction = Direction.INCOMING, type = "OWNS")
  private Cell<Account> account;
}
