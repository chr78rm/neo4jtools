package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.neo4jtools.idgen.IdGeneratorService;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Account1;
import de.christofreichardt.neo4jtools.ogm.model.Account2;
import de.christofreichardt.neo4jtools.ogm.model.Document;
import de.christofreichardt.neo4jtools.ogm.model.EncryptedDocument;
import de.christofreichardt.neo4jtools.ogm.model.KeyItem;
import de.christofreichardt.neo4jtools.ogm.model.KeyItem1;
import de.christofreichardt.neo4jtools.ogm.model.KeyRing;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Christof Reichardt
 */
public class Object2NodeMapperUnit extends BasicMapperUnit {
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  public Object2NodeMapperUnit(Properties properties) {
    super(properties);
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
      
      traceAllNodes();
      checkNodes(0);
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
  public void idIsNull() throws MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "idIsNull()");
    
    try {
      Account account = new Account(null);
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      
      thrown.expect(Object2NodeMapper.Exception.class);
      thrown.expectMessage("Primary key is null.");
      
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
      KeyRing keyRing = new KeyRing(0L);
      keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
      List<KeyItem> keyItems = new ArrayList<>();
      KeyItem keyItem = new KeyItem(0L);
      keyItem.setKeyRing(keyRing);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyItems.add(keyItem);
      keyRing.setKeyItems(keyItems);
      account.setKeyRing(keyRing);
      List<Document> documents = new ArrayList<>();
      Document document = new Document(0L);
      document.setAccount(account);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      documents.add(document);
      document = new Document(1L);
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
      checkNodes(1);
      
      object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
      checkNodes(1);
      
      account.setDocuments(null);
      
      object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
      checkNodes(2);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void automaticIds() throws MappingInfo.Exception, Object2NodeMapper.Exception, InterruptedException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "automaticIds()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(Object2NodeMapperUnit.graphDatabaseService, Account.class.getName(), Document.class.getName(), KeyRing.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account account = new Account("Tester");
        account.setCountryCode("DE");
        account.setLocalityName("Rodgau");
        account.setStateName("Hessen");
        
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 10;
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(account);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          documents.add(document);
        }
        account.setDocuments(documents);
        
        KeyRing keyRing = new KeyRing();
        keyRing.setAccount(account);
        keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
        account.setKeyRing(keyRing);
        
