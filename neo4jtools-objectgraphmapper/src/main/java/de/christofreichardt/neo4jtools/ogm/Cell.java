/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

/**
 *
 * @author Developer
 */
public interface Cell<T> {
  T getEntity();
  boolean isLoaded();
}
