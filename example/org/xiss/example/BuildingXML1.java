package org.xiss.example;

import org.xiss.XML;

public class BuildingXML1 {
  public static void main(String[] args) {
    XML.Doc doc = XML.doc();
    doc.comment("This is the structure for a person");
    XML.E person = doc.root("person");
    {
      person.e("first-name", "Bob");
      person.e("last-name").setText("Jones");
      person.comment("This is the structure for addresses");
      XML.E addresses = person.e("addresses");
      {
        XML.E homeAddress = addresses.e("address").set("location", "Home");
        {
          homeAddress.e("address", "100 Main St");
          homeAddress.e("city", "Richmond");
          homeAddress.e("state", "VA");
          homeAddress.e("zip", "23233");
        }
        addresses.cdata("This is a cdata section! <test> of cdata!");
      }
    }
    System.out.println(doc);
  }
}
