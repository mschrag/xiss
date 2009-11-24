package org.xiss;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * XML is the container class for all of the XISS XML builder classes and methods.
 * 
 * @author mschrag
 */
public class XML {
  /**
   * Item is the base class of everything that can appear in an XML document. 
   * 
   * @author mschrag
   */
  public static abstract class Item {
    private Item _parent;

    /**
     * Constructs a new Item.
     */
    public Item() {
    }

    /**
     * Sets the parent of the this item.
     * 
     * @param parent the parent of the this item
     */
    protected void setParent(Item parent) {
      _parent = parent;
    }

    /**
     * Returns the parent of this item, or null if there isn't one.
     * 
     * @return the parent of this item, or null if there isn't one
     */
    public Item parent() {
      return _parent;
    }

    /**
     * Returns the XML document, or null if it isn't in a document.
     *  
     * @return the XML document, or null if it isn't in a document
     */
    public XML.Doc doc() {
      XML.Item item = this;
      XML.Item parent = null;
      while ((parent = item.parent()) != null) {
        item = parent;
      }
      XML.Doc doc = null;
      if (item instanceof XML.Doc) {
        doc = (XML.Doc) item;
      }
      return doc;
    }

    /**
     * Writes this item to the given writer with a certain indentation. All
     * items are pretty printed.
     * 
     * @param writer the writer to write to
     * @param indent the current indentation
     */
    public abstract void write(PrintWriter writer, int indent);

    /**
     * Visits this item and any of its children (if the visitor allows).
     * 
     * @param visitor the visitor to visit with
     */
    public abstract void visit(XML.Visitor visitor);

