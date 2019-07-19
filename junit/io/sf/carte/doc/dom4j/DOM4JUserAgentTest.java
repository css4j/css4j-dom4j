/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMStringList;

import io.sf.carte.doc.DocumentException;
import io.sf.carte.doc.agent.net.DefaultOriginPolicy;
import io.sf.carte.doc.style.css.nsac.Parser2;

public class DOM4JUserAgentTest {
	TestUserAgent agent;

	@Before
	public void setUp() {
		agent = new TestUserAgent();
		agent.getTestDocumentFactory().getMockURLFactory()
			.setHeader("html", "Default-Style", "Alter 2");
	}

	@Test
	public void getSelectedStyleSheetSet() throws IOException, DocumentException {
		URL url = new URL("http://www.example.com/xhtml/htmlsample.html");
		XHTMLDocument xhtmlDoc = agent.readURL(url);
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
	}

	/**
	 * Test User Agent based on DOM4J document trees.
	 * <p>
	 * 
	 * @author Carlos Amengual
	 */
	public class TestUserAgent extends DOM4JUserAgent {

		TestDocumentFactory factory = new TestDocumentFactory();

		protected TestUserAgent() {
			super(EnumSet.noneOf(Parser2.Flag.class));
			setOriginPolicy(DefaultOriginPolicy.getInstance());
		}

		TestDocumentFactory getTestDocumentFactory() {
			return factory;
		}

		@Override
		protected URLConnection createConnection(URL url) throws IOException {
			return factory.openConnection(url);
		}

	}
}
