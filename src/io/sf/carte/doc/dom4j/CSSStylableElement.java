/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.w3c.css.sac.DescendantSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SiblingSelector;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.doc.agent.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.nsac.Parser2;
import io.sf.carte.doc.style.css.om.AbstractSelectorMatcher;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.parser.CSSParser;

/**
 * An element that is stylable with CSS.
 * 
 * @author Carlos Amengual
 * 
 */
abstract public class CSSStylableElement extends DOMElement implements
	CSSElement {

	private static final long serialVersionUID = 8L;

	private SelectorMatcher selectorMatcher = null;

	private Map<String, ExtendedCSSStyleDeclaration> overrideStyleSet = null;

	protected CSSStylableElement(String name) {
		super(name);
	}

	protected CSSStylableElement(QName qname) {
		super(qname);
	}

	protected CSSStylableElement(QName qname, int attributeCount) {
        super(qname, attributeCount);
    }

	@Override
	public XHTMLDocument getOwnerDocument() {
		return (XHTMLDocument) super.getDocument();
	}

	@Override
	public XHTMLDocument getDocument() {
		return (XHTMLDocument) super.getDocument();
	}

	@Override
	protected XHTMLDocumentFactory getDocumentFactory() {
		return (XHTMLDocumentFactory) super.getDocumentFactory();
	}

	@Override
	public String getBaseURI() {
		URL baseURL = getOwnerDocument().getBaseURL();
		if(baseURL != null) {
			return baseURL.toExternalForm();
		} else {
			return null;
		}
	}

	@Override
	public void setIdAttributeNode(Attr idAttr, boolean isId) {
		if (isId && !"id".equalsIgnoreCase(idAttr.getLocalName())) {
			super.setIdAttributeNode(idAttr, isId);
		}
	}

	/**
	 * Gets the <code>id</code> attribute of this element.
	 * 
	 * @return the <code>id</code> attribute, or the empty string if has no ID.
	 */
	@Override
    abstract public String getId();

	/**
	 * Gets the inline style declaration from the current contents of the <code>style</code>
	 * XHTML attribute.
	 * 
	 * @return the style declaration, or <code>null</code> if the element has no
	 *         <code>style</code> attribute.
	 */
	@Override
	public ExtendedCSSStyleDeclaration getStyle() {
		StyleAttribute styleAttr = (StyleAttribute) getAttributeNode("style");
		if (styleAttr == null) {
			Iterator<Attribute> it = attributeIterator();
			while (it.hasNext()) {
				Attribute dom4jattr = it.next();
				if (dom4jattr.getQualifiedName().equalsIgnoreCase("style")) {
					styleAttr = (StyleAttribute) dom4jattr;
					break;
				}
			}
			if (styleAttr == null) {
				return null;
			}
		}
		return styleAttr.getStyle();
	}

	@Override
	public boolean hasPresentationalHints() {
		return false;
	}

	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
	}

	@Override
	public boolean hasOverrideStyle(String pseudoElt) {
		if(overrideStyleSet == null) {
			return false;
		}
		return overrideStyleSet.containsKey(pseudoElt);
	}

	@Override
	public ExtendedCSSStyleDeclaration getOverrideStyle(String pseudoElt) {
		ExtendedCSSStyleDeclaration overrideStyle = null;
		if(overrideStyleSet == null) {
			overrideStyleSet = new HashMap<String, ExtendedCSSStyleDeclaration>(1);
		} else {
			overrideStyle = overrideStyleSet.get(pseudoElt);
		}
		if(overrideStyle == null) {
			overrideStyle = getDocumentFactory().createInlineStyle(this);
			overrideStyleSet.put(pseudoElt, overrideStyle);
		}
		return overrideStyle;
	}

	/**
	 * Gets the computed style declaration that applies to this element.
	 * 
	 * @param pseudoElt
	 *            the pseudo-element name.
	 * @return the computed style declaration.
	 */
	@Override
	public ComputedCSSStyle getComputedStyle(String pseudoElt) {
		XHTMLDocument doc = getOwnerDocument();
		if (doc != null) {
			// Get the style declaration
			ComputedCSSStyle styledecl = (ComputedCSSStyle) doc.getStyleSheet().getComputedStyle(this, pseudoElt);
			return styledecl;
		} else {
			return null;
		}
	}

	/**
	 * Gets the computed style declaration that applies to this element.
	 * 
	 * @return the computed style declaration, or null if none applies.
	 */
	public ComputedCSSStyle getComputedStyle() {
		DocumentCSSStyleSheet css = getOwnerDocument().getStyleSheet();
		if (css != null) {
			// Get the style declaration
			ComputedCSSStyle styledecl = (ComputedCSSStyle) css.getComputedStyle(this, null);
			return styledecl;
		} else {
			return null;
		}
	}

	/**
	 * DOM4J CSS Selector matcher.
	 * 
	 * @author Carlos Amengual
	 * 
	 */
	class DOM4JSelectorMatcher extends AbstractSelectorMatcher {

		DOM4JSelectorMatcher() {
			super();
			setLocalName(getName().toLowerCase(Locale.ROOT).intern());
		}

		@Override
		protected AbstractSelectorMatcher getParentSelectorMatcher() {
			Element parent = getParent();
			if(parent instanceof CSSStylableElement) {
				return (AbstractSelectorMatcher) ((CSSStylableElement)parent).getSelectorMatcher();
			} else {
				return null;
			}
		}

		@Override
		protected AbstractSelectorMatcher getPreviousSiblingSelectorMatcher() {
			Element parent = getParent();
			if(parent == null) {
				return null;
			}
			@SuppressWarnings("rawtypes")
			List elements = parent.elements();
			// Determine previous sibling
			int sibindex = elements.indexOf(CSSStylableElement.this) - 1;
			if (sibindex != -1) {
				Object sibling = elements.get(sibindex);
				if(sibling instanceof CSSStylableElement) {
					return (AbstractSelectorMatcher) ((CSSStylableElement)sibling).getSelectorMatcher();
				}
			}
			return null;
		}

		@Override
		protected int indexOf(SelectorList selectors) {
			Element parent = getParent();
			if(parent == null) {
				return 1; // root element
			}
			NodeList list = getParentNode().getChildNodes();
			int sz = list.getLength();
			int idx = 0;
			for (int i=0; i<sz; i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && matchSelectors(selectors, (CSSStylableElement)node)) {
					idx++;
					if (node == CSSStylableElement.this) {
						break;
					}
				}
			}
			return idx == sz ? -1 : idx;
		}

		@Override
		protected int reverseIndexOf(SelectorList selectors) {
			Element parent = getParent();
			if(parent == null) {
				return 1; // root element
			}
			NodeList list = getParentNode().getChildNodes();
			int sz = list.getLength();
			int idx = 0;
			for (int i=sz-1; i>=0; i--) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && matchSelectors(selectors, (CSSStylableElement)node)) {
					idx++;
					if (node == CSSStylableElement.this) {
						break;
					}
				}
			}
			return idx;
		}

		private boolean matchSelectors(SelectorList selectors, CSSStylableElement node) {
			if (selectors == null) {
				return true;
			}
			int sz = selectors.getLength();
			for (int i=0; i<sz; i++) {
				if (node.getSelectorMatcher().matches(selectors.item(i))) {
					return true;
				}
			}
			return false;
		}

		@Override
		protected boolean isActivePseudoClass(String pseudoclassName) {
			CSSDocument doc = getOwnerDocument();
			CSSCanvas canvas;
			if (doc != null && (canvas = doc.getCanvas()) != null) {
				return canvas.isActivePseudoClass(CSSStylableElement.this, pseudoclassName);
			}
			return false;
		}

		@Override
		protected boolean isFirstChild() {
			Element parent = getParent();
			if(parent == null) {
				return true; // root element
			}
			return parent.elements().indexOf(CSSStylableElement.this) == 0;
		}

		@Override
		protected boolean isLastChild() {
			Element parent = getParent();
			if(parent == null) {
				return true; // root element
			}
			@SuppressWarnings("rawtypes")
			List elems = parent.elements();
			return elems.indexOf(CSSStylableElement.this) == elems.size() - 1;
		}

		@Override
		protected boolean isFirstOfType() {
			Node sibling = getPreviousSibling();
			while (sibling != null) {
				if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(sibling.getNodeName())) {
					return false;
				}
				sibling = sibling.getPreviousSibling();
			}
			return true;
		}

		@Override
		protected boolean isLastOfType() {
			Node sibling = getNextSibling();
			while (sibling != null) {
				if (sibling.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(sibling.getNodeName())) {
					return false;
				}
				sibling = sibling.getNextSibling();
			}
			return true;
		}

		@Override
		protected boolean isNthOfType(int step, int offset) {
			int idx = 0;
			Element parent = getParent();
			if(parent != null) {
				NodeList list = getParentNode().getChildNodes();
				int sz = list.getLength();
				for (int i=0; i<sz; i++) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(node.getNodeName())) {
						idx++;
						if (node == CSSStylableElement.this) {
							break;
						}
					}
				}
			} else {
				idx = 1;
			}
			idx -= offset;
			return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
		}

		@Override
		protected boolean isNthLastOfType(int step, int offset) {
			int idx = 0;
			Element parent = getParent();
			if(parent != null) {
				NodeList list = getParentNode().getChildNodes();
				int sz = list.getLength();
				for (int i=sz-1; i>=0; i--) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE && getLocalName().equals(node.getNodeName())) {
						idx++;
						if (node == CSSStylableElement.this) {
							break;
						}
					}
				}
			} else {
				idx = 1;
			}
			idx -= offset;
			return step == 0 ? idx == 0 : Math.floorMod(idx, step) == 0;
		}

		@Override
		protected boolean isNotVisitedLink() {
			String href = getAttribute("href");
			if (href != null && href.length() != 0) {
				return !getOwnerDocument().isVisitedURI(href);
			} else {
				return false;
			}
		}

		@Override
		protected boolean isVisitedLink() {
			String href = getAttribute("href");
			if (href != null && href.length() != 0) {
				return getOwnerDocument().isVisitedURI(href);
			} else {
				return false;
			}
		}

		@Override
		protected boolean isTarget() {
			return false;
		}

		@Override
		protected boolean isRoot() {
			return getOwnerDocument().getDocumentElement() == CSSStylableElement.this;
		}

		@Override
		protected boolean isEmpty() {
			if (hasChildNodes()) {
				NodeList list = getChildNodes();
				int sz = list.getLength();
				for (int i=0; i<sz; i++) {
					Node node = list.item(i);
					short type = node.getNodeType();
					if (type == Node.ELEMENT_NODE) {
						return false;
					} else if (type == Node.TEXT_NODE) {
						String value = node.getNodeValue();
						if (value != null && value.length() > 0) {
							return false;
						}
					} else if (type == Node.ENTITY_REFERENCE_NODE && node.hasChildNodes()) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		protected boolean isBlank() {
			if (hasChildNodes()) {
				NodeList list = getChildNodes();
				int sz = list.getLength();
				for (int i=0; i<sz; i++) {
					Node node = list.item(i);
					short type = node.getNodeType();
					if (type == Node.ELEMENT_NODE) {
						return false;
					} else if (type == Node.TEXT_NODE) {
						String value = node.getNodeValue();
						if (value != null && value.trim().length() > 0) {
							return false;
						}
					} else if (type == Node.ENTITY_REFERENCE_NODE && node.hasChildNodes()) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		protected boolean isDisabled() {
			/*
			 * A form control is disabled if its disabled attribute is set, or if it is a descendant
			 * of a fieldset element whose disabled attribute is set and is not a descendant of that
			 * fieldset element's first legend element child, if any.
			 */
			if (hasAttribute("disabled")) {
				return true;
			}
			Element parent = getParent();
			if (parent != null && "fieldset".equalsIgnoreCase(parent.getName()) && !"legend".equals(getLocalName())) {
				return parent.attribute("disabled") != null;
			}
			return false;
		}

		@Override
		protected boolean isDefaultButton() {
			// "A form element's default button is the first submit button
			//  in tree order whose form owner is that form element."
			CSSStylableElement parent = (CSSStylableElement) getParent();
			if (parent == null) {
				return false;
			}
			while (parent != null) {
				if ("form".equalsIgnoreCase(parent.getTagName())) {
					break;
				}
				parent = (CSSStylableElement) parent.getParent();
			}
			String formid;
			if (parent == null) {
				formid = null;
			} else {
				formid = parent.getId();
			}
			Node sibling = getPreviousSibling();
			while (sibling != null) {
				if (sibling.getNodeType() == Node.ELEMENT_NODE) {
					CSSStylableElement element = (CSSStylableElement) sibling;
					if (!defaultButtonCheck(element, formid)) {
						return false;
					}
					if (element.hasChildNodes()) {
						NodeList list = element.getChildNodes();
						int sz = list.getLength();
						for (int i = 0; i < sz; i++) {
							Node node = list.item(i);
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if (!defaultButtonCheck((CSSStylableElement)node, formid)) {
									return false;
								}
							}
						}
					}
					sibling = sibling.getPreviousSibling();
				}
			}
			return true;
		}

		private boolean defaultButtonCheck(CSSElement element, String formid) {
			if (!element.hasAttribute("disabled")) {
				String form = element.getAttribute("form");
				if (form == null || form.equals(formid)) {
					String tagname = element.getTagName().toLowerCase(Locale.ROOT);
					if (tagname.equals("input")) {
						String type = element.getAttribute("type");
						if ("submit".equalsIgnoreCase(type) || "image".equalsIgnoreCase(type)) {
							return false;
						}
					} else if (tagname.equals("button")) {
						if ("submit".equalsIgnoreCase(element.getAttribute("type"))) {
							return false;
						}
					}
				}
			}
			return true;
		}

		@Override
		protected String getNamespaceURI() {
			return CSSStylableElement.this.getNamespaceURI();
		}

		@Override
		protected String getAttributeValue(String attrName) {
			String value = attributeValue(attrName);
			return value != null? value : "";
		}

		@Override
		protected boolean hasAttribute(String attrName) {
			return attribute(attrName) != null;
		}

		@Override
		protected String getId() {
			return CSSStylableElement.this.getId();
		}

		@Override
		protected CSSDocument.ComplianceMode getComplianceMode() {
			return CSSStylableElement.this.getOwnerDocument().getComplianceMode();
		}

		@Override
		protected String getLanguage() {
			/*
			 *  In (X)HTML, the lang attribute contains the language,
			 *  but that may not be true for other XML.
			 */
			String lang = attributeValue("lang");
			if(lang == null) {
				lang = attributeValue("LANG");
			}
			Element parent = CSSStylableElement.this;
			while(lang == null || lang.length() == 0) {
				parent = parent.getParent();
				if(parent == null) {
					break;
				} else {
					lang = parent.attributeValue("lang");
					if(lang == null) {
						lang = attributeValue("LANG");
					}
				}
			}
			if(lang == null) {
				lang = "";
			}
			return lang;
		}

		@Override
		protected boolean scopeMatchChild(DescendantSelector selector) {
			SimpleSelector desc = selector.getSimpleSelector();
			NodeList list = getChildNodes();
			int sz = list.getLength();
			for (int i=0; i<sz; i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					SelectorMatcher childSM = ((CSSStylableElement) node).getSelectorMatcher();
					if (childSM.matches(desc)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		protected boolean scopeMatchDirectAdjacent(SiblingSelector selector) {
			SelectorMatcher siblingSM = null;
			Node sibling = getNextSibling();
			while (sibling != null) {
				if (sibling.getNodeType() == Node.ELEMENT_NODE) {
					siblingSM = ((CSSStylableElement) sibling).getSelectorMatcher();
					break;
				}
				sibling = sibling.getNextSibling();
			}
			if (siblingSM != null) {
				return siblingSM.matches(selector.getSiblingSelector());
			}
			return false;
		}
	}

	/**
	 * Gets the selector matcher for this element.
	 * 
	 * @return the selector matcher.
	 */
	@Override
	public SelectorMatcher getSelectorMatcher() {
		if(selectorMatcher == null) {
			selectorMatcher = new DOM4JSelectorMatcher();
		}
		return selectorMatcher;
	}

	@Override
	public boolean matches(String selectorString, String pseudoElement) throws DOMException {
		Parser2 parser = new CSSParser();
		InputSource source = new InputSource(new StringReader(selectorString));
		SelectorList list;
		try {
			list = parser.parseSelectors(source);
		} catch (Exception e) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Unable to parse selector in: " + selectorString);
		}
		return matches(list, pseudoElement);
	}

	@Override
	public boolean matches(SelectorList selist, String pseudoElement) {
		SelectorMatcher matcher = getSelectorMatcher();
		matcher.setPseudoElement(pseudoElement);
		return matcher.matches(selist) != -1;
	}
}
