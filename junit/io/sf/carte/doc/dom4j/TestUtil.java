/*

 Copyright (c) 2005-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.io.Reader;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XPP3Reader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.style.css.om.SampleCSS;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class TestUtil {

	private static TestDocumentFactory factoryUASheet;

	static {
		factoryUASheet = new TestDocumentFactory();
		factoryUASheet.getStyleSheetFactory().setDefaultHTMLUserAgentSheet();
	}

	private TestUtil() {
	}

	public static XHTMLDocument parseXML(InputSource is) throws DocumentException, SAXException {
		SAXReader reader = new SAXReader(factoryUASheet);
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
		reader.setEntityResolver(new DefaultEntityResolver());
		XHTMLDocument document = (XHTMLDocument) reader.read(is);
		return document;
	}

	public static XHTMLDocument parseXPP3(Reader re) throws Exception {
		TestDocumentFactory factory = new TestDocumentFactory();
		XPP3Reader reader = new XPP3Reader(factory);
		XHTMLDocument document = (XHTMLDocument) reader.read(re);
		return document;
	}

	public static XHTMLDocument sampleXHTML() throws DocumentException, SAXException, IOException {
		Reader re = SampleCSS.sampleHTMLReader();
		InputSource isrc = new InputSource(re);
		XHTMLDocument xhtmlDoc = parseXML(isrc);
		re.close();
		return xhtmlDoc;
	}

}
