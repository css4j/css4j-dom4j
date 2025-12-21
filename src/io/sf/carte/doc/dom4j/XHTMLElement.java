/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.w3c.dom.DOMException;
import org.w3c.dom.UserDataHandler;

import io.sf.carte.doc.style.css.LinkStyle;

/**
 * An XHTML element.
 * 
 * @author Carlos Amengual
 * 
 */
public class XHTMLElement extends CSSStylableElement {

	private static final long serialVersionUID = 6L;

	public XHTMLElement(String name) {
		super(name);
	}

	public XHTMLElement(QName qname) {
		super(qname);
	}

	public XHTMLElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	/**
	 * Gets the <code>id</code> attribute of this element.
	 * 
	 * @return the <code>id</code> attribute, or the empty string if has no ID.
	 */
	@Override
	public String getId() {
		String id = attributeValue("id");
		if (id == null) {
			id = attributeValue("ID");
		}
		return id != null ? id : "";
	}

	@Override
	protected String elementID(Element element) {
		String idAttr = element.attributeValue("id");
		if (idAttr == null) {
			idAttr = element.attributeValue("ID");
		}
		return idAttr;
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		if ("id".equals(name) && !hasAttribute("id") && hasAttribute("ID")) {
			name = "ID";
		}
		super.setAttribute(name, value);
	}

	@Override
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
			throws DOMException {
		if ("id".equals(qualifiedName) && !hasAttributeNS(namespaceURI, "id")
				&& hasAttributeNS(namespaceURI, "ID")) {
			qualifiedName = "ID";
		}
		super.setAttributeNS(namespaceURI, qualifiedName, value);
	}

	@Override
	protected void childAdded(Node node) {
		super.childAdded(node);
		XHTMLDocument doc;
		if (node instanceof LinkStyle && (doc = getOwnerDocument()) != null) {
			doc.onLinkStyleAdd((LinkStyle<?>) node);
		}
	}

	@Override
	protected void childRemoved(Node node) {
		XHTMLDocument doc;
		if (node instanceof LinkStyle && (doc = getOwnerDocument()) != null) {
			doc.onLinkStyleRemove((LinkStyle<?>) node);
		}
		super.childRemoved(node);
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	@Deprecated
	@Override
	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
	}
}
