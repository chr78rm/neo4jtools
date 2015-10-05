package de.christofreichardt.neo4jtools.idgen;

/**
 *
 * @author Christof Reichardt
 */
public class IdGenDescription {
  final private int bufferSize;
  private final long step;
  final private long startId;
  final private String entityName;


  public IdGenDescription(int bufferSize, long step, long startId, String entityName) {
    this.bufferSize = bufferSize;
    this.step = step;
    this.startId = startId;
    this.entityName = entityName;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public long getStep() {
    return step;
  }

  public long getStartId() {
    return startId;
  }

  public String getEntityName() {
    return entityName;
  }

  @Override
  public String toString() {
    return "IdGenDescription[" + "bufferSize=" + bufferSize + ", step=" + step + ", startId=" + startId + ", entityName=" + entityName + ']';
  }
  
}
