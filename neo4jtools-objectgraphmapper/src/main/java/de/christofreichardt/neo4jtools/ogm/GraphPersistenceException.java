/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm;

/**
 *
 * @author Developer
 */
public class GraphPersistenceException extends Exception {

  public GraphPersistenceException(String message) {
    super(message);
  }

  public GraphPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
