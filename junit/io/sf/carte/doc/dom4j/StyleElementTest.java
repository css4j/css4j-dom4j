/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;

public class StyleElementTest {

	XHTMLDocument xDoc = null;

	HeadElement headElement = null;

	QName style_qname = null;

	StyleElement styleElement = null;

	@Before
	public void setUp() {
		XHTMLDocumentFactory factory = XHTMLDocumentFactory.getInstance();
		headElement = (HeadElement) factory.createElement("head");
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
	public void getNamespaceURI() {
		styleElement = (StyleElement) headElement.addElement(style_qname);
		assertEquals(XHTMLDocument.XHTML_NAMESPACE_URI, styleElement.getNamespaceURI());
	}

}
