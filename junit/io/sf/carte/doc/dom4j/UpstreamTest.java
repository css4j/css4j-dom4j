/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Check upstream regressions.
 */
public class UpstreamTest {

	/**
	 * Test the security defaults of {@code SAXReader}'s default constructor.
	 */
	@Test
	@Timeout(value = 700, unit = TimeUnit.MILLISECONDS)
	public void testParseWithDefaults() {
		SAXReader builder = new SAXReader();
		StringReader re = new StringReader(
				"<!DOCTYPE foo SYSTEM \"http://www.example.com/foo.dtd\"><foo>a&eacute;i</foo>");
		InputSource is = new InputSource(re);
		try {
			org.dom4j.Document document = builder.read(is);
			org.dom4j.Element docElm = document.getRootElement();
			String text = docElm.getText();
			assertEquals("ai", text);
		} catch (DocumentException e) {
			Throwable cause = e.getCause();
			assertEquals("java.io.FileNotFoundException", cause.getClass().getName());
			fail("Attempt to retrieve the DTD");
		}
	}

	/**
	 * Test the security defaults of {@link DocumentHelper#parseText(String)}.
	 */
	@Test
	@Timeout(value = 700, unit = TimeUnit.MILLISECONDS)
	public void testDocumentHelperParseText() {
		try {
			org.dom4j.Document document = DocumentHelper.parseText(
					"<!DOCTYPE foo SYSTEM \"http://www.example.com/foo.dtd\"><foo>a&eacute;i</foo>");
			org.dom4j.Element docElm = document.getRootElement();
			String text = docElm.getText();
			assertEquals("ai", text);
		} catch (DocumentException e) {
			Throwable cause = e.getCause();
			assertEquals("java.io.FileNotFoundException", cause.getClass().getName());
			fail("Attempt to retrieve the DTD");
		}
	}

	/**
	 * Test the security defaults of the String constructors of {@code SAXReader}.
	 * 
	 * @throws SAXException
	 */
	@Disabled
	@Test
	@Timeout(value = 700, unit = TimeUnit.MILLISECONDS)
	public void testConstructorString() throws SAXException {
		SAXReader builder = new SAXReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		StringReader re = new StringReader(
				"<!DOCTYPE foo SYSTEM \"http://127.0.0.1:6666/foo.dtd\"><foo>a&eacute;i</foo>");
		InputSource is = new InputSource(re);
		try {
			org.dom4j.Document document = builder.read(is);
			org.dom4j.Element docElm = document.getRootElement();
			String text = docElm.getText();
			assertEquals("ai", text);
		} catch (DocumentException e) {
			fail("Attempt to retrieve the DTD");
		}
	}

	/*
	 * SAXReader and XPP3 fail this test that css4j-dom4j passes with
	 * XMLDocumentBuilder.
	 */
	@Disabled
	@Test
	public void testParseInputSourceImpliedHtmlElement() throws DocumentException {
		String xml = "<!DOCTYPE html><body><div id='divid'><br/></div></body>";
		testImpliedHtmlElement(parseDocumentWithSAXReader(new StringReader(xml)));
	}

	private void testImpliedHtmlElement(org.dom4j.Document document) {
		assertNotNull(document);
		org.dom4j.Element docElement = document.getRootElement();
		assertNotNull(docElement);
		org.dom4j.Element element = document.elementByID("divid");
		assertNotNull(element);
		assertTrue(element.hasContent());
		element = element.getParent();
		assertNotNull(element);
		assertEquals("body", element.getName());
		element = element.getParent();
		assertNotNull(element);
		assertEquals("html", element.getName());
		assertTrue(docElement == element);
	}

	private org.dom4j.Document parseDocumentWithSAXReader(Reader re) throws DocumentException {
		SAXReader builder = new SAXReader();
		InputSource is = new InputSource(re);
		org.dom4j.Document document = builder.read(is);
		try {
			re.close();
		} catch (IOException e) {
			fail(e);
		}
		return document;
	}

}
