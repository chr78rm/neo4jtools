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
import java.util.List;
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
  public void roundTrip() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "roundTrip()");
    
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
        
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, ObjectGraphMapperUnit.graphDatabaseService);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
          transaction.success();
        }
        traceAllNodes();
        
        final Long DOCUMENT_ID = 3L;
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = new ObjectGraphMapper<>(new MappingInfo(), 
            ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
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
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = new ObjectGraphMapper<>(new MappingInfo(), 
            ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
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
