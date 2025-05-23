/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import org.dom4j.dom.DOMElement;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.BoxModelHelper;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;

/**
 * Computed style for DOM4J.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class DOM4JComputedStyle extends ComputedCSSStyle {

	private static final long serialVersionUID = 1L;

	private transient ComputedCSSStyle parentStyle = null;

	DOM4JComputedStyle(BaseDocumentCSSStyleSheet docSheet) {
		super(docSheet);
	}

	DOM4JComputedStyle(ComputedCSSStyle copiedObject) {
		super(copiedObject);
	}

	@Override
	protected void setOwnerNode(CSSElement node) {
		super.setOwnerNode(node);
	}

	@Override
	public ComputedCSSStyle getParentComputedStyle() {
		if (parentStyle == null) {
			Node node = getOwnerNode();
			while (node != null) {
				node = node.getParentNode();
				if (node == null) {
					break;
				}
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					parentStyle = ((CSSStylableElement) node).getComputedStyle();
					break;
				}
			}
		}
		return parentStyle;
	}

	/**
	 * Gets the (whitespace-trimmed) text content of the node associated to this
	 * style.
	 * 
	 * @return the text content, or the empty string if the box has no text.
	 */
	@Override
	public String getText() {
		String text;
		Node node = getOwnerNode();
		if (node instanceof DOMElement) {
			text = ((DOMElement) node).getTextTrim();
		} else if (node instanceof org.dom4j.Node) {
			text = BoxModelHelper.contractSpaces(((org.dom4j.Node) node).getText());
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
		if (node != null) {
			CSSDocument doc = (CSSDocument) node.getOwnerDocument();
			return doc.getStyleDatabase();
		}
		return null;
	}

}
