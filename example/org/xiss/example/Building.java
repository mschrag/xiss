package org.xiss.example;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xiss.XML;

public class Building {
  public static void main(String[] args) throws TransformerFactoryConfigurationError, TransformerException {
    XML.Doc doc = XML.doc(
        XML.comment("This is the structure for a person"),
        XML.e("person",
            XML.e("first-name", "Mike"),
            XML.e("last-name", "Schrag"),
            XML.e("addresses",
                XML.e("address",
                    XML.a("location", "Home"),
                    XML.e("address", "100 Main St."),
                    XML.e("city", "Richmond")
                )
            )
        )
    );
    System.out.println(doc);
  }
}
