package org.xiss;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xiss.XML.Item;
import org.xiss.XML.Visitor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocTest extends TestCase {

  public void testBlank() {
    XML.Doc doc = XML.doc();
    assertNull(doc.root());
    assertNotNull(doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
  }

  public void testDeclaration() {
    XML.Doc doc = XML.doc();
    assertNotNull(doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));

    doc.setDeclaration(null);
    assertNull(doc.declaration());
    assertEquals(0, doc.children().size());

    XML.Declaration declaration = XML.declaration("1.0", "UTF-8");
    doc.setDeclaration(declaration);
    assertNotNull(doc.declaration());
    assertEquals(declaration, doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(declaration, doc.children().get(0));

    doc.setDeclaration(null);
    doc.root("root");
    XML.Declaration declaration2 = XML.declaration("1.0", "UTF-8");
    doc.setDeclaration(declaration2);
    assertNotNull(doc.declaration());
    assertEquals(declaration2, doc.declaration());
    assertEquals(2, doc.children().size());
    assertEquals(declaration2, doc.children().get(0));
    assertEquals(doc.root(), doc.children().get(1));

    XML.Declaration declaration3 = XML.declaration("1.0", "UTF-8");
    doc.setDeclaration(declaration3);
    assertNotNull(doc.declaration());
    assertEquals(declaration3, doc.declaration());
    assertEquals(2, doc.children().size());
    assertEquals(declaration3, doc.children().get(0));
    assertEquals(doc.root(), doc.children().get(1));

    doc.setDeclaration(null);
    assertNull(doc.declaration());
    assertEquals(null, doc.declaration());
    assertEquals(1, doc.children().size());
    assertEquals(doc.root(), doc.children().get(0));
  }

  public void testRoot() {
    XML.Doc doc = XML.doc();
    assertNull(doc.root());
    XML.E root = doc.root("test");
    assertEquals(root, doc.root());
    assertEquals(2, doc.children().size());
    assertEquals(root, doc.children().get(1));
    try {
      doc.root("test2");
      throw new AssertionFailedError("should have failed");
    }
    catch (IllegalStateException e) {
      // EXPECTED
    }
  }

  public void testSetRoot() {
    XML.Doc doc = XML.doc();
    XML.E root = XML.e("test");
    doc.setRoot(root);
    assertEquals("test", root.name());
    assertEquals(root, doc.root());
    assertEquals(2, doc.children().size());

    XML.E root2 = XML.e("test2");
    doc.setRoot(root2);
    assertEquals(root2, doc.root());
    assertEquals(2, doc.children().size());
    assertEquals(root2, doc.children().get(1));

    doc.setRoot(null);
    assertEquals(null, doc.root());
    assertEquals(1, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
  }

  public void testComment() {
    XML.Doc doc = XML.doc();
    doc.comment("This is a comment");
    assertEquals(2, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals(XML.Comment.class, doc.children().get(1).getClass());
    assertEquals("This is a comment", ((XML.Comment) doc.children().get(1)).text());
  }

  public void testAdd() {
    XML.Doc doc = XML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");

    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals("comment1", ((XML.Comment) doc.children().get(1)).text());
    assertEquals("comment2", ((XML.Comment) doc.children().get(2)).text());
    assertEquals("comment3", ((XML.Comment) doc.children().get(3)).text());
  }

  public void testRemove() {
    XML.Doc doc = XML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");

    assertEquals(4, doc.children().size());
    XML.Comment comment = (XML.Comment) doc.children().get(2);
    assertEquals("comment2", comment.text());

    doc.remove(comment);
    assertEquals(3, doc.children().size());
    assertEquals(doc.declaration(), doc.children().get(0));
    assertEquals("comment1", ((XML.Comment) doc.children().get(1)).text());
    assertEquals("comment3", ((XML.Comment) doc.children().get(2)).text());
  }

  public void testChildren() {
    XML.Doc doc = XML.doc();
    doc.comment("comment1");
    doc.comment("comment2");
    doc.comment("comment3");
    assertNotNull(doc.children());
    assertEquals(4, doc.children().size());
  }

  public void testWrite() {
    XML.Doc doc = XML.doc();
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", doc.toString());

    doc.comment("comment1");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- comment1 -->\n", doc.toString());

    doc.root("parent");
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- comment1 -->\n<parent />\n", doc.toString());
  }

  public void testVisit() {
    XML.Doc doc = XML.doc();
    doc.comment("comment1");
    doc.root("parent");

    final Set<XML.Item> shouldVisit = new HashSet<XML.Item>();
    shouldVisit.add(doc);
    shouldVisit.addAll(doc.children());
    final Set<XML.Item> visited = new HashSet<XML.Item>();
    doc.visit(new Visitor() {
      public boolean visit(Item item) {
        visited.add(item);
        return true;
      }
    });
    assertEquals(shouldVisit, visited);
  }

  public void testW3C() throws SAXException, IOException, ParserConfigurationException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<person><first-name>Mike</first-name><last-name>Schrag</last-name><addresses><address location=\"home\"><address>100 Main St</address><city>Richmond</city></address></addresses></person>")));
    XML.Doc doc = XML.doc(document);
    assertEquals("person", doc.root().name());
    assertEquals(((XML.E) doc.root().children().get(0)).name(), "first-name");
    assertEquals(((XML.E) doc.root().children().get(0)).text(), "Mike");
    assertEquals(((XML.E) doc.root().children().get(2)).name(), "addresses");
    assertEquals(((XML.E) ((XML.E) doc.root().children().get(2)).children().get(0)).name(), "address");
    assertEquals(((XML.E) ((XML.E) doc.root().children().get(2)).children().get(0)).get("location"), "home");
  }
}