    /**
     * Writes the given escaped String to the writer.
     * 
     * @param value the string value to escape
     * @param writer the writer to write to
     */
    protected void writeEscapedString(String value, PrintWriter writer) {
      if (value != null) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
          char c = value.charAt(i);
          switch (c) {
          case '<':
            writer.print("&lt;");
            break;
          case '>':
            writer.print("&gt;");
            break;
          case '&':
            writer.print("&amp;");
            break;
          case '"':
            writer.print("&quot;");
            break;
          default:
            writer.print(c);
          }
        }
      }
    }

    /**
     * Writes an indentation to the writer.
     * 
     * @param indent the indentation to write
     * @param writer the writer to write to
     */
    protected void writeIndent(int indent, PrintWriter writer) {
      for (int i = 0; i < indent; i++) {
        writer.print("  ");
      }
    }

    /**
     * Generates a formatted representation of this item.
     * 
     * @return a formatted representation of this item
     */
    @Override
    public String toString() {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      write(pw, 0);
      return sw.toString();
    }
  }

  /**
   * Declaration represents an XML declaration (the &lt? ... ?&gt; part of the XML document).
   * 
   * @author mschrag
   */
  public static class Declaration extends Item {
    private String _version;
    private String _encoding;

    /**
     * Constructs a new Declaration.
     * 
     * @param version the version of XML used by this document
     * @param encoding the encoding used by this document
     */
    public Declaration(String version, String encoding) {
      _version = version;
      _encoding = encoding;
    }

    /**
     * Returns the version of this declaration.
     * 
     * @return the version of this declaration
     */
    public String version() {
      return _version;
    }

    /**
     * Sets the version of this document.
     * 
     * @param version the version of this document
     */
    public void setVersion(String version) {
      _version = version;
    }

    /**
     * Returns the encoding for this document.
     * 
     * @return the encoding for this document
     */
    public String encoding() {
      return _encoding;
    }

    /**
     * Sets the encoding for this document.
     * 
     * @param encoding the encoding for this document
     */
    public void setEncoding(String encoding) {
      _encoding = encoding;
    }

    @Override
    public void visit(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      writer.print("<?xml");
      if (_version != null) {
        writer.print(" version=\"");
        writer.print(_version);
        writer.print("\"");
      }
      if (_encoding != null) {
        writer.print(" encoding=\"");
        writer.print(_encoding);
        writer.print("\"");
      }
      writer.print("?>");
      writer.println();
    }
  }

  /**
   * <p>
   * Doc represents the top level XML Document object, and is typically the first object you will make when building.
   * </p>
   * 
   * <pre>
   * XML.Doc doc = XML.doc();
   * XML.E person = doc.root("person");
   * </pre>
   * 
   * @author mschrag
   */
  public static class Doc extends Item {
    private XML.E _root;
    private XML.Declaration _declaration;
    private List<XML.Item> _children;

    /**
     * Constructs a new Document.
     */
    public Doc() {
      _children = new LinkedList<XML.Item>();
      setDeclaration(new XML.Declaration("1.0", "UTF-8"));
    }

    /**
     * Checks if there is already a root element and throws if there is.
     */
    protected void checkNullRoot() {
      if (_root != null) {
        throw new IllegalStateException("There is already a root node for this document.");
      }
    }

    /**
     * Returns the declaration for this document, or null if there isn't one. Every Document
     * gets a version="1.0" encoding="UTF-8" declaration by default, so you should call
     * setDeclaration(null) if you want to remove this default.
     * 
     * @return the declaration for this document
     */
    public XML.Declaration declaration() {
      return _declaration;
    }

    /**
     * Sets the declaration for this document.
     * 
     * @param declaration the declaration for this document
     */
    public void setDeclaration(XML.Declaration declaration) {
      if (_declaration != null) {
        remove(_declaration);
      }
      _declaration = declaration;
      if (_declaration != null) {
        _declaration.setParent(this);
        _children.add(0, _declaration);
      }
    }

    /**
     * Creates a new root element with the given name and returns it. If there is already
     * a root element, this will throw an exception.
     * 
     * @param name the name of the new root element
     * @return the new root element
     */
    public XML.E root(String name) {
      checkNullRoot();
      XML.E root = XML.e(name);
      setRoot(root);
      return root;
    }

    /**
     * Creates a new root element with the given name and initial text contents and returns 
     * it. If there is already a root element, this will throw an exception.
     * 
     * @param name the name of the new root element
     * @param value the initial text value of this element
     * @return the new root element
     */
    public XML.E root(String name, String value) {
      checkNullRoot();
      XML.E root = XML.e(name);
      setRoot(root);
      return root;
    }

    /**
     * Returns the root element (or null if there isn't one). Documents do not start out
     * with a root element, so this might be null.
     *
     * @return the root element
     */
    public XML.E root() {
      return _root;
    }

    /**
     * Sets the root element of this document.
     * 
     * @param root the new root element
     */
    public void setRoot(XML.E root) {
      if (_root != null) {
        if (root != null) {
          int rootIndex = _children.indexOf(_root);
          _children.set(rootIndex, root);
        }
        else {
          _children.remove(_root);
        }
      }
      else if (root != null) {
        add(root);
      }
      _root = root;
    }

    /**
     * Creates and returns a new comment for this document. If you want comments above
     * the root element, you should call doc.comment(..) prior to calling .root(..).
     * 
     * @param comment a new comment for this document
     * @return this document
     */
    public XML.Doc comment(String comment) {
      add(XML.comment(comment));
      return this;
    }

    /**
     * Removes the given child from this document.
     * 
     * @param child the child to remove
     */
    public void remove(XML.Item child) {
      _children.remove(child);
    }

    /**
     * Adds a new child to this document. No validation is performed of these items.
     * 
     * @param <T> the type of the item
     * @param child the child to add
     * @return the newly added item
     */
    public <T extends XML.Item> T add(T child) {
      child.setParent(this);
      _children.add(child);
      return child;
    }

    /**
     * Returns the children of this document.
     * 
     * @return the children of this document
     */
    public List<XML.Item> children() {
      return _children;
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      for (XML.Item item : _children) {
        item.write(writer, indent);
      }
    }

    @Override
    public void visit(XML.Visitor visitor) {
      if (visitor.visit(this)) {
        for (XML.Item item : _children) {
          item.visit(visitor);
        }
      }
    }

    public org.w3c.dom.Document w3c() {
      try {
        org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        if (_children != null) {
          for (XML.Item child : _children) {
            if (child instanceof XML.Node) {
              org.w3c.dom.Node childNode = ((XML.Node) child).w3c(doc);
              doc.appendChild(childNode);
            }
          }
        }
        return doc;
      }
      catch (Throwable t) {
        throw new IllegalArgumentException("Failed to create a W3C Document from the this Doc.", t);
      }
    }
  }

  /**
   * Node is the abstract superclass of all Node items in a Document.
   * 
   * @author mschrag
   */
  public static abstract class Node extends XML.Item {
    public abstract org.w3c.dom.Node w3c(Document doc);
  }

  /**
   * Content is the abstract superclass of all Nodes that have text content.
   * 
   * @author mschrag
   */
  public static abstract class Content extends XML.Node {
    private String _text;

    /**
     * Constructs a new Content.
     * 
     * @param text the text of the node
     */
    public Content(String text) {
      _text = text;
    }

    /**
     * Returns the text of this node.
     * 
     * @return the text of this node
     */
    public String text() {
      return _text;
    }

    /**
     * Sets text text of this node.
     * 
     * @param text text text of this node
     */
    public void setText(String text) {
      _text = text;
    }

    @Override
    public void visit(Visitor visitor) {
      visitor.visit(this);
    }

    /**
     * Writes the text of this node to the writer.
     * 
     * @param writer the writer to write to
     */
    protected abstract void writeText(PrintWriter writer);

    @Override
    public void write(PrintWriter writer, int indent) {
      if (_text != null) {
        writeIndent(indent, writer);
        writeText(writer);
      }
    }
  }

  /**
   * Text represents a bare text node.
   * 
   * @author mschrag
   */
  public static class Text extends Content {
    /**
     * Creates a new text node.
     *  
     * @param text the text of the node
     */
    public Text(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writeEscapedString(text(), writer);
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.Text text = doc.createTextNode(text());
      return text;
    }
  }

  /**
   * CDATA represents a CDATA section of your document.
   * 
   * @author mschrag
   */
  public static class CDATA extends Content {
    public CDATA(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writer.print("<![CDATA[");
      writer.print(text());
      writer.println("]]>");
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.CDATASection cdata = doc.createCDATASection(text());
      return cdata;
    }
  }

  /**
   * Comment represents a Comment section of the document.
   * 
   * @author mschrag
   */
  public static class Comment extends Content {
    public Comment(String text) {
      super(text);
    }

    @Override
    protected void writeText(PrintWriter writer) {
      writer.print("<!-- ");
      writer.print(text());
      writer.println(" -->");
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      org.w3c.dom.Comment comment = doc.createComment(text());
      return comment;
    }
  }

  /**
   * Attr represents a key-value pair attribute of an element.
   * 
   * @author mschrag
   */
  public static class Attr {
    private String _name;
    private String _value;

    /**
     * Constructs a new attribute.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    public Attr(String name, String value) {
      _name = name;
      _value = value;
    }

    /**
     * Returns the name of this attribute.
     * 
     * @return the name of this attribute
     */
    public String name() {
      return _name;
    }

    /**
     * Sets the name of this attribute.
     * 
     * @param name the name of this attribute
     */
    public void setName(String name) {
      _name = name;
    }

    /**
     * Returns the value of this attribute.
     * 
     * @return the value of this attribute
     */
    public String value() {
      return _value;
    }

    /**
     * Sets the value of this attribute.
     * 
     * @param value the value of this attribute
     */
    public void setValue(String value) {
      _value = value;
    }
  }

  /**
   * E represents an element of the XML document, which can
   * have attributes and children nodes.
   * 
   * @author mschrag
   */
  public static class E extends XML.Node {
    private String _name;
    private List<Node> _children;
    private List<Attr> _attributes;

    /**
     * Constructs a new element.
     * 
     * @param name the name of this element
     */
    public E(String name) {
      _name = name;
    }

    /**
     * Constructs a new element.
     * 
     * @param name the name of this element
     * @param text the initial text of this element
     */
    public E(String name, String text) {
      this(name);
      text(text);
    }

    /**
     * Sets the name of this element.
     * 
     * @param name the name of this element
     */
    public void setName(String name) {
      _name = name;
    }

    /**
     * Returns the name of this element.
     * 
     * @return the name of this element
     */
    public String name() {
      return _name;
    }

    @Override
    public void visit(XML.Visitor visitor) {
      if (visitor.visit(this) && _children != null) {
        for (XML.Node node : _children) {
          node.visit(visitor);
        }
      }
    }

    /**
     * Sets the attribute of the given name to the given value. If there is
     * already an attribute with this name, it is replaced.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @return this element
     */
    public XML.E set(String name, String value) {
      remove(name);
      add(new XML.Attr(name, value));
      return this;
    }

    /**
     * Sets a series of attributes in the format "name1","value1", "name2","value2", ... If any
     * name already exists, it will be replaced with the value you provide.
     * 
     * @param nvPairs an array of name-value pairs
     * @return this element
     */
    public XML.E set(String... nvPairs) {
      for (int i = 0; i < nvPairs.length; i += 2) {
        String name = nvPairs[i];
        String value = nvPairs[i + 1];
        set(name, value);
      }
      return this;
    }

    /**
     * Adds the given attribute to this element. This does
     * not check for duplicates.
     * 
     * @param attribute the attribute to add
     * @return this element
     */
    public XML.E add(XML.Attr attribute) {
      if (_attributes == null) {
        _attributes = new LinkedList<XML.Attr>();
      }
      _attributes.add(attribute);
      return this;
    }

    /**
     * Returns the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to look up
     * @return the attribute with the given name, or null if there isn't one
     */
    public XML.Attr getAttr(String attributeName) {
      if (_attributes != null) {
        for (XML.Attr attribute : _attributes) {
          if (attribute._name.equals(attributeName)) {
            return attribute;
          }
        }
      }
      return null;
    }

    /**
     * Removes the attribute with the given name.
     * 
     * @param attributeName the name of the attribute to remove
     * @return the removed attribute, or null if there wasn't one
     */
    public XML.Attr remove(String attributeName) {
      XML.Attr attribute = getAttr(attributeName);
      if (attribute != null) {
        remove(attribute);
      }
      return attribute;
    }

    /**
     * Removes the given attribute from this element.
     * 
     * @param attribute the attribute to remove
     */
    public void remove(XML.Attr attribute) {
      _attributes.remove(attribute);
      if (_attributes.size() == 0) {
        _attributes = null;
      }
    }

    /**
     * Returns the value of the attribute with the given name, or null if there isn't one.
     * 
     * @param attributeName the name of the attribute to look up
     * @return the value of the attribute with the given name, or null if there isn't one
     */
    public String get(String attributeName) {
      XML.Attr attribute = getAttr(attributeName);
      String value = null;
      if (attribute != null) {
        value = attribute.value();
      }
      return value;
    }

    /**
     * Returns the attributes for this element.
     * 
     * @return the attributes for this element
     */
    public List<XML.Attr> attributes() {
      return _attributes;
    }

    /**
     * Sets the text value of this element. This will replace an existing Text child with
     * the provided string, or create a new one if one doesn't exist. If there is already
     * a child in this element that is not a Text node, this will throw an exception.
     * 
     * @param text the new text value for this node
     * @return this element
     */
    public XML.E setText(String text) {
      if (_children != null) {
        if (_children.size() == 1) {
          XML.Node node = _children.get(0);
          if (node instanceof XML.Text) {
            ((XML.Text) node).setText(text);
          }
          else {
            throw new IllegalStateException("There was already a non-text child of this element: " + node);
          }
        }
        else {
          throw new IllegalStateException("There was more than one child of this element: " + this);
        }
      }
      else {
        text(text);
      }
      return this;
    }

    /**
     * Returns the text value of this element, or null if there isn't one. If there is a non-text
     * child of this element, this will throw an exception.
     * 
     * @return the text value of this element
     */
    public String text() {
      String text = null;
      if (_children != null) {
        if (_children.size() == 1) {
          XML.Node node = _children.get(0);
          if (node instanceof XML.Text) {
            text = ((XML.Text) node).text();
          }
          else {
            throw new IllegalStateException("There was only a non-text child of this element: " + node);
          }
        }
        else {
          throw new IllegalStateException("There was more than one child of this element: " + this);
        }
      }
      return text;
    }

    /**
     * Returns the children nodes of this element.
     * 
     * @return the children nodes of this element
     */
    public List<XML.Node> children() {
      return _children;
    }

    /**
     * Returns the text of the child element with the given name from this element, or null if there isn't one.
     * 
     * @param name the name of the element to look up
     * @return the text of the child element with the given name from this element, or null if there isn't one
     */
    public String childText(String name) {
      String text = null;
      XML.E child = child(name);
      if (child != null) {
        text = child.text();
      }
      return text;
    }

    /**
     * Returns the child element with the given name from this element, or null if there isn't one.
     * 
     * @param name the name of the element to look up
     * @return the child element with the given name from this element, or null if there isn't one
     */
    public XML.E child(String name) {
      XML.E matchingElement = null;
      if (_children != null) {
        for (XML.Node node : _children) {
          if (node instanceof XML.E && ((XML.E) node)._name.equals(name)) {
            if (matchingElement == null) {
              matchingElement = (XML.E) node;
            }
            else {
              throw new IllegalStateException("There was more than one child named '" + name + "'.");
            }
          }
        }
      }
      return matchingElement;
    }

    /**
     * Returns a list of direct children of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the children elements to look up
     * @return a list of direct children of this element that have the given name, or an empty
     * list if there aren't any
     */
    public List<XML.E> children(String name) {
      List<XML.E> children = new LinkedList<XML.E>();
      if (_children != null) {
        for (XML.Node node : _children) {
          if (node instanceof XML.E && ((XML.E) node)._name.equals(name)) {
            children.add((XML.E) node);
          }
        }
      }
      return children;
    }

    /**
     * Returns a set of the text of the descendent elements of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the descendent elements to look up
     * @return a set of the text of the descendents of this element that have the given name, or an empty
     * set if there aren't any
     */
    public Set<String> descendentsText(String name) {
      Set<XML.E> descendents = descendents(name);
      Set<String> descendentsText = new LinkedHashSet<String>();
      for (XML.E descendent : descendents) {
        descendentsText.add(descendent.text());
      }
      return descendentsText;
    }

    /**
     * Returns a set of descendent elements of this element that have the given name, or an empty
     * list if there aren't any.
     *  
     * @param name the name of the descendent elements to look up
     * @return a set of descendents of this element that have the given name, or an empty
     * set if there aren't any
     */
    public Set<XML.E> descendents(final String name) {
      final Set<XML.E> descendents = new LinkedHashSet<XML.E>();
      if (_children != null) {
        XML.Visitor visitor = new XML.Visitor() {
          public boolean visit(Item item) {
            if (item instanceof XML.E && ((XML.E) item)._name.equals(name)) {
              descendents.add((XML.E) item);
            }
            return true;
          }
        };

        for (XML.Node node : _children) {
          node.visit(visitor);
        }
      }
      return descendents;
    }

    /**
     * Creates and appends a new element to this element.
     *  
     * @param name the name of the new element
     * @return the new element
     */
    public XML.E e(String name) {
      return add(XML.e(name));
    }

    /**
     * Creates and appends a new element to this element.
     *  
     * @param name the name of the new element
     * @param value the text value of the new element
     * @return the new element
     */
    public XML.E e(String name, String value) {
      return add(XML.e(name, value));
    }

    /**
     * Creates and appends a new text node to this element.
     *  
     * @param text the text value to append
     * @return the new text node
     */
    public XML.Text text(String text) {
      return add(XML.text(text));
    }

    /**
     * Creates and appends a new CDATA node to this element.
     * 
     * @param cdata the cdata value to append
     * @return the new cdata node
     */
    public XML.CDATA cdata(String cdata) {
      return add(XML.cdata(cdata));
    }

    /**
     * Creates and appends a new comment node to this element.
     * 
     * @param comment the comment value to append
     * @return the new comment node
     */
    public XML.Comment comment(String comment) {
      return add(XML.comment(comment));
    }

    /**
     * Removes the given node from this element.
     * 
     * @param child the child node to remove
     */
    public void remove(XML.Node child) {
      if (_children != null) {
        _children.remove(child);
        if (_children.size() == 0) {
          _children = null;
        }
      }
    }

    /**
     * Adds a new node to this element.
     * 
     * @param <T> the type of the node to add
     * @param child the child to add
     * @return the added child
     */
    public <T extends XML.Node> T add(T child) {
      child.setParent(this);
      if (_children == null) {
        _children = new LinkedList<Node>();
      }
      _children.add(child);
      return child;
    }

    /**
     * Writes the attributes of this element to the writer.
     * 
     * @param writer the writer to write attributes to
     */
    protected void writeAttributes(PrintWriter writer) {
      if (_attributes != null) {
        for (XML.Attr attribute : _attributes) {
          writer.print(" ");
          writer.print(attribute.name());
          writer.print("=\"");
          writeEscapedString(attribute.value(), writer);
          writer.print("\"");
        }
      }
    }

    @Override
    public void write(PrintWriter writer, int indent) {
      writeIndent(indent, writer);
      if (_children != null && _children.size() > 0) {
        writer.print("<");
        writer.print(_name);
        writeAttributes(writer);
        writer.print(">");
        if (_children.size() == 1 && _children.get(0) instanceof XML.Text) {
          _children.get(0).write(writer, 0);
        }
        else {
          writer.println();
          for (XML.Node node : _children) {
            node.write(writer, indent + 1);
          }
          writeIndent(indent, writer);
        }
        writer.print("</");
        writer.print(_name);
        writer.println(">");
      }
      else {
        writer.print("<");
        writer.print(_name);
        writeAttributes(writer);
        writer.println(" />");
      }
    }

    @Override
    public org.w3c.dom.Node w3c(Document doc) {
      Element e = doc.createElement(_name);
      if (_attributes != null) {
        for (XML.Attr attribute : _attributes) {
          e.setAttribute(attribute.name(), attribute.value());
        }
      }
      if (_children != null) {
        for (XML.Node child : _children) {
          org.w3c.dom.Node childNode = child.w3c(doc);
          e.appendChild(childNode);
        }
      }
      return e;
    }
  }

  /**
   * Visitor is an interface that can be passed to the visit
   * method of any XML.Item to walk the DOM.
   * 
   * @author mschrag
   */
  public static interface Visitor {
    /**
     * Called by the visit method of each item.
     * 
     * @param item the item being visited
     * @return true if the children of the given item should be visited, false if it should skip the children
     */
    public boolean visit(XML.Item item);
  }

  /**
   * Creates and returns a new Document.
   * 
   * @return a new document
   */
  public static XML.Doc doc() {
    return new XML.Doc();
  }

  /**
   * Creates and return a document parsed from the given string.
   * 
   * @param documentString the string to parse as XML
   * @return a new parsed document
   */
  public static XML.Doc doc(String documentString) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(documentString)));
      return XML.doc(document);
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided string.", t);
    }
  }

  /**
   * Creates and return a document parsed from the given reader.
   * 
   * @param reader the reader to parse from
   * @return a new parsed document
   */
  public static XML.Doc doc(Reader reader) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(reader));
      return XML.doc(document);
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided reader.", t);
    }
  }

  /**
   * Creates and return a document parsed from the given file.
   * 
   * @param file the file to parse from
   * @return a new parsed document
   */
  public static XML.Doc doc(File file) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
      return XML.doc(document);
    }
    catch (Throwable t) {
      throw new IllegalArgumentException("Failed to parse a document from the provided file.", t);
    }
  }

  /**
   * Converts a W3C Element into an XML.E.
   * 
   * @param w3cElement the W3C Element
   * @return the equivalent XML.E
   */
  public static XML.E e(Element w3cElement) {
    XML.E e = XML.e(w3cElement.getNodeName());
    org.w3c.dom.NamedNodeMap attributes = w3cElement.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      org.w3c.dom.Node w3cAttribute = attributes.item(i);
      String attributeName = w3cAttribute.getNodeName();
      String attributeValue = w3cAttribute.getNodeValue();
      e.set(attributeName, attributeValue);
    }
    org.w3c.dom.NodeList w3cChildren = w3cElement.getChildNodes();
    for (int i = 0; i < w3cChildren.getLength(); i++) {
      org.w3c.dom.Node w3cChild = w3cChildren.item(i);
      if (w3cChild instanceof org.w3c.dom.Text) {
        e.text(((org.w3c.dom.Text) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.CDATASection) {
        e.cdata(((org.w3c.dom.CDATASection) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.Comment) {
        e.comment(((org.w3c.dom.Comment) w3cChild).getNodeValue());
      }
      else if (w3cChild instanceof org.w3c.dom.Element) {
        e.add(XML.e((org.w3c.dom.Element) w3cChild));
      }
      else {
        throw new IllegalArgumentException("Unable to handle nodes of type '" + w3cChild + "'.");
      }
    }
    return e;
  }

  /**
   * Converts a W3C Document into an XML.Doc.
   * 
   * @param w3cDocument the W3C Document
   * @return the equivalent XML.Doc
   */
  public static XML.Doc doc(org.w3c.dom.Document w3cDocument) {
    org.w3c.dom.Element w3cElement = w3cDocument.getDocumentElement();
    Doc doc = XML.doc();
    doc.setRoot(XML.e(w3cElement));
    return doc;
  }

  /**
   * Creates and returns a new Declaration.
   * 
   * @param version the version of the declaration
   * @param encoding the encoding of the declaration
   * @return a new declaration
   */
  public static XML.Declaration declaration(String version, String encoding) {
    return new XML.Declaration(version, encoding);
  }

  /**
   * Creates and returns a new Element.
   * 
   * @param name the name of the element
   * @return a new element
   */
  public static XML.E e(String name) {
    return new XML.E(name);
  }

  /**
   * Creates and returns a new Element.
   * 
   * @param name the name of the element
   * @param text the text of the element
   * @return a new element
   */
  public static XML.E e(String name, String text) {
    return new XML.E(name, text);
  }

  /**
   * Creates and returns a new text node.
   * 
   * @param text the text of the node
   * @return a new text node
   */
  public static XML.Text text(String text) {
    return new XML.Text(text);
  }

  /**
   * Creates and returns a new cdata node.
   * 
   * @param text the text of the cdata node
   * @return a new cdata node
   */
  public static XML.CDATA cdata(String cdata) {
    return new XML.CDATA(cdata);
  }

  /**
   * Creates and returns a new comment node.
   * 
   * @param text the text of the comment node
   * @return a new comment node
   */
  public static XML.Comment comment(String comment) {
    return new XML.Comment(comment);
  }
}
