/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.idgen.IdGenLabels;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Account2;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.io.File;
import java.util.Properties;
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
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 *
 * @author Christof Reichardt
 */
public class Node2ObjectMapperUnit implements Traceable {
  static private GraphDatabaseService graphDatabaseService;
  final static String DB_PATH = "." + File.separator + "db";
  final private Properties properties;

  public Node2ObjectMapperUnit(Properties properties) {
    this.properties = properties;
  }
  
  @BeforeClass
  static public void startDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", Node2ObjectMapperUnit.class, "startDB()");
    
    try {
      Node2ObjectMapperUnit.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Schema schema = Node2ObjectMapperUnit.graphDatabaseService.schema();
        for (IndexDefinition index : schema.getIndexes()) {
          index.drop();
        }
        schema.indexFor(RESTfulCryptoLabels.ACCOUNTS)
            .on("commonName")
            .create();
        schema.indexFor(RESTfulCryptoLabels.DOCUMENTS)
            .on("id")
            .create();
        schema.indexFor(RESTfulCryptoLabels.KEY_RINGS)
            .on("id")
            .create();
        schema.indexFor(RESTfulCryptoLabels.KEY_ITEMS)
            .on("id")
            .create();
        schema.indexFor(RESTfulCryptoLabels.ROLES)
            .on("id")
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
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(Node2ObjectMapperUnit.graphDatabaseService).getAllNodes();
        nodes.forEach(node -> {
          tracer.out().printfIndentln("Delete: node[%d].", node.getId());
          tracer.out().printfIndentln("node.getDegree() = %d", node.getDegree());
          node.getRelationships().forEach(relationShip -> {
            tracer.out().printfIndentln("Delete: relationShip[%d].", relationShip.getId());
            relationShip.delete();
          });
          node.delete();
        });
        
        Assert.assertTrue("Expected an empty database.", !GlobalGraphOperations.at(Node2ObjectMapperUnit.graphDatabaseService).getAllNodes().iterator().hasNext());
        
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void indexDefinitions() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "indexDefinitions()");
    
    try {
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Node2ObjectMapperUnit.graphDatabaseService
            .schema()
            .getIndexes()
            .forEach(indexDefinition -> {
              tracer.out().printfIndentln("indexDefinition = %s", indexDefinition);
            });
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void coverFields() throws MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "coverFields()");
    
    try {
      Account account = new Account2("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      
      Node node;
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Node2ObjectMapperUnit.graphDatabaseService);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        node = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
      
      Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        account = (Account) node2ObjectMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        
        tracer.out().printfIndentln("account = %s", account);
        
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  private void traceAllNodes() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "traceAllNodes()");
    
    try {
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        for (RESTfulCryptoLabels label : RESTfulCryptoLabels.values()) {
          tracer.out().printfIndentln("--> label: %s", label);
          Node2ObjectMapperUnit.graphDatabaseService.findNodes(label).forEachRemaining(node -> {
            RichNode richNode = new RichNode(node);
            richNode.trace(tracer);
          });
        }
        Node2ObjectMapperUnit.graphDatabaseService.findNodes(IdGenLabels.ID_GENERATOR).forEachRemaining(node -> {
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
    tracer.entry("void", Node2ObjectMapperUnit.class, "shutdownDB()");
    
    try {
      Node2ObjectMapperUnit.graphDatabaseService.shutdown();
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
