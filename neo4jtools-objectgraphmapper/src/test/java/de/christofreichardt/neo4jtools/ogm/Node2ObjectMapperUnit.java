/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.neo4jtools.ogm.model.Account;
import de.christofreichardt.neo4jtools.ogm.model.Account2;
import de.christofreichardt.neo4jtools.ogm.model.Document;
import de.christofreichardt.neo4jtools.ogm.model.KeyItem;
import de.christofreichardt.neo4jtools.ogm.model.KeyRing;
import de.christofreichardt.neo4jtools.ogm.model.RESTFulCryptoRelationships;
import de.christofreichardt.neo4jtools.ogm.model.RESTfulCryptoLabels;
import de.christofreichardt.neo4jtools.ogm.model.Role;
import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
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
public class Node2ObjectMapperUnit extends BasicMapperUnit {
//  static private GraphDatabaseService graphDatabaseService;
//  final static String DB_PATH = "." + File.separator + "db";
//  final private Properties properties;

  public Node2ObjectMapperUnit(Properties properties) {
    super(properties);
  }
  
//  @BeforeClass
//  static public void startDB() {
//    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
//    tracer.entry("void", Node2ObjectMapperUnit.class, "startDB()");
//    
//    try {
//      Node2ObjectMapperUnit.graphDatabaseService = new GraphDatabaseFactory()
//          .newEmbeddedDatabaseBuilder(DB_PATH)
//          .setConfig(GraphDatabaseSettings.keep_logical_logs, "10 files")
//          .newGraphDatabase();
//      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
//        Schema schema = Node2ObjectMapperUnit.graphDatabaseService.schema();
//        for (IndexDefinition index : schema.getIndexes()) {
//          index.drop();
//        }
//        schema.indexFor(RESTfulCryptoLabels.ACCOUNTS)
//            .on("commonName")
//            .create();
//        schema.indexFor(RESTfulCryptoLabels.DOCUMENTS)
//            .on("id")
//            .create();
//        schema.indexFor(RESTfulCryptoLabels.KEY_RINGS)
//            .on("id")
//            .create();
//        schema.indexFor(RESTfulCryptoLabels.KEY_ITEMS)
//            .on("id")
//            .create();
//        schema.indexFor(RESTfulCryptoLabels.ROLES)
//            .on("id")
//            .create();
//        transaction.success();
//      }
//    }
//    finally {
//      tracer.wayout();
//    }
//  }
//  
//  @Before
//  public void init() {
//    AbstractTracer tracer = getCurrentTracer();
//    tracer.entry("void", this, "init()");
//
//    try {
//      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
//        ResourceIterable<Node> nodes = GlobalGraphOperations.at(Node2ObjectMapperUnit.graphDatabaseService).getAllNodes();
//        nodes.forEach(node -> {
//          tracer.out().printfIndentln("Delete: node[%d].", node.getId());
//          tracer.out().printfIndentln("node.getDegree() = %d", node.getDegree());
//          node.getRelationships().forEach(relationShip -> {
//            tracer.out().printfIndentln("Delete: relationShip[%d].", relationShip.getId());
//            relationShip.delete();
//          });
//          node.delete();
//        });
//        
//        Assert.assertTrue("Expected an empty database.", !GlobalGraphOperations.at(Node2ObjectMapperUnit.graphDatabaseService).getAllNodes().iterator().hasNext());
//        
//        transaction.success();
//      }
//    }
//    finally {
//      tracer.wayout();
//    }
//  }
  
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
      Account2 account = new Account2("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      account.setLastName("Reichardt");
      
      Node node;
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Node2ObjectMapperUnit.graphDatabaseService);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        node = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
      
      Object entity;
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
        entity = node2ObjectMapper.map(RESTFulCryptoRelationships.class);
        
        tracer.out().printfIndentln("entity = %s", entity);
        
