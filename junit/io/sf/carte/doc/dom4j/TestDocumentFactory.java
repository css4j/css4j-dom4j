/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.dom4j.DocumentException;
import org.dom4j.dom.DOMDocumentType;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;

import io.sf.carte.doc.agent.MockURLConnectionFactory;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory;
import io.sf.carte.doc.style.css.om.TestStyleDatabase;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * A document factory for test purposes.
 * 
 * @author Carlos Amengual
 *
 */
public class TestDocumentFactory extends XHTMLDocumentFactory {

	private static final long serialVersionUID = 2L;

	private static final StyleDatabase styleDb = new TestStyleDatabase();

	private final MockURLConnectionFactory urlFactory = new MockURLConnectionFactory();

	public TestDocumentFactory() {
		super();
		getStyleSheetFactory().setDeviceFactory(new TestDeviceFactory());
	}

	public MockURLConnectionFactory getConnectionFactory() {
		return urlFactory;
	}

	@Override
	public XHTMLDocument createDocument() {
		XHTMLDocument mydoc = new MyXHTMLDocument();
		mydoc.setDocumentFactory(this);
		return mydoc;
	}

	@Override
	protected XHTMLDocument createDocument(DOMDocumentType documentType) {
		XHTMLDocument document = new MyXHTMLDocument(documentType);
		document.setDocumentFactory(this);
		return document;
	}

	class MyXHTMLDocument extends XHTMLDocument {

		private static final long serialVersionUID = 4L;

		MyXHTMLDocument() {
			super();
		}

		MyXHTMLDocument(String name) {
			super(name);
		}

		MyXHTMLDocument(DOMElement rootElement) {
			super(rootElement);
		}

		MyXHTMLDocument(DOMDocumentType docType) {
			super(docType);
		}

		MyXHTMLDocument(DOMElement rootElement, DOMDocumentType docType) {
			super(rootElement, docType);
		}

		MyXHTMLDocument(String name, DOMElement rootElement, DOMDocumentType docType) {
			super(name, rootElement, docType);
		}

		@Override
		public URLConnection openConnection(URL url) throws IOException {
			return urlFactory.createConnection(url);
		}

	}

	public URLConnection openConnection(URL url) {
		return urlFactory.createConnection(url);
	}

	class TestDeviceFactory extends DummyDeviceFactory {
		@Override
		public StyleDatabase getStyleDatabase(String targetMedium) {
			return styleDb;
		}
	}

	public static XHTMLDocument loadDocument(Reader re) throws DocumentException, IOException {
		HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
		parser.setCommentPolicy(XmlViolationPolicy.ALLOW);
		parser.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
		TestDocumentFactory factory = new TestDocumentFactory();
		factory.getStyleSheetFactory().setDefaultHTMLUserAgentSheet();
		SAXReader reader = new SAXReader(factory);
		reader.setXMLReader(parser);
		ErrorHandler errorHandler = new PermissiveErrorHandler();
		reader.setErrorHandler(errorHandler);
		return (XHTMLDocument) reader.read(re);
	}

}
