/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.neo4jtools.idgen.IdGeneratorService;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Account2;
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
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
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
public class ObjectGraphMapperUnit extends BasicMapperUnit {
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  public ObjectGraphMapperUnit(Properties properties) {
    super(properties);
  }
  
  @Test
  public void roundTrip_1() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "roundTrip_1()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(ObjectGraphMapperUnit.graphDatabaseService, Document.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account account = new Account("Tester");
        account.setCountryCode("DE");
        account.setLocalityName("Rodgau");
        account.setStateName("Hessen");
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 5;
        account.setDocuments(new ArrayList<>());
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(account);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          account.getDocuments().add(document);
        }
        final Long KEYRING_ID = 31L;
        account.setKeyRing(new KeyRing(KEYRING_ID));
        account.getKeyRing().setPath("dummy");
        account.getKeyRing().setAccount(account);
        
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          objectGraphMapper.save(account);
          transaction.success();
        }
        traceAllNodes();
        
        final Long DOCUMENT_ID = 3L;
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Document document;
          document = objectGraphMapper.load(Document.class, DOCUMENT_ID);

          tracer.out().printfIndentln("document = %s", document);
          Assert.assertTrue("Wrong Document.", Objects.equals(DOCUMENT_ID, document.getId()));
          Assert.assertTrue("Wrong Document.", Objects.equals(document.getTitle(), "Testdocument-" + DOCUMENT_ID));
          tracer.out().printfIndentln("document.getAccount() = %s", document.getAccount());
          
          document.setTitle("Changed title.");
          objectGraphMapper.save(document);
          
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Node accountNode = ObjectGraphMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");

          Assert.assertNotNull("Expected an account node", accountNode);
          Assert.assertTrue("Expected " + TEST_DOCUMENTS + " outgoing relationships to documents.", 
              accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == TEST_DOCUMENTS);
          Assert.assertTrue("Expected an outgoing relationship to a keyring.", 
              accountNode.getDegree(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) == 1);
          
          Node documentNode = ObjectGraphMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.DOCUMENTS, "id", DOCUMENT_ID);
          
          Assert.assertNotNull("Expected an document node", documentNode);
          Assert.assertTrue("Wrong title.", Objects.equals(documentNode.getProperty("title"), "Changed title."));
          Assert.assertTrue("Expected a single incoming relationship to an account.", 
              documentNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.INCOMING) == 1);
          
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void roundTrip_2() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "roundTrip_2()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(ObjectGraphMapperUnit.graphDatabaseService, Document.class.getName(), KeyRing.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account account = new Account("Tester");
        account.setCountryCode("DE");
        account.setLocalityName("Rodgau");
        account.setStateName("Hessen");
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 5;
        account.setDocuments(new ArrayList<>());
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(account);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          account.getDocuments().add(document);
        }
        final Long KEYRING_ID = 31L;
        account.setKeyRing(new KeyRing(KEYRING_ID));
        account.getKeyRing().setPath("dummy");
        account.getKeyRing().setAccount(account);
        
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          objectGraphMapper.save(account);
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          KeyRing keyRing = objectGraphMapper.load(KeyRing.class, KEYRING_ID);

          tracer.out().printfIndentln("keyRing = %s", keyRing);
          Assert.assertTrue("Wrong Document.", Objects.equals(KEYRING_ID, keyRing.getId()));
          tracer.out().printfIndentln("keyRing.getAccount() = %s", keyRing.getAccount());
          
          objectGraphMapper.save(keyRing.getAccount());
          
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");

          Assert.assertNotNull("Expected an account node", accountNode);
          Assert.assertTrue("Expected " + TEST_DOCUMENTS + " outgoing relationships to documents.", 
              accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == TEST_DOCUMENTS);
          Assert.assertTrue("Expected an outgoing relationship to a keyring.", 
              accountNode.getDegree(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) == 1);
          
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void polymorphicLoad() throws Object2NodeMapper.Exception, InterruptedException, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "polymorphicLoad()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(ObjectGraphMapperUnit.graphDatabaseService, Document.class.getName(), KeyRing.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account2 extAccount = new Account2("Tester");
        extAccount.setLastName("Dummy");
        extAccount.setCountryCode("DE");
        extAccount.setLocalityName("Rodgau");
        extAccount.setStateName("Hessen");
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 5;
        extAccount.setDocuments(new ArrayList<>());
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(extAccount);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          extAccount.getDocuments().add(document);
        }
        final Long KEYRING_ID = 31L;
        extAccount.setKeyRing(new KeyRing(KEYRING_ID));
        extAccount.getKeyRing().setPath("dummy");
        extAccount.getKeyRing().setAccount(extAccount);
        
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          objectGraphMapper.save(extAccount);
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Account account = objectGraphMapper.load(Account.class, "Tester");

          tracer.out().printfIndentln("account = %s", account);
          Assert.assertTrue("Wrong Account.", Objects.equals("Tester", account.getUserId()));
          Assert.assertTrue("Wrong type.", account instanceof Account2);
          
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");

          Assert.assertNotNull("Expected an account node", accountNode);
          Assert.assertTrue("Expected " + TEST_DOCUMENTS + " outgoing relationships to documents.", 
              accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == TEST_DOCUMENTS);
          Assert.assertTrue("Expected an outgoing relationship to a keyring.", 
              accountNode.getDegree(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) == 1);
          
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void invalidPrimaryKey() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "invalidPrimaryKey()");
    
    try {
      this.thrown.expect(IllegalArgumentException.class);
      this.thrown.expectMessage("Invalid type for the primary key.");
      
      try {
        IdGeneratorService.getInstance().init(ObjectGraphMapperUnit.graphDatabaseService, Document.class.getName(), KeyRing.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account account = new Account("Tester");
        account.setCountryCode("DE");
        account.setLocalityName("Rodgau");
        account.setStateName("Hessen");
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 5;
        account.setDocuments(new ArrayList<>());
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(account);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          account.getDocuments().add(document);
        }
        final Long KEYRING_ID = 31L;
        account.setKeyRing(new KeyRing(KEYRING_ID));
        account.getKeyRing().setPath("dummy");
        account.getKeyRing().setAccount(account);
        
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          objectGraphMapper.save(account);
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          KeyRing keyRing = objectGraphMapper.load(KeyRing.class, "Invalidkey");

          tracer.out().printfIndentln("keyRing = %s", keyRing);
          Assert.assertTrue("Wrong Document.", Objects.equals(KEYRING_ID, keyRing.getId()));
          tracer.out().printfIndentln("keyRing.getAccount() = %s", keyRing.getAccount());
          
          objectGraphMapper.save(keyRing.getAccount());
          
          transaction.success();
        }
        traceAllNodes();
        
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Node accountNode = Object2NodeMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ACCOUNTS, "commonName", "Tester");

          Assert.assertNotNull("Expected an account node", accountNode);
          Assert.assertTrue("Expected " + TEST_DOCUMENTS + " outgoing relationships to documents.", 
              accountNode.getDegree(RESTFulCryptoRelationships.HAS, Direction.OUTGOING) == TEST_DOCUMENTS);
          Assert.assertTrue("Expected an outgoing relationship to a keyring.", 
              accountNode.getDegree(RESTFulCryptoRelationships.OWNS, Direction.OUTGOING) == 1);
          
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void saveAndLoad() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "saveAndLoad()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(ObjectGraphMapperUnit.graphDatabaseService, Document.class.getName(), KeyRing.class.getName());
        IdGeneratorService.getInstance().start();
        
        Account account = new Account("Tester");
        account.setCountryCode("DE");
        account.setLocalityName("Rodgau");
        account.setStateName("Hessen");
        LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
        String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        final int TEST_DOCUMENTS = 10;
        account.setDocuments(new ArrayList<>());
        for (int i = 0; i < TEST_DOCUMENTS; i++) {
          Document document = new Document();
          document.setAccount(account);
          document.setTitle("Testdocument-" + i);
          document.setType("pdf");
          document.setCreationDate(formattedTime);
          account.getDocuments().add(document);
        }
        
        Node node;
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          node = objectGraphMapper.save(account);
          transaction.success();
        }
        traceAllNodes();
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void nonNullableSingleLinkConstraintViolation() throws Node2ObjectMapper.Exception, Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "nonNullableSingleLinkConstraintViolation()");
    
    try {
      this.thrown.expect(RuntimeException.class);
      this.thrown.expectMessage("Value required for");
      
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      final Long KEYITEM_ID = 0L;
      KeyRing keyRing = new KeyRing(0L);
      keyRing.setPath("." + File.separator + "store" + File.separator + "keystore.jks");
      KeyItem keyItem = new KeyItem(KEYITEM_ID);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyRing.setKeyItems(new ArrayList<>());
      keyRing.getKeyItems().add(keyItem);
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      Node node;
      try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
        node = objectGraphMapper.save(keyRing);
        transaction.success();
      }
      traceAllNodes();
      
      try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
        keyRing = objectGraphMapper.load(KeyRing.class, 0L);
        ArrayList<KeyItem> keyItems = new ArrayList<>(keyRing.getKeyItems());
        
        Assert.assertTrue("Expected a KeyItem.", !keyItems.isEmpty());
        tracer.out().printfIndentln("keyItems.get(0) = %s", keyItems.get(0));
        Assert.assertTrue("Wrong KeyItem.", Objects.equals(keyItems.get(0).getId(), KEYITEM_ID));
        
        keyRing.getAccount();
        
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void nonNullableSingleLinks() throws MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "nonNullableSingleLinks()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      final Long KEYITEM_ID = 0L;
      KeyRing keyRing = new KeyRing(0L);
      keyRing.setPath("." + File.separator + "store" + File.separator + "keystore.jks");
      KeyItem keyItem = new KeyItem(KEYITEM_ID);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(formattedTime);
      keyRing.setKeyItems(new ArrayList<>());
      keyRing.getKeyItems().add(keyItem);
      
      Node node;
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper
          = new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        node = objectGraphMapper.save(keyRing);
        transaction.success();
      }
      traceAllNodes();
      
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
        Set<SingleLinkData> nonNullableSingleLinkViolations = node2ObjectMapper.nonNullableSingleLinkViolations(RESTFulCryptoRelationships.class);
        for (SingleLinkData singleLinkData : nonNullableSingleLinkViolations) {
          tracer.out().printfIndentln("singleLinkData = %s", singleLinkData);
        }
        
        Assert.assertTrue("Expected exactly one violation.", nonNullableSingleLinkViolations.size() == 1);
        
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
}
