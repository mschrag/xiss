package org.xiss.example;

import org.xiss.XML;

public class Parsing {
  public static void main(String[] args) {
    XML.Doc doc = XML.doc("<person><first-name>Mike</first-name><last-name>Schrag</last-name><addresses><address location=\"home\"><address>100 Main St</address><city>Richmond</city></address></addresses></person>");
    System.out.println(doc);

    org.w3c.dom.Document w3cDoc = doc.w3c();
    XML.Doc doc2 = XML.doc(w3cDoc);
    System.out.println(doc2);
  }
}
