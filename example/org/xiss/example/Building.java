package org.xiss.example;

import org.xiss.XML;

public class Building {
  public static void main(String[] args) {
    XML.Doc doc = XML.doc(
        XML.comment("This is the structure for a person"),
        XML.e("person",
            XML.e("first-name", "Mike"),
            XML.e("last-name", "Schrag"),
            XML.e("addresses",
                XML.e("address",
                    XML.a("location", "Home"),
                    XML.e("address", "100 Main St."),
                    XML.e("city", "Richmond"),
                    XML.e("state", "VA"),
                    XML.e("zip", "23233")
                ),
                XML.cdata("This is a cdata section! <test> of cdata!")
            )
        )
    );
    System.out.println(doc);
  }
}