        transaction.success();
      }
      
      Assert.assertTrue("Wrong entity class.", entity instanceof Account2);
      
      account = (Account2) entity;
      
      Assert.assertTrue("Wrong user id.", "Tester".equals(account.getUserId()));
      Assert.assertTrue("Wrong country code.", "DE".equals(account.getCountryCode()));
      Assert.assertTrue("Wrong locality.", "Rodgau".equals(account.getLocalityName()));
      Assert.assertTrue("Wrong state.", "Hessen".equals(account.getStateName()));
      Assert.assertTrue("Wrong last name.", "Reichardt".equals(account.getLastName()));
      Assert.assertNull("Expected undefined first name.", account.getFirstName());
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void proxyList() throws MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "proxyList()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account2 account = new Account2("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      account.setLastName("Reichardt");
      List<Document> documents = new ArrayList<>();
      Document document0 = new Document(0L);
      document0.setAccount(account);
      document0.setTitle("Testdocument-1");
      document0.setType("pdf");
      document0.setCreationDate(formattedTime);
      documents.add(document0);
      Document document1 = new Document(1L);
      document1.setAccount(account);
      document1.setTitle("Testdocument-2");
      document1.setType("pdf");
      document1.setCreationDate(formattedTime);
      documents.add(document1);
      account.setDocuments(documents);
      
      Node node;
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Node2ObjectMapperUnit.graphDatabaseService);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        node = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
      
      Object entity;
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
        entity = node2ObjectMapper.map(RESTFulCryptoRelationships.class);
        
        tracer.out().printfIndentln("entity = %s", entity);
        Assert.assertTrue("Wrong entity class.", entity instanceof Account2);

        account = (Account2) entity;

        Assert.assertTrue("Expected a " + ProxyList.class.getSimpleName() + " instance.", account.getDocuments() instanceof ProxyList);
        Assert.assertTrue("Expected a " + ProxyList.class.getSimpleName() + " instance.", account.getRoles() instanceof ProxyList);
        tracer.out().printfIndentln("account.getDocuments().size() = %d", account.getDocuments().size());
        
        transaction.success();
      }
      
      Assert.assertTrue("Expected two documents.", account.getDocuments().size() == 2);
      Assert.assertTrue("Expected the '" + document0 + "'.", account.getDocuments().contains(document0));
      Assert.assertTrue("Expected the '" + document1 + "'.", account.getDocuments().contains(document1));
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void proxyObject() throws MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "proxyObject()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account2 account = new Account2("Tester");
      account.setCountryCode("DE");
      account.setLocalityName("Rodgau");
      account.setStateName("Hessen");
      account.setLastName("Reichardt");
      List<Document> documents = new ArrayList<>();
      Document document0 = new Document(0L);
      document0.setAccount(account);
      document0.setTitle("Testdocument-1");
      document0.setType("pdf");
      document0.setCreationDate(formattedTime);
      documents.add(document0);
      Document document1 = new Document(1L);
      document1.setAccount(account);
      document1.setTitle("Testdocument-2");
      document1.setType("pdf");
      document1.setCreationDate(formattedTime);
      documents.add(document1);
      account.setDocuments(documents);
      
      Node node;
      Object2NodeMapper object2NodeMapper = new Object2NodeMapper(account, Node2ObjectMapperUnit.graphDatabaseService);
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        node = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
      
      Object entity;
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
        entity = node2ObjectMapper.map(RESTFulCryptoRelationships.class);
        
        tracer.out().printfIndentln("entity = %s", entity);
        Assert.assertTrue("Wrong entity class.", entity instanceof Account2);

        account = (Account2) entity;

        Assert.assertTrue("Expected a " + ProxyList.class.getSimpleName() + " instance.", account.getDocuments() instanceof ProxyList);
        Assert.assertTrue("Expected a " + ProxyList.class.getSimpleName() + " instance.", account.getRoles() instanceof ProxyList);
        tracer.out().printfIndentln("account.getDocuments().size() = %d", account.getDocuments().size());
        Assert.assertTrue("Expected two documents.", account.getDocuments().size() == 2);
        
        Document document = account.getDocuments().iterator().next();

        tracer.out().printfIndentln("document.getAccount() = %s", document.getAccount());
        Assert.assertTrue("Expected an Account2 instance.", document.getAccount() instanceof Account2);
        
        transaction.success();
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
  @Test
  public void entityTree() throws MappingInfo.Exception, Object2NodeMapper.Exception, Node2ObjectMapper.Exception {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "entityTree()");
    
    try {
      LocalDateTime localDateTime = IsoChronology.INSTANCE.dateNow().atTime(LocalTime.now());
      String formattedTime = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      
      Account2 superTester = new Account2("Supertester");
      superTester.setCountryCode("DE");
      superTester.setLocalityName("Rodgau");
      superTester.setStateName("Hessen");
      superTester.setLastName("Reichardt");
      List<Document> superTesterDocuments = new ArrayList<>();
      Document document0 = new Document(0L);
      document0.setAccount(superTester);
      document0.setTitle("Testdocument-1");
      document0.setType("pdf");
      document0.setCreationDate(formattedTime);
      superTesterDocuments.add(document0);
      Document document1 = new Document(1L);
      document1.setAccount(superTester);
      document1.setTitle("Testdocument-2");
      document1.setType("pdf");
      document1.setCreationDate(formattedTime);
      superTesterDocuments.add(document1);
      superTester.setDocuments(superTesterDocuments);
      List<Role> superTesterRoles = new ArrayList<>();
      Role adminRole = new Role(0L);
      adminRole.setName("Administrator");
      superTesterRoles.add(adminRole);
      Role userRole = new Role(1L);
      userRole.setName("User");
      superTesterRoles.add(userRole);
      superTester.setRoles(superTesterRoles);
      KeyRing superTesterKeyRing = new KeyRing(0L);
      superTesterKeyRing.setPath("." + File.separator + "store" + File.separator + "theSuperTesterKeystore.jks");
      List<KeyItem> superTesterKeyItems = new ArrayList<>();
      KeyItem keyItem0 = new KeyItem(0L);
      keyItem0.setKeyRing(superTesterKeyRing);
      keyItem0.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem0.setCreationDate(formattedTime);
      superTesterKeyItems.add(keyItem0);
      superTesterKeyRing.setKeyItems(superTesterKeyItems);
      superTester.setKeyRing(superTesterKeyRing);
      Account tester = new Account("Tester");
      tester.setCountryCode("DE");
      tester.setLocalityName("Hainhausen");
      tester.setStateName("Hessen");
      List<Document> testerDocuments = new ArrayList<>();
      Document document2 = new Document(2L);
      document2.setAccount(superTester);
      document2.setTitle("Testdocument-2");
      document2.setType("pdf");
      document2.setCreationDate(formattedTime);
      testerDocuments.add(document2);
      tester.setDocuments(testerDocuments);
      List<Role> testerRoles = new ArrayList<>();
      testerRoles.add(userRole);
      tester.setRoles(testerRoles);
      KeyRing testerKeyRing = new KeyRing(1L); // TODO: provoke a constraint violation by setting the Id to 0L
      testerKeyRing.setPath("." + File.separator + "store" + File.separator + "theTesterKeystore.jks");
      List<KeyItem> testerKeyItems = new ArrayList<>();
      KeyItem keyItem1 = new KeyItem(1L);
      keyItem1.setKeyRing(testerKeyRing);
      keyItem1.setAlgorithm("AES/CBC/PKCS5Padding");
      keyItem1.setCreationDate(formattedTime);
      testerKeyItems.add(keyItem1);
      testerKeyRing.setKeyItems(testerKeyItems);
      tester.setKeyRing(testerKeyRing);
      
      Node superTesterNode;
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Object2NodeMapper object2NodeMapper = new Object2NodeMapper(superTester, Node2ObjectMapperUnit.graphDatabaseService);
        superTesterNode = object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        object2NodeMapper = new Object2NodeMapper(tester, Node2ObjectMapperUnit.graphDatabaseService);
        object2NodeMapper.map(RESTfulCryptoLabels.class, RESTFulCryptoRelationships.class);
        transaction.success();
      }
      traceAllNodes();
      
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Traverser traverser = Node2ObjectMapperUnit.graphDatabaseService
            .traversalDescription()
            .depthFirst()
            .evaluator((Path path) -> {
              tracer.out().printfIndentln("%s", path);
              return Evaluation.INCLUDE_AND_CONTINUE;
            })
            .uniqueness(Uniqueness.NODE_GLOBAL)
            .traverse(superTesterNode);
        
        int nodeCounter = 0;
        ResourceIterator<Node> iter = traverser.nodes().iterator();
        while (iter.hasNext()) {
          iter.next();
          nodeCounter++;
        }
        
        final int EXPECTED_NODES = 11;
        tracer.out().printfIndentln("nodeCounter = %d", nodeCounter);
        Assert.assertTrue("Expected " + EXPECTED_NODES + " nodes.", nodeCounter == EXPECTED_NODES);
        
        transaction.success();
      }
      
      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
        Node node = Node2ObjectMapperUnit.graphDatabaseService.findNode(RESTfulCryptoLabels.ROLES, "id", 0L);
        Node2ObjectMapper node2ObjectMapper = new Node2ObjectMapper(node);
        Object entity = node2ObjectMapper.map(RESTFulCryptoRelationships.class);
        
        RichNode richNode = new RichNode(node);
        richNode.trace(tracer);
        Assert.assertTrue("Expected a Role entity.", entity instanceof Role);
        
        Role role = (Role) entity;
        
        tracer.out().printfIndentln("role.getName() = %s", role.getName());
        Assert.assertTrue("Expected an Administrator role.", Objects.equals("Administrator", role.getName()));
        
        Iterator<Account> iter = role.getAccounts().iterator();
        
        Assert.assertTrue("Expected at least one account.", iter.hasNext());
        
        Account account = iter.next();
        
        tracer.out().printfIndentln("account.getUserId() = %s", account.getUserId());
        Assert.assertTrue("Expected the 'Supertester' account.", Objects.equals("Supertester", account.getUserId()));
        Assert.assertTrue("Expected that Supertester has two documents .", account.getDocuments().size() == 2);
        
        Optional<Role> optionalRole = account.getRoles()
            .stream()
            .filter(theRole -> Objects.equals("User", theRole.getName()))
            .findAny();
        
        tracer.out().printfIndentln("optionalRole = %s", optionalRole);
        Assert.assertTrue("Expected a present role.", optionalRole.isPresent());
        
        role = optionalRole.get();
        
        tracer.out().printfIndentln("role = %s", role);
        Assert.assertTrue("Expected a present role.", Objects.equals("User", role.getName()));
        
        Optional<Account> optionalAccount = role.getAccounts()
            .stream()
            .filter(theAccount -> Objects.equals("Tester", theAccount.getUserId()))
            .findAny();
        
        tracer.out().printfIndentln("optionalAccount = %s", optionalAccount);
        Assert.assertTrue("Expected a present account.", optionalAccount.isPresent());
        
        account = optionalAccount.get();
        
        tracer.out().printfIndentln("account.getUserId() = %s", account.getUserId());
        Assert.assertTrue("Expected the 'Tester' account.", Objects.equals("Tester", account.getUserId()));
        Assert.assertTrue("Expected at least one document.", !account.getDocuments().isEmpty());
        tracer.out().printfIndentln("account.getKeyRing() = %s", account.getKeyRing());
        Assert.assertTrue("Wrong id.", Objects.equals(account.getKeyRing().getId(), testerKeyRing.getId()));
        
        Document document = account.getDocuments().iterator().next();
        
        tracer.out().printfIndentln("document = %s", document);
        Assert.assertTrue("Expected the 'Testdocument-2'.", Objects.equals("Testdocument-2", document.getTitle()));
        tracer.out().printfIndentln("document.getAccount() = %s", document.getAccount());
        Assert.assertTrue("Expected the 'Tester' account.", Objects.equals(account, document.getAccount()));
        
        KeyRing keyRing = account.getKeyRing();
        
        tracer.out().printfIndentln("keyRing.getAccount() = %s", keyRing.getAccount());
        Assert.assertTrue("Expected the 'Tester' account.", Objects.equals(account, keyRing.getAccount()));
        Assert.assertTrue("Expected one key item.", keyRing.getKeyItems().size() == 1);
      }
    }
    finally {
      tracer.wayout();
    }
  }
  
