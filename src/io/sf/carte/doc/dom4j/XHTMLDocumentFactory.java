/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.util.EnumSet;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.dom.DOMDocumentType;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.nsac.Parser2.Flag;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.CompatInlineStyle;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.InlineStyle;

/**
 * DocumentFactory for CSS-styled XHTML documents.
 * <p>
 * This factory creates XHTMLDocuments and other objects with support for CSS
 * style sheets. A default style sheet can be set with the
 * {@link CSSStyleSheetFactory#getUserAgentStyleSheet(CSSDocument.ComplianceMode)}
 * method (use {@link #getStyleSheetFactory()} to get the factory). If no
 * default sheet is specified, an internal default sheet will be used.
 * <p>
 * It is possible to produce elements with the ability to cache its own computed
 * style, if you call <code>setStyleCache(true)</code>. Although the performance
 * benefit is generally not noticeable, in some corner cases it could enhance
 * performance for applications that may call the
 * <code>getComputedStyle()</code> method of a single stylable element many
 * times. Due to the generally negligible performance gain, cache is off by
 * default.
 * 
 * @author Carlos Amengual
 * 
 */
public class XHTMLDocumentFactory extends DOMDocumentFactory {

	private static final long serialVersionUID = 8L;

	private final DOM4JCSSStyleSheetFactory cssFactory;

	private boolean styleCacheOn = false;

	private static transient XHTMLDocumentFactory singleton = new XHTMLDocumentFactory();

	protected XHTMLDocumentFactory() {
		this(EnumSet.noneOf(Parser2.Flag.class));
	}

	public XHTMLDocumentFactory(EnumSet<Flag> enumSet) {
		super();
		cssFactory = new DOM4JCSSStyleSheetFactory(enumSet);
		cssFactory.setDefaultHTMLUserAgentSheet();
	}

	/**
	 * Gets the instance of the document factory.
	 * 
	 * @return the XHTML (DOM4J-derived) document factory instance.
	 */
	public static XHTMLDocumentFactory getInstance() {
		return singleton;
	}

	/**
	 * Gets the CSS style sheet factory.
	 * 
	 * @return the CSS style sheet factory.
	 */
	public BaseCSSStyleSheetFactory getStyleSheetFactory() {
		return cssFactory;
	}

	InlineStyle createInlineStyle(Node owner) {
		return cssFactory.createInlineStyle(owner);
	}

	BaseCSSStyleSheet createLinkedStyleSheet(Node ownerNode, String title, MediaQueryList mediaList) {
		return cssFactory.createLinkedStyleSheet(ownerNode, title, mediaList);
	}

	BaseDocumentCSSStyleSheet getDefaultStyleSheet(CSSDocument.ComplianceMode mode) {
		return cssFactory.getDefaultStyleSheet(mode);
	}

	/**
	 * Indicates whether the stylable elements currently produced by this
	 * factory are cache-enabled or not.
	 * 
	 * @return <code>true</code> if the per-element cache is enabled, <code>false</code> otherwise.
	 */
	public boolean isStyleCacheOn() {
		return styleCacheOn;
	}

	/**
	 * Can turn on or off the per-Element style caching capability (by default
	 * is off).
	 * <p>
	 * Only applications that repeatedly call the
	 * {@link CSSStylableElement#getComputedStyle()} method on the same Element
	 * should turn it on.
	 * 
	 * @param onOff
	 *            set to true to turn on the cache capability, to false to turn
	 *            it off.
	 */
	public void setStyleCache(boolean onOff) {
		this.styleCacheOn = onOff;
	}

	@Override
	public CSSStylableElement createElement(String name) {
		return (CSSStylableElement) super.createElement(name);
	}

