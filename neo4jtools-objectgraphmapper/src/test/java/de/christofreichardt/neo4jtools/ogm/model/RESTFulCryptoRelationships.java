/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm.model;

import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author developer
 */
public enum RESTFulCryptoRelationships implements RelationshipType {
  OWNS, FULFILLS, HAS
}
