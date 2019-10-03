/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CSSOMBridge;
import io.sf.carte.doc.style.css.om.CSSStyleDeclarationRule;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class DOM4JSelectorMatcherTest {

	private static XHTMLDocumentFactory factory;
	private Parser cssParser;
	private XHTMLDocument document;

	@BeforeClass
	public static void setUpBeforeClass() {
		factory = XHTMLDocumentFactory.getInstance();
	}

	@Before
	public void setUp() {
		this.cssParser = new CSSParser();
		CSSStylableElement root = factory.createElement("html");
		document = factory.createDocument(root);
	}

	@Test
	public void testMatchSelector1Element() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title] {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title=\"hi\"] {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[title~=\"hi\"] {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("title", "ho hi");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4Attribute() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p[lang|=\"en\"] {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("lang", "en-US");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Lang() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:lang(en) {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
		elm.addAttribute("lang", "en-US");
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(".exampleclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("z.exampleclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelector3Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4Class() throws Exception {
		AbstractCSSStyleSheet css = parseStyle(".exampleclass span {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass secondclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.secondclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass secondclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector3MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", " firstclass");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector4MultipleClasses() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.firstclass {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "firstclass ");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector1Firstchild() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement elm = createElement("p");
		elm.addAttribute("class", "exampleclass");
		elm.addAttribute("id", "exampleid");
		document.getDocumentElement().add(elm);
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
	}

	@Test
	public void testMatchSelector2Id() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("#exampleid span {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
	public void testMatchSelectorAdjacentSibling() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass + p {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
	public void testMatchSelectorPseudoClass1() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:first-child {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement parent = createElement("div");
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		parent.add(factory.createElement("p"));
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		css = parseStyle("p:last-child {color: blue;}");
		rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		selist = CSSOMBridge.getSelectorList(rule);
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:only-child {color: blue;}");
		rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		selist = CSSOMBridge.getSelectorList(rule);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoClassNth() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-child(1) {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = CSSOMBridge.getSelectorList(rule);
			CSSStylableElement parent = createElement("div");
			parent.add(factory.createText("foo"));
			CSSStylableElement elm = createElement("p");
			parent.add(elm);
			parent.add(factory.createElement("p"));
			SelectorMatcher matcher = elm.getSelectorMatcher();
			assertTrue(matcher.matches(selist) >= 0);
			css = parseStyle("p:nth-last-child(1) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) < 0);
			css = parseStyle("p:nth-last-child(2) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
			parent.add(createElement("div"));
			css = parseStyle("p:nth-last-child(3) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
		}
	}

	@Test
	public void testMatchSelectorPseudoClassNthSelector() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-child(1 of p) {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = CSSOMBridge.getSelectorList(rule);
			CSSStylableElement parent = createElement("div");
			parent.add(createElement("div"));
			parent.add(factory.createText("foo"));
			CSSStylableElement elm = createElement("p");
			parent.add(elm);
			parent.add(createElement("p"));
			SelectorMatcher matcher = elm.getSelectorMatcher();
			assertTrue(matcher.matches(selist) >= 0);
			css = parseStyle("p:nth-last-child(2 of p) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
			css = parseStyle("p:nth-child(even of p) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) < 0);
			css = parseStyle("p:nth-child(odd of p) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
		}
	}

	@Test
	public void testMatchSelectorPseudoClassNthOfType() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:nth-of-type(1) {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = CSSOMBridge.getSelectorList(rule);
			CSSStylableElement parent = createElement("div");
			parent.add(createElement("div"));
			parent.add(factory.createText("foo"));
			CSSStylableElement elm = createElement("p");
			parent.add(elm);
			parent.add(createElement("p"));
			SelectorMatcher matcher = elm.getSelectorMatcher();
			assertTrue(matcher.matches(selist) >= 0);
			css = parseStyle("p:nth-last-of-type(2) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
			css = parseStyle("p:nth-of-type(even) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) < 0);
			css = parseStyle("p:nth-of-type(odd) {color: blue;}");
			rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
			selist = CSSOMBridge.getSelectorList(rule);
			assertTrue(matcher.matches(selist) >= 0);
		}
	}

	@Test
	public void testMatchSelectorPseudoOfType() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p:first-of-type {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		CSSStylableElement parent = createElement("div");
		parent.add(createElement("div"));
		parent.add(factory.createText("foo"));
		CSSStylableElement elm = createElement("p");
		parent.add(elm);
		parent.add(createElement("p"));
		SelectorMatcher matcher = elm.getSelectorMatcher();
		assertTrue(matcher.matches(selist) >= 0);
		css = parseStyle("p:last-of-type {color: blue;}");
		rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		selist = CSSOMBridge.getSelectorList(rule);
		assertTrue(matcher.matches(selist) < 0);
		css = parseStyle("p:only-of-type {color: blue;}");
		rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		selist = CSSOMBridge.getSelectorList(rule);
		assertTrue(matcher.matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoRoot() throws Exception {
		CSSStylableElement root = document.getDocumentElement();
		root.add(createElement("div"));
		CSSStylableElement div = createElement("div");
		root.add(div);
		AbstractCSSStyleSheet css = parseStyle(":root {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
		assertTrue(root.getSelectorMatcher().matches(selist) >= 0);
		assertTrue(div.getSelectorMatcher().matches(selist) < 0);
	}

	@Test
	public void testMatchSelectorPseudoEmpty() throws Exception {
		CSSStylableElement div = createElement("div");
		document.getDocumentElement().add(div);
		div.add(factory.createText(""));
		AbstractCSSStyleSheet css = parseStyle("div:empty {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
	public void testMatchSelectorPseudoHas2() throws Exception {
		AbstractCSSStyleSheet css = parseStyle("p.exampleclass:has(+ p) {color: blue;}");
		CSSStyleDeclarationRule rule = (CSSStyleDeclarationRule) css.getCssRules().item(0);
		if (rule != null) {
			SelectorList selist = CSSOMBridge.getSelectorList(rule);
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
	}

	AbstractCSSStyleSheet parseStyle(String style) throws CSSException, IOException {
		AbstractCSSStyleSheet css = factory.getStyleSheetFactory().createStyleSheet(null,
				null);
		StringReader re = new StringReader(style);
		cssParser.setDocumentHandler(CSSOMBridge.createDocumentHandler((BaseCSSStyleSheet) css, true));
		cssParser.parseStyleSheet(re);
		return css;
	}

	private CSSStylableElement createElement(String elementName) {
		return factory.createElement(elementName);
	}

}