	@Override
	public CSSStylableElement createElement(QName qname) {
		String name = qname.getName().toLowerCase(Locale.ROOT);
		if ("base".equals(name)) {
			return new BaseURLElement(qname);
		} else if ("style".equals(name)) {
			return new StyleElement(qname);
		} else if ("link".equals(name)) {
			return new LinkElement(qname);
		} else if ("head".equals(name)) {
			return new HeadElement(qname);
		} else if ("img".equals(name)) {
			return new ImgElement(qname);
		} else if ("title".equals(name) || "meta".equals(name)) {
			return new XHTMLElement(qname);
		} else if ("font".equals(name) || "basefont".equals(name)) {
			return new FontElement(qname);
		} else if (styleCacheOn) {
			if ("td".equals(name) || "th".equals(name)) {
				return new CachedTableCellElement(qname);
			} else if ("tr".equals(name)) {
				return new CachedTableRowElement(qname);
			} else if ("table".equals(name)) {
				return new CachedTableElement(qname);
			} else {
				// Produce a cached element
				return new CachedXHTMLElement(qname);
			}
		} else {
			if ("td".equals(name) || "th".equals(name)) {
				return new TableCellElement(qname);
			} else if ("tr".equals(name)) {
				return new TableRowElement(qname);
			} else if ("table".equals(name)) {
				return new TableElement(qname);
			} else {
				// Produce a regular XHTML element
				return new XHTMLElement(qname);
			}
		}
	}

	@Override
	public CSSStylableElement createElement(QName qname, int attributeCount) {
		String name = qname.getName().toLowerCase(Locale.ROOT);
		if ("base".equals(name)) {
			return new BaseURLElement(qname, attributeCount);
		} else if ("style".equals(name)) {
			return new StyleElement(qname, attributeCount);
		} else if ("link".equals(name)) {
			return new LinkElement(qname, attributeCount);
		} else if ("head".equals(name)) {
			return new HeadElement(qname, attributeCount);
		} else if ("img".equals(name)) {
			return new ImgElement(qname, attributeCount);
		} else if ("title".equals(name) || "meta".equals(name)) {
			return new XHTMLElement(qname, attributeCount);
		} else if ("font".equals(name) || "basefont".equals(name)) {
			return new FontElement(qname, attributeCount);
		} else if (styleCacheOn) {
			if ("td".equals(name) || "th".equals(name)) {
				return new CachedTableCellElement(qname, attributeCount);
			} else if ("tr".equals(name)) {
				return new CachedTableRowElement(qname, attributeCount);
			} else if ("table".equals(name)) {
				return new CachedTableElement(qname, attributeCount);
			} else {
				// Produce a cached element
				return new CachedXHTMLElement(qname, attributeCount);
			}
		} else {
			if ("td".equals(name) || "th".equals(name)) {
				return new TableCellElement(qname, attributeCount);
			} else if ("tr".equals(name)) {
				return new TableRowElement(qname, attributeCount);
			} else if ("table".equals(name)) {
				return new TableElement(qname, attributeCount);
			} else {
				// Produce a regular XHTML element
				return new XHTMLElement(qname, attributeCount);
			}
		}
	}

	@Override
	public XHTMLDocument createDocument() {
		XHTMLDocument mydoc = new XHTMLDocument();
		mydoc.setDocumentFactory(this);
		return mydoc;
	}

	@Override
	public XHTMLDocument createDocument(Element rootElement) {
		XHTMLDocument mydoc = createDocument();
		mydoc.setRootElement(rootElement);
		return mydoc;
	}

	@Override
	public XHTMLDocument createDocument(String namespaceURI, String qualifiedName, DocumentType docType)
			throws DOMException {
		XHTMLDocument document;
		if (docType != null) {
			DOMDocumentType documentType = asDocumentType(docType);
			document = new XHTMLDocument(documentType);
		} else {
			document = new XHTMLDocument();
		}
		document.setDocumentFactory(this);
		if (qualifiedName != null) {
			if (namespaceURI == null) {
				namespaceURI = "";
			}
			document.add(createElement(createQName(qualifiedName, namespaceURI)));
		}
		return document;
	}

