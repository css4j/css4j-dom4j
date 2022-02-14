/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.StyleFormattingFactory;
import io.sf.carte.doc.style.css.om.TestStyleFormattingFactory;

public class StyleElementTest {

	XHTMLDocument xDoc = null;

	HeadElement headElement = null;

	QName style_qname = null;

	StyleElement styleElement = null;

	@Before
	public void setUp() {
		XHTMLDocumentFactory factory = XHTMLDocumentFactory.getInstance();
		headElement = (HeadElement) factory.createElement("head", XHTMLDocument.XHTML_NAMESPACE_URI);
		xDoc = factory.createDocument(headElement);
		style_qname = new QName("style", new Namespace("", 
				XHTMLDocument.XHTML_NAMESPACE_URI));
		style_qname.setDocumentFactory(factory);
	}

	@Test
	public void addRemove() {
		assertEquals(0, xDoc.embeddedStyle.size());
		styleElement = (StyleElement) headElement.addElement(style_qname);
		assertEquals(1, xDoc.embeddedStyle.size());
		styleElement.setAttribute("type", "foo/bar");
		assertEquals(0, xDoc.getStyleSheets().getLength());
		styleElement.setAttribute("type", "text/css");
		assertEquals(1, xDoc.getStyleSheets().getLength());
		styleElement.setText("p {font-size: smaller; font-style: italic; }");
		assertEquals(1, xDoc.getStyleSheets().getLength());
		headElement.remove(styleElement);
		assertEquals(0, xDoc.embeddedStyle.size());
	}

	@Test
	public void change() {
		styleElement = (StyleElement) headElement.addElement(style_qname);
		styleElement.setAttribute("type", "text/css");
		xDoc.getStyleSheet();
		int iniSerial = xDoc.getStyleCacheSerial();
		styleElement.setText("p {font-size: large; font-style: italic; }");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

	@Test
	public void getText() {
		styleElement = (StyleElement) headElement.addElement(style_qname);
		styleElement.setAttribute("type", "text/css");
		styleElement.setText("p {font-size: large; font-style: italic;}");
		assertEquals("p {font-size: large; font-style: italic;}", styleElement.getText());
		//
		styleElement.normalize();
		assertEquals("p {font-size: large; font-style: italic;}", styleElement.getText());
		//
		xDoc.getStyleSheet();
		StyleFormattingFactory formattingf = new TestStyleFormattingFactory();
		xDoc.getDocumentFactory().getStyleSheetFactory().setStyleFormattingFactory(formattingf);
		styleElement.normalize();
		assertEquals("p {font-size: large; font-style: italic; }", styleElement.getText());
		// Empty type
		styleElement.setAttribute("type", "");
		assertNotNull(styleElement.getSheet());
		styleElement.normalize();
		assertEquals("p {font-size: large; font-style: italic; }", styleElement.getText());
		//
		styleElement.setText("not style-related element");
		assertEquals("not style-related element", styleElement.getText());
		styleElement.normalize();
		assertEquals("not style-related element", styleElement.getText());
		// Remove attribute
		styleElement.removeAttribute("type");
		styleElement.normalize();
		assertEquals("not style-related element", styleElement.getText());
		// Other type
		styleElement.setAttribute("type", "text/xsl");
		styleElement.setText(
				"<?xml version=\"1.0\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
				+ "<xsl:output method=\"text\"/><xsl:template match=\"foo\">bar<xsl:value-of select=\".\"/>"
				+ "</xsl:template></xsl:stylesheet>");
		assertNull(styleElement.getSheet());
		styleElement.normalize();
		assertEquals(
				"<?xml version=\"1.0\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
				+ "<xsl:output method=\"text\"/><xsl:template match=\"foo\">bar<xsl:value-of select=\".\"/>"
				+ "</xsl:template></xsl:stylesheet>",
				styleElement.getText());
	}

	@Test
	public void getNamespaceURI() {
		styleElement = (StyleElement) headElement.addElement(style_qname);
		assertEquals(XHTMLDocument.XHTML_NAMESPACE_URI, styleElement.getNamespaceURI());
	}

}
