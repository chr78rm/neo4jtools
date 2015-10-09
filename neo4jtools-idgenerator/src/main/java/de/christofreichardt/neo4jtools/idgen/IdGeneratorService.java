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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author Christof Reichardt
 */
public class IdGeneratorService implements Traceable {
  final private Map<String, IdGenerator> entity2IdGenerator = new HashMap<>();
  final private ExecutorService executorService;
  private Set<Future<IdGenDescription>> futures;

  public IdGeneratorService(GraphDatabaseService graphDatabaseService, String... entityNames) {
    for (String entityName : entityNames) {
      this.entity2IdGenerator.put(entityName, new IdGenerator(graphDatabaseService, entityName));
    }
    this.executorService = initThreadPool();
  }
  
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
  
  public void start() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "start()");
    
    try {
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
    }
    finally {
      tracer.wayout();
    }
  }
  
  public void shutDown() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "shutDown()");
    
    try {
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
    }
    finally {
      tracer.wayout();
    }
  }
  
  public long getNextId(String entityName) throws InterruptedException {
    return this.entity2IdGenerator.get(entityName).getNextId();
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
}