	@Override
	public Attribute createAttribute(Element owner, QName qname, String value) {
		String name = qname.getName();
		if (owner instanceof StyleDefinerElement) {
			return new DocumentStyleEventAttribute(qname, value);
		} else if ((name = qname.getName()).equals("href") && owner instanceof BaseURLElement) {
			return new BaseHrefAttribute(qname, value);
		} else if (name.equalsIgnoreCase("style")) {
			return new StyleAttribute(qname, value);
		} else {
			return super.createAttribute(owner, qname, value);
		}
	}

	/**
	 * CSS style sheet factory for DOM4J.
	 * 
	 * @author Carlos Amengual
	 * 
	 */
	class DOM4JCSSStyleSheetFactory extends BaseCSSStyleSheetFactory {

		private BaseDocumentCSSStyleSheet defStyleSheet = null;
		private BaseDocumentCSSStyleSheet defQStyleSheet = null;

		/**
		 * User-agent style sheet for standards (strict) mode.
		 */
		private BaseDocumentCSSStyleSheet uaStyleSheet = null;

		/**
		 * User-agent style sheet for quirks mode.
		 */
		private BaseDocumentCSSStyleSheet uaQStyleSheet = null;

		public DOM4JCSSStyleSheetFactory(EnumSet<Flag> enumSet) {
			super(enumSet);
		}

		@Override
		protected AbstractCSSStyleSheet getUserImportantStyleSheet() {
			return super.getUserImportantStyleSheet();
		}

		@Override
		protected AbstractCSSStyleSheet getUserNormalStyleSheet() {
			return super.getUserNormalStyleSheet();
		}

		@Override
		public AbstractCSSStyleDeclaration createAnonymousStyleDeclaration(Node node) {
			return createInlineStyle(node);
		}

		@Override
		protected BaseDocumentCSSStyleSheet createDocumentStyleSheet(byte origin) {
			return new MyDOM4JDocumentCSSStyleSheet(null, origin);
		}

		@Override
		protected BaseCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title,
				MediaQueryList mediaList) {
			return new MyDOM4JCSSStyleSheet(title, null, mediaList, ownerRule, ownerRule.getOrigin());
		}

		@Override
		protected BaseCSSStyleSheet createLinkedStyleSheet(Node ownerNode, String title,
				MediaQueryList mediaList) {
			return new MyDOM4JCSSStyleSheet(title, ownerNode, mediaList, null, CSSStyleSheetFactory.ORIGIN_AUTHOR);
		}

		DOM4JCSSStyleSheet createCSSStyleSheet(String title, Node ownerNode, MediaQueryList media,
				CSSRule ownerRule, byte origin) {
			return new MyDOM4JCSSStyleSheet(title, ownerNode, media, ownerRule, origin);
		}

		DOM4JDocumentCSSStyleSheet createDocumentStyleSheet(String targetMedium, byte origin) {
			return new MyDOM4JDocumentCSSStyleSheet(targetMedium, origin);
		}

		private class MyDOM4JDocumentCSSStyleSheet extends DOM4JDocumentCSSStyleSheet {
			public MyDOM4JDocumentCSSStyleSheet(String targetMedium, byte origin) {
				super(targetMedium, origin);
			}

			@Override
			public DOM4JCSSStyleSheetFactory getStyleSheetFactory() {
				return DOM4JCSSStyleSheetFactory.this;
			}
		}

		private class MyDOM4JCSSStyleSheet extends DOM4JCSSStyleSheet {
			public MyDOM4JCSSStyleSheet(String title, Node ownerNode, MediaQueryList media, CSSRule ownerRule,
					byte origin) {
				super(title, ownerNode, media, ownerRule, origin);
			}

			@Override
			public BaseCSSStyleSheetFactory getStyleSheetFactory() {
				return DOM4JCSSStyleSheetFactory.this;
			}

		}

		@Override
		protected InlineStyle createInlineStyle(Node owner) {
			InlineStyle style;
			if (!hasCompatValueFlags()) {
				style = new MyInlineStyle(owner);
			} else {
				style = new MyCompatInlineStyle(owner);
			}
			return style;
		}

		class MyInlineStyle extends InlineStyle {

