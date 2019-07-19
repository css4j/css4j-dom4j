/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.dom4j.dom.DOMDocumentType;
import org.dom4j.dom.DOMElement;

import io.sf.carte.doc.agent.MockURLFactory;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.TestStyleDatabase;
import io.sf.carte.doc.style.css.om.DummyDeviceFactory;

/**
 * A document factory for test purposes.
 * 
 * @author Carlos Amengual
 *
 */
public class TestDocumentFactory extends XHTMLDocumentFactory {

	private static final long serialVersionUID = 2L;

	private static final StyleDatabase styleDb = new TestStyleDatabase();

	private MockURLFactory urlFactory = new MockURLFactory();

	public TestDocumentFactory() {
		super();
		getStyleSheetFactory().setDeviceFactory(new TestDeviceFactory());
	}

	public MockURLFactory getMockURLFactory() {
		return urlFactory;
	}

	@Override
	public XHTMLDocument createDocument() {
		XHTMLDocument mydoc = new MyXHTMLDocument();
		mydoc.setDocumentFactory(this);
		return mydoc;
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

}
