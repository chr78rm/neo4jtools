# neo4jtools (work in progress)

A lightweight framework for working with embedded neo4j graph database instances. The main focus lies on object-graph-mapping
routines. Contrary to [neo4j-ogm](https://github.com/neo4j/neo4j-ogm) neo4jtools doesn't operate "over the wire". It loads and
saves objects directly from and to [GraphDatabaseService](http://neo4j.com/docs/2.2.6/javadocs/org/neo4j/graphdb/GraphDatabaseService.html)
instances based upon mapping annotations on entity classes. Whole (uniformly typed) object graphs can be (lazily) retrieved by an
`Iterable<T>` given by mapping through the results of a [traversal](http://neo4j.com/docs/2.2.6/tutorial-traversal-concepts.html).
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
  @Id
  @Property(name = "commonName")
  private String userId;
...
```

maps the String field `userId` onto the mentioned property `commonName`. Assuming an object identity given by a `Long` field, ids can
be generated automatically by a service:

```java
@NodeEntity(label = "DOCUMENTS")
public class Document {
  @Id
  @Property
  @GeneratedValue
  private Long id;
...
```
