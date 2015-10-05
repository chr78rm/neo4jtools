package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.KeyItem;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
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
public class MappingInfoUnit implements Traceable {
  static public GraphDatabaseService graphDatabaseService;
  final static String DB_PATH = "." + File.separator + "db";
  final private Properties properties;

  public MappingInfoUnit(Properties properties) {
    this.properties = properties;
  }
  
  @BeforeClass
  static public void startDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", MappingInfoUnit.class, "startDB()");
    
    try {
      MappingInfoUnit.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
      try (Transaction transaction = MappingInfoUnit.graphDatabaseService.beginTx()) {
        Schema schema = MappingInfoUnit.graphDatabaseService.schema();
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
      try (Transaction transaction = MappingInfoUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(MappingInfoUnit.graphDatabaseService).getAllNodes();
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
  public void noArgsConstructor() throws MappingInfo.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "noArgsConstructor()");
    
    try {
      MappingInfo mappingInfo = new MappingInfo();
      
      tracer.out().println();
      
      Set<Class<?>> mappedEntities = mappingInfo.getMappedEntities();
      mappedEntities.forEach(mappedEntity -> {
        tracer.out().printfIndentln("--> mappedEntity = %s", mappedEntity.getName());
        tracer.out().printfIndentln("index = %s", mappingInfo.getPrimaryIndexName(mappedEntity));
        String idFieldName = mappingInfo.getIdFieldName(mappedEntity);
        tracer.out().printfIndentln("idField = %s", idFieldName);
        tracer.out().printfIndentln("idProperty = %s", mappingInfo.getPropertyMappingForField(idFieldName, mappedEntity));
        
        Set<Map.Entry<String, PropertyData>> propertyMappings = mappingInfo.getPropertyMappings(mappedEntity);
        propertyMappings.forEach(propertyMapping -> {
          tracer.out().printfIndentln("%s: %s", propertyMapping.getKey(), propertyMapping.getValue());
        });
        
        Set<Map.Entry<String, LinkData>> linkMappings = mappingInfo.getLinkMappings(mappedEntity);
        linkMappings.forEach(linkMapping -> {
          tracer.out().printfIndentln("%s: %s", linkMapping.getKey(), linkMapping.getValue());
        });
        
        Set<Map.Entry<String, SingleLinkData>> singleLinkMappings = mappingInfo.getSingleLinkMappings(mappedEntity);
        singleLinkMappings.forEach(singleLinkMapping -> {
          tracer.out().printfIndentln("%s: %s", singleLinkMapping.getKey(), singleLinkMapping.getValue());
        });
        
        tracer.out().println();
      });
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
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, MappingInfoUnit.graphDatabaseService);
      try (Transaction transaction = MappingInfoUnit.graphDatabaseService.beginTx()) {
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
      
      Account account = new Account("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      List<KeyItem> keyItems = new ArrayList<>();
      KeyItem keyItem = new KeyItem(0);
      keyItem.setAccount(account);
      keyItem.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem.setCreationDate(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      keyItems.add(keyItem);
      account.setKeyItems(keyItems);
      
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, MappingInfoUnit.graphDatabaseService);
      try (Transaction transaction = MappingInfoUnit.graphDatabaseService.beginTx()) {
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
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
      try (Transaction transaction = MappingInfoUnit.graphDatabaseService.beginTx()) {
        ResourceIterable<Node> nodes = GlobalGraphOperations.at(MappingInfoUnit.graphDatabaseService).getAllNodes();
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
  
  @AfterClass
  static public void shutdownDB() {
    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
    tracer.entry("void", MappingInfoUnit.class, "shutdownDB()");
    
    try {
      MappingInfoUnit.graphDatabaseService.shutdown();
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
