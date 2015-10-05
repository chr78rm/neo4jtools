package de.christofreichardt.neo4jtools.idgen;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Christof Reichardt
 */
final class IdGenerator implements Callable<IdGenDescription>, Traceable {
  final public static int DEFAULT_BUFFER_SIZE = 25;
  final public static long DEFAULT_ID = 0L;
  final public static long DEFAULT_STEP = 1L;
  
  final private BlockingQueue<Long> blockingQueue;
  final private GraphDatabaseService graphDatabaseService;
  final private IdGenDescription idGenDescription;
  final private CountDownLatch countDownLatch;
  
  private long nextId;
  private volatile boolean mainSwitch = true;
  private Thread worker;
  private volatile boolean active = false;
  private volatile boolean finished = false;
  
  public int getBufferSize() {
    return this.idGenDescription.getBufferSize();
  }
  
  public long getStep() {
    return this.idGenDescription.getStep();
  }
  
  public long getStartId() {
    return this.idGenDescription.getStartId();
  }
  
  public long getNextId() throws InterruptedException {
    if (!this.active)
      throw new IllegalStateException("IdGenerator hasn't been started yet..");
    return this.blockingQueue.take();
  }

  public boolean isActive() {
    return active;
  }

  public boolean isFinished() {
    return finished;
  }
  
  public String getEntityName() {
    return this.idGenDescription.getEntityName();
  }

  protected IdGenerator(GraphDatabaseService graphDatabaseService, String entityName) {
    this.graphDatabaseService = graphDatabaseService;
    this.idGenDescription = findIdGenDescription(entityName);
    this.blockingQueue = new ArrayBlockingQueue<>(getBufferSize());
    this.nextId = getStartId();
    this.countDownLatch = new CountDownLatch(getBufferSize());
  }
  
  private IdGenDescription findIdGenDescription(String entityName) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Node", this, "findIdGenDescription()");
    
    try {
      IdGenDescription igd;
      try (Transaction transaction = this.graphDatabaseService.beginTx()) {
      Node node;
        ResourceIterator<Node> nodes = this.graphDatabaseService.findNodes(IdGenLabels.ID_GENERATOR, "entity", entityName);
        if (!nodes.hasNext()) {
          tracer.out().printfIndentln("Creating IdGenerator node ...");
          
          node = this.graphDatabaseService.createNode(IdGenLabels.ID_GENERATOR);
          node.setProperty("entity", entityName);
          node.setProperty("nextId", DEFAULT_ID);
          node.setProperty("step", DEFAULT_STEP);
          node.setProperty("bufferSize", DEFAULT_BUFFER_SIZE);
        }
        else {
          node = nodes.next();
        }
        
        assert entityName.equals(node.getProperty("entity")) : "Wrong entity.";
        
        igd = new IdGenDescription(
            (int) node.getProperty("bufferSize", DEFAULT_BUFFER_SIZE), 
            (long) node.getProperty("step", DEFAULT_STEP), 
            (long) node.getProperty("nextId", DEFAULT_ID),
            entityName
        );
        
        transaction.success();
      }
      
      return igd;
    }
    finally {
      tracer.wayout();
    }
  }
  
  public void stop() throws InterruptedException {
    if (this.worker != null) {
      this.mainSwitch = false;
      this.blockingQueue.poll(5, TimeUnit.SECONDS);
    }
  }
  
  public void await() throws InterruptedException {
    this.countDownLatch.await();
  }

  @Override
  public IdGenDescription call() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.initCurrentTracingContext(5, true);
    tracer.entry("Long", this, "call()");
    
    try {
      this.active = true;
      this.worker = Thread.currentThread();
      if (this.finished) {
        tracer.logMessage(LogLevel.ERROR, "Restart of service isn't allowed.", getClass(), "call()");
        throw new IllegalStateException("Restart of service isn't allowed.");
      }
      
      while (this.mainSwitch) {
        try {
          this.countDownLatch.countDown();
          this.blockingQueue.put(this.nextId);
          this.nextId += getStep();
        }
        catch (InterruptedException ex) {
          tracer.logException(LogLevel.INFO, ex, getClass(), "call()");
          this.active = false;
          this.finished = true;
          break;
        }
      }
      
      IdGenDescription igd;
      try (Transaction transaction = this.graphDatabaseService.beginTx()) {
        Node node;
        ResourceIterator<Node> nodes = this.graphDatabaseService.findNodes(IdGenLabels.ID_GENERATOR, "entity", getEntityName());
        if (!nodes.hasNext()) {
          transaction.failure();
          throw new IllegalStateException("Cannot found the IdGenerator node.");
        }

        tracer.out().printfIndentln("this.nextId = %d", this.nextId);

        node = nodes.next();
        node.setProperty("nextId", this.nextId);

        igd = new IdGenDescription(
            (int) node.getProperty("bufferSize", DEFAULT_BUFFER_SIZE), 
            (long) node.getProperty("step", DEFAULT_STEP), 
            (long) node.getProperty("nextId", DEFAULT_ID), 
            (String) node.getProperty("entity"));
        
        transaction.success();
      }
      
      return igd;
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    AbstractTracer tracer;
    try {
      tracer = TracerFactory.getInstance().getTracer("IdGeneratorTracer");
    }
    catch (TracerFactory.Exception ex) {
      tracer = TracerFactory.getInstance().getDefaultTracer();
    }
    
    return tracer;
  }
  
}
