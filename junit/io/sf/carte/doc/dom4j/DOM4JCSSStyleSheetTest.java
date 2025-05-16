/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CSSRuleArrayList;
import io.sf.carte.doc.style.css.om.SampleCSS;

public class DOM4JCSSStyleSheetTest {

	@Test
	public void testParseCSSStyleSheet() throws CSSException, IOException {
		AbstractCSSStyleSheet css = XHTMLDocumentFactory.getInstance().getStyleSheetFactory()
				.createStyleSheet(null, null);
		Reader re = SampleCSS.loadSampleCSSReader();
		css.parseStyleSheet(re);
		re.close();
		CSSRuleArrayList rules = css.getCssRules();
		assertEquals(SampleCSS.RULES_IN_SAMPLE_CSS, rules.getLength());
		assertFalse(css.getErrorHandler().hasSacErrors());
	}

}
