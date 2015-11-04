package de.christofreichardt.neo4jtools.ogm.model;

import de.christofreichardt.neo4jtools.apt.GeneratedValue;
import de.christofreichardt.neo4jtools.apt.Id;
import de.christofreichardt.neo4jtools.apt.NodeEntity;
import de.christofreichardt.neo4jtools.apt.Property;
import de.christofreichardt.neo4jtools.apt.SingleLink;
import de.christofreichardt.neo4jtools.ogm.Cell;
import de.christofreichardt.neo4jtools.ogm.Wrapper;
import java.util.Objects;
import org.neo4j.graphdb.Direction;

/**
 *
 * @author Christof Reichardt
 */
@NodeEntity(label = "DOCUMENTS")
public class Document {
  @Id
  @Property
  @GeneratedValue
  private Long id;
  
  @Property
  private String title;
  
  @Property
  private String creationDate;
  
  @Property
  private String type;
  
  @SingleLink(direction = Direction.INCOMING, type = "HAS")
  private Cell<Account> account;

  public Document() {
  }

  public Document(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
    return "Document[" + "id=" + this.id + ", title=" + this.title + "]";
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + Objects.hashCode(this.id);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    boolean matched;
    
    if (this == object) {
      matched = true;
    }
    else if (object == null) {
      matched = false;
    }
    else if (getClass() != object.getClass()) {
      matched = false;
    }
    else {
      final Document other = (Document) object;
      matched = Objects.equals(this.id, other.id);
    }
    
    return matched;
  }
}
