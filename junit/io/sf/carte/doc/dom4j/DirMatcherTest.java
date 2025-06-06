/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.SampleCSS;
import io.sf.carte.doc.style.css.parser.CSSParser;

public class DirMatcherTest {

	XHTMLDocument document;

	@BeforeEach
	public void setUp() throws IOException, DocumentException {
		document = TestDocumentFactory.loadDocument(SampleCSS.directionalityHTMLReader());
	}

	@Test
	public void testMatchDirectionality() {
		// Prepare selectors
		CSSParser parser = new CSSParser();
		SelectorList ltr = parser.parseSelectors(":dir(ltr)");
		SelectorList rtl = parser.parseSelectors(":dir(rtl)");
		//
		CSSStylableElement elm = document.getElementById("head");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("h1");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("para1");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("para2");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("h2");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("tableid");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("cell12");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("tr1");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("form1");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("username");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("phonelabel");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("telephone");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		/* FAIL
		elm = document.getElementById("textareartl");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("textarealtr");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("textareaempty");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		*/
		elm = document.getElementById("bdirtl");
		assertTrue(elm.matches(rtl, null));
		assertFalse(elm.matches(ltr, null));
		elm = document.getElementById("bdiltr");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("bdiauto");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("bdiempty");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("bdiautoempty");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
		elm = document.getElementById("bdibadempty");
		assertFalse(elm.matches(rtl, null));
		assertTrue(elm.matches(ltr, null));
	}

}
