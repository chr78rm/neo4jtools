package de.christofreichardt.neo4jtools.idgen;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 *
 * @author Christof Reichardt
 */
public class IdGeneratorUnit implements Traceable {
  static public GraphDatabaseService graphDatabaseService;
  final static String DB_PATH = "." + File.separator + "db";
  final private Properties properties;

  public IdGeneratorUnit(Properties properties) {
    this.properties = properties;
  }
  
  @BeforeClass
  static public void startDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", IdGeneratorUnit.class, "startDB()");
    
    try {
      IdGeneratorUnit.graphDatabaseService = new GraphDatabaseFactory()
          .newEmbeddedDatabaseBuilder(DB_PATH)
          .setConfig(GraphDatabaseSettings.keep_logical_logs, "10 files")
          .newGraphDatabase();
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Before
  public void init() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "init()");

    try {
      try (Transaction transaction = IdGeneratorUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(IdGeneratorUnit.graphDatabaseService).getAllNodes();
        nodes.forEach(node -> {
          tracer.out().printfIndentln("Delete: node[%d].", node.getId());
          node.delete();
        });
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void findIdGenDescription() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "findIdGenDescription()");
    
    try {
      final String ENTITY_NAME = "Test";
      IdGenerator idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      
      tracer.out().printfIndentln("idGenerator.getBufferSize() = %d", idGenerator.getBufferSize());
      tracer.out().printfIndentln("idGenerator.getStep() = %d", idGenerator.getStep());
      tracer.out().printfIndentln("idGenerator.getStartId() = %d", idGenerator.getStartId());
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      Assert.assertTrue("Expected the default buffer size.", idGenerator.getBufferSize() == IdGenerator.DEFAULT_BUFFER_SIZE);
      Assert.assertTrue("Expected the default step.", idGenerator.getStep()== IdGenerator.DEFAULT_STEP);
      Assert.assertTrue("Expected the default id.", idGenerator.getStartId() == IdGenerator.DEFAULT_ID);
      Assert.assertTrue("Expected an inactive IdGenerator.", !idGenerator.isActive());
      
      idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      
      tracer.out().printfIndentln("idGenerator.getBufferSize() = %d", idGenerator.getBufferSize());
      tracer.out().printfIndentln("idGenerator.getStep() = %d", idGenerator.getStep());
      tracer.out().printfIndentln("idGenerator.getStartId() = %d", idGenerator.getStartId());
      Assert.assertTrue("Expected the default buffer size.", idGenerator.getBufferSize() == IdGenerator.DEFAULT_BUFFER_SIZE);
      Assert.assertTrue("Expected the default step.", idGenerator.getStep()== IdGenerator.DEFAULT_STEP);
      Assert.assertTrue("Expected the default id.", idGenerator.getStartId() == IdGenerator.DEFAULT_ID);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void cancel() throws InterruptedException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "cancel()");
    
    try {
      final String ENTITY_NAME = "Test";
      IdGenerator idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      
      tracer.out().printfIndentln("idGenerator.getStartId() = %d", idGenerator.getStartId());
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      Assert.assertTrue("Expected the default id.", idGenerator.getStartId() == IdGenerator.DEFAULT_ID);
      Assert.assertTrue("Expected an inactive IdGenerator.", !idGenerator.isActive());
      
      ExecutorService executorService = Executors.newSingleThreadExecutor((Runnable runnable) -> new Thread(runnable, "IdGeneratorWorker-cancel"));
      Future<IdGenDescription> future = executorService.submit(idGenerator);
      idGenerator.await();
      
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      Assert.assertTrue("Expected an active IdGenerator.", idGenerator.isActive());
      
      future.cancel(true);
      executorService.shutdown();
      boolean awaitTermination = executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
      
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      tracer.out().printfIndentln("awaitTermination = %b", awaitTermination);
      Assert.assertTrue("Expected a terminated executor service.", awaitTermination);
      
      idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      
      tracer.out().printfIndentln("idGenerator.getBufferSize() = %d", idGenerator.getBufferSize());
      tracer.out().printfIndentln("idGenerator.getStartId() = %d", idGenerator.getStartId());
      tracer.out().printfIndentln("idGenerator.getStep() = %d", idGenerator.getStep());
      Assert.assertTrue("Expected an updated start id.", idGenerator.getStartId() >= IdGenerator.DEFAULT_BUFFER_SIZE - 1);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void stress() throws InterruptedException, ExecutionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "stress()");
    
    try {
      final String ENTITY_NAME = "Test";
      final int NUMBER_OF_CONSUMERS = 3;
      final int NUMBER_OF_TASKS = 100;
      Set<Long> expectedIds = new HashSet<>();
      for (long i=0; i<NUMBER_OF_TASKS; i++) {
        expectedIds.add(i);
      }
      ExecutorService idGeneratorService = Executors.newSingleThreadExecutor((Runnable runnable) -> new Thread(runnable, "IdGeneratorWorker"));
      final IdGenerator idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      Future<IdGenDescription> idGeneratorFuture = idGeneratorService.submit(idGenerator);
      idGenerator.await();
      
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      Assert.assertTrue("Expected an active IdGenerator.", idGenerator.isActive());
      
      class IdConsumer implements Callable<Long> {
        @Override
        public Long call() throws Exception {
          AbstractTracer tracer = getCurrentTracer();
          tracer.initCurrentTracingContext();
          tracer.entry("Long", this, "call()");
          try {
            long nextId = idGenerator.getNextId();
            tracer.out().printfIndentln("nextId = %d", nextId);
            return nextId;
          }
          finally {
            tracer.wayout();
          }
        }
      }
      
      final AtomicInteger consumerNr = new AtomicInteger();
      ExecutorService consumerService = Executors.newFixedThreadPool(NUMBER_OF_CONSUMERS, 
          (Runnable runnable) -> new Thread(runnable, "ConsumerWorker-" + consumerNr.getAndIncrement()));
      List<Future<Long>> idConsumerFutures = new ArrayList<>();
      for (int i=0; i<NUMBER_OF_TASKS; i++) {
        idConsumerFutures.add(consumerService.submit(new IdConsumer()));
      }
      idConsumerFutures.stream().forEach(idConsumerFuture -> {
        try {
          Long nextId = idConsumerFuture.get();
          tracer.out().printfIndentln("nextId = %d", nextId);
          Assert.assertTrue("Invalid Id.", expectedIds.contains(nextId));
          expectedIds.remove(nextId);
        }
        catch (InterruptedException | ExecutionException ex) {
          tracer.logException(LogLevel.ERROR, ex, getClass(), "generate()");
        }
      });
      Assert.assertTrue("Missing Ids.", expectedIds.isEmpty());
      
//      idGeneratorFuture.cancel(true);
      idGenerator.stop();
      
      idGeneratorService.shutdown();
      consumerService.shutdown();
      idGeneratorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
      consumerService.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void stop() throws InterruptedException, ExecutionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "stop()");
    
    try {
      final String ENTITY_NAME = "Test";
      IdGenerator idGenerator = new IdGenerator(IdGeneratorUnit.graphDatabaseService, ENTITY_NAME);
      
      tracer.out().printfIndentln("idGenerator.getStartId() = %d", idGenerator.getStartId());
      Assert.assertTrue("Expected the default id.", idGenerator.getStartId() == IdGenerator.DEFAULT_ID);
      
      ExecutorService executorService = Executors.newSingleThreadExecutor((Runnable runnable) -> new Thread(runnable, "IdGeneratorWorker-stop"));
      Future<IdGenDescription> future = executorService.submit(idGenerator);
      idGenerator.await();
      
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      Assert.assertTrue("Expected an active IdGenerator.", idGenerator.isActive());
      
      idGenerator.getNextId();
      idGenerator.stop();
      IdGenDescription idGenDescription = future.get();
      executorService.shutdown();
      boolean awaitTermination = executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
      
      tracer.out().printfIndentln("idGenerator.isActive() = %b", idGenerator.isActive());
      tracer.out().printfIndentln("idGenerator.isFinished() = %b", idGenerator.isFinished());
      tracer.out().printfIndentln("awaitTermination = %b", awaitTermination);
      Assert.assertTrue("Expected a terminated executor service.", awaitTermination);
      tracer.out().printfIndentln("idGenDescription = %s", idGenDescription);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void service() throws InterruptedException, ExecutionException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "service()");
    
    try {
      final String[] ENTITY_NAMES = {"Test-0", "Test-1", "Test-2"};
      final int NUMBER_OF_TASKS = 100;
      
      List<Set<Long>> expectedIdsList = new ArrayList<>();
      for (int i = 0; i < ENTITY_NAMES.length; i++) {
        expectedIdsList.add(new HashSet<>());
      }
      expectedIdsList.forEach(expectedIds -> {
        for (long i = 0; i < NUMBER_OF_TASKS; i++) {
          expectedIds.add(i);
        }
      });
      
      IdGeneratorService idGeneratorService = new IdGeneratorService(IdGeneratorUnit.graphDatabaseService, ENTITY_NAMES);
      idGeneratorService.start();
      
      class IdConsumer implements Callable<Long> {
        final String name;
        public IdConsumer(String name) {
          this.name = name;
        }
        @Override
        public Long call() throws Exception {
          return idGeneratorService.getNextId(this.name);
        }
      }
      
      try {
        IdConsumer[] idConsumers = new IdConsumer[ENTITY_NAMES.length];
        for (int i = 0; i < ENTITY_NAMES.length; i++) {
          idConsumers[i] = new IdConsumer(ENTITY_NAMES[i]);
        }
        
        List<Set<Future<Long>>> futuresList = new ArrayList<>();
        for (int i = 0; i < ENTITY_NAMES.length; i++) {
          futuresList.add(new HashSet<>());
        }
        ExecutorService consumerService = Executors.newFixedThreadPool(idConsumers.length);
        for (int j = 0; j < NUMBER_OF_TASKS; j++) {
          for (int i = 0; i < ENTITY_NAMES.length; i++) {
            futuresList.get(i).add(consumerService.submit(idConsumers[i]));
          }
        }
        
        for (int i=0; i<ENTITY_NAMES.length; i++) {
          tracer.out().printfIndentln("futuresList.get(%d).size() = %d", i, futuresList.get(i).size());
          
          for (Future<Long> future : futuresList.get(i)) {
            Long id = future.get();
            Assert.assertTrue("Unexpected id fetched: " + id, expectedIdsList.get(i).contains(id));
            expectedIdsList.get(i).remove(id);
          }
          Assert.assertTrue("Unmatched ids remaining.", expectedIdsList.get(i).isEmpty());
        }
        
        consumerService.shutdown();
        boolean terminated = consumerService.awaitTermination(5, TimeUnit.SECONDS);
        
        tracer.out().printfIndentln("terminated = %b", terminated);
      }
      finally {
        idGeneratorService.shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @After
  public void exit() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "exit()");
    
    try {
    }
    finally {
      tracer.wayout();
    }
  }
  
  @AfterClass
  static public void shutdownDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", IdGeneratorUnit.class, "shutdownDB()");
    
    try {
      IdGeneratorUnit.graphDatabaseService.shutdown();
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }
  
}
