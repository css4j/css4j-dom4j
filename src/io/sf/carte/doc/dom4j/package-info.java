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
 * 
 * <h3>Non-standard interfaces</h3>
 * <p>
 * The <code>CSSComputedProperties</code> interface is a convenient extension to W3C's
 * <code>CSSStyleDeclaration</code>.
 * </p>
 * <p>
 * Some CSS default values are system-dependent, and there are other processes that rely
 * on device-specific information. This information can be provided by a user-supplied
 * <code>DeviceFactory</code> object. You can set the appropriate
 * <code>DeviceFactory</code> at the <code>XHTMLDocumentFactory</code> with the
 * <code>setDeviceFactory</code> method.
 * <p>
 * If you use the AWT, you may consider using the the java2d module. For example:
 * 
 * <pre>
 * Color color = Java2DStyleDatabase.getAWTColor(style.getCSSColor());
 * </pre>
 * 
 * Read the documentation of the individual classes for information on
 * additional capabilities, like caching or the use of customized style sheets.
 * <h3>Default style sheet and user preferences</h3>
 * <p>
 * CSS4J uses a default user-agent style sheet as the basis for style
 * computations, although you can set a different one with the
 * <code>setUserAgentStyleSheet</code> method in
 * <code>XHTMLDocumentFactory</code>. User preferences can be specified with the
 * <code>setUserStyleSheet</code> method.
 * </p>
 */
package io.sf.carte.doc.dom4j;
