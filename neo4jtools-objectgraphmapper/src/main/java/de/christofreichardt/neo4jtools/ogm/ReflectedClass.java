/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author Developer
 */
public class ReflectedClass implements Traceable {
  final private Class<?> clazz;

  public ReflectedClass(Class<?> clazz) {
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
          ReflectedClass myClass = new ReflectedClass(clazz);
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
  
  public Field getDeclaredField(String fieldName) throws NoSuchFieldException {
    AbstractTracer tracer = getCurrentTracer();
    tracer.entry("Field", this, "getDeclaredField(String fieldName)");

    try {
      tracer.out().printfIndentln("fieldName = %s", fieldName);
      
      Class<?> currentClazz = this.clazz;
      Field field = null;
      do {
        Field[] declaredFields = currentClazz.getDeclaredFields();
        Optional<Field> optionalField = Arrays.asList(declaredFields).stream().filter(declaredField -> declaredField.getName().equals(fieldName)).findFirst();
        if (optionalField.isPresent()) {
          field = optionalField.get();
          break;
        }
        currentClazz = currentClazz.getSuperclass();
      } while(currentClazz != null);
      
      if (field == null)
        throw new NoSuchFieldException(fieldName);
      
      return field;
    }
    finally {
      tracer.wayout();
    }
  }

  @Override
  public AbstractTracer getCurrentTracer() {
    return TracerFactory.getInstance().getDefaultTracer();
  }
}
