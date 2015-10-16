/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
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
  static public GraphDatabaseService graphDatabaseService;
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
  public void coverFields() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "coverFields()");
    
    try {
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
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
