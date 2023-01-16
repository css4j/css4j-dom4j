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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;
import io.sf.carte.doc.style.css.property.ValueList;

public class DOM4JCSSStyleDeclarationTest {

	static CSSStyleSheet sheet;

	static XHTMLDocument xhtmlDoc;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		sheet = DOMCSSStyleSheetFactoryTest.loadXHTMLSheet();
	}

	@Before
	public void setUp() throws Exception {
		xhtmlDoc = XHTMLDocumentFactoryTest.sampleXHTML();
	}

	@Test
	public void testGeneric() {
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) node).getComputedStyle();
		assertNotNull(style);
		assertNull(((BaseCSSStyleDeclaration) style).getParentRule());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void getUsedFontFamily1() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		List pList = xhtmlDoc.selectNodes("/*[name()='html']//*[name()='h3']");
		Iterator it = pList.iterator();
		assertTrue(it.hasNext());
		Object elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals("Helvetica", style.getUsedFontFamily());
		pList.clear();
	}

	@Test
	public void getUsedFontFamily2() throws CSSMediaException {
		xhtmlDoc.setTargetMedium("screen");
		Element elm = xhtmlDoc.elementByID("listpara");
		assertNotNull(elm);
		assertTrue(elm instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals("Helvetica", style.getUsedFontFamily());
	}

	@Test
	public void getFontSize1() {
		XHTMLDocument newdoc = xhtmlDoc.getDocumentFactory().createDocument();
		CSSElement root = newdoc.createElement("html");
		newdoc.appendChild(root);
		CSSElement elm = newdoc.createElement("body");
		elm.setAttribute("style", "font-size: 12pt");
		root.appendChild(elm);
		CSSElement h3 = newdoc.createElement("h3");
		elm.appendChild(h3);
		CSSComputedProperties style = newdoc.getStyleSheet().getComputedStyle(h3, null);
		assertNotNull(style);
		CSSStyleRule rule = defaultStyleRule("h3", "font-size");
		assertNotNull(rule);
		CSSTypedValue val = (CSSTypedValue) rule.getStyle().getPropertyCSSValue("font-size");
		assertNotNull(val);
		assertEquals(12f * val.getFloatValue(CSSUnit.CSS_EM), style.getComputedFontSize(), 0.01f);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void getFontSize2() {
		List pList = xhtmlDoc.selectNodes("/*[name()='html']//*[@id='tablepara']/*[name()='span']");
		Iterator it = pList.iterator();
		assertTrue(it.hasNext());
		Object elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals(1.5f * 12f, style.getComputedFontSize(), 0.01f);
		pList.clear();
	}

	@Test
	public void getFontSizeMedia() throws CSSMediaException {
		Element elm = xhtmlDoc.elementByID("span1");
		assertTrue(elm instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals(15f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		Element para = xhtmlDoc.elementByID("para2");
		assertTrue(para instanceof CSSStylableElement);
		CSSComputedProperties stylePara = ((CSSStylableElement) para).getComputedStyle();
		assertNotNull(stylePara);
		assertEquals(12f, stylePara.getComputedFontSize(), 1e-5);

		xhtmlDoc.setTargetMedium("screen");
		assertEquals("screen", xhtmlDoc.getStyleSheet().getTargetMedium());
		style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals(20f, style.getComputedFontSize(), 1e-5);
		assertEquals("#fd8eab", style.getPropertyValue("color"));

		stylePara = ((CSSStylableElement) para).getComputedStyle();
		assertEquals(16f, stylePara.getComputedFontSize(), 1e-5);
		xhtmlDoc.setTargetMedium("all");
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleWarnings());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void getColor() throws CSSPropertyValueException {
		List pList = xhtmlDoc.selectNodes("/*[name()='html']//*[name()='h3']");
		Iterator it = pList.iterator();
		assertTrue(it.hasNext());
		Object elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSStyleDeclaration style = ((CSSStylableElement) elm).getStyle();
		assertNotNull(style);
		CSSTypedValue color = (CSSTypedValue) style.getPropertyCSSValue("color");
		assertEquals("navy", color.getStringValue());
		assertEquals("#000080", color.toRGBColor().toString());
		assertEquals(128, ((CSSTypedValue) color.toRGBColor().getBlue()).getFloatValue(CSSUnit.CSS_NUMBER),
				0.001f);
		assertEquals(0, ((CSSTypedValue) color.toRGBColor().getRed()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		assertEquals(0, ((CSSTypedValue) color.toRGBColor().getGreen()).getFloatValue(CSSUnit.CSS_NUMBER), 0.001f);
		pList.clear();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void nonexistentTag() {
		List pList = xhtmlDoc.selectNodes("/*[name()='html']//*[@id='listpara']/*[name()='nonexistenttag']");
		Iterator it = pList.iterator();
		assertTrue(it.hasNext());
		Object elm = it.next();
		assertTrue(elm instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) elm).getComputedStyle();
		assertNotNull(style);
		assertEquals(12f, style.getComputedFontSize(), 0.01f);
		pList.clear();
	}

	@Test
	public void getBackgroundImages() {
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) node).getComputedStyle();
		assertNotNull(style);
		assertEquals("http://www.example.com/css/background.png", style.getBackgroundImages()[0]);
	}

	@Test
	public void getBackgroundImagesForMedia() {
		try {
			xhtmlDoc.setTargetMedium("print");
		} catch (CSSMediaException e) {
			fail("Failed to change medium: " + e.getMessage());
		}
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) node).getComputedStyle();
		assertNotNull(style);
		assertEquals(2, style.getBackgroundImages().length);
		assertEquals("http://www.example.com/media/printbg.png", style.getBackgroundImages()[0]);
		assertEquals("http://www.example.com/media/printbg2.png", style.getBackgroundImages()[1]);
	}

	@Test
	public void getComputedStyleForBackgroundImages() {
		Node node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		CSSComputedProperties style = ((CSSStylableElement) node).getComputedStyle();
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/css/background.png')", val.getCssText());
		try {
			xhtmlDoc.setTargetMedium("print");
		} catch (CSSMediaException e) {
			fail("Failed to change medium: " + e.getMessage());
		}
		node = xhtmlDoc.getElementsByTagName("body").item(0);
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		style = ((CSSStylableElement) node).getComputedStyle();
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CssType.LIST, val.getCssValueType());
		ValueList list = (ValueList) val;
		assertEquals(2, list.getLength());
		assertEquals("url('http://www.example.com/media/printbg.png')", list.item(0).getCssText());
		assertEquals("url('http://www.example.com/media/printbg2.png')", list.item(1).getCssText());
		assertEquals(
				"url('http://www.example.com/media/printbg.png'), url('http://www.example.com/media/printbg2.png')",
				list.getCssText());
	}

	@Test
	public void getComputedStyleForBackgroundImagesInStyleAttribute() {
		Node node = xhtmlDoc.getElementById("h1");
		assertNotNull(node);
		assertTrue(node instanceof CSSStylableElement);
		CSSStylableElement elt = (CSSStylableElement) node;
		CSSComputedProperties style = elt.getComputedStyle();
		assertNotNull(style);
		CSSValue val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/headerbg.png')", val.getCssText());
		elt.getOverrideStyle(null).setCssText("background: url('override.png')");
		style = elt.getComputedStyle();
		assertNotNull(style);
		val = style.getPropertyCSSValue("background-image");
		assertNotNull(val);
		assertEquals(CSSValue.CssType.TYPED, val.getCssValueType());
		assertEquals("url('http://www.example.com/override.png')", val.getCssText());
	}

	CSSStyleRule defaultStyleRule(String selectorText, String propertyName) {
		CSSRuleList rules = sheet.getCssRules();
		for (int i = 0; i < rules.getLength(); i++) {
			CSSRule rule = rules.item(i);
			if (rule instanceof CSSStyleRule) {
				String selText = ((CSSStyleRule) rule).getSelectorText();
				// Small hack
				StringTokenizer st = new StringTokenizer(selText, ",");
				while (st.hasMoreElements()) {
					String selector = st.nextToken();
					if (selector.equals(selectorText)) {
						if (((CSSStyleRule) rule).getStyle().getPropertyCSSValue(propertyName) != null) {
							return ((CSSStyleRule) rule);
						}
						break;
					}
				}
			}
		}
		return null;
	}

}