        keyRing = new KeyRing();
        keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
        
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
        try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
          object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
          transaction.success();
        }
        
        object2NodeMapper = new Object2NodeMapper(keyRing, Object2NodeMapperUnit.graphDatabaseService);
        try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
          Node keyRingNode = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
          
          Assert.assertTrue("Expected a node instance..", keyRingNode != null);
          Assert.assertTrue("Wrong label.", keyRingNode.hasLabel(RESTfulCryptoLabels.KEY_RINGS));
          Assert.assertTrue("Expected an id property.", keyRingNode.hasProperty("id"));
          
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
        
      traceAllNodes();
      checkNodes(3);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void circularDependencies() throws MappingInfo.Exception, Object2NodeMapper.Exception, NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "circularDependencies()");
    
    try {
      ReflectedClass reflectedClass = new ReflectedClass(KeyItem1.class);
      Field declaredField = reflectedClass.getDeclaredField("id");
      
      Assert.assertEquals("Wrong field.", "id", declaredField.getName());
      
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      KeyRing keyRing = new KeyRing(0L);
      keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
      List<KeyItem> keyItems = new ArrayList<>();
      KeyItem1 keyItem = new KeyItem1(0L);
      keyItem.setKeyRing(keyRing);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyItem.setAccount(account);
      keyItems.add(keyItem);
      keyRing.setKeyItems(keyItems);
      account.setKeyRing(keyRing);
      
      try {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Object2NodeMapperUnit.graphDatabaseService);
        try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
          object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
          transaction.success();
        }
      }
      finally {
        traceAllNodes();
      }
        
      checkNodes(4);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void inheritedMapping() throws MappingInfo.Exception, Object2NodeMapper.Exception, NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "inheritedMapping()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account2 account = new Account2("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      account.setLastName("Reichardt");
      KeyRing keyRing = new KeyRing(0L);
      keyRing.setPath("." + File.separator + "store" + File.separator + "theKeystore.jks");
      List<KeyItem> keyItems = new ArrayList<>();
      KeyItem keyItem = new KeyItem(0L);
      keyItem.setKeyRing(keyRing);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyItems.add(keyItem);
      keyRing.setKeyItems(keyItems);
      account.setKeyRing(keyRing);
      List<Document> documents = new ArrayList<>();
      Document document = new Document(0L);
      document.setAccount(account);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      documents.add(document);
      document = new Document(1L);
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
      checkNodes(5);
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void singleLinkConstrainedViolation_1() throws MappingInfo.Exception, Object2NodeMapper.Exception, NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "singleLinkConstrainedViolation_1()");
    
    try {
      this.thrown.expect(Object2NodeMapper.Exception.class);
      this.thrown.expectMessage("Constraint violated.");
      
      Account superTester = new Account("Supertester");
      superTester.setCountryCode("DE");
      superTester.setLocalityName("Rodgau");
      superTester.setStateName("Hessen");
      KeyRing superTesterKeyRing = new KeyRing(0L);
      superTesterKeyRing.setPath("." + File.separator + "store" + File.separator + "theSuperTesterKeystore.jks");
      superTester.setKeyRing(superTesterKeyRing);
      Account tester = new Account("Tester");
      tester.setCountryCode("DE");
      tester.setLocalityName("Hainhausen");
      tester.setStateName("Hessen");
      tester.setKeyRing(superTesterKeyRing);
      
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(superTester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        object2NodeMapper = new Object2NodeMapper(tester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void singleLinkConstrainedViolation_2() throws MappingInfo.Exception, Object2NodeMapper.Exception, NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "singleLinkConstrainedViolation_2()");
    
    try {
      this.thrown.expect(Object2NodeMapper.Exception.class);
      this.thrown.expectMessage("Constraint violated.");
      
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account superTester = new Account("Supertester");
      superTester.setCountryCode("DE");
      superTester.setLocalityName("Rodgau");
      superTester.setStateName("Hessen");
      KeyRing superTesterKeyRing = new KeyRing(0L);
      superTesterKeyRing.setPath("." + File.separator + "store" + File.separator + "theSuperTesterKeystore.jks");
      superTester.setKeyRing(superTesterKeyRing);
      KeyItem keyItem = new KeyItem(0L);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      superTesterKeyRing.setKeyItems(new ArrayList<>());
      superTesterKeyRing.getKeyItems().add(keyItem);
      Account tester = new Account("Tester");
      tester.setCountryCode("DE");
      tester.setLocalityName("Hainhausen");
      tester.setStateName("Hessen");
      tester.setKeyRing(superTesterKeyRing);
      KeyRing testerKeyRing = new KeyRing(1L);
      testerKeyRing.setPath("." + File.separator + "store" + File.separator + "theTesterKeystore.jks");
      tester.setKeyRing(testerKeyRing);
      testerKeyRing.setKeyItems(new ArrayList<>());
      testerKeyRing.getKeyItems().add(keyItem);
      
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(superTester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        object2NodeMapper = new Object2NodeMapper(tester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void staleData() throws MappingInfo.Exception, Object2NodeMapper.Exception, NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "staleData()");
    
    try {
      this.thrown.expect(Object2NodeMapper.Exception.class);
      this.thrown.expectMessage("Stale data.");
      
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account superTester = new Account("Supertester");
      superTester.setCountryCode("DE");
      superTester.setLocalityName("Rodgau");
      superTester.setStateName("Hessen");
      superTester.setDocuments(new ArrayList<>());
      EncryptedDocument document = new EncryptedDocument(0L);
      document.setAccount(superTester);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      superTester.getDocuments().add(document);
      
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(superTester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      
      traceAllNodes();
      
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(superTester, Object2NodeMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private void checkNodes(int scenarioNr) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "checkNodes(int scenarioNr)");
    
    try {
      tracer.out().printfIndentln("scenarioNr = %d", scenarioNr);
      
      try (Transaction transaction = Object2NodeMapperUnit.graphDatabaseService.beginTx()) {
        switch(scenarioNr) {
          case 0: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Expected no outgoing relationships.", accountNode.getDegree(Direction.OUTGOING) == 0);
            Assert.assertTrue("Wrong property.", accountNode.getProperty("localityName").equals("Rodgau"));
            Assert.assertTrue("Wrong property.", accountNode.getProperty("stateName").equals("Hessen"));
            Assert.assertTrue("Wrong property.", accountNode.getProperty("countryCode").equals("DE"));
          }
          break;
            
          case 1: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Expected three outgoing relationships.", accountNode.getDegree(Direction.OUTGOING) == 3);
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.OWNS + "'relationship.", 
                accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) != null);
            Node endNode = accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_RINGS + "' label.", endNode.hasLabel(RESTfulCryptoLabels.KEY_RINGS));
            Assert.assertTrue("Wrong keyring id.", (long) endNode.getProperty("id")  == 0);
            Assert.assertTrue("Wrong keyring path.", endNode.getProperty("path").equals("." + File.separator + "store" + File.separator + "theKeystore.jks"));
            Assert.assertTrue("Expected two outgoing '" + RESTFulCryptoRelationships.HAS + "'relationships.", 
                accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == 2);
            accountNode.getRelationships(Direction.OUTGOING, RESTFulCryptoRelationships.HAS)
                .forEach(relationship -> {
                  Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.DOCUMENTS + "' label.", relationship.getEndNode().hasLabel(RESTfulCryptoLabels.DOCUMENTS));
                  Assert.assertTrue("Wrong properties.", 
                      relationship.getEndNode().hasProperty("id") 
                          && relationship.getEndNode().hasProperty("title") 
                          && relationship.getEndNode().hasProperty("creationDate") 
                          && relationship.getEndNode().hasProperty("type"));
                });
          }
          break;
          
          case 2: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Expected three outgoing relationships.", accountNode.getDegree(Direction.OUTGOING) == 1);
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.OWNS + "'relationship.", 
                accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) != null);
            Node endNode = accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_RINGS + "' label.", endNode.hasLabel(RESTfulCryptoLabels.KEY_RINGS));
            Assert.assertTrue("Wrong keyring id.", (long) endNode.getProperty("id")  == 0);
            Assert.assertTrue("Wrong keyring path.", endNode.getProperty("path").equals("." + File.separator + "store" + File.separator + "theKeystore.jks"));
            Assert.assertTrue("Expected no outgoing '" + RESTFulCryptoRelationships.HAS + "'relationships.", 
                accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == 0);
          }
          break;
          
          case 3: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Expected 10 '" + RESTFulCryptoRelationships.HAS + "' relationships.", accountNode.getDegree(RESTFulCryptoRelationships.HAS) == 10);
            accountNode.getRelationships(Direction.OUTGOING, RESTFulCryptoRelationships.HAS).forEach(relationship -> {
              Node endNode = relationship.getEndNode();
              Assert.assertTrue("Wrong label.", endNode.hasLabel(RESTfulCryptoLabels.DOCUMENTS));
              Assert.assertTrue("Expected an id node.", endNode.hasProperty("id"));
            });
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.OWNS + "'relationship.", 
                accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) != null);
          }
          break;
          
          case 4: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.OWNS + "'relationship.", 
                accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) != null);
            Node keyRingNode = accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_RINGS + "' label.", keyRingNode.hasLabel(RESTfulCryptoLabels.KEY_RINGS));
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.CONTAINS + "'relationship.", 
                keyRingNode.getSingleRelationship(RESTFulCryptoRelationships.CONTAINS, Direction.OUTGOING) != null);
            Node keyItemNode = keyRingNode.getSingleRelationship(RESTFulCryptoRelationships.CONTAINS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_ITEMS + "' label.", keyItemNode.hasLabel(RESTfulCryptoLabels.KEY_ITEMS));
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.BELONGS_TO + "'relationship.", 
                keyItemNode.getSingleRelationship(RESTFulCryptoRelationships.BELONGS_TO, Direction.OUTGOING) != null);
            Node endNode = keyItemNode.getSingleRelationship(RESTFulCryptoRelationships.BELONGS_TO, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected the account node", accountNode.getId() == endNode.getId());
          }
          break;
          
          case 5: {
            Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");
            Assert.assertTrue("Expected an account node.", accountNode != null);
            Assert.assertTrue("Wrong property value.", accountNode.getProperty("localityName").equals("Rodgau"));
            Assert.assertTrue("Wrong property value.", accountNode.getProperty("stateName").equals("Hessen"));
            Assert.assertTrue("Wrong property value.", accountNode.getProperty("countryCode").equals("DE"));
            Assert.assertTrue("Wrong property value.", accountNode.getProperty("lastName").equals("Reichardt"));
            Assert.assertTrue("Expected an undefined first name.", !accountNode.hasProperty("firstName"));
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.OWNS + "' relationship.", 
                accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) != null);
            Node keyRingNode = accountNode.getSingleRelationship(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_RINGS + "' label.", keyRingNode.hasLabel(RESTfulCryptoLabels.KEY_RINGS));
            Assert.assertTrue("Expected a single outgoing '" + RESTFulCryptoRelationships.CONTAINS + "'relationship.", 
                keyRingNode.getSingleRelationship(RESTFulCryptoRelationships.CONTAINS, Direction.OUTGOING) != null);
            Node keyItemNode = keyRingNode.getSingleRelationship(RESTFulCryptoRelationships.CONTAINS, Direction.OUTGOING).getEndNode();
            Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.KEY_ITEMS + "' label.", keyItemNode.hasLabel(RESTfulCryptoLabels.KEY_ITEMS));
            Assert.assertTrue("Expected two '" + RESTFulCryptoRelationships.HAS + "' relationships.", accountNode.getDegree(RESTFulCryptoRelationships.HAS) == 2);
            accountNode.getRelationships(RESTFulCryptoRelationships.HAS).forEach(relationship -> {
              Assert.assertTrue("Expected a '" + RESTfulCryptoLabels.DOCUMENTS + "' label.", 
                  relationship.getEndNode().hasLabel(RESTfulCryptoLabels.DOCUMENTS));
            });
          }
          break;
            
          default:
            Assert.fail("Unknown scenario.");
            break;
        }
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
}
