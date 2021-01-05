/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.io.StringReader;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

public class UpstreamTest {

	@Test(timeout = 500)
	public void testParseWithDefaults() throws Exception {
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

	@Test(timeout = 500)
	public void testDocumentHelperParseText() throws Exception {
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

	/*
	 * SAXReader and XPP3 fail this test that css4j-dom4j passes with
	 * XMLDocumentBuilder.
	 */
	@Ignore
	@Test
	public void testParseInputSourceImpliedHtmlElement() throws Exception {
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

	private org.dom4j.Document parseDocumentWithSAXReader(Reader re) throws Exception {
		SAXReader builder = new SAXReader();
		InputSource is = new InputSource(re);
		org.dom4j.Document document = builder.read(is);
		re.close();
		return document;
	}

}
