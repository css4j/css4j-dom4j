/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.om.SampleCSS;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class XHTMLDocumentFactoryTest {

	static XHTMLDocument xhtmlDoc;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		Reader re = SampleCSS.sampleHTMLReader();
		InputSource isrc = new InputSource(re);
		xhtmlDoc = TestUtil.parseXML(isrc);
		re.close();
	}

	@Test
	public void testCreateDocument() {
		assertEquals(CSSDocument.ComplianceMode.STRICT, xhtmlDoc.getComplianceMode());
		XHTMLDocumentFactory factory = XHTMLDocumentFactory.getInstance();
		XHTMLDocument document = factory.createDocument(null, null, null);
		assertEquals("BackCompat", document.getCompatMode());
		assertEquals(CSSDocument.ComplianceMode.QUIRKS, document.getComplianceMode());
		DocumentType doctype = factory.createDocumentType("html", null, null);
		document = factory.createDocument(null, null, doctype);
		assertEquals("CSS1Compat", document.getCompatMode());
		assertEquals(CSSDocument.ComplianceMode.STRICT, document.getComplianceMode());
	}

	@Test
	public void testGetRootElement() {
		assertEquals("html", xhtmlDoc.getRootElement().getName());
	}

	@Test
	public void testMockDocumentFactory() throws Exception {
		TestDocumentFactory factory = new TestDocumentFactory();
		XHTMLDocument doc = factory.createDocument();
		InputStream is = doc.openStream("http://www.example.com/css/common.css");
		assertNotNull(is);
		is.close();
		URLConnection ucon = doc
				.openConnection(new URI("http://www.example.com/css/common.css").toURL());
		assertNotNull(ucon);
		is = ucon.getInputStream();
		assertNotNull(is);
		InputStreamReader re = new InputStreamReader(is, StandardCharsets.UTF_8);
		StringWriter sw = new StringWriter(100);
		char[] b = new char[3000];
		int n;
		while ((n = re.read(b)) != -1) {
			sw.write(b, 0, n);
		}
		re.close();
		assertEquals("body {", sw.toString().substring(0, 6));
	}

	@Test
	public void testEntities1() throws Exception {
		// First, check plain dom4j behaviour
		SAXReader reader = new SAXReader();
		reader.setEntityResolver(new DefaultEntityResolver());
		Reader re = SampleCSS.sampleHTMLReader();
		org.dom4j.Document dom4jdocument = reader.read(re);
		re.close();
		org.dom4j.Element dom4jelm = dom4jdocument.elementByID("entity");
		assertNotNull(dom4jelm);
		assertEquals("span", dom4jelm.getName());
		assertEquals("<>", dom4jelm.getText());
		assertEquals(2, dom4jelm.nodeCount());
		assertEquals(org.dom4j.Node.TEXT_NODE, dom4jelm.node(0).getNodeType());
		assertEquals("<", dom4jelm.node(0).getText());
		assertEquals(org.dom4j.Node.TEXT_NODE, dom4jelm.node(1).getNodeType());
		assertEquals(">", dom4jelm.node(1).getText());
		// Now css4j-dom4j
		XHTMLElement elm = xhtmlDoc.getElementById("entity");
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(2, nl.getLength());
		Node ent0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, ent0.getNodeType());
		assertEquals("<", ent0.getNodeValue());
		Node ent1 = nl.item(1);
		assertEquals(Node.TEXT_NODE, ent1.getNodeType());
		assertEquals(">", ent1.getNodeValue());
	}

	@Test
	public void testEntities2PlainDom4j() throws Exception {
		// First, check plain dom4j behaviour
		SAXReader reader = new SAXReader();
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
		reader.setEntityResolver(new DefaultEntityResolver());
		Reader re = SampleCSS.sampleHTMLReader();
		org.dom4j.Document document = reader.read(re);
		re.close();
		org.dom4j.Element dom4jelm = document.elementByID("entiacute");
		assertNotNull(dom4jelm);
		assertEquals("span", dom4jelm.getName());
		assertEquals("ítem", dom4jelm.getText());
		assertEquals(1, dom4jelm.nodeCount());
		assertEquals(org.dom4j.Node.TEXT_NODE, dom4jelm.node(0).getNodeType());
		assertEquals("ítem", dom4jelm.node(0).getText());
	}

	@Test
	public void testEntities2() throws Exception {
		// Now css4j-dom4j
		XHTMLElement elm = xhtmlDoc.getElementById("entiacute");
		assertNotNull(elm);
		assertEquals("span", elm.getTagName());
		String text = elm.getText();
		assertEquals("ítem", text);
		NodeList nl = elm.getChildNodes();
		assertNotNull(nl);
		assertEquals(1, nl.getLength());
		Node ent0 = nl.item(0);
		assertEquals(Node.TEXT_NODE, ent0.getNodeType());
		assertEquals("ítem", ent0.getNodeValue());
	}

}
