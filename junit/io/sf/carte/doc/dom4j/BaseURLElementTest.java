/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Reader;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;

public class BaseURLElementTest {

	CSSDocument xDoc = null;

	HeadElement headElement = null;

	QName base_qname = null;

	BaseURLElement baseElement = null;

	@Before
	public void setUp() {
		XHTMLDocumentFactory factory = XHTMLDocumentFactory.getInstance();
		headElement = (HeadElement) factory.createElement("head");
		xDoc = factory.createDocument(headElement);
		base_qname = new QName("base", new Namespace("", XHTMLDocument.XHTML_NAMESPACE_URI));
		base_qname.setDocumentFactory(factory);
	}

	@Test
	public void addRemoveAttribute() {
		baseElement = (BaseURLElement) headElement.addElement(base_qname);
		assertNull(xDoc.getBaseURL());
		baseElement.addAttribute("href", "http://www.example.com/");
		assertNotNull(xDoc.getBaseURL());
		assertEquals("http://www.example.com/", xDoc.getBaseURL().toExternalForm());
		baseElement.remove(baseElement.attribute("href"));
		assertNull(xDoc.getBaseURL());
	}

	@Test
	public void attributeSetValue() {
		baseElement = (BaseURLElement) headElement.addElement(base_qname);
		baseElement.addAttribute("href", "http://www.example.com/");
		assertEquals(XHTMLDocument.XHTML_NAMESPACE_URI, baseElement.getNamespaceURI());
		assertNotNull(xDoc.getBaseURL());
		assertEquals("http://www.example.com/", xDoc.getBaseURL().toExternalForm());
		baseElement.attribute("href").setValue("http://www.example.com/html/");
		assertEquals("http://www.example.com/html/", xDoc.getBaseURL().toExternalForm());
		baseElement.attribute("href").setValue(null);
		assertNull(xDoc.getBaseURL());
	}

	@Test
	public void childAddedNode() throws Exception {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleHTMLReader();
		InputSource isrc = new InputSource(re);
		CSSDocument xhtmlDoc = XHTMLDocumentFactoryTest.parseXML(isrc);
		re.close();
		assertNotNull(xhtmlDoc.getBaseURL());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURL().toExternalForm());
	}

	@Test
	public void childAddedNodeXPP3() throws Exception {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleHTMLReader();
		CSSDocument xhtmlDoc = XHTMLDocumentFactoryTest.parseXPP3(re);
		assertNotNull(xhtmlDoc.getBaseURL());
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURL().toExternalForm());
		re.close();
	}

}
