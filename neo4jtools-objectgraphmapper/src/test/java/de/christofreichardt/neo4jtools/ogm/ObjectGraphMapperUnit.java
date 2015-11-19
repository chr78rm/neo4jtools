/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.neo4jtools.idgen.IdGeneratorService;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Document;
import de.christofreichardt.neo4jtools.ogm.model.KeyRing;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Christof Reichardt
 */
public class ObjectGraphMapperUnit extends BasicMapperUnit {
  public ObjectGraphMapperUnit(Properties properties) {
    super(properties);
  }
  
  @Test
  public void roundTrip_1() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "roundTrip_1()");
    
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
        
        final Long DOCUMENT_ID = 3L;
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          Document document;
          document = objectGraphMapper.load(Document.class, DOCUMENT_ID);

          tracer.out().printfIndentln("document = %s", document);
          Assert.assertTrue("Wrong Document.", Objects.equals(DOCUMENT_ID, document.getId()));
          tracer.out().printfIndentln("document.getAccount() = %s", document.getAccount());
          
          objectGraphMapper.save(document.getAccount());
          
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
}
