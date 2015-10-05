package de.christofreichardt.neo4jtools.idgen;

import de.christofreichardt.junit.MySuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Christof Reichardt
 */
@RunWith(MySuite.class)
@Suite.SuiteClasses({
  IdGeneratorUnit.class,
})
public class IdGeneratorSuite {
}
