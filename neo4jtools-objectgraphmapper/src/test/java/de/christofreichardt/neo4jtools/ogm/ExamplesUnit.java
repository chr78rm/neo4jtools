/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.util.Properties;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author Christof Reichardt
 */
public class ExamplesUnit extends BasicMapperUnit {

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
          new ObjectGraphMapper<>(ObjectGraphMapperUnit.graphDatabaseService, RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
      try (Transaction transaction = ObjectGraphMapperUnit.graphDatabaseService.beginTx()) {
        objectGraphMapper.save(account);
        transaction.success();
      }
      traceAllNodes();
    }
    finally {
      tracer.wayout();
    }
  }
}
