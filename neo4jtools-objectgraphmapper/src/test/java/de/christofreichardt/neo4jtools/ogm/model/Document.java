package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import de.christofreichardt.neo4jtools.ogm.Wrapper;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "DOCUMENTS")
public class Document {
  @Id
  @Property
  private final Integer id;
  
  @Property
  private String title;
  
  @Property
  private String creationDate;
  
  @Property
  private String type;
  
  @SingleLink(direction = Direction.INCOMING, type = "OWNS")
  private Cell<Account> account;

  public Document(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Account getAccount() {
    Account theAccount;
    if (this.account != null)
      theAccount = this.account.getEntity();
    else
      theAccount = null;
    
    return theAccount;
  }

  public void setAccount(Account account) {
    this.account = new Wrapper<>(account);
  }

  @Override
  public String toString() {
    return "Document[" + "id=" + id + "]";
  }
}
