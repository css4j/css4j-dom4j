/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.TestConfig;
import io.sf.carte.doc.style.css.CSSComputedProperties;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractSelectorMatcher;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CSSOMBridge;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory.DummyCanvas;
import io.sf.carte.doc.style.css.om.StyleRule;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class DOM4JSelectorMatcherTest {

	private static XHTMLDocumentFactory factory;
	private CSSParser cssParser;
	private XHTMLDocument document;

	@BeforeAll
	public static void setUpBeforeClass() {
		factory = new TestDocumentFactory();
	}

	@BeforeEach
	public void setUp() {
		this.cssParser = new CSSParser();
		CSSStylableElement root = factory.createElement("html");
		document = factory.createDocument(root);
	}

	@Test
	public void testMatchSelectorUniversal() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("* {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector1Element() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertEquals(0, matcher.matches(selist));

		CSSStylableElement div = createElement("div");
		SelectorMatcher divmatcher = div.getSelectorMatcher();
		assertEquals(-1, divmatcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementUppercase() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("P");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementPrefix() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		String nsURI = "http://www.example.com/ns";
		CSSStylableElement elm = factory.createElement("pre:P", nsURI);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ElementNS() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"@namespace svg url('http://www.w3.org/2000/svg'); p {color: blue;} svg|svg {margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();
		SelectorList svgselist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		CSSStylableElement svg = factory.createElement("svg", TestConfig.SVG_NAMESPACE_URI);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		SelectorMatcher svgmatcher = selectorMatcher(svg);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);

		assertTrue(svgmatcher.matches(selist) == -1);
		assertTrue(matcher.matches(svgselist) == -1);
		selidx = svgmatcher.matches(svgselist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), svgmatcher);
	}

	@Test
	public void testMatchSelector1ElementNoNS() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"@namespace url('https://www.w3.org/1999/xhtml/'); p{color: blue;} |div{margin-left: 5pt;}");
		SelectorList selist = ((StyleRule) css.getCssRules().item(1)).getSelectorList();

		XHTMLDocument xdoc = factory.createDocument("", null, null);

		CSSStylableElement root = xdoc.createElement("html");
		xdoc.appendChild(root);

		CSSStylableElement p = xdoc.createElement("p");
		p.setAttribute("id", "p1");
		root.appendChild(p);
		CSSStylableElement pns = xdoc.createElementNS("https://www.w3.org/1999/xhtml/", "p");
		pns.setAttribute("id", "p2");
		root.appendChild(pns);
		CSSStylableElement div = xdoc.createElement("div");
		div.setAttribute("id", "div1");
		root.appendChild(div);
		CSSStylableElement divns = xdoc.createElementNS("https://www.w3.org/1999/xhtml/", "div");
		divns.setAttribute("id", "div2");
		root.appendChild(divns);

		assertFalse(p.matches(selist, null));
		assertTrue(pns.matches(selist, null));

		selist = ((StyleRule) css.getCssRules().item(2)).getSelectorList();
		assertFalse(divns.matches(selist, null));
		assertTrue(div.matches(selist, null));
	}

	@Test
	public void testMatchSelector1Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorUniversalAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("*[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorTypeAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1AttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("TITLE", "hi");
		assertTrue(matcher.matches(selist) >= 0);

		elm.removeAttribute("TITLE");
		assertFalse(elm.hasAttributes());
		elm.setAttributeNS("http://www.example.com/examplens", "Title", "hi");
		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2AttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("Title", "hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.removeAttribute("Title");
		assertFalse(elm.hasAttributes());
		elm.setAttributeNS("http://www.example.com/examplens", "Title", "hi");
		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorOneOfAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "ho hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorOneOfAttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("Title", "ho hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorBeginHyphenAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[lang|=\"en\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("lang", "en-US");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorBeginHyphenAttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[lang|=\"en\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("lang", "EN-US");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("lang", "EN");
		assertTrue(matcher.matches(selist) >= 0);

		elm.setAttribute("lang", "en_US");
		assertTrue(matcher.matches(selist) < 0);

		elm = createElement("div");
		elm.setAttribute("lang", "en");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginsAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title^=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "hi");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorBeginsAttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title^=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "h");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorEndsAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title$=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorEndsAttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title$=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "i");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorSubstringAttribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title*=\"hi\"] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "ho hi ho");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorSubstringAttributeCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title*=\"hi\" i] {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("title", "HO HI HO");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("title", "HI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "HI HO");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "HOHI");
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);

		elm.setAttribute("title", "H");
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("div");
		elm.setAttribute("title", "hi");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLang() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(en) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("lang", "en-US");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorLangString() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(de,'en-US') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangStringDQ() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(de, \"en-US\") {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	/*
	 * Verify that a wrong selector serializes correctly
	 */
	@Test
	public void testMatchSelectorLangStringBadLang() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(de,'1-US') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLang2() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(en-US) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "en-US");
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("lang", "en-US");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(\\*) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRangeString() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang('*') {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRangeStringDQ() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(\"*\") {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "en-GB");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange2() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(fr,\\*-Latn) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "de-Latn");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorLangRange3() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(fr-FR,de-\\*-DE) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE");
		SelectorMatcher matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-DE");
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("lang", "de-Latn-DE-1996");
		matcher = selectorMatcher(elm);
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		// Case insensitive
		elm.setAttribute("class", "exampleClass");
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ClassCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(".exampleClass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);

		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		// Case insensitive
		elm.setAttribute("class", "exampleClass");
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector1ClassStrict() throws Exception {
		DocumentType docType = factory.createDocType("html", null, null);
		document.setDocType(docType);
		AbstractCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);

		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		// Case sensitive
		elm.setAttribute("class", "exampleClass");
		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorMultipleClass() throws Exception {
		// QUIRKS
		AbstractCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		document.getDocumentElement().add(elm);

		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) >= 0);

		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		document.getDocumentElement().add(elm);

		matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);

		// STRICT
		DocumentType docType = factory.createDocType("html", null, null);
		document.setDocType(docType);
		css = parseStyle(".exampleclass {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();

		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleClass barclass");
		document.getDocumentElement().add(elm);

		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));

		elm = createElement("p");
		elm.setAttribute("class", "fooclass exampleclass barclass");
		document.getDocumentElement().add(elm);

		matcher = selectorMatcher(elm);
		selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelector2Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("z.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector3Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("p");
		parent.addAttribute("class", "exampleclass");
		parent.addAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("span");
		elm.addAttribute("id", "childid1");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("b");
		elm.addAttribute("id", "childid2");
		parent.add(elm);
		matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		CSSStylableElement child2 = createElement("span");
		child2.addAttribute("id", "grandchildid1");
		elm.add(child2);
		matcher = child2.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass secondclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.secondclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass secondclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorTwoClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.secondclass.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.setAttribute("class", "firstclass secondclass thirdclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorTwoClassesTwoPseudo() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"p.secondclass::before.firstclass::first-line {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.setAttribute("class", "firstclass secondclass thirdclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		Condition pseudo = cssParser.parsePseudoElement("::before::first-line");
		matcher.setPseudoElement(pseudo);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 3, selist.item(selidx), matcher);

		// Fail the pseudo-element match
		pseudo = cssParser.parsePseudoElement("::first-line");
		matcher.setPseudoElement(pseudo);
		selidx = matcher.matches(selist);
		assertTrue(selidx == -1);
	}

	@Test
	public void testMatchSelector3MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", " firstclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass ");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Firstchild() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.addAttribute("id", "parentid");
		CSSStylableElement firstChild = createElement("p");
		firstChild.addAttribute("id", "pid1");
		parent.add(firstChild);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("id", "pid2");
		parent.add(elm);
		SelectorMatcher matcher = firstChild.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector1Id() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		elm.addAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		// Case insensitive
		elm.addAttribute("id", "exampleId");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1IdCI() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("#exampleId {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		elm.addAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		// Case insensitive
		elm.addAttribute("id", "exampleId");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1IdCIStrict() throws Exception {
		DocumentType docType = factory.createDocType("html", null, null);
		document.setDocType(docType);
		AbstractCSSStyleSheet css = parseStyle("#exampleId {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		elm.addAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		// Case insensitive
		elm.addAttribute("id", "exampleId");
		matcher = elm.getSelectorMatcher();
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelector2Id() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("p");
		parent.addAttribute("class", "exampleclass");
		parent.addAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("span");
		elm.addAttribute("id", "childid1");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("b");
		elm.addAttribute("id", "childid2");
		parent.add(elm);
		matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		CSSStylableElement child2 = createElement("span");
		child2.addAttribute("id", "grandchildid1");
		elm.add(child2);
		matcher = child2.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3Id() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("#exampleid > span {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("p");
		parent.addAttribute("class", "exampleclass");
		parent.addAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("span");
		elm.addAttribute("id", "childid");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		elm = createElement("b");
		parent.add(elm);
		matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorTypeId() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p#exampleid {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement elm = createElement("p");
		elm.setAttribute("class", "exampleclass");
		elm.setAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);

		elm = createElement("div");
		elm.setAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		matcher = selectorMatcher(elm);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorAdjacentSibling() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass + p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		document.getDocumentElement().add(parent);
		parent.addAttribute("id", "div1");
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("id", "childid1");
		elm.addAttribute("class", "exampleclass");
		parent.add(elm);
		elm = createElement("p");
		elm.addAttribute("id", "childid2");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorSubsequentSibling() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass ~ p {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("p.exampleclass~p", CSSOMBridge.selectorListToString(selist, rule));
		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().appendChild(parent);
		parent.appendChild(document.createElement("pre"));
		CSSStylableElement elm = document.createElement("p");
		elm.setAttribute("id", "childidp1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		parent.appendChild(document.createElement("pre"));
		CSSStylableElement pre = document.createElement("pre");
		parent.appendChild(pre);
		elm = document.createElement("p");
		elm.setAttribute("id", "childidp2");
		parent.appendChild(elm);
		assertTrue(elm.matches(selist, null));
		assertFalse(pre.matches(selist, null));

		SelectorMatcher matcher = elm.getSelectorMatcher();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorDescendant() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("ul li a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("ul li a", CSSOMBridge.selectorListToString(selist, rule));
		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().appendChild(parent);
		CSSStylableElement ul = document.createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSStylableElement li = document.createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSStylableElement p = document.createElement("p");
		li.appendChild(p);
		CSSStylableElement a = document.createElement("a");
		a.setAttribute("id", "a1");
		p.appendChild(a);
		CSSStylableElement a2 = document.createElement("a");
		a2.setAttribute("id", "a2");
		p.appendChild(a2);
		assertFalse(p.matches(selist, null));
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSStylableElement li2 = document.createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSStylableElement a3 = document.createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));

		SelectorMatcher matcher = a3.getSelectorMatcher();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 3, selist.item(selidx), matcher);

		CSSStylableElement span = document.createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorChild() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("*>ul>li>a{padding:20px}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals("*>ul>li>a", CSSOMBridge.selectorListToString(selist, rule));
		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().appendChild(parent);
		CSSStylableElement ul = document.createElement("ul");
		ul.setAttribute("id", "ul1");
		parent.appendChild(ul);
		CSSStylableElement li = document.createElement("li");
		li.setAttribute("id", "li1");
		ul.appendChild(li);
		CSSStylableElement a = document.createElement("a");
		a.setAttribute("id", "a1");
		li.appendChild(a);
		CSSStylableElement a2 = document.createElement("a");
		a2.setAttribute("id", "a2");
		li.appendChild(a2);
		assertTrue(a.matches(selist, null));
		assertTrue(a2.matches(selist, null));
		CSSStylableElement li2 = document.createElement("li");
		li2.setAttribute("id", "li2");
		ul.appendChild(li2);
		CSSStylableElement a3 = document.createElement("a");
		a3.setAttribute("id", "a3");
		li.appendChild(a3);
		assertTrue(a3.matches(selist, null));

		SelectorMatcher matcher = a3.getSelectorMatcher();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 3, selist.item(selidx), matcher);

		CSSStylableElement span = document.createElement("span");
		a3.appendChild(span);
		assertFalse(span.matches(selist, null));
	}

	@Test
	public void testMatchSelectorPseudoClass1() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		parent.add(factory.createElement("p"));
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);

		css = parseStyle("p:last-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);

		css = parseStyle("p:only-child {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassNth() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-child(1) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		assertNotNull(rule);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		parent.add(factory.createElement("p"));
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);

		css = parseStyle("p:nth-last-child(1) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);

		css = parseStyle("p:nth-last-child(2) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
		parent.add(createElement("div"));

		css = parseStyle("p:nth-last-child(3) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassNthSelector() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-child(1 of p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		assertNotNull(rule);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.add(createElement("div"));
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		parent.add(createElement("p"));
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);

		css = parseStyle("p:nth-last-child(2 of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);

		css = parseStyle("p:nth-child(even of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);

		css = parseStyle("p:nth-child(odd of p) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassNthOfType() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-of-type(1) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		assertNotNull(rule);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.add(createElement("div"));
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertEquals(0, matcher.matches(selist));

		parent.add(createElement("P"));
		assertEquals(0, matcher.matches(selist));

		css = parseStyle("p:nth-last-of-type(2) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(0, matcher.matches(selist));

		css = parseStyle("p:nth-of-type(even) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("p:nth-of-type(odd) {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(0, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		CSSStylableElement parent = createElement("div");
		parent.add(createElement("div"));
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();

		AbstractCSSStyleSheet css = parseStyle("p:only-of-type {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList onlyOfTypeSel = rule.getSelectorList();
		assertEquals(0, matcher.matches(onlyOfTypeSel));

		parent.add(createElement("P"));
		assertEquals(-1, matcher.matches(onlyOfTypeSel));

		css = parseStyle("p:first-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(0, matcher.matches(selist));
		parent.insertBefore(createElement("p"), elm);
		assertEquals(-1, matcher.matches(selist));

		css = parseStyle("p:last-of-type {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		assertEquals(-1, matcher.matches(selist));
		CSSStylableElement lastp = createElement("p");
		parent.appendChild(lastp);
		CSSStylableElement lastdiv = createElement("div");
		parent.appendChild(lastdiv);
		assertEquals(0, lastp.getSelectorMatcher().matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoAnyLink() throws Exception {
		CSSStylableElement a = createElement("a");
		a.setAttribute("href", "foo");
		CSSStylableElement elm = document.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = a.getSelectorMatcher();
		AbstractCSSStyleSheet css = parseStyle(":any-link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(0, matcher.matches(selist));

		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));

		a.removeAttribute("href");
		matcher = a.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoLink() throws Exception {
		CSSStylableElement a = createElement("a");
		a.setAttribute("href", "foo");
		CSSStylableElement elm = document.getDocumentElement();
		elm.appendChild(a);
		SelectorMatcher matcher = a.getSelectorMatcher();
		AbstractCSSStyleSheet css = parseStyle(":link {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertEquals(0, matcher.matches(selist));

		matcher = elm.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		elm.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "bar");
		assertEquals(0, matcher.matches(selist));

		a.removeAttribute("href");
		matcher = a.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoRoot() throws Exception {
		CSSStylableElement root = document.getDocumentElement();
		root.add(createElement("div"));
		CSSStylableElement div = createElement("div");
		root.add(div);
		AbstractCSSStyleSheet css = parseStyle(":root {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(root.getSelectorMatcher().matches(selist) >= 0);
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoEmpty() throws Exception {
		CSSStylableElement div = createElement("div");
		document.getDocumentElement().add(div);
		div.add(factory.createText(""));
		AbstractCSSStyleSheet css = parseStyle("div:empty {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(div.getSelectorMatcher().matches(selist) >= 0);
		CSSStylableElement p = createElement("p");
		div.appendChild(p);
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
		div.removeChild(p);
		assertTrue(div.getSelectorMatcher().matches(selist) >= 0);
		div.add(factory.createText("foo"));
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoBlank() throws Exception {
		CSSStylableElement div = createElement("div");
		document.getDocumentElement().add(div);
		div.add(factory.createText("   "));
		AbstractCSSStyleSheet css = parseStyle("div:blank {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(div.getSelectorMatcher().matches(selist) >= 0);
		CSSStylableElement p = createElement("p");
		div.appendChild(p);
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
		div.removeChild(p);
		assertTrue(div.getSelectorMatcher().matches(selist) >= 0);
		div.add(factory.createText("foo"));
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoHas1() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass:has(> img) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("p");
		parent.setAttribute("id", "p1");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		CSSStylableElement elm2 = createElement("img");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		SelectorMatcher matcher = parent.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		assertTrue(matcher.matches(selist) >= 0);

		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas2() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass:has(+ p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("p");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		CSSStylableElement elm2 = createElement("p");
		elm2.setAttribute("id", "childid2");
		parent.appendChild(elm2);
		assertTrue(elm.getSelectorMatcher().matches(selist) < 0);
		elm.setAttribute("class", "exampleclass");
		assertTrue(elm.getSelectorMatcher().matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoHas3() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("div.exampleclass:has(p>span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("div");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("p");
		parent.appendChild(elm);
		CSSStylableElement span = createElement("span");
		elm.appendChild(span);
		SelectorMatcher matcher = parent.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		assertTrue(matcher.matches(selist) >= 0);

		elm.removeChild(span);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas4() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("div.exampleclass:has(span + p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("div");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = createElement("span");
		parent.appendChild(elm);
		CSSStylableElement elm2 = createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = parent.getSelectorMatcher();
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		assertTrue(matcher.matches(selist) >= 0);

		parent.removeChild(elm);
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoHas5() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"body>div.exampleclass:has(span + p) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement body = createElement("body");
		CSSStylableElement parent = createElement("div");
		document.getDocumentElement().add(body);
		body.appendChild(parent);
		CSSStylableElement elm = createElement("span");
		parent.appendChild(elm);
		CSSStylableElement elm2 = createElement("p");
		parent.appendChild(elm2);
		SelectorMatcher matcher = selectorMatcher(parent);
		assertEquals(-1, matcher.matches(selist));
		parent.setAttribute("class", "exampleclass");
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 4, selist.item(selidx), matcher);

		parent.removeChild(elm);
		parent.removeChild(elm2);
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoIs() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				":is(.exampleclass span[foo], div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("span");
		parent.appendChild(elm);
		elm = document.createElement("b");
		parent.appendChild(elm);
		CSSStylableElement child2 = document.createElement("span");
		child2.setAttribute("foo", "bar");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoIsID() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"span:is(.exampleclass span[foo], div > span, #fooID) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("span");
		parent.appendChild(elm);
		elm = document.createElement("b");
		parent.appendChild(elm);
		CSSStylableElement child2 = document.createElement("span");
		child2.setAttribute("foo", "bar");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoIsNested() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				":is(.fooclass span, div > span, :is(p#exampleid.exampleclass span#sp2Id.spcl)) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("span");
		parent.appendChild(elm);
		elm = document.createElement("b");
		parent.appendChild(elm);
		CSSStylableElement child2 = document.createElement("span");
		child2.setAttribute("class", "spcl");
		child2.setAttribute("id", "sp2Id");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(2, 2, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoWhere() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				":where(.exampleclass span, div > span) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("p");
		parent.setAttribute("class", "exampleclass");
		parent.setAttribute("id", "exampleid");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("span");
		elm.setAttribute("id", "childid1");
		parent.appendChild(elm);
		elm = document.createElement("b");
		elm.setAttribute("id", "childid2");
		parent.appendChild(elm);
		CSSStylableElement child2 = document.createElement("span");
		child2.setAttribute("id", "grandchildid1");
		elm.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(child2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 0, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoWhere2() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("span:where(.foo .bar, div > .bar) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("div");
		parent.setAttribute("class", "foo");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("span");
		elm.setAttribute("class", "bar");
		parent.appendChild(elm);
		CSSStylableElement p = document.createElement("p");
		parent.appendChild(p);
		CSSStylableElement child2 = document.createElement("span");
		p.appendChild(child2);
		SelectorMatcher matcher = selectorMatcher(elm);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 0, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNot() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass:not(:last-child) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("p");
		elm.setAttribute("id", "p1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSStylableElement elm2 = document.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 2, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNotId() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(
				"p.exampleclass:not(:last-child,p#noID) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		document.getDocumentElement().add(parent);
		CSSStylableElement elm = document.createElement("p");
		elm.setAttribute("id", "p1");
		elm.setAttribute("class", "exampleclass");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSStylableElement elm2 = document.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(1, 1, 2, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoNotNested() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:not(:last-child, :not(p)) {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		CSSStylableElement parent = createElement("div");
		parent.setAttribute("id", "div1");
		CSSStylableElement elm = document.createElement("p");
		elm.setAttribute("id", "p1");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		assertTrue(matcher.matches(selist) < 0);
		CSSStylableElement elm2 = document.createElement("p");
		elm2.setAttribute("id", "p2");
		parent.appendChild(elm2);
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);
	}

	@Test
	public void testMatchSelectorPseudoClassEnabledDisabled() throws Exception {
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "checkbox");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:disabled {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();

		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) >= 0);
		elm.removeAttribute("disabled");
		css = parseStyle("input:enabled {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassReadWriteReadOnly() throws Exception {
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "checkbox");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:read-only {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) >= 0);
		elm.removeAttribute("disabled");
		css = parseStyle("input:read-write {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("disabled", "disabled");
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("div:read-write {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();

		CSSStylableElement div = createElement("div");
		SelectorMatcher divmatcher = selectorMatcher(div);
		assertTrue(divmatcher.matches(selist) < 0);
		div.setAttribute("contenteditable", "true");
		selidx = divmatcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), divmatcher);
	}

	@Test
	public void testMatchSelectorPseudoClassPlaceholderShown() throws Exception {
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "text");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:placeholder-shown {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		assertTrue(matcher.matches(selist) < 0);
		elm.setAttribute("placeholder", "Enter text");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault() throws Exception {
		CSSStylableElement button = createElement("button");
		button.setAttribute("type", "submit");
		button.setAttribute("disabled", "disabled");
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(button);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault2() throws Exception {
		CSSStylableElement div = createElement("div");
		CSSStylableElement button = createElement("button");
		button.setAttribute("type", "submit");
		button.setAttribute("disabled", "disabled");
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(div);
		div.appendChild(button);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button.removeAttribute("disabled");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassDefault3() throws Exception {
		CSSStylableElement div = createElement("div");
		CSSStylableElement button1 = createElement("button");
		button1.setAttribute("type", "submit");
		CSSStylableElement button2 = createElement("button");
		button2.setAttribute("type", "submit");
		button2.setAttribute("disabled", "disabled");
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "submit");
		parent.appendChild(div);
		div.appendChild(button1);
		parent.appendChild(button2);
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:default {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertEquals(-1, selidx);

		div.removeChild(button1);
		selidx = matcher.matches(selist);
		assertEquals(0, selidx);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		button2.removeAttribute("disabled");
		assertEquals(-1, matcher.matches(selist));
	}

	@Test
	public void testMatchSelectorPseudoClassChecked() throws Exception {
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("checked", "checked");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:checked {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.removeAttribute("checked");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassIndeterminate() throws Exception {
		CSSStylableElement parent = createElement("form");
		CSSStylableElement elm = createElement("input");
		elm.setAttribute("type", "checkbox");
		elm.setAttribute("indeterminate", "true");
		parent.appendChild(elm);
		SelectorMatcher matcher = selectorMatcher(elm);
		AbstractCSSStyleSheet css = parseStyle("input:indeterminate {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		int selidx = matcher.matches(selist);
		assertTrue(selidx >= 0);
		// Specificity
		CSSOMBridge.assertSpecificity(0, 1, 1, selist.item(selidx), matcher);

		elm.setAttribute("indeterminate", "false");
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassState() throws Exception {
		CSSStylableElement head = createElement("head");
		CSSStylableElement style = createElement("style");
		CSSStylableElement elm = createElement("p");
		style.setAttribute("type", "text/css");
		style.appendChild(document
				.createTextNode("p:hover {text-decoration-line:underline; text-align: center;}"));
		document.getDocumentElement().appendChild(head);
		head.appendChild(style);
		CSSStylableElement body = createElement("body");
		body.appendChild(elm);
		CSSStylableElement span = createElement("span");
		elm.appendChild(span);
		document.getDocumentElement().appendChild(body);
		document.setTargetMedium("screen");
		CSSComputedProperties styledecl = document.getStyleSheet().getComputedStyle(elm, null);
		assertNotNull(styledecl);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		DummyCanvas canvas = (DummyCanvas) document.getCanvas();
		assertNotNull(canvas);
		List<String> statePseudoClasses = new LinkedList<>();
		statePseudoClasses.add("hover");
		canvas.registerStatePseudoclasses(elm, statePseudoClasses);
		styledecl = document.getStyleSheet().getComputedStyle(elm, null);
		assertEquals("underline", styledecl.getPropertyValue("text-decoration-line"));
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("center", document.getStyleSheet().getComputedStyle(span, null)
				.getPropertyValue("text-align"));
		styledecl = document.getStyleSheet().getComputedStyle(body, null);
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
		styledecl = document.getStyleSheet().getComputedStyle(span, null);
		assertEquals("center", styledecl.getPropertyValue("text-align"));
		assertEquals("none", styledecl.getPropertyValue("text-decoration-line"));
	}

	@Test
	public void testFindStaticPseudoClasses() throws Exception {
		List<String> statePseudoClasses = new LinkedList<>();
		AbstractCSSStyleSheet css = parseStyle("div:blank {color: blue;}");
		StyleRule rule = (StyleRule) css.getCssRules().item(0);
		SelectorList selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
		statePseudoClasses.clear();
		css = parseStyle("div:hover {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.contains("hover"));
		statePseudoClasses.clear();
		css = parseStyle(":playing {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.contains("playing"));
		statePseudoClasses.clear();
		css = parseStyle("div > p {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
		statePseudoClasses.clear();
		css = parseStyle("div p {color: blue;}");
		rule = (StyleRule) css.getCssRules().item(0);
		selist = rule.getSelectorList();
		AbstractSelectorMatcher.findStatePseudoClasses(selist.item(0), statePseudoClasses);
		assertTrue(statePseudoClasses.isEmpty());
	}

	AbstractCSSStyleSheet parseStyle(String style) throws CSSException, IOException {
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) factory.getStyleSheetFactory()
				.createStyleSheet(null, null);
		StringReader re = new StringReader(style);
		cssParser.setDocumentHandler(
				CSSOMBridge.createDocumentHandler(css, CSSStyleSheet.COMMENTS_IGNORE));
		cssParser.parseStyleSheet(re);
		return css;
	}

	private CSSStylableElement createElement(String elementName) {
		return factory.createElement(elementName);
	}

	private static SelectorMatcher selectorMatcher(CSSStylableElement elm) {
		return elm.getSelectorMatcher();
	}

}
