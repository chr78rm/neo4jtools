package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Account1;
import de.christofreichardt.neo4jtools.ogm.model.Document;
import de.christofreichardt.neo4jtools.ogm.model.KeyItem;
import de.christofreichardt.neo4jtools.ogm.model.KeyRing;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 *
 * @author Christof Reichardt
 */
public class Object2NodeMapperUnit implements Traceable {
  static public GraphDatabaseService graphDatabaseService;
  final static String DB_PATH = "." + File.separator + "db";
  final private Properties properties;
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  public Object2NodeMapperUnit(Properties properties) {
    this.properties = properties;
  }
  
  @BeforeClass
  static public void startDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", Object2NodeMapperUnit.class, "startDB()");
    
    try {
      Object2NodeMapperUnit.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        Schema schema = Object2NodeMapperUnit.graphDatabaseService.schema();
        for (IndexDefinition index : schema.getIndexes()) {
          index.drop();
        }
        schema.indexFor(RESTfulCryptoLabels.ACCOUNTS)
            .on("commonName")
            .create();
        transaction.success();
      }
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
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(Object2NodeMapperUnit.graphDatabaseService).getAllNodes();
        nodes.forEach(node -> {
          tracer.out().printfIndentln("Delete: node[%d].", node.getId());
          tracer.out().printfIndentln("node.getDegree() = %d", node.getDegree());
          node.getRelationships().forEach(relationShip -> {
            tracer.out().printfIndentln("Delete: relationShip[%d].", relationShip.getId());
            relationShip.delete();
          });
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
  public void singleEntity() throws MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "singleEntity()");
    
    try {
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void nonNullableProperty() throws MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "nonNullableProperty()");
    
    try {
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName(null);
      
      thrown.expect(Object2NodeMapper.Exception.class);
      thrown.expectMessage("Value required for property");
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void nonNullableSingleLinks() throws MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "nonNullableSingleLinks()");
    
    try {
      Account1 account = new Account1("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      
      thrown.expect(Object2NodeMapper.Exception.class);
      thrown.expectMessage("Entity required for");
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void entityTree() throws MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "entityTree()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      KeyRing keyRing = new KeyRing(0);
      keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
      List<KeyItem> keyItems = new ArrayList<>();
      KeyItem keyItem = new KeyItem(0);
      keyItem.setKeyRing(keyRing);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyItems.add(keyItem);
      keyRing.setKeyItems(keyItems);
      account.setKeyRing(keyRing);
      List<Document> documents = new ArrayList<>();
      Document document = new Document(0);
      document.setAccount(account);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      documents.add(document);
      document = new Document(1);
      document.setAccount(account);
      document.setTitle("Testdocument-2");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      documents.add(document);
      account.setDocuments(documents);
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
      
      object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
      
      account.setDocuments(null);
      
      object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }
  
  private void traceAllNodes() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "traceAllNodes()");
    
    try {
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(Object2NodeMapperUnit.graphDatabaseService).getAllNodes();
        nodes.forEach(node -> {
          RichNode richNode = new RichNode(node);
          richNode.trace(tracer);
        });
        transaction.success();
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
    tracer.entry("void", Object2NodeMapperUnit.class, "shutdownDB()");
    
    try {
      Object2NodeMapperUnit.graphDatabaseService.shutdown();
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
