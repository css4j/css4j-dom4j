/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import org.dom4j.DocumentException;
import org.dom4j.dom.DOMDocumentType;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XPP3Reader;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import io.sf.carte.doc.agent.AbstractUserAgent;
import io.sf.carte.doc.dom4j.DOM4JUserAgent.AgentXHTMLDocumentFactory.AgentXHTMLDocument;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.nsac.Parser.Flag;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;
import io.sf.carte.util.agent.AgentUtil;

/**
 * User Agent based on DOM4J document trees.
 * <p>
 * 
 * @author Carlos Amengual
 */
public class DOM4JUserAgent extends AbstractUserAgent {

	private static final long serialVersionUID = 1L;

	protected EntityResolver resolver = createEntityResolver();

	private final boolean useXPP3;

	private final XHTMLDocumentFactory factory;

	protected DOM4JUserAgent(EnumSet<Parser.Flag> parserFlags) {
		this(parserFlags, false);
	}

	protected DOM4JUserAgent(EnumSet<Parser.Flag> parserFlags, boolean useXPP3) {
		super(parserFlags);
		this.useXPP3 = useXPP3;
		factory = new AgentXHTMLDocumentFactory(getParserFlags());
	}

	/**
	 * Creates an user agent that reads XHTML documents with DOM4J.
	 * 
	 * @param useXPP3 Sets the use of the XPP3 pull parser to parse the documents.
	 * @return the user agent.
	 */
	public static AbstractUserAgent createUserAgent(EnumSet<Parser.Flag> parserFlags, boolean useXPP3) {
		return new DOM4JUserAgent(parserFlags, useXPP3);
	}

	protected EntityResolver createEntityResolver() {
		return new DefaultEntityResolver();
	}

