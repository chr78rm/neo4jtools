/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.christofreichardt.neo4jtools.ogm.model;

import org.neo4j.graphdb.Label;

/**
 *
 * @author developer
 */
public enum RESTfulCryptoLabels implements Label {
  ACCOUNTS, DOCUMENTS, KEY_ITEMS, ROLES, KEY_RINGS, ACCOUNT1S, ACCOUNT2S, ENCRYPTED_DOCUMENTS, PLAINTEXT_DOCUMENTS,
  KEY_ITEM1S, PRIVATE_KEY_ITEMS, PUBLIC_KEY_ITEMS, SECRET_KEY_ITEMS
}
