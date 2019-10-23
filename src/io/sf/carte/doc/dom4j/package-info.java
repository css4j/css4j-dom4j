/**
 * Built on top of the DOM4J package, provides XHTML parsing with built-in
 * support for CSS style sheets.
 * <p>
 * DOM4J is an XML DOM-like software package with support for Java
 * language constructs, like Collections. See
 * <a href="https://github.com/dom4j" target="_blank">https://github.com/dom4j</a>
 * for more information.
 * </p>
 * <p>
 * This implementation integrates DOM4J with the CSS DOM implementation found in
 * the <code>io.sf.carte.doc.style.css</code> package and subpackages.
 * </p>
 * <h3>Short example</h3>
 * <p>
 * This is the easiest way to use this package:
 * </p>
 * 
 * <pre>
 Reader re = ...  [reader for XHTML document]
 InputSource source = new InputSource(re);
 SAXReader reader = new SAXReader(XHTMLDocumentFactory.getInstance());
 reader.setEntityResolver(new DefaultEntityResolver());
 Document document = reader.read(source);
 * </pre>
 * <p>
 * And once you got the element you want style for (see, for example, the
 * <a href="https://github.com/dom4j/dom4j/wiki/Quick-Start-Guide" target=
 * "_blank">DOM4J Quick Start Guide</a>), just get it:
 * </p>
 * 
 * <pre>
 * CSSComputedProperties style = ((CSSStylableElement) element).getComputedStyle();
 * String propertyValue = style.getPropertyValue("display");
 * </pre>
 * <p>
 * Please read the documentation of the individual classes for information on
 * additional capabilities, like caching or the use of customized style sheets.
 * </p>
 */
package io.sf.carte.doc.dom4j;
