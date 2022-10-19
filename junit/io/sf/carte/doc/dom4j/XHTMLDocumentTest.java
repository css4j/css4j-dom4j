/*

 Copyright (c) 2005-2022, Carlos Amengual.

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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSUnit;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValueSyntax;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.InputSource;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSDeclarationRule;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CSSOMParser;
import io.sf.carte.doc.style.css.om.CSSRuleArrayList;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.DOMCSSStyleSheetFactoryTest;
import io.sf.carte.doc.style.css.om.MediaRule;
import io.sf.carte.doc.style.css.om.PropertyCountVisitor;
import io.sf.carte.doc.style.css.om.StyleCountVisitor;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.SyntaxParser;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.TypedValue;

public class XHTMLDocumentTest {

	XHTMLDocument xhtmlDoc;

	@Before
	public void setUp() throws Exception {
		Reader re = DOMCSSStyleSheetFactoryTest.sampleHTMLReader();
		org.xml.sax.InputSource isrc = new org.xml.sax.InputSource(re);
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
		int defSz = xhtmlDoc.getDocumentFactory().getDefaultStyleSheet(xhtmlDoc.getComplianceMode())
			.getCssRules().getLength();
		assertEquals(113, defSz);
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(7, countInternalSheets);
		assertEquals(7, xhtmlDoc.getStyleSheets().getLength());
		assertEquals("http://www.example.com/css/common.css",
			xhtmlDoc.getStyleSheets().item(0).getHref());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());

		Iterator<LinkElement> it = xhtmlDoc.linkedStyle.iterator();
		assertTrue(it.hasNext());
		AbstractCSSStyleSheet sheet = it.next().getSheet();
		assertNotNull(sheet);
		assertEquals(null, sheet.getTitle());
		assertEquals(3, sheet.getCssRules().getLength());
		assertEquals("background-color: red;\n",
			((CSSStyleRule) sheet.getCssRules().item(0)).getStyle().getCssText());
		AbstractCSSStyleDeclaration fontface = ((BaseCSSDeclarationRule) sheet.getCssRules()
			.item(1)).getStyle();
		assertEquals("url('http://www.example.com/fonts/OpenSans-Regular.ttf')",
			fontface.getPropertyValue("src"));
		CSSValue ffval = fontface.getPropertyCSSValue("src");
		assertEquals(CSSValue.CssType.TYPED, ffval.getCssValueType());
		assertEquals(CSSValue.Type.URI, ((CSSTypedValue) ffval).getPrimitiveType());
		assertTrue(sheet.getCssRules().item(2).getMinifiedCssText()
			.startsWith("@font-feature-values Foo Sans,Bar"));

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

		assertEquals(defSz + 25, css.getCssRules().getLength());
		assertFalse(xhtmlDoc.getStyleSheet().getErrorHandler().hasSacErrors());
	}

	@Test
	public void getStyleSheetXPP3() throws Exception {
		int defSz = xhtmlDoc.getDocumentFactory().getDefaultStyleSheet(xhtmlDoc.getComplianceMode()).getCssRules()
				.getLength();
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		int countInternalSheets = xhtmlDoc.embeddedStyle.size() + xhtmlDoc.linkedStyle.size();
		assertEquals(7, countInternalSheets);
		assertEquals(7, xhtmlDoc.getStyleSheets().getLength());
		assertEquals(3, xhtmlDoc.getStyleSheetSets().getLength());
		assertEquals(defSz + 25, css.getCssRules().getLength());
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
		assertEquals(CSSRule.MEDIA_RULE, rule.getType());
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
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasIOErrors());
		xhtmlDoc.getErrorHandler().reset();
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
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(parent));
		xhtmlDoc.getErrorHandler().reset();
		parent.setAttribute("bgcolor", "#90ff77");
		styledecl = sheet.getComputedStyle(parent, null);
		assertEquals(12, styledecl.getLength());
		assertEquals("#90ff77", styledecl.getPropertyValue("background-color"));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
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
		el.getOverrideStyle(null).setCssText("margin: 16pt; color: red");
		assertEquals("red", el.getOverrideStyle(null).getPropertyValue("color"));
		assertEquals("margin: 16pt; color: red; ", el.getOverrideStyle(null).getCssText());
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
	public void testComputedStyleRegisteredProperties() throws CSSParseException, IOException {
		// Prepare property definition
		SyntaxParser syntaxParser = new SyntaxParser();
		CSSValueSyntax syn = syntaxParser.parseSyntax("<length>");
		//
		CSSOMParser parser = new CSSOMParser();
		LexicalUnit lunit = parser.parsePropertyValue(new StringReader("15pt"));
		LexicalValue value = new LexicalValue();
		value.setLexicalUnit(lunit);
		CSSPropertyDefinition pdef = xhtmlDoc.getStyleSheet().getStyleSheetFactory().createPropertyDefinition("--foo",
				syn, false, value);
		xhtmlDoc.registerProperty(pdef);
		//
		XHTMLElement elm = xhtmlDoc.getElementById("div1");
		assertNotNull(elm);
		/*
		 * custom property substitution.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		CSSComputedProperties style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,7pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(7f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,1vb);--foo:8pt");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		//
		XHTMLElement listpara = xhtmlDoc.getElementById("listpara");
		listpara.getOverrideStyle(null).setCssText("font-size:var(--foo,19pt)");
		style = listpara.getComputedStyle(null);
		CSSTypedValue customProperty = (CSSTypedValue) style.getPropertyCSSValue("--foo");
		assertNotNull(customProperty);
		assertEquals(15f, customProperty.getFloatValue(CSSUnit.CSS_PT), 1e-6);
		CSSTypedValue fontSize = (CSSTypedValue) style.getPropertyCSSValue("font-size");
		assertEquals(19f, fontSize.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(listpara));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		XHTMLElement body = (XHTMLElement) elm.getParentNode();
		body.getOverrideStyle(null).setCssText("--foo:9pt");
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * Same as above, with custom property set in parent style, fallback
		 */
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		body = (XHTMLElement) elm.getParentNode();
		elm.getOverrideStyle(null).setCssText("margin-left:var(--foo,21pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(21f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		/*
		 * custom property substitution, var() in fallback, fallback-of-fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:var(--no-way,var(--foo,17pt));");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(17f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors(elm));
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
	}

	@Test
	public void testComputedStyleAttr() {
		XHTMLElement elm = xhtmlDoc.getElementById("firstH3");
		/*
		 * attr() value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin,.6em)");
		ComputedCSSStyle style = elm.getComputedStyle(null);
		CSSTypedValue marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(12.96f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value in calc(), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin,.6em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(25.92f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, do not reparse.
		 */
		elm.setAttribute("leftmargin", " .8em");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSValue.Type.STRING, marginLeft.getPrimitiveType());
		assertEquals(" .8em", marginLeft.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, expected type.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin length)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(17.28f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value, string expected type, do not reparse.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin string)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(CSSValue.Type.STRING, marginLeft.getPrimitiveType());
		assertEquals(" .8em", marginLeft.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() value in calc().
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:calc(attr(leftmargin length,.6em)*2)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(34.56f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (em vs color).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin color)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Currentcolor
		assertEquals(CSSValue.Type.COLOR, marginLeft.getPrimitiveType());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (color), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin color,.4em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.64f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (number).
		 */
		elm.setAttribute("leftmargin", "0.3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (number).
		 */
		elm.setAttribute("leftmargin", "0.3px");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (integer).
		 */
		elm.setAttribute("leftmargin", "0.3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin integer)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (integer).
		 */
		elm.setAttribute("leftmargin", "3");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin integer)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_NUMBER), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (angle-deg).
		 */
		elm.setAttribute("leftmargin", "3deg");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin angle)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_DEG), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (angle-rad).
		 */
		elm.setAttribute("leftmargin", "3rad");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin angle)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_RAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (time).
		 */
		elm.setAttribute("leftmargin", "3s");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin time)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_S), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (time II).
		 */
		elm.setAttribute("leftmargin", "3ms");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin time)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(3f, marginLeft.getFloatValue(CSSUnit.CSS_MS), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (frequency).
		 */
		elm.setAttribute("leftmargin", "0.3Hz");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin frequency)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_HZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (frequency II).
		 */
		elm.setAttribute("leftmargin", "0.3kHz");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin frequency)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(0.3f, marginLeft.getFloatValue(CSSUnit.CSS_KHZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (pt).
		 */
		elm.setAttribute("leftmargin", "15 ");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin pt)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (px).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin px)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(15f, marginLeft.getFloatValue(CSSUnit.CSS_PX), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (em).
		 */
		elm.setAttribute("leftmargin", "1.6 ");
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(34.56f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (deg).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin deg)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_DEG), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (grad).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin grad)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_GRAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (rad).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin rad)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_RAD), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (s).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin s)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_S), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (ms).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin ms)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_MS), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (Hz).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin Hz)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_HZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unit-value type (kHz).
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin kHz)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("foo");
		assertEquals(1.6f, marginLeft.getFloatValue(CSSUnit.CSS_KHZ), 1e-5);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() unknown unit-value type.
		 */
		elm.getOverrideStyle(null).setCssText("foo:attr(leftmargin foo)");
		style = elm.getComputedStyle(null);
		assertNull(style.getPropertyCSSValue("foo"));
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() unknown unit-value type (II).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin foo)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 1e-5);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() valid value type (uri).
		 */
		elm.setAttribute("myuri", "https://www.example.com/foo");
		elm.getOverrideStyle(null).setCssText("background-image:attr(myuri url)");
		style = elm.getComputedStyle(null);
		CSSTypedValue value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("https://www.example.com/foo", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() valid value type (relative uri).
		 */
		elm.setAttribute("myuri", "foo");
		elm.getOverrideStyle(null).setCssText("background-image:attr(myuri url)");
		style = elm.getComputedStyle(null);
		value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("http://www.example.com/foo", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() missing url value, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("background-image:attr(noattr url,\"https://www.example.com/bar\")");
		style = elm.getComputedStyle(null);
		value = (CSSTypedValue) style.getPropertyCSSValue("background-image");
		assertEquals(CSSValue.Type.URI, value.getPrimitiveType());
		assertEquals("https://www.example.com/bar", value.getStringValue());
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() invalid value type (number).
		 */
		elm.setAttribute("leftmargin", "foo");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin number)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Default fallback
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() invalid value type (ident vs number), fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin number,.4em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(8.64f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors();
		/*
		 * attr() percentage invalid value type.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin percentage)");
		assertEquals("margin-left: attr(leftmargin percentage); ", elm.getOverrideStyle(null).getCssText());
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		// Default fallback
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() percentage invalid value type, fallback.
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin percentage, 1.2em)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(25.92f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() percentage value type.
		 */
		elm.setAttribute("leftmargin", "2");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin percentage)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() percentage value type (II).
		 */
		elm.setAttribute("leftmargin", "2%");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin percentage)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(2f, marginLeft.getFloatValue(CSSUnit.CSS_PERCENTAGE), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		/*
		 * attr() recursive.
		 */
		elm.setAttribute("leftmargin", "attr(leftmargin length)");
		elm.getOverrideStyle(null).setCssText("margin-left:attr(leftmargin length)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() recursive with custom property (I).
		 */
		elm.getOverrideStyle(null)
				.setCssText("margin-left:attr(noattr length,var(--foo));--foo:attr(noattr,var(margin-left))");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertTrue(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertTrue(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
		xhtmlDoc.getErrorHandler().resetComputedStyleErrors(elm);
		/*
		 * attr() recursive with custom property (II).
		 */
		elm.getOverrideStyle(null).setCssText("margin-left:attr(noattr length,var(--foo));--foo:attr(noattr length)");
		style = elm.getComputedStyle(null);
		marginLeft = (CSSTypedValue) style.getPropertyCSSValue("margin-left");
		assertEquals(0f, marginLeft.getFloatValue(CSSUnit.CSS_PT), 0.01f);
		assertFalse(xhtmlDoc.getErrorHandler().hasComputedStyleErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasErrors());
		assertFalse(xhtmlDoc.getErrorHandler().hasWarnings());
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
		elm.getOverrideStyle(null).setCssText("color: darkmagenta ! important;");
		style = xhtmlDoc.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(style);
		assertEquals("#8a2be2", style.getPropertyValue("color"));
	}

	@Test
	public void testCascade2() throws IOException {
		BaseCSSStyleSheet sheet = (BaseCSSStyleSheet) xhtmlDoc.getStyleSheets().item(5);

		// Obtain the rule where a value is declared
		CSSParser parser = new CSSParser();
		SelectorList selist = parser.parseSelectors("p.boldmargin");
		StyleRule rule = (StyleRule) sheet.getFirstStyleRule(selist);
		assertNotNull(rule);

		AbstractCSSStyleDeclaration declStyle = rule.getStyle();
		TypedValue declMarginLeft = (TypedValue) declStyle.getPropertyCSSValue("margin-left");
		assertEquals("2%", declMarginLeft.getCssText());

		/*
		 * Get an element that obtains the above value as computed style
		 */
		XHTMLElement elm = xhtmlDoc.getElementById("para1");
		assertNotNull(elm);
		CSSStyleDeclaration style = elm.getComputedStyle(null);
		assertEquals("2%", style.getPropertyValue("margin-left"));

		// Change the value itself
		declMarginLeft.setFloatValue(CSSUnit.CSS_PX, 6f);
		style = elm.getComputedStyle(null);
		assertEquals("6px", style.getPropertyValue("margin-left"));

		// Overwrite the property's value
		declStyle.setProperty("margin-left", "4px", null);
		style = elm.getComputedStyle(null);
		// The new value is not there yet
		assertEquals("6px", style.getPropertyValue("margin-left"));

		// Rebuild the cascade
		xhtmlDoc.rebuildCascade();
		style = elm.getComputedStyle(null);
		assertEquals("4px", style.getPropertyValue("margin-left"));
	}

	@Test
	public void testVisitors() throws IOException {
		StyleCountVisitor visitor = new StyleCountVisitor();
		xhtmlDoc.getStyleSheets().acceptStyleRuleVisitor(visitor);
		assertEquals(29, visitor.getCount());
		//
		PropertyCountVisitor visitorP = new PropertyCountVisitor();
		xhtmlDoc.getStyleSheets().acceptDeclarationRuleVisitor(visitorP);
		assertEquals(111, visitorP.getCount());
		//
		visitorP.reset();
		xhtmlDoc.getStyleSheets().acceptDescriptorRuleVisitor(visitorP);
		assertEquals(2, visitorP.getCount());
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
	public void testBaseElement() throws MalformedURLException {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		CSSElement base = (CSSElement) xhtmlDoc.getElementsByTagName("base").item(0);
		base.setAttribute("href", "http://www.example.com/newbase/");
		assertEquals("http://www.example.com/newbase/", xhtmlDoc.getBaseURI());
		// Wrong URL
		base.setAttribute("href", "http//");
		assertNull(xhtmlDoc.getBaseURI());
		assertEquals("http//", base.getAttribute("href"));
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
		// Relative URL
		base.setAttribute("href", "foo");
		assertEquals("foo", base.getAttribute("href"));
		assertNull(xhtmlDoc.getBaseURI());
		// Remove attribute
		base.removeAttribute("href");
		assertNull(xhtmlDoc.getBaseURI());
		// Unsafe base assignment
		xhtmlDoc.setDocumentURI("http://www.example.com/document.html");
		assertEquals("http://www.example.com/document.html", xhtmlDoc.getDocumentURI());
		base.setAttribute("href", "jar:http://www.example.com/evil.jar!/file");
		assertTrue(xhtmlDoc.getErrorHandler().hasPolicyErrors());
		assertEquals("http://www.example.com/document.html", xhtmlDoc.getBaseURI());
		assertEquals("jar:http://www.example.com/evil.jar!/file", base.getAttribute("href"));
	}

	@Test
	public void testBaseElement2() throws MalformedURLException {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		// Set documentURI and then unsafe attribute
		xhtmlDoc.setDocumentURI("http://www.example.com/doc-uri");
		CSSElement base = (CSSElement) xhtmlDoc.getElementsByTagName("base").item(0);
		// Relative URL
		base.setAttribute("href", "foo");
		assertEquals("foo", base.getAttribute("href"));
		assertEquals("http://www.example.com/foo", xhtmlDoc.getBaseURI());
		// Wrong URL
		base.setAttribute("href", "foo://");
		assertEquals("foo://", base.getAttribute("href"));
		assertEquals("http://www.example.com/doc-uri", xhtmlDoc.getBaseURI());
		assertTrue(xhtmlDoc.getErrorHandler().hasIOErrors());
		// Remove attribute
		base.removeAttribute("href");
		assertEquals("http://www.example.com/doc-uri", xhtmlDoc.getBaseURI());
		// Unsafe base assignment
		base.setAttribute("href", "jar:http://www.example.com/evil.jar!/file");
		assertEquals("http://www.example.com/doc-uri", xhtmlDoc.getBaseURI());
		assertEquals("jar:http://www.example.com/evil.jar!/file", base.getAttribute("href"));
		assertTrue(xhtmlDoc.getErrorHandler().hasPolicyErrors());
	}

	@Test
	public void testSetBaseURL() throws MalformedURLException {
		assertEquals("http://www.example.com/", xhtmlDoc.getBaseURI());
		xhtmlDoc.setBaseURL(new URL("http://www.example.com/newbase/"));
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

	/*
	 * It is not encouraged to use the addStyleSheet old method, but there are
	 * people using it.
	 */
	@Test
	public void testAddStyleSheet() throws IOException {
		DocumentCSSStyleSheet css = xhtmlDoc.getStyleSheet();
		assertNotNull(css);
		assertNotNull(css.getCssRules());
		assertEquals(138, css.getCssRules().getLength());
		StringReader re = new StringReader("p{color:#a1e3f0}#noid{margin:0.04em;}");
		InputSource cssSrc = new InputSource(re);
		xhtmlDoc.addStyleSheet(cssSrc);
		assertEquals(140, css.getCssRules().getLength());
	}

}
