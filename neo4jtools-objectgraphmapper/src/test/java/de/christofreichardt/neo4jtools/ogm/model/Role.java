package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.GeneratedValue;
import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.Links;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import java.util.Collection;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "ROLES")
public class Role {
  @Id
  @Property
  @GeneratedValue
  private Long id;
  
  @Property
  private String name;

  @Links(direction = Direction.INCOMING, type = "FULFILLS")
  private Collection<Account> accounts;
}
