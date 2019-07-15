/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;

public class LinkElementTest {

	XHTMLDocument xDoc = null;

	HeadElement headElement = null;

	QName link_qname = null;

	LinkElement linkElement = null;

	@Before
	public void setUp() {
		XHTMLDocumentFactory factory = new TestDocumentFactory();
		headElement = (HeadElement) factory.createElement("head");
		xDoc = factory.createDocument(headElement);
		link_qname = new QName("link", new Namespace("", XHTMLDocument.XHTML_NAMESPACE_URI));
		link_qname.setDocumentFactory(factory);
	}

	@Test
	public void addRemove() {
		assertEquals(0, xDoc.linkedStyle.size());
		linkElement = (LinkElement) headElement.addElement(link_qname);
		assertEquals(1, xDoc.linkedStyle.size());
		headElement.remove(linkElement);
		assertEquals(0, xDoc.linkedStyle.size());
	}

	@Test
	public void addRemoveAttribute() {
		linkElement = (LinkElement) headElement.addElement(link_qname);
		xDoc.getStyleSheet();
		int iniSerial = xDoc.getStyleCacheSerial();
		linkElement.addAttribute("href", "http://www.example.com/css/example.css");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		xDoc.getStyleSheet();
		assertNull(linkElement.getSheet());
		assertFalse(xDoc.getErrorHandler().hasErrors());
		linkElement.addAttribute("rel", "stylesheet");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		xDoc.getStyleSheet();
		assertNotNull(linkElement.getSheet());
		assertTrue(xDoc.getErrorHandler().hasErrors());
		linkElement.remove(linkElement.attribute("href"));
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

	@Test
	public void attributeSetValue() {
		linkElement = (LinkElement) headElement.addElement(link_qname);
		linkElement.addAttribute("rel", "stylesheet");
		linkElement.addAttribute("href", "http://www.example.com/css/example.css");
		xDoc.getStyleSheet();
		assertTrue(xDoc.getErrorHandler().hasErrors());
		int iniSerial = xDoc.getStyleCacheSerial();
		linkElement.attribute("href").setValue("http://www.example.com/example2.css");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

}
