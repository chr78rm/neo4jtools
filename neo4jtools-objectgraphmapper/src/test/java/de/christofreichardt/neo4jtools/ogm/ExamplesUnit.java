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
import de.christofreichardt.neo4jtools.ogm.model.KeyItem;
import de.christofreichardt.neo4jtools.ogm.model.KeyRing;
import de.christofreichardt.neo4jtools.ogm.model.PlaintextDocument;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

/**
 *
 * @author Christof Reichardt
 */
public class ExamplesUnit extends BasicMapperUnit {
  
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  public ExamplesUnit(Properties properties) {
    super(properties);
  }

  @Test
  public void example_1() throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_1()");
    
    try {
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        Node node = objectGraphMapper.save(account);
        transaction.success();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void example_2() throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_2()");
    
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
      account.setDocuments(new ArrayList<>());
      Document document = new Document(0L);
      document.setAccount(account);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      account.getDocuments().add(document);
      document = new Document(1L);
      document.setAccount(account);
      document.setTitle("Testdocument-2");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      account.getDocuments().add(document);
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      Node node;
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        node = objectGraphMapper.save(account);
        transaction.success();
      }
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        Traverser traverser = graphDatabaseService
            .traversalDescription()
            .depthFirst()
            .evaluator((Path path) -> {
              tracer.out().printfIndentln("%s", path);
              return Evaluation.INCLUDE_AND_CONTINUE;
            })
            .uniqueness(Uniqueness.NODE_GLOBAL)
            .traverse(node);
        ResourceIterator<Node> iter = traverser.nodes().iterator();
        while (iter.hasNext()) {
          iter.next();
        }
        transaction.success();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void example_3() throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_3()");
    
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
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        objectGraphMapper.save(superTester);
        objectGraphMapper.save(tester);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void example_4() throws Object2NodeMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_4()");
    
    try {
      this.thrown.expect(Object2NodeMapper.Exception.class);
      this.thrown.expectMessage("Value required for property");
      
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        objectGraphMapper.save(account);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void example_5() throws Object2NodeMapper.Exception, InterruptedException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_5()");
    
    try {
      try {
        IdGeneratorService.getInstance().init(graphDatabaseService, Document.class.getName(), KeyRing.class.getName());
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
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
            new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = graphDatabaseService.beginTx()) {
          objectGraphMapper.save(account);
          transaction.success();
        }
      }
      finally {
        IdGeneratorService.getInstance().shutDown();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }

  @Test
  public void example_6() throws Object2NodeMapper.Exception, InterruptedException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "example_6()");
    
    try {
      this.thrown.expect(Object2NodeMapper.Exception.class);
      this.thrown.expectMessage("Stale data.");
      
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      Account account = new Account("Supertester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      account.setDocuments(new ArrayList<>());
      PlaintextDocument document = new PlaintextDocument(0L);
      document.setAccount(account);
      document.setTitle("Testdocument-1");
      document.setType("pdf");
      document.setCreationDate(formattedTime);
      account.getDocuments().add(document);
      ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = 
          new ObjectGraphMapper<>(graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        objectGraphMapper.save(account);
        transaction.success();
      }
      traceAllNodes();
      try (Transaction transaction = graphDatabaseService.beginTx()) {
        objectGraphMapper.save(account);
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
}
