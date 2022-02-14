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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XPP3Reader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import io.sf.carte.doc.dom.XMLDocumentBuilder;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XMLDocumentBuilderTest {

	private static XHTMLDocumentFactory factory;
	private static EntityResolver2 resolver;

	@BeforeClass
	public static void setUpBeforeClass() {
		factory = new TestDocumentFactory();
		resolver = new DefaultEntityResolver();
	}

	/*
	 * XPP3 fails this test
	 */
	@Test
	public void testParseInputSourceEntities() throws Exception {
		testEntities(parseDocument(io.sf.carte.doc.dom.XMLDocumentBuilderTest.loadEntitiesReader()));
		testEntities(parseDocumentWithSAXReader(io.sf.carte.doc.dom.XMLDocumentBuilderTest.loadEntitiesReader()));
	}

	private void testEntities(Document document) {
		assertNotNull(document);
		DocumentType docType = document.getDoctype();
		assertNotNull(docType);
		assertEquals("html", docType.getName());
		Element element = document.getElementById("entity");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		boolean isdom4j = element instanceof DOMElement;
		if (isdom4j) {
			assertEquals("<>", ((DOMElement) element).getText());
		}
		element = document.getElementById("entiamp");
		assertNotNull(element);
		if (isdom4j) {
			assertEquals("&", ((DOMElement) element).getText());
		}
		element = document.getElementById("entiacute");
		assertNotNull(element);
		if (isdom4j) {
			assertEquals("ítem", ((DOMElement) element).getText());
		}
		element = document.getElementById("doesnotexist");
		assertNotNull(element);
		if (isdom4j) {
			assertEquals("list", ((DOMElement) element).getText());
		}
		element = document.getElementById("para1");
		assertNotNull(element);
		assertTrue(element.hasAttribute("donotexist"));
		assertEquals("nothing", element.getAttribute("donotexist"));
	}

	/*
	 * XPP3 fails this test
	 */
	@Test
	public void testParseInputSourceNoDTD() throws Exception {
		testEntitiesNoDTD(parseDocument(io.sf.carte.doc.dom.XMLDocumentBuilderTest.loadEntitiesNoDTDReader()));
		testEntitiesNoDTD(parseDocumentWithSAXReader(io.sf.carte.doc.dom.XMLDocumentBuilderTest.loadEntitiesNoDTDReader()));
	}

	private void testEntitiesNoDTD(Document document) {
		assertNotNull(document);
		assertNull(document.getDoctype());
		Element element = document.getElementById("entity");
		assertNotNull(element);
		boolean isdom4j = element instanceof DOMElement;
		if (isdom4j) {
			assertEquals("<>", ((DOMElement) element).getText());
		}
		element = document.getElementById("entiamp");
		assertNotNull(element);
		if (isdom4j) {
			assertEquals("&", ((DOMElement) element).getText());
		}
	}

	/*
	 * SAXReader and XPP3 fail this test.
	 */
	@Test
	public void testParseInputSourceImpliedHtmlElement() throws Exception {
		String xml = "<!DOCTYPE html><body><div id='divid'><br/></div></body>";
		testImpliedHtmlElement(parseDocument(new StringReader(xml)));
	}

	private void testImpliedHtmlElement(Document document) {
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		Element element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("body", element.getNodeName());
		element = (Element) element.getParentNode();
		assertNotNull(element);
		assertEquals("html", element.getNodeName());
		assertTrue(docElement == element);
	}

	@Test
	public void testParseInputSourceXML() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><body><div id='divid'><br/></div></body>";
		testPlainXML(parseDocument(new StringReader(xml)));
		testPlainXML(parseDocumentWithSAXReader(new StringReader(xml)));
		testPlainXML(parseDocumentWithXPP3Reader(new StringReader(xml)));
	}

	private void testPlainXML(Document document) {
		assertNotNull(document);
		Element docElement = document.getDocumentElement();
		assertNotNull(docElement);
		assertEquals("body", docElement.getNodeName());
		Element element = document.getElementById("divid");
		assertNotNull(element);
		assertTrue(element.hasChildNodes());
		Node parent = element.getParentNode();
		assertNotNull(parent);
		assertEquals("body", parent.getNodeName());
		assertTrue(docElement == parent);
	}

	private Document parseDocument(Reader re) throws SAXException, IOException {
		XMLDocumentBuilder builder = new XMLDocumentBuilder(factory);
		builder.setIgnoreElementContentWhitespace(true);
		builder.setHTMLProcessing(true);
		builder.setEntityResolver(resolver);
		InputSource is = new InputSource(re);
		Document document = builder.parse(is);
		re.close();
		return document;
	}

	private Document parseDocumentWithSAXReader(Reader re) throws Exception {
		SAXReader builder = new SAXReader(factory);
		try {
			builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
		} catch (SAXException e) {
		}
		builder.setEntityResolver(resolver);
		InputSource is = new InputSource(re);
		Document document = (Document) builder.read(is);
		re.close();
		return document;
	}

	/*
	 * XPP3 does not support entities
	 */
	private Document parseDocumentWithXPP3Reader(Reader re) throws Exception {
		XPP3Reader builder = new XPP3Reader(factory);
		Document document = (Document) builder.read(re);
		re.close();
		return document;
	}

}
