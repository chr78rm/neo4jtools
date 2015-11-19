/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

/**
 *
 * @author Developer
 */
public class PropertyData {
  private final String name;
  private final boolean nullable;
  private final boolean version;
  private final Class<?> clazz;

  public PropertyData(String name, boolean nullable, boolean version, Class<?> clazz) {
    this.name = name;
    this.nullable = nullable;
    this.version = version;
    this.clazz = clazz;
  }

  public String getName() {
    return name;
  }

  public boolean isNullable() {
    return nullable;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("PropertyData[");
    builder.append("name=").append(this.name).append(", ");
    builder.append("nullable=").append(this.nullable).append(", ");
    builder.append("version=").append(this.version);
    builder.append("]");
    
    return builder.toString();
  }
  
}
