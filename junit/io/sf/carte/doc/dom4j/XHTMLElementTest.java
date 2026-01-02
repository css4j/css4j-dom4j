/*

 Copyright (c) 2005-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class XHTMLElementTest {

	private static XHTMLDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		xhtmlDoc = TestUtil.sampleXHTML();
	}

	@Test
	public void testGetId() {
		org.dom4j.Element elm = xhtmlDoc.elementByID("listpara");
		assertNotNull(elm);
		XHTMLElement elmdom = xhtmlDoc.getElementById("listpara");
		assertNotNull(elmdom);
		assertTrue(elm == elmdom);
		assertEquals("listpara", ((XHTMLElement) elm).getId());
		((XHTMLElement) elm).setAttribute("id", "foo");
		assertEquals("foo", ((XHTMLElement) elm).getId());
	}

	@Test
	public void testGetIdUppercase() {
		org.dom4j.Element elm = xhtmlDoc.elementByID("ul1li1");
		assertNotNull(elm);
		XHTMLElement elmdom = xhtmlDoc.getElementById("ul1li1");
		assertNotNull(elmdom);
		assertTrue(elm == elmdom);
		assertEquals("ul1li1", ((XHTMLElement) elm).getAttribute("ID"));
		assertEquals("ul1li1", ((XHTMLElement) elm).getId());
		((XHTMLElement) elm).setAttribute("id", "foo");
		assertEquals("foo", ((XHTMLElement) elm).getId());
		assertEquals("foo", ((XHTMLElement) elm).getAttribute("ID"));
	}

	/*
	 * The next test aims at detecting any future attempt to fix the non-conformant
	 * behavior in DOM4J. The CSS4J-DOM4J selector matcher assumes the current
	 * non-conforming one.
	 */
	@Test
	public void testNames() {
		String nsURI = "http://www.example.com/examplens";
		XHTMLElement elmPrefix = xhtmlDoc.createElementNS(nsURI, "p:e");
		assertEquals("e", elmPrefix.getLocalName());
		assertEquals("e", elmPrefix.getNodeName());
		assertEquals("e", elmPrefix.getTagName());
		assertEquals("p", elmPrefix.getPrefix());
		assertEquals(nsURI, elmPrefix.getNamespaceURI());

		XHTMLElement elm = xhtmlDoc.createElementNS(nsURI, "e");
		assertEquals("e", elm.getLocalName());
		assertEquals("e", elm.getNodeName());
		assertEquals("e", elm.getTagName());
		assertEquals("", elm.getPrefix());
		assertEquals(nsURI, elmPrefix.getNamespaceURI());

		XHTMLElement elmNoNS = xhtmlDoc.createElement("e");
		assertEquals("e", elmNoNS.getLocalName());
		assertEquals("e", elmNoNS.getNodeName());
		assertEquals("e", elmNoNS.getTagName());
		assertEquals("", elmNoNS.getPrefix());
		assertEquals("", elmNoNS.getNamespaceURI());

		elmNoNS = xhtmlDoc.createElement("E");
		assertEquals("E", elmNoNS.getLocalName());
		assertEquals("E", elmNoNS.getNodeName());
		assertEquals("E", elmNoNS.getTagName());
		assertEquals("", elmNoNS.getPrefix());
		assertEquals("", elmNoNS.getNamespaceURI());
	}

}
