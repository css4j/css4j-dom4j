/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.SampleCSS;

public class CSSStylableElementTest {

	XHTMLDocument xhtmlDoc;

	@BeforeEach
	public void setUp() throws Exception {
		Reader re = SampleCSS.sampleHTMLReader();
		InputSource isrc = new InputSource(re);
		xhtmlDoc = TestUtil.parseXML(isrc);
		re.close();
		xhtmlDoc.setTargetMedium("screen");
	}

	@Test
	public void getStyle() {
		List<Node> pList = xhtmlDoc.selectNodes("/*[name()='html']//*[name()='h3']");
		Iterator<Node> it = pList.iterator();
		assertTrue(it.hasNext());
		Node elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSStyleDeclaration style = ((CSSStylableElement) elm).getStyle();
		assertNotNull(style);
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		pList.clear();
	}

	@Test
	public void getStyle2() {
		XHTMLElement body = xhtmlDoc.createElement("body");
		assertNull(body.getStyle());
		assertFalse(body.hasAttributes());
		xhtmlDoc.getDocumentElement().appendChild(body);
		body.setAttribute("style", "font-family:Arial");
		assertTrue(body.hasAttributes());
		assertTrue(body.hasAttribute("style"));
		assertEquals("font-family:Arial", body.getAttribute("style"));
		CSSStyleDeclaration style = body.getStyle();
		assertNotNull(style);
		assertEquals(1, style.getLength());
		assertEquals("font-family: Arial; ", body.getAttribute("style"));
		assertEquals("font-family: Arial; ", style.getCssText());
		style.setCssText("font-family: Helvetica");
		assertEquals("font-family: Helvetica; ", style.getCssText());
		assertEquals("font-family: Helvetica; ", body.getAttribute("style"));
	}

	@Test
	public void getStyleUppercase() {
		Element elm = xhtmlDoc.getElementById("ul1li1");
		assertEquals("li", elm.getLocalName());
		assertEquals("li", elm.getNodeName());

		assertTrue(elm instanceof CSSStylableElement);
		CSSStyleDeclaration style = ((CSSStylableElement) elm).getStyle();
		assertNotNull(style);
		assertEquals("color: blue; ", style.getCssText());
	}

	@Test
	public void getComputedStyle() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		List<Node> pList = xhtmlDoc.selectNodes("/*[name()='html']//*[name()='p']");
		Iterator<Node> it = pList.iterator();
		assertTrue(it.hasNext());
		Node elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSStyleDeclaration style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals("bold", style.getPropertyValue("font-weight"));
		pList.clear();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSetIdAttributeNodeAttrBoolean() {
		XHTMLElement body = xhtmlDoc.createElement("body");
		xhtmlDoc.getDocumentElement().appendChild(body);
		Attr attr = xhtmlDoc.createAttribute("id");
		attr.setValue("myId");
		Attr fooAttr = xhtmlDoc.createAttribute("foo");
		fooAttr.setValue("bar");
		body.setAttributeNode(attr);
		body.setAttributeNode(fooAttr);
		body.setIdAttributeNode(attr, true);
		body.setIdAttributeNode(attr, false);
		body.setIdAttributeNode(fooAttr, false);
		try {
			body.setIdAttributeNode(fooAttr, true);
			fail("Must throw exception");
		} catch (DOMException e) {
		}
		// test for xml:id
		attr = xhtmlDoc.createAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:id");
		attr.setValue("xmlId");
		body.setAttributeNode(attr);
		body.setIdAttributeNode(attr, true);
		body.setIdAttributeNode(attr, false);
		assertEquals("myId", body.getAttribute("id"));
		assertEquals("xmlId", body.getAttributeNS("http://www.w3.org/XML/1998/namespace", "id"));
		assertTrue(body == xhtmlDoc.getElementById("myId"));
	}

	@AfterEach
	public void tearDown() {
		xhtmlDoc = null;
	}

}
