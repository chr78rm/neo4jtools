package de.christofreichardt.neo4jtools.idgen;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Christof Reichardt
 */
public class IdGeneratorService implements Traceable {
  final private Map<String, IdGenerator> entity2IdGenerator = new HashMap<>();
  private ExecutorService executorService;
  private Set<Future<IdGenDescription>> futures;
  private boolean active = false;
  
  private static class Holder {
    private static final IdGeneratorService INSTANCE = new IdGeneratorService();
  }

  private IdGeneratorService() {
  }

  public boolean isActive() {
    return active;
  }
  
  public static IdGeneratorService getInstance() {
    return Holder.INSTANCE;
  }

//  public IdGeneratorService(GraphDatabaseService graphDatabaseService, String... entityNames) {
//    for (String entityName : entityNames) {
//      this.entity2IdGenerator.put(entityName, new IdGenerator(graphDatabaseService, entityName));
//    }
//    this.executorService = initThreadPool();
//  }
  
  private ExecutorService initThreadPool() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("ExecutorService", this, "initThreadPool()");
    
    try {
      final AtomicInteger counter = new AtomicInteger();
      return Executors.newFixedThreadPool(this.entity2IdGenerator.size(), 
          (Runnable runnable) -> new Thread(runnable, "IdGeneratorWorker-" + counter.getAndIncrement()));
    }
    finally {
      tracer.wayout();
    }
  }
  
  synchronized void init(GraphDatabaseService graphDatabaseService, String... entityNames) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "init(GraphDatabaseService graphDatabaseService, String... entityNames)");
    
    try {
      if (this.active)
        throw new IllegalStateException("Service is alread active.");
      
      this.entity2IdGenerator.clear();
      for (String entityName : entityNames) {
        this.entity2IdGenerator.put(entityName, new IdGenerator(graphDatabaseService, entityName));
      }
      this.executorService = initThreadPool();
    }
    finally {
      tracer.wayout();
    }
  }
  
  synchronized public void start() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "start()");
    
    try {
      if (this.executorService == null  ||  this.executorService.isShutdown())
        throw new IllegalStateException("Service hasn't been initialized yet.");
      
      if (!this.active) {
        this.futures = this.entity2IdGenerator.entrySet().stream()
            .map(entry -> {
              tracer.out().printfIndentln("Starting %s ...", entry.getKey());
              Future<IdGenDescription> future = this.executorService.submit(entry.getValue());
              try {
                entry.getValue().await();
                assert entry.getValue().isActive();
              }
              catch (InterruptedException ex) {
                tracer.logException(LogLevel.WARNING, ex, getClass(), "start()");
              }
              return future;
            })
            .collect(Collectors.toSet());
        this.active = true;
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  synchronized public void shutDown() throws InterruptedException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "shutDown()");
    
    try {
      if (this.active) {
        this.entity2IdGenerator.forEach((entity, idgenerator) -> {
          tracer.out().printfIndentln("Stopping %s ... (%s)", entity, idgenerator.getEntityName());
          try {
            idgenerator.stop();
          }
          catch (InterruptedException ex) {
            tracer.logException(LogLevel.WARNING, ex, getClass(), "shutDown()");
          }
        });
        this.futures.forEach(future -> {
          try {
            tracer.out().printfIndentln("Status: %s (%d)", future.get(), future.hashCode());
          }
          catch (InterruptedException | ExecutionException ex) {
            tracer.logException(LogLevel.WARNING, ex, getClass(), "shutDown()");
          }
        });
        
        this.executorService.shutdown();
        this.executorService.awaitTermination(10, TimeUnit.SECONDS);
        if (!this.executorService.isTerminated())
          throw new RuntimeException("Problems during the shutdown of the service.");
        
        this.active = false;
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  public long getNextId(String entityName) throws InterruptedException {
    if (!this.active)
      throw new IllegalStateException("Service hasn't been started yet.");
    if (!this.entity2IdGenerator.containsKey(entityName))
      throw new IllegalArgumentException("Unknown entity name '" + entityName + "'.");
    
    return this.entity2IdGenerator.get(entityName).getNextId();
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
