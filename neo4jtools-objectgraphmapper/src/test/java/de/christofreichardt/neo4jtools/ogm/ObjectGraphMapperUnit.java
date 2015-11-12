/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import java.util.Properties;
import org.junit.Test;

/**
 *
 * @author Christof Reichardt
 */
public class ObjectGraphMapperUnit extends BasicMapperUnit {
  public ObjectGraphMapperUnit(Properties properties) {
    super(properties);
  }
  
  @Test
  public void dummy() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "dummy()");
    
    try {
    }
    finally {
      tracer.wayout();
    }
  }
}
