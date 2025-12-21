/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import org.dom4j.Node;
import org.dom4j.QName;

import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;

/**
 * Style definer element.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class StyleDefinerElement extends XHTMLElement implements LinkStyle<AbstractCSSRule> {

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
