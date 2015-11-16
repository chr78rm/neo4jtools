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
  public void loadEntity() throws InterruptedException, MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "loadEntity()");
    
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
        
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, ObjectGraphMapperUnit.graphDatabaseService);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
          transaction.success();
        }
        traceAllNodes();
        
        Document document;
        final Long DOCUMENT_ID = 8L;
        ObjectGraphMapper<RESTfulCryptoLabels, RESTFulCryptoRelationships> objectGraphMapper = new ObjectGraphMapper(new MappingInfo(), 
            ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
          document = objectGraphMapper.load(Document.class, DOCUMENT_ID);
          transaction.success();
        }

        tracer.out().printfIndentln("document = %s", document);
        Assert.assertTrue("Wrong Document.", Objects.equals(DOCUMENT_ID, document.getId()));
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
