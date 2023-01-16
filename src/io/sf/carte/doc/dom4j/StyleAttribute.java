/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.om.AbstractCSSStyleDeclaration;

/**
 * Style attribute.
 * 
 * @author Carlos Amengual
 *
 */
class StyleAttribute extends DOMAttribute {

	private AbstractCSSStyleDeclaration inlineStyle = null;

	private static final long serialVersionUID = 3L;

	StyleAttribute(QName qname) {
		super(qname);
	}

	StyleAttribute(QName qname, String value) {
		super(qname, value);
	}

	StyleAttribute(Element parent, QName qname, String value) {
		super(parent, qname, value);
	}

	@Override
	public XHTMLDocument getDocument() {
		return (XHTMLDocument) super.getDocument();
	}

	@Override
	public CSSStylableElement getOwnerElement() {
		return (CSSStylableElement) super.getOwnerElement();
	}

	@Override
	public String getValue() {
		if (inlineStyle == null || inlineStyle.getLength() == 0) {
			return super.getValue();
		}
		return getStyle().getCssText();
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		if (inlineStyle != null) {
			setInlineStyle(value);
		} else {
			getStyle();
		}
		// Style is being modified, notify parent element?
		Element parent = getParent();
		if (parent instanceof CachedXHTMLElement) {
			((CachedXHTMLElement) parent).onStyleModify();
		}
	}

	public AbstractCSSStyleDeclaration getStyle() {
		if (inlineStyle == null) {
			XHTMLDocumentFactory factory;
			XHTMLDocument doc = getDocument();
			if (doc != null) {
				factory = doc.getDocumentFactory();
			} else {
				factory = XHTMLDocumentFactory.getInstance();
			}
			inlineStyle = factory.createInlineStyle(this);
			setInlineStyle(super.getValue());
		}
		return inlineStyle;
	}

	void setInlineStyle(String value) {
		if (value == null) {
			value = "";
		}
		try {
			inlineStyle.setCssText(value);
		} catch (DOMException e) {
			getDocument().getErrorHandler().inlineStyleError(getOwnerElement(), e, value);
		}
	}

}
