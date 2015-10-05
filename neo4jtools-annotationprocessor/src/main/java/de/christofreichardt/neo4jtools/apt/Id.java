/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Developer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
  String label();
}
