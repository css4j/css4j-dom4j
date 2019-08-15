/*

 Copyright (c) 2005-2019, Carlos Amengual.

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

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSValue;
import org.xml.sax.InputSource;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSDeclarationRule;
import io.sf.carte.doc.style.css.om.CSSRuleArrayList;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.MediaRule;

public class XHTMLDocumentTest {
	XHTMLDocument xhtmlDoc;

	@Before
	public void setUp() throws Exception {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleHTMLReader();
		InputSource isrc = new InputSource(re);
		xhtmlDoc = XHTMLDocumentFactoryTest.parseXML(isrc);
		re.close();
	}

	@Test
	public void getDocumentElement() {
		CSSStylableElement elm = xhtmlDoc.getDocumentElement();
		assertNotNull(elm);
		assertEquals("html", elm.getTagName());
	}

	@Test
	public void getElementByIdString() {
		CSSStylableElement elm = xhtmlDoc.getElementById("h1");
		assertNotNull(elm);
		assertEquals("h1", elm.getTagName());
		assertEquals("h1", elm.getId());
	}

	@Test
	public void getStyleSheet() throws Exception {
		int defSz = xhtmlDoc.getDocumentFactory()
				.getDefaultStyleSheet(xhtmlDoc.getComplianceMode()).getCssRules().getLength();
		assertEquals(106, defSz);
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(6, countInternalSheets);
		assertEquals(6, xhtmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css", xhtmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		Iterator<LinkElement> it = xhtmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertEquals("background-color: red;\n", ((CSSStyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules().item(1)).getStyle();
		assertEquals("url('http://www.example.com/css/font/MechanicalBd.otf')", fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CSSValue.CSS_PRIMITIVE_VALUE, ffval.getCssValueType());
		assertEquals(CSSPrimitiveValue.CSS_URI, ((CSSPrimitiveValue) ffval).getPrimitiveType());
		assertTrue(
				sheet.getCssRules().item(2).getMinifiedCssText().startsWith("@font-feature-values Foo Sans,Bar"));
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 1", sheet.getTitle());
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Alter 2", sheet.getTitle());
		assertEquals(1, sheet.getCssRules().getLength());
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals("Default", sheet.getTitle());
		assertEquals(1, sheet.getCssRules().getLength());
		assertTrue(it.hasNext());
		sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertNull(sheet.getTitle());
		assertEquals("print", sheet.getMedia().getMediaText());
		assertEquals(1, sheet.getCssRules().getLength());
		assertEquals(defSz + 20, css.getCssRules().getLength());
	}

	@Test
	public void getStyleSheetXPP3() throws Exception {
		int defSz = xhtmlDoc.getDocumentFactory()
				.getDefaultStyleSheet(xhtmlDoc.getComplianceMode()).getCssRules().getLength();
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(6, countInternalSheets);
		assertEquals(6, xhtmlDoc.getStyleSheets().getLength());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		assertEquals(defSz + 20, css.getCssRules().getLength());
	}

	@Test
	public void getSelectedStyleSheetSet() {
		DOMStringList list = xhtmlDoc.getStyleSheetSets();
		assertEquals(3, list.getLength());
		assertTrue(list.contains("Default"));
		assertTrue(list.contains("Alter 1"));
		assertTrue(list.contains("Alter 2"));
		assertNull(xhtmlDoc.getLastStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("foo");
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Alter 2");
		assertEquals("Alter 2", xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.enableStyleSheetsForSet("Alter 1");
		assertNull(xhtmlDoc.getSelectedStyleSheetSet());
		xhtmlDoc.setSelectedStyleSheetSet("Default");
		assertEquals("Default", xhtmlDoc.getSelectedStyleSheetSet());
		assertEquals("Default", xhtmlDoc.getLastStyleSheetSet());
	}

	@Test
	public void setTargetMedium() throws Exception {
		xhtmlDoc.setTargetMedium("print");
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css.getCssRules());
		AbstractCSSRule rule = null;
		int rlen = css.getCssRules().getLength();
		for (int i = 0; i < rlen; i++) {
			rule = css.getCssRules().item(i);
			if (rule.getType() == CSSRule.MEDIA_RULE) {
				break;
			}
		}
		assertNotNull(rule);
		MediaRule mr = (MediaRule) rule;
		assertEquals("print", mr.getMedia().getMediaText());
		CSSRuleArrayList list = mr.getCssRules();
		assertEquals(1, list.getLength());
		rule = list.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
	}

	@Test
	public void getElementgetStyle() {
		CSSElement elm = xhtmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertEquals("font-family: 'Does Not Exist', Neither; color: navy; ", style.getCssText());
		assertEquals(2, style.getLength());
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(19, styledecl.getLength());
		assertEquals("#000080", styledecl.getPropertyValue("color"));
		assertEquals("21.6pt", styledecl.getPropertyValue("font-size"));
		assertEquals("bold", styledecl.getPropertyValue("font-weight"));
		assertEquals("  foo  bar  ", styledecl.getPropertyValue("content"));
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
		// Error in inline style
		style.setCssText("width:calc(80%-)");
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		StyleDeclarationErrorHandler eh = xhtmlDoc.getErrorHandler().getInlineStyleErrorHandler(elm);
		assertNotNull(eh);
		assertTrue(eh.hasErrors());
	}

	@Test
	public void getElementgetComputedStylePresentationalAttribute() {
		CSSElement elm = xhtmlDoc.getElementById("fooimg");
		assertNotNull(elm);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("200px", styledecl.getPropertyValue("width"));
		assertEquals("180px", styledecl.getPropertyValue("height"));
		elm.setAttribute("style", "width: 220px; height: 193px;");
		styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(2, styledecl.getLength());
		assertEquals("220px", styledecl.getPropertyValue("width"));
		assertEquals("193px", styledecl.getPropertyValue("height"));
		// Check error handling
		CSSElement parent = (CSSElement) elm.getParentNode();
		parent.setAttribute("bgcolor", "#90fz77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(11, styledecl.getLength());
		assertEquals("rgb(0 0 0 / 0)", styledecl.getPropertyValue("background-color"));
		assertTrue(sheet.getErrorHandler().hasOMErrors());
		sheet.getErrorHandler().resetComputedStyleErrors();
		parent.setAttribute("bgcolor", "#90ff77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("#90ff77", styledecl.getPropertyValue("background-color"));
		assertFalse(sheet.getErrorHandler().hasOMErrors());
	}

	@Test
	public void testGetContentFromStyleElement() {
		XHTMLElement elm = xhtmlDoc.getElementById("firstH3");
		assertNotNull(elm);
		ComputedCSSStyle style = elm.getComputedStyle(null);
		assertEquals("  foo  bar  ", style.getPropertyValue("content"));
	}

	@Test
	public void getOverrideStyle() {
		Element elm = xhtmlDoc.getElementById("tablerow1");
		assertTrue(elm instanceof CSSStylableElement);
		CSSStylableElement el = (CSSStylableElement) elm;
		CSSComputedProperties style = el.getComputedStyle();
		assertNotNull(style);
		assertEquals("10px", style.getPropertyValue("margin-top"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 10px; margin-right: 10px; margin-bottom: 10px; margin-left: 10px; ",
				style.getCssText());
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("margin: 16pt; color: red");
		assertEquals("red", xhtmlDoc.getOverrideStyle(elm, null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", xhtmlDoc.getOverrideStyle(elm, null).getCssText());
		style = el.getComputedStyle();
		assertNotNull(style);
		assertEquals("16pt", style.getPropertyValue("margin-top"));
		assertEquals("#f00", style.getPropertyValue("color"));
		assertEquals(
				"display: table-row; vertical-align: middle; border-top-color: #808080; border-right-color: #808080; border-bottom-color: #808080; border-left-color: #808080; unicode-bidi: embed; margin-top: 16pt; margin-right: 16pt; margin-bottom: 16pt; margin-left: 16pt; color: #f00; ",
				style.getCssText());
		assertEquals(
				"display:table-row;vertical-align:middle;border-color:#808080;unicode-bidi:embed;margin:16pt;color:#f00;",
				style.getMinifiedCssText());
	}

	@Test
	public void testCompatComputedStyle() {
		CSSElement elm = xhtmlDoc.getElementById("cell12");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getStyle();
		assertNull(style);
		DocumentCSSStyleSheet sheet = xhtmlDoc.getStyleSheet();
		CSSStyleDeclaration styledecl = sheet.getComputedStyle(elm, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("5pt", styledecl.getPropertyValue("margin-left"));
		assertEquals("4pt", styledecl.getPropertyValue("padding-top"));
		assertEquals("6pt", styledecl.getPropertyValue("padding-left"));
		// Check for non-existing property
		assertNull(styledecl.getPropertyCSSValue("does-not-exist"));
		assertEquals("", styledecl.getPropertyValue("does-not-exist"));
	}

	@Test
	public void testCascade() throws IOException {
		Reader re = DOMCSSStyleSheetFactoryTest.loadSampleUserCSSReader();
		xhtmlDoc.getDocumentFactory().getStyleSheetFactory().setUserStyleSheet(re);
		re.close();
		CSSElement elm = xhtmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("#cd853f", style.getPropertyValue("background-color"));
		assertEquals("#8a2be2", style.getPropertyValue("color"));
		xhtmlDoc.getOverrideStyle(elm, null).setCssText("color: darkmagenta ! important;");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));
	}

	@Test
	public void testStyleElement() {
		StyleElement style = (StyleElement) xhtmlDoc.getElementsByTagName("style").item(0);
		AbstractCSSStyleSheet sheet = style.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == style);
		style.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = style.getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertTrue(sheet2.getCssRules().getLength() > 0);
		style.clearContent();
		style.addText("body {font-size: 14pt; margin-left: 7%;} h1 {font-size: 2.4em;}");
		sheet = style.getSheet();
		assertTrue(sheet2 == sheet);
		assertEquals(2, sheet.getCssRules().getLength());
		assertTrue(sheet.getOwnerNode() == style);
		// Remove child node
		Node text = style.getFirstChild();
		style.removeChild(text);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	public void testLinkElement() {
		LinkElement link = (LinkElement) xhtmlDoc.getElementsByTagName("link").item(0);
		AbstractCSSStyleSheet sheet = link.getSheet();
		assertNotNull(sheet);
		assertEquals(0, sheet.getMedia().getLength());
		assertTrue(sheet.getCssRules().getLength() > 0);
		assertTrue(sheet.getOwnerNode() == link);
		link.setAttribute("media", "screen");
		AbstractCSSStyleSheet sheet2 = link.getSheet();
		assertNotNull(sheet2);
		assertTrue(sheet2 == sheet);
		assertEquals(1, sheet2.getMedia().getLength());
		assertEquals("screen", sheet2.getMedia().item(0));
		assertEquals(sheet.getCssRules().getLength(), sheet2.getCssRules().getLength());
		link.setAttribute("href", "http://www.example.com/css/alter1.css");
		sheet = link.getSheet();
		assertTrue(sheet2 == sheet);
		assertTrue(sheet.getOwnerNode() == link);
		assertTrue(sheet.getCssRules().item(sheet.getCssRules().getLength() - 1)
				.equals(sheet2.getCssRules().item(sheet2.getCssRules().getLength() - 1)));
	}

	@Test
	public void testBaseElement() {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		CSSElement base = (CSSElement) xhtmlDoc.getElementsByTagName("base").item(0);
		base.setAttribute("href", "http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xhtmlDoc.getBaseURI());
	}

	@Test
	public void testMetaElementDefaultSheetSet() {
		XHTMLElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("http-equiv", "Default-Style");
		meta.setAttribute("content", "Alter 1");
		XHTMLElement head = (XHTMLElement) xhtmlDoc.getElementsByTagName("head").item(0);
		head.appendChild(meta);
		assertEquals("Alter 1", xhtmlDoc.getSelectedStyleSheetSet());
	}

	@Test
	public void testMetaElementReferrerPolicy() {
		assertEquals("same-origin", xhtmlDoc.getReferrerPolicy());
		XHTMLElement meta = xhtmlDoc.createElement("meta");
		meta.setAttribute("name", "referrer");
		meta.setAttribute("content", "origin");
		XHTMLElement head = (XHTMLElement) xhtmlDoc.getElementsByTagName("head").item(0);
		head.appendChild(meta);
		assertEquals("origin", xhtmlDoc.getReferrerPolicy());
	}

	@Test
	public void testIsSafeOrigin() throws MalformedURLException {
		URL url = new URL(xhtmlDoc.getBaseURI());
		assertTrue(xhtmlDoc.isSafeOrigin(url));
	}

}
