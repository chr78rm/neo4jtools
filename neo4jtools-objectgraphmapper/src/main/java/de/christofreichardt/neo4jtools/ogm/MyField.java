/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.NullTracer;
import de.christofreichardt.diagnosis.TracerFactory;
import java.lang.reflect.Field;

/**
 *
 * @author Developer
 */
public class MyField {
  final private Field field;
  final private AbstractTracer nullTracer = new NullTracer();

  public MyField(Field field) {
    this.field = field;
  }
  
  public boolean typeIsSubClassOf(Class<?> superClass) {
    AbstractTracer tracer = getTracer();
    tracer.entry("boolean", this, "typeIsSubClassOf(Class<?> superClass)");

    try {
      tracer.out().printfIndentln("this.field.getType().getName() = %s", this.field.getType().getName());
      tracer.out().printfIndentln("superClass.getName() = %s", superClass.getName());
      
      boolean flag = false;
      Class<?> clazz = this.field.getType();
      do {
        tracer.out().printfIndentln("clazz.getName() = %s", clazz.getName());
        
        if (clazz == superClass) {
          flag = true;
          break;
        }
        clazz = clazz.getSuperclass();
      } while (clazz != null);
      
      return flag;
    }
    finally {
      tracer.wayout();
    }
  }
  
  public boolean typeImplements(Class<?> anInterface) {
    AbstractTracer tracer = getTracer();
    tracer.entry("boolean", this, "typeImplements(Class<?> anInterface)");

    try {
      if (!anInterface.isInterface())
        throw new IllegalArgumentException("'" + anInterface.getSimpleName() + "' isn't an interface.");
      
      tracer.out().printfIndentln("this.field.getType().getName() = %s", this.field.getType().getName());
      tracer.out().printfIndentln("anInterface.getName() = %s", anInterface.getName());
      
      boolean flag = this.field.getType() == anInterface;
      if (!flag) {
        for (Class<?> clazz : this.field.getType().getInterfaces()) {
          tracer.out().printfIndentln("clazz.getName() = %s", clazz.getName());

          if (clazz == anInterface) {
            flag = true;
            break;
          }
          else {
            MyClass myClass = new MyClass(clazz);
            if (myClass.isImplementing(anInterface)) {
              flag = true;
              break;
            }
          }
        }
      }
      
      return flag;
    }
    finally {
      tracer.wayout();
    }
  }
  
  private AbstractTracer getTracer() {
    return this.nullTracer;
  }
}
