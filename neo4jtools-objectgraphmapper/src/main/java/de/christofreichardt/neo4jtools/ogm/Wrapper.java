package de.christofreichardt.neo4jtools.ogm;

/**
 *
 * @author Christof Reichardt
 */
public class Wrapper<T> implements Cell<T> {
  final private T entity;

  public Wrapper(T entity) {
    if (entity == null)
      throw new NullPointerException("Need a non-null entity instance.");
    this.entity = entity;
  }
  
  @Override
  public T getEntity() {
    return this.entity;
  }
  
}
