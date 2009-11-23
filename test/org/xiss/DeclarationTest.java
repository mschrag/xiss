package org.xiss;
import junit.framework.TestCase;

import org.xiss.XML;

public class DeclarationTest extends TestCase {
  public void testWrite() {
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", new XML.Declaration("1.0", "UTF-8").toString());
    assertEquals("<?xml version=\"1.0\"?>\n", new XML.Declaration("1.0", null).toString());
    assertEquals("<?xml?>\n", new XML.Declaration(null, null).toString());
  }
}
