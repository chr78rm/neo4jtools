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
  MappingInfoUnit.class
})
public class ObjectGraphMapperSuite {

}
