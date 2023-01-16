/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Before;
import org.junit.Test;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;

public class LinkElementTest {

	XHTMLDocument xDoc = null;

	HeadElement headElement = null;

	QName link_qname = null;

	@Before
	public void setUp() {
		XHTMLDocumentFactory factory = new TestDocumentFactory();
		headElement = (HeadElement) factory.createElement("head", XHTMLDocument.XHTML_NAMESPACE_URI);
		xDoc = factory.createDocument(headElement);
		link_qname = new QName("link", new Namespace("", XHTMLDocument.XHTML_NAMESPACE_URI));
		link_qname.setDocumentFactory(factory);
	}

	@Test
	public void addRemove() {
		assertEquals(0, xDoc.linkedStyle.size());
		LinkElement linkElement = (LinkElement) headElement.addElement(link_qname);
		assertEquals(1, xDoc.linkedStyle.size());
		headElement.remove(linkElement);
		assertEquals(0, xDoc.linkedStyle.size());
	}

	@Test
	public void addRemoveAttribute() {
		LinkElement linkElement = (LinkElement) headElement.addElement(link_qname);
		xDoc.getStyleSheet();
		int iniSerial = xDoc.getStyleCacheSerial();
		linkElement.addAttribute("href", "http://www.example.com/css/example.css");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		xDoc.getStyleSheet();
		assertNull(linkElement.getSheet());
		assertFalse(xDoc.getErrorHandler().hasErrors());
		assertFalse(xDoc.hasStyleIssues());

		linkElement.addAttribute("rel", "stylesheet");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		xDoc.getStyleSheet();
		assertNotNull(linkElement.getSheet());
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.hasStyleIssues());

		linkElement.remove(linkElement.attribute("href"));
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

	@Test
	public void attributeSetValue() {
		LinkElement linkElement = (LinkElement) headElement.addElement(link_qname);
		linkElement.addAttribute("rel", "stylesheet");
		linkElement.addAttribute("href", "http://www.example.com/css/example.css");
		xDoc.getStyleSheet();
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.hasStyleIssues());

		int iniSerial = xDoc.getStyleCacheSerial();
		linkElement.attribute("href").setValue("http://www.example.com/example2.css");
		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

	@Test
	public void getSheet() throws MalformedURLException {
		LinkElement linkElement = (LinkElement) headElement.addElement(link_qname);
		linkElement.addAttribute("rel", "stylesheet");
		linkElement.addAttribute("href", "http://www.example.com/css/common.css");
		xDoc.getStyleSheet();
		assertFalse(xDoc.getErrorHandler().hasErrors());
		assertFalse(xDoc.hasStyleIssues());

		AbstractCSSStyleSheet sheet = linkElement.getSheet();
		assertNotNull(sheet);
		assertEquals(3, sheet.getCssRules().getLength());
		//
		int iniSerial = xDoc.getStyleCacheSerial();
		linkElement.attribute("href").setValue("jar:http://www.example.com/evil.jar!/file");
		sheet = linkElement.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xDoc.hasStyleIssues());

		iniSerial++;
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		// Setting BASE does not change things
		xDoc.getErrorHandler().reset();
		CSSStylableElement base = xDoc.getDocumentFactory().createElement("base");
		base.setAttribute("href", "jar:http://www.example.com/evil.jar!/dir/file");
		headElement.appendChild(base);
		sheet = linkElement.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xDoc.hasStyleIssues());
		// Set document URI to enable policy enforcement
		xDoc.setDocumentURI("http://www.example.com/example.html");
		//
		xDoc.getErrorHandler().reset();
		linkElement.attribute("href").setValue("file:/dev/zero");
		sheet = linkElement.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xDoc.hasStyleIssues());
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
		//
		xDoc.getErrorHandler().reset();
		linkElement.attribute("href").setValue("http://www.example.com/etc/fakepasswd");
		sheet = linkElement.getSheet();
		assertNull(sheet);
		assertTrue(xDoc.getErrorHandler().hasErrors());
		assertTrue(xDoc.getErrorHandler().hasPolicyErrors());
		assertTrue(xDoc.hasStyleIssues());
		assertEquals(iniSerial, xDoc.getStyleCacheSerial());
	}

}
