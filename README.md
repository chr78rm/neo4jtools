# neo4jtools (work in progress)

A lightweight framework for working with embedded [Neo4j](http://neo4j.com/) graph database instances. The main focus lies on object-graph-mapping
routines. Contrary to [neo4j-ogm](https://github.com/neo4j/neo4j-ogm) neo4jtools doesn't operate "over the wire". It loads and
saves objects directly from and to [GraphDatabaseService](http://neo4j.com/docs/2.3.1/javadocs/org/neo4j/graphdb/GraphDatabaseService.html)
instances based upon mapping annotations on entity classes. Whole (uniformly typed) object graphs can be (lazily) retrieved by an
`Iterable<T>` given by mapping through the results of a [traversal](http://neo4j.com/docs/stable/tutorial-traversal.html).
That is, the Cypher Query Language won't be needed for this.

[Maven](https://maven.apache.org/) is required to compile the library. Use

`$ mvn clean install`

to build the library along with the unit tests.

# Modelling entities with annotations

To illustrate the provided basic object-graph-mapping facilities, I'll make use of a simple domain model consisting of Accounts, Roles,
Keyrings, Keyitems and Documents. A particular Account may fulfill multiple Roles. A certain Role can be fulfilled by multiple Accounts, 
that is we have a many-to-many relationship between Accounts and Roles. An Account might own a Keyring whereas a Keyring always belongs 
to an Account. A Keyring may contain some Keyitems. Furthermore an Account has Documents. Documents are divided into Plaintext Documents
and Encrypted Documents. See the UML class diagram below:

![uml-class-diagram](graphics/uml-class-diagram.png?raw=true "Simple Domain Model")

## Mapping the identity

Neo4j 2.x has added (optional) schema support. Neo4jtools uses some of these features to map the application managed object identity. 
The index definition below

```java
GraphDatabaseService graphDatabaseService = new GraphDatabaseFactory()
    .newEmbeddedDatabaseBuilder(DB_PATH)
    .newGraphDatabase();
try (Transaction transaction = graphDatabaseService.beginTx()) {
  Schema schema = graphDatabaseService.schema();
  schema.indexFor(MyLabels.ACCOUNTS)
      .on("commonName")
      .create();
  transaction.success();
}
```

creates an index for Account nodes labelled with `MyLabels.ACCOUNTS` on the property `commonName`. Entity classes must be annotated with 
the `NodeEntity` annotation which calls for a label associated with the corresponding entity. Since the domain specific labels cannot be 
known beforehand, [Label](http://neo4j.com/docs/2.2.6/javadocs/org/neo4j/graphdb/Label.html)s must be provided by a String literal. Now 
the following definition of the Account entity class 

```java
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id @Property(name = "commonName") private String userId;
...
}
```

maps the String field `userId` onto the mentioned property `commonName`. Assuming an object identity given by a `Long` field, ids can
be generated automatically by a service:

```java
@NodeEntity(label = "DOCUMENTS")
public class Document {
  @Id @Property @GeneratedValue private Long id;
...
}
```

## Mapping properties

Fields annotated with `Property` are mapped on corresponding node properties. Since Neo4j supports only primitives like `int`, `long` together
with `java.lang.String` as value types, it is an error to place a `Property` annotation onto a complex type (like e.g. `java.math.BigInteger`).
By default a field annotated with `Property` is considered as non-nullable. An example for a nullable field is shown below:

```java
@NodeEntity(label = "KEY_RINGS")
public class KeyRing {
...
  @Property(nullable = true) private String password;
...
}
```

Properties are key-value pairs. By default a field will be mapped on the property given by taking the field name as key. If this is
inappropriate, the desired key must be explicitly provided, see the example below:

```java
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id @Property(name = "commonName") private String userId;
...
}
```

## Mapping relationships

Neo4jtools provides two different annotations to model relationships: `Links` and `SingleLink`. With combinations of these annotations someone
is able to map one-to-many, many-to-many and one-to-one relationships.

### one-to-many

An example for a one-to-many relationship is the relationship between a Keyring and its Keyitems. From a perspective of a graph database, 
a Keyring node might have zero, one or multiple (directed) `contains` edges leading to Keyitem nodes. On the other hand every Keyitem
node exhibits exactly one `contains` edge incoming from a Keyring node. The Keyring entity class makes use of the `Links` annotation whereas 
the Keyitem entity class utilizes the `SingleLink`, see the example below:

```java
@NodeEntity(label = "KEY_RINGS")
public class KeyRing {
...
  @Links(direction = Direction.OUTGOING, type = "CONTAINS")
  private Collection<KeyItem> keyItems;
...
}
```

The data type of a field annotated with a `SingleLink` is `Cell<?>`. This is a container which is able to hold a single entity. There is a reason for
not using directly entity types: Consider the loading of an entity instance from the graph database. All fields annotated with
`Links` and `SingleLink`s will initially preset with proxy objects. Otherwise such a load might resolve the whole database.

```java
@NodeEntity(label = "KEY_ITEMS")
public class KeyItem {
...
  @SingleLink(direction = Direction.INCOMING, type = "CONTAINS")
  private Cell<KeyRing> keyRing;
...
  public KeyRing getKeyRing() {
    return this.keyRing != null ? this.keyRing.getEntity() : null;
  }
  public void setKeyRing(KeyRing keyRing) {
    this.keyRing = new Wrapper<>(keyRing);
  }
...
}
```

### many-to-many

Many-to-many relationships are modelled with `Links` annotations on both sides. Indeed an Account node might have multiple `FULFILLS`
edges leading to various Role nodes whereas a certain Role node might has multiple incoming `FULFILLS` edges from different Account nodes:

```java
@NodeEntity(label = "ACCOUNTS")
public class Account {
...  
  @Links(direction = Direction.OUTGOING, type = "FULFILLS")
  private Collection<Role> roles;
}
```

```java
@NodeEntity(label = "ROLES")
public class Role {
...
  @Links(direction = Direction.INCOMING, type = "FULFILLS")
  private Collection<Account> accounts;
...
}
```

### one-to-one

As one might expect, one-to-one relationships are modelled with `SingleLink`s on both sides. An example for a one-to-one 
relationship is the association between an Account and its Keyring. Whereas an Account didn't need necessarily a Keyring,
a Keyring comes only together with an Account. That is, the `SingleLink` on the Account side is nullable:

```java
@NodeEntity(label = "ACCOUNTS")
public class Account {
...  
  @SingleLink(direction = Direction.OUTGOING, type = "OWNS", nullable = true)
  private Cell<KeyRing> keyRing;
...
}
```

```java
@NodeEntity(label = "KEY_RINGS")
public class KeyRing {
...
  @SingleLink(direction = Direction.INCOMING, type = "OWNS")
  private Cell<Account> account;
...
}
```

# The ObjectGraphMapper

The `ObjectGraphMapper` API is the main entry point for managing entity instances, such as loading an entity (graph) from the
database or saving it. You need to provide a [GraphDatabaseService](http://neo4j.com/docs/2.3.1/javadocs/org/neo4j/graphdb/GraphDatabaseService.html)
instance and [Enum](http://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html) implementation types of
[Label](http://neo4j.com/docs/2.3.1/javadocs/org/neo4j/graphdb/Label.html)s and 
[RelationshipType](http://neo4j.com/docs/2.3.1/javadocs/org/neo4j/graphdb/RelationshipType.html)s to create an `ObjectGraphMapper`
instance. The `Label`s and `RelationshipType`s are part of your (graph) database schema whereas the `GraphDatabaseService` 
constitutes your database instance. The complete generic type definition of the `ObjectGraphMapper` is

<p align="center">ObjectGraphMapper&lt;S extends Enum&lt;S&gt; & Label, T extends Enum&lt;T&gt; & RelationshipType&gt;</p>

The string representations of your enumerations must match the string literals used within your mapping definitions.

## Saving an entity (graph)

In principle, every mapped entity may serve as starting point for persisting an entity (graph) to the database. Initally, the `ObjectGraphMapper`
will inspect the annotated id field to decide if there is a matching node within the database. If so, the matching node will be
fetched for a merging operation or otherwise a new node will be created. In the latter case the required labels will be added to the just created node.
In the event of a merging operation and if the entity has an annotated version field the `ObjectGraphMapper` will check the entity for staleness and 
will cancel the operation if necessary. Next, the mapped properties will be processed. Missing non-nullable properties will cause a failure. Subsequently, 
all outgoing links (`SingleLink` and `Links`) will be inspected. There are two possibilities: Either the corresponding fields are occupied by
proxies or not. In the former case the given entity has been previously loaded from the database and its links has been preset with
proxies. Again there are two possibilities. The load of the referenced entities has been triggered or not. In the former case
every loaded entity must be recursively processed by the `ObjectGraphMapper` as it is the case if we have accessed the 'real'
entities, e.g. an `ArrayList`  of entities. A link preset with an unloaded proxy will be ignored. If a link must be processed and in the event of a 
merging operation all the corresponding relationships of the given node consistent with the link definition will be deleted at first, since we are 
assuming that those relationships will be refreshed by the given entity.

Simply call `Node save(Object entity)` on an `ObjectGraphMapper` instance to persist an entity (graph). Below are given some examples.

### Saving a single entity

```java
GraphDatabaseService graphDatabaseService = ...
Account account = new Account("Tester");
account.setCountryCode("DE");
account.setLocalityName("Rodgau");
account.setStateName("Hessen");
ObjectGraphMapper<MyLabels, MyRelationships> objectGraphMapper = 
    new ObjectGraphMapper<>(graphDatabaseService, MyLabels.class, MyRelationships.class);
try (Transaction transaction = graphDatabaseService.beginTx()) {
  Node node = objectGraphMapper.save(account);
  transaction.success();
}
```

### Saving an entity graph

Note that only outgoing links will be (recursively) processed.

```java
GraphDatabaseService graphDatabaseService = ...
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
ObjectGraphMapper<MyLabels, MyRelationships> objectGraphMapper = 
    new ObjectGraphMapper<>(graphDatabaseService, MyLabels.class, MyRelationships.class);
try (Transaction transaction = graphDatabaseService.beginTx()) {
  node = objectGraphMapper.save(account);
  transaction.success();
}
 ```

### SingleLink constraint violations

Basically, there are two ways to violate a SingleLink constraint. First, you fail to add a certain single link between two nodes (entities) but that
link has been marked as non-nullable (which is the default). Or, you might try to add a second relationship (or rather link) between two nodes
but that relationship type had been mapped as SingleLink. In the latter case, the `ObjectGraphMapper` implements a fail-fast behaviour, that is such
errors will be detected during a save operation, see the subsequent example:

```java
GraphDatabaseService graphDatabaseService = ...
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
ObjectGraphMapper<MyLabels, MyRelationships> objectGraphMapper = 
    new ObjectGraphMapper<>(graphDatabaseService, MyLabels.class, MyRelationships.class);
try (Transaction transaction = graphDatabaseService.beginTx()) {
  objectGraphMapper.save(superTester);
  objectGraphMapper.save(tester);
transaction.success();
}
```

In the example above, the same Keyring is added to different Account entities which is easy enough but that would result in two incoming `OWNS` links on the
KeyRing node and that has been ruled out by the mapping definitions. This will raise an exception and as a consequence the transaction will be rolled back.

On the other hand non-nullable SingleLink violations won't be detected during a save operation at present but will raise an appropriate exception when trying to access
an entity via a missing link after a load operation. This is due to the fact that these kind of errors can't be detected in the first run but will require a second pass. Unsatisfied
links might occure deep in the recursion at any time but they might be resolved later on when revisiting nodes that have been saved already. 

### Non-nullable properties

By default mapped properties are non-nullable. The `ObjectGraphMapper` enforces this by a fail-fast behaviour. Given the mapping definitions below

```java
@NodeEntity(label = "ACCOUNTS")
public class Account {
  @Id @Property(name = "commonName") String userId;
  @Property String localityName;
  @Property String stateName;
  @Property String countryCode;
...
}
```

the subsequent example will raise an exception and therefore the transaction will be rolled back:

```java
GraphDatabaseService graphDatabaseService = ...
Account account = new Account("Tester");
account.setCountryCode("DE");
account.setLocalityName("Rodgau");
ObjectGraphMapper<MyLabels, MyRelationships> objectGraphMapper = 
    new ObjectGraphMapper<>(graphDatabaseService, MyLabels.class, MyRelationships.class);
try (Transaction transaction = graphDatabaseService.beginTx()) {
  objectGraphMapper.save(account);
  transaction.success();
}
```

### Automatic IDs

The `ObjectGraphMapper` can provide automatically IDs for the appropriate annotated properties by relying on a background service.
Obviously, without this service a missing ID would lead to a failure when saving entities. For every entity that participates
in the service a corresponding database node will be managed. A certain property on this node will serve as high-water mark for IDs. During startup
of the service the high-water mark on the corresponding nodes will be evaluated and the service will provide a buffer of IDs counting from
the high-water mark. During shutdown of the service new high-water marks will be written on these nodes. The subsequent example uses this feature for
Document and KeyRing entities:

```java
GraphDatabaseService graphDatabaseService = ...
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
  ObjectGraphMapper<MyLabels, MyRelationships> objectGraphMapper = 
      new ObjectGraphMapper<>(graphDatabaseService, MyLabels.class, MyRelationships.class);
  try (Transaction transaction = graphDatabaseService.beginTx()) {
    objectGraphMapper.save(account);
    transaction.success();
  }
}
finally {
  IdGeneratorService.getInstance().shutDown();
}
```
