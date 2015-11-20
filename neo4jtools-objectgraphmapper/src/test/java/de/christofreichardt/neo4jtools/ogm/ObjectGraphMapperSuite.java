package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.junit.MySuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Christof Reichardt
 */
@RunWith(MySuite.class)
@Suite.SuiteClasses({
  AnnotationProcessorUnit.class,
  MappingInfoUnit.class,
  Object2NodeMapperUnit.class,
  Node2ObjectMapperUnit.class,
  ObjectGraphMapperUnit.class,
  ExamplesUnit.class
})
public class ObjectGraphMapperSuite {

}