	/**
	 * Sets the entity resolver to be used when parsing documents.
	 * 
	 * @param resolver the entity resolver.
	 */
	@Override
	public void setEntityResolver(EntityResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Reads and parses an XHTML document located in the given URL.
	 * 
	 * @param url the URL that points to the document.
	 * @return the XHTMLDocument.
	 * @throws IOException if there is an I/O problem reading the URL.
	 * @throws io.sf.carte.doc.DocumentException if there is a problem parsing the document.
	 */
	@Override
	public XHTMLDocument readURL(URL url) throws IOException, io.sf.carte.doc.DocumentException {
		long time = System.currentTimeMillis();
		URLConnection con = openConnection(url, time);
		con.connect();
		String conType = con.getContentType();
		String contentEncoding = con.getContentEncoding();
		InputStream is = null;
		AgentXHTMLDocument xdoc = null;
		try {
			is = openInputStream(con);
			xdoc = parseDocument(AgentUtil.inputStreamToReader(is, conType, contentEncoding, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw e;
		} catch (io.sf.carte.doc.DocumentException e) {
			throw new io.sf.carte.doc.DocumentException(
					"Error parsing document " + url.toExternalForm(), e.getCause());
		} finally {
			if (is != null) {
				is.close();
			}
		}
		xdoc.setLoadingTime(time);
		xdoc.setDocumentURI(url.toExternalForm());
		// Check for preferred style
		String defStyle = con.getHeaderField("Default-Style");
		NodeList list = xdoc.getElementsByTagName("meta");
		int listL = list.getLength();
		for (int i = listL - 1; i >= 0; i--) {
			CSSStylableElement element = (CSSStylableElement) list.item(i);
			if ("default-style".equalsIgnoreCase(element.getAttributeValue("http-equiv"))) {
				String metaDefStyle = element.getAttributeValue("content");
				if (metaDefStyle.length() != 0) {
					// Per HTML4 spec § 14.3.2:
					// "If two or more META declarations or HTTP headers specify 
					//  the preferred style sheet, the last one takes precedence."
					defStyle = metaDefStyle;
				}
			}
		}
		if (defStyle != null) {
			xdoc.setSelectedStyleSheetSet(defStyle);
		}
		// Referrer Policy
		String referrerPolicy = con.getHeaderField("Referrer-Policy");
		if (referrerPolicy != null) {
			xdoc.setReferrerPolicyHeader(referrerPolicy);
		}
		// Close connection if appropriate
		if (con instanceof HttpURLConnection) {
			HttpURLConnection hcon = (HttpURLConnection) con;
			hcon.disconnect();
		}
		return xdoc;
	}

	protected InputStream openInputStream(URLConnection con) throws IOException {
		return con.getInputStream();
	}

	protected AgentXHTMLDocument parseDocument(Reader re) throws io.sf.carte.doc.DocumentException, IOException {
		try {
			if (useXPP3) {
				return parseWithXPP3Reader(re);
			} else {
				return parseWithSAXReader(re);
			}
		} catch (DocumentException e) {
			throw new io.sf.carte.doc.DocumentException("Error parsing document", e);
		}
	}

	private AgentXHTMLDocument parseWithSAXReader(Reader re) throws DocumentException {
		InputSource isrc = new InputSource(re);
		XHTMLDocumentFactory factory = getXHTMLDocumentFactory();
		SAXReader reader = new SAXReader(factory);
		reader.setEntityResolver(resolver);
		return (AgentXHTMLDocument) reader.read(isrc);
	}

	private AgentXHTMLDocument parseWithXPP3Reader(Reader re) throws DocumentException, IOException {
		XHTMLDocumentFactory factory = getXHTMLDocumentFactory();
		XPP3Reader reader = new XPP3Reader(factory);
		try {
			return (AgentXHTMLDocument) reader.read(re);
		} catch (XmlPullParserException e) {
			throw new DocumentException(e);
		}
	}

	public XHTMLDocumentFactory getXHTMLDocumentFactory() {
		return factory;
	}

	public class AgentXHTMLDocumentFactory extends XHTMLDocumentFactory {

		private static final long serialVersionUID = 2L;

		protected AgentXHTMLDocumentFactory(EnumSet<Flag> enumSet) {
			super(enumSet);
		}

		@Override
		public XHTMLDocument createDocument() {
			XHTMLDocument mydoc = new AgentXHTMLDocument();
			mydoc.setDocumentFactory(this);
			return mydoc;
		}

		@Override
		protected XHTMLDocument createDocument(DOMDocumentType documentType) {
			AgentXHTMLDocument document = new AgentXHTMLDocument(documentType);
			document.setDocumentFactory(this);
			return document;
		}

		public class AgentXHTMLDocument extends XHTMLDocument {

			private static final long serialVersionUID = 3L;

			private long loadingTime;

			AgentXHTMLDocument() {
				super();
			}

			AgentXHTMLDocument(String name) {
				super(name);
			}

			AgentXHTMLDocument(DOMElement rootElement) {
				super(rootElement);
			}

			AgentXHTMLDocument(DOMDocumentType docType) {
				super(docType);
			}

			AgentXHTMLDocument(DOMElement rootElement, DOMDocumentType docType) {
				super(rootElement, docType);
			}

			AgentXHTMLDocument(String name, DOMElement rootElement, DOMDocumentType docType) {
				super(name, rootElement, docType);
			}

			@Override
			public URLConnection openConnection(URL url) throws IOException {
				return DOM4JUserAgent.this.openConnection(url, loadingTime);
			}

			@Override
			public boolean isVisitedURI(String href) {
				try {
					return isVisitedURL(getURL(href));
				} catch (MalformedURLException e) {
					return false;
				}
			}

			/**
			 * Set the time at which this document was loaded from origin.
			 * 
			 * @param time the time of loading, in milliseconds.
			 */
			public void setLoadingTime(long time) {
				this.loadingTime = time;
			}

			@Override
			protected void setReferrerPolicyHeader(String policy) {
				super.setReferrerPolicyHeader(policy);
			}
		}

	}

}
