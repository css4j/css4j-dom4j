/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;

import org.dom4j.DocumentException;
import org.dom4j.io.XPP3Reader;
import org.junit.jupiter.api.Test;
import org.xmlpull.v1.XmlPullParserException;

import io.sf.carte.doc.style.css.om.SampleCSS;

public class XPP3Test {

	@Test
	public void parseXPP3() throws DocumentException, IOException, XmlPullParserException {
		XHTMLDocumentFactory factory = XHTMLDocumentFactory.getInstance();
		XPP3Reader reader = new XPP3Reader(factory);
		Reader re = SampleCSS.sampleHTMLReader();
		XHTMLDocument document = (XHTMLDocument) reader.read(re);
		re.close();
		assertEquals("html", document.getRootElement().getName());
	}

}
