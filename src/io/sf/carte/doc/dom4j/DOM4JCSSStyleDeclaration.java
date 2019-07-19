/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.dom.DOMElement;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.om.BoxModelHelper;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;

/**
 * Style declaration for DOM4J.
 * 
 * @author Carlos Amengual
 * 
 */
class DOM4JCSSStyleDeclaration extends ComputedCSSStyle {

	public DOM4JCSSStyleDeclaration() {
		super();
	}

	DOM4JCSSStyleDeclaration(ComputedCSSStyle copiedObject) {
		super(copiedObject);
	}

	@Override
	public ComputedCSSStyle getParentComputedStyle() {
		ComputedCSSStyle parentCss = null;
		Node node = getOwnerNode();
		while (node != null) {
			node = node.getParentNode();
			if(node == null) {
				break;
			}
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				parentCss = ((CSSStylableElement)node).getComputedStyle();
				break;
			}
		}
		return parentCss;
	}

	@Override
	protected void setOwnerNode(Node node) {
		super.setOwnerNode(node);
	}

	/**
	 * Gets the (whitespace-trimmed) text content of the 
	 * node associated to this style.
	 * 
	 * @return the text content, or the empty string if the box 
	 * has no text.
	 */
	@Override
	public String getText() {
		String text;
		Node node = getOwnerNode();
		if(node instanceof DOMElement) {
			text = ((DOMElement)node).getTextTrim();
		} else if(node instanceof org.dom4j.Node) {
			text = BoxModelHelper.contractSpaces(((org.dom4j.Node)node).getText());
		} else {
			text = "";
		}
		return text.trim();
	}

	/**
	 * Gets the style database which is used to compute the style.
	 * 
	 * @return the style database, or null if no style database has been selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		Node node = getOwnerNode();
		if(node != null) {
			CSSDocument doc = (CSSDocument) node.getOwnerDocument();
			return doc.getStyleDatabase();
		}
		return null;
	}

	@Override
	public ComputedCSSStyle clone() {
		DOM4JCSSStyleDeclaration styleClone = new DOM4JCSSStyleDeclaration(this);
		return styleClone;
	}
}