			MyInlineStyle(Node owner) {
				super();
				setOwnerNode(owner);
			}

			MyInlineStyle(InlineStyle copiedObject) {
				super(copiedObject);
			}

			@Override
			protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
				return DOM4JCSSStyleSheetFactory.this;
			}

			@Override
			public InlineStyle clone() {
				return new MyInlineStyle(this);
			}

		}

		class MyCompatInlineStyle extends CompatInlineStyle {

			MyCompatInlineStyle(Node owner) {
				super();
				setOwnerNode(owner);
			}

			MyCompatInlineStyle(CompatInlineStyle copiedObject) {
				super(copiedObject);
			}

			@Override
			protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
				return DOM4JCSSStyleSheetFactory.this;
			}

			@Override
			public InlineStyle clone() {
				return new MyCompatInlineStyle(this);
			}

		}

		DOM4JComputedStyle createComputedStyle(CSSElement ownerNode) {
			return new MyDOM4JComputedStyle(ownerNode);
		}

		class MyDOM4JComputedStyle extends DOM4JComputedStyle {
			MyDOM4JComputedStyle(CSSElement ownerNode) {
				super(ownerNode);
			}

			private MyDOM4JComputedStyle(ComputedCSSStyle copiedObject) {
				super(copiedObject);
			}

			@Override
			protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
				return DOM4JCSSStyleSheetFactory.this;
			}

			@Override
			public ComputedCSSStyle clone() {
				MyDOM4JComputedStyle styleClone = new MyDOM4JComputedStyle(this);
				return styleClone;
			}
		}

		/**
		 * Gets the User Agent default CSS style sheet to be used by this factory in the
		 * given mode.
		 * <p>
		 * By default, it contains a default XHTML5 sheet.
		 * </p>
		 * <p>
		 * This implementation does not support <code>!important</code> rules in the
		 * user agent style sheet.
		 * 
		 * @param mode the compliance mode.
		 * @return the user agent style sheet.
		 */
		@Override
		public BaseDocumentCSSStyleSheet getUserAgentStyleSheet(CSSDocument.ComplianceMode mode) {
			if (mode == CSSDocument.ComplianceMode.STRICT) {
				if (uaStyleSheet == null) {
					// Create an empty one
					uaStyleSheet = createDocumentStyleSheet(CSSStyleSheetFactory.ORIGIN_USER_AGENT);
				}
				return uaStyleSheet;
			}
			if (uaQStyleSheet == null) {
				// Create an empty one
				uaQStyleSheet = createDocumentStyleSheet(CSSStyleSheetFactory.ORIGIN_USER_AGENT);
			}
			return uaQStyleSheet;
		}

		@Override
		protected BaseDocumentCSSStyleSheet getDefaultStyleSheet(CSSDocument.ComplianceMode mode) {
			if (defStyleSheet == null) {
				mergeUserSheets();
			}
			BaseDocumentCSSStyleSheet sheet;
			if (mode == CSSDocument.ComplianceMode.STRICT) {
				sheet = defStyleSheet;
			} else {
				sheet = defQStyleSheet;
			}
			return sheet;
		}

		private void mergeUserSheets() {
			defStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.STRICT).clone();
			defQStyleSheet = getUserAgentStyleSheet(CSSDocument.ComplianceMode.QUIRKS).clone();
			AbstractCSSStyleSheet usersheet = getUserNormalStyleSheet();
			if (usersheet != null) {
				defStyleSheet.addStyleSheet(usersheet);
				defQStyleSheet.addStyleSheet(usersheet);
			}
		}

		/**
		 * Sets a default HTML default style sheet as the user agent style
		 * sheet.
		 * <p>
		 * This is not necessary in the DOM4J backend, as that style sheet is
		 * loaded by default.
		 */
		@Override
		public void setDefaultHTMLUserAgentSheet() {
			uaStyleSheet = htmlDefaultSheet();
			uaQStyleSheet = htmlQuirksDefaultSheet();
			defStyleSheet = null;
			defQStyleSheet = null;
		}

	}

}
