package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Reichardt
 */
public class MappingInfoUnit implements Traceable {
  final private Properties properties;

  public MappingInfoUnit(Properties properties) {
    this.properties = properties;
  }
  
  @Before
  public void init() {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("void", this, "init()");

    try {
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
      
      Set<Class<?>> mappedEntities = mappingInfo.getMappedEntityClasses();
      mappedEntities.forEach(mappedEntity -> {
        tracer.out().printfIndentln("--> mappedEntity = %s", mappedEntity.getName());
        tracer.out().printfIndentln("topLabel = %s", mappingInfo.getPrimaryKeyMapping(mappedEntity).getLabel());
        tracer.out().printIndent("labels: ");
        Iterator<String> iter = mappingInfo.getLabels(mappedEntity).iterator();
        while (iter.hasNext()) {
          tracer.out().print(iter.next());
          if (iter.hasNext())
            tracer.out().print(", ");
        }
        tracer.out().println();
        String idFieldName = mappingInfo.getPrimaryKeyMapping(mappedEntity).getFieldName();
        boolean generated = mappingInfo.getPrimaryKeyMapping(mappedEntity).isGenerated();
        tracer.out().printfIndentln("idField = %s, generated = %b", idFieldName, generated);
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
  
  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getCurrentPoolTracer();
  }

}
