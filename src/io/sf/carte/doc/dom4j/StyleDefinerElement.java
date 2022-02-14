/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.Node;
import org.dom4j.QName;
import org.w3c.dom.stylesheets.LinkStyle;

import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;

/**
 * Style definer element.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class StyleDefinerElement extends XHTMLElement implements LinkStyle {

	private static final long serialVersionUID = 2L;

	protected AbstractCSSStyleSheet linkedSheet = null;

	protected boolean needsUpdate = true;

	protected StyleDefinerElement(String name) {
		super(name);
	}

	protected StyleDefinerElement(QName qname) {
		super(qname);
	}

	protected StyleDefinerElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	abstract public AbstractCSSStyleSheet getSheet();

	@Override
	protected void childAdded(Node node) {
		super.childAdded(node);
		resetLinkedSheet();
	}

	@Override
	protected void childRemoved(Node node) {
		super.childRemoved(node);
		resetLinkedSheet();
	}

	@Override
	protected void contentRemoved() {
		super.contentRemoved();
		resetLinkedSheet();
	}

	ErrorHandler getErrorHandler() {
		return getDocument().getErrorHandler();
	}

	void resetLinkedSheet() {
		if (linkedSheet != null) {
			linkedSheet.getCssRules().clear();
		}
		needsUpdate = true;
		XHTMLDocument doc = getOwnerDocument();
		if (doc != null) {
			doc.onStyleModify();
		}
	}

}
