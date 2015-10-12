package de.christofreichardt.neo4jtools.ogm;

/**
 *
 * @author Christof Reichardt
 */
public class PrimaryKeyData {
  final private String label;
  final private String fieldName;
  final private boolean generated;

  public PrimaryKeyData(String label, String fieldName, boolean generated) {
    this.label = label;
    this.fieldName = fieldName;
    this.generated = generated;
  }

  public String getLabel() {
    return label;
  }

  public String getFieldName() {
    return fieldName;
  }

  public boolean isGenerated() {
    return generated;
  }

  @Override
  public String toString() {
    return "PrimaryKeyData[" + "label=" + label + ", fieldName=" + fieldName + ", generated=" + generated + "]";
  }
}
