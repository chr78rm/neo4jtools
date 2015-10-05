/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;

/**
 *
 * @author Developer
 */
public class MyClass implements Traceable {
  final private Class<?> clazz;

  public MyClass(Class<?> clazz) {
    this.clazz = clazz;
  }
  
  public boolean isSubClassOf(Class<?> superClass) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("boolean", this, "sSubClassOf(Class<?> superClass)");

    try {
      tracer.out().printfIndentln("this.field.getType().getName() = %s", this.clazz.getName());
      tracer.out().printfIndentln("superClass.getName() = %s", superClass.getName());
      
      boolean flag = false;
      Class<?> currentClazz = this.clazz;
      do {
        tracer.out().printfIndentln("currentClazz.getName() = %s", currentClazz.getName());
        
        if (currentClazz == superClass) {
          flag = true;
          break;
        }
        currentClazz = currentClazz.getSuperclass();
      } while (currentClazz != null);
      
      return flag;
    }
    finally {
      tracer.wayout();
    }
  }
  
  public boolean isImplementing(Class<?> anInterface) {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("boolean", this, "isImplementing(Class<?> anInterface)");

    try {
      tracer.out().printfIndentln("this.field.getType().getName() = %s", this.clazz.getName());
      tracer.out().printfIndentln("anInterface.getName() = %s", anInterface.getName());
      
      boolean flag = false;
      for (Class<?> clazz : this.clazz.getInterfaces()) {
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
      
      return flag;
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
