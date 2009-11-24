package org.xiss.example;

import org.xiss.XML;

public class Printing {
  public static void main(String[] args) {
    XML.Doc doc = XML.doc();
    doc.comment("This is the structure for a person");
    XML.E person = doc.root("person").set("firstName", "Mike");

    System.out.println(person);

    System.out.println(doc);
  }
}