//  private void traceAllNodes() {
//    AbstractTracer tracer = getCurrentTracer();
//    tracer.entry("void", this, "traceAllNodes()");
//    
//    try {
//      try (Transaction transaction = Node2ObjectMapperUnit.graphDatabaseService.beginTx()) {
//        for (RESTfulCryptoLabels label : RESTfulCryptoLabels.values()) {
//          tracer.out().printfIndentln("--> label: %s", label);
//          Node2ObjectMapperUnit.graphDatabaseService.findNodes(label).forEachRemaining(node -> {
//            RichNode richNode = new RichNode(node);
//            richNode.trace(tracer);
//          });
//        }
//        Node2ObjectMapperUnit.graphDatabaseService.findNodes(IdGenLabels.ID_GENERATOR).forEachRemaining(node -> {
//          RichNode richNode = new RichNode(node);
//          richNode.trace(tracer);
//        });
//        transaction.success();
//      }
//    }
//    finally {
//      tracer.wayout();
//    }
//  }
//  
//  @After
//  public void exit() {
//    AbstractTracer tracer = getCurrentTracer();
//    tracer.entry("void", this, "exit()");
//    
//    try {
//    }
//    finally {
//      tracer.wayout();
//    }
//  }
//  
//  @AfterClass
//  static public void shutdownDB() {
//    AbstractTracer tracer = TracerFactory.getInstance().getCurrentPoolTracer();
//    tracer.entry("void", Node2ObjectMapperUnit.class, "shutdownDB()");
//    
//    try {
//      Node2ObjectMapperUnit.graphDatabaseService.shutdown();
//    }
//    finally {
//      tracer.wayout();
//    }
//  }
//
//  @Override
//  public AbstractTracer getCurrentTracer() {
//    return TracerFactory.getInstance().getCurrentPoolTracer();
//  }

}
