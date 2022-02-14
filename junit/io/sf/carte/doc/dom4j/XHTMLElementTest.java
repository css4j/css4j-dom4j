/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

public class XHTMLElementTest {

	private static XHTMLDocument xhtmlDoc;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		xhtmlDoc = XHTMLDocumentFactoryTest.sampleXHTML();
	}

	@Test
	public void testGetId() {
		Element elm = (Element) xhtmlDoc.elementByID("listpara");
		assertNotNull(elm);
		Element elmdom = xhtmlDoc.getElementById("listpara");
		assertNotNull(elmdom);
		assertTrue(elmdom instanceof XHTMLElement);
		assertTrue(elm == elmdom);
		assertEquals("listpara", ((XHTMLElement)elm).getId());
		((XHTMLElement)elm).setAttribute("id", "foo");
		assertEquals("foo", ((XHTMLElement)elm).getId());
	}

	@Test
	public void testGetIdUppercase() {
		Element elm = (Element) xhtmlDoc.elementByID("ul1li1");
		assertNotNull(elm);
		Element elmdom = xhtmlDoc.getElementById("ul1li1");
		assertNotNull(elmdom);
		assertTrue(elmdom instanceof XHTMLElement);
		assertTrue(elm == elmdom);
		assertEquals("ul1li1", ((XHTMLElement)elm).getAttribute("ID"));
		assertEquals("ul1li1", ((XHTMLElement)elm).getId());
		((XHTMLElement)elm).setAttribute("id", "foo");
		assertEquals("foo", ((XHTMLElement)elm).getId());
		assertEquals("foo", ((XHTMLElement)elm).getAttribute("ID"));
	}

}
