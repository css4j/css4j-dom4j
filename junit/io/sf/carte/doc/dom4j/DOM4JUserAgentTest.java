/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMStringList;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.agent.net.DefaultOriginPolicy;
import io.sf.carte.doc.style.css.nsac.Parser;

public class DOM4JUserAgentTest {
	TestUserAgent agent;

	@BeforeEach
	public void setUp() {
		agent = new TestUserAgent();
	}

	@Test
	public void getSelectedStyleSheetSet() throws Exception {
		URL url = new URI("http://www.example.com/xhtml/htmlsample.html").toURL();
		agent.getTestDocumentFactory().getConnectionFactory().setHeader("html", "Default-Style", "Alter 2");
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

	@Test
	public void getSelectedStyleSheetSetMeta() throws Exception {
		URL url = new URI("http://www.example.com/xhtml/meta-default-style.html").toURL();
		agent.getTestDocumentFactory().getConnectionFactory().registerURL(url.toExternalForm(),
				"meta-default-style.html");
		XHTMLDocument xhtmlDoc = agent.readURL(url);
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
	}

	/*
	 * HTML 4.01, § 14.3.2:
	 * "If two or more META declarations or HTTP headers specify the preferred style
	 * sheet, the last one takes precedence. HTTP headers are considered to occur
	 * earlier than the document HEAD for this purpose."
	 */
	@Test
	public void getSelectedStyleSheetSetMetaOverride() throws Exception {
		URL url = new URI("http://www.example.com/xhtml/meta-default-style.html").toURL();
		MockURLConnectionFactory connFactory = agent.getTestDocumentFactory().getConnectionFactory();
		connFactory.registerURL(url.toExternalForm(), "meta-default-style.html");
		connFactory.setHeader("html", "Default-Style", "Alter 2");
		XHTMLDocument xhtmlDoc = agent.readURL(url);
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
	}

	/**
	 * Test User Agent based on DOM4J document trees.
	 * <p>
	 * 
	 * @author Carlos Amengual
	 */
	public class TestUserAgent extends DOM4JUserAgent {

		private static final long serialVersionUID = 1L;

		TestDocumentFactory factory = new TestDocumentFactory();

		protected TestUserAgent() {
			super(EnumSet.noneOf(Parser.Flag.class));
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
