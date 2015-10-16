# neo4jtools (work in progress)

A lightweight framework for working with embedded neo4j graph database instances. The main focus lies on object-graph-mapping
routines. Contrary to [neo4j-ogm](https://github.com/neo4j/neo4j-ogm) neo4jtools doesn't operate "over the wire". It loads and
saves objects directly from and to [GraphDatabaseService](http://neo4j.com/docs/2.2.6/javadocs/org/neo4j/graphdb/GraphDatabaseService.html)
instances based upon mapping annotations on entity classes. Whole object graphs can be (lazily) retrieved by an
`Iterable<T>` given by mapping through the results of a [traversal](http://neo4j.com/docs/2.2.6/tutorial-traversal-concepts.html).
That is, the Cypher Query Language won't be needed for this.

[Maven](https://maven.apache.org/) is required to compile the library. Use

`$ mvn clean install`

to build the library along with the unit tests.
