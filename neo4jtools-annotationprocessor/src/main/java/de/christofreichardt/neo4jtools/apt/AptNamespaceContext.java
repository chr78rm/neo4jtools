/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.apt;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author Christof Reichardt
 */
public class AptNamespaceContext implements NamespaceContext {

  @Override
  public String getNamespaceURI(String prefix) {
    return "http://www.christofreichardt.de/neo4jtools/apt";
  }

  @Override
  public String getPrefix(String namespaceURI) {
    return XMLConstants.DEFAULT_NS_PREFIX;
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    throw new UnsupportedOperationException("Not needed yet.");
  }
}
