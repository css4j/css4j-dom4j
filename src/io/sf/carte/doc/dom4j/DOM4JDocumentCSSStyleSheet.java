/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import io.sf.carte.doc.dom4j.XHTMLDocumentFactory.DOM4JCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;
import io.sf.carte.doc.style.css.om.InlineStyle;

/**
 * Document CSS Style Sheet for DOM4J.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class DOM4JDocumentCSSStyleSheet extends BaseDocumentCSSStyleSheet {

	private static final long serialVersionUID = 1L;

	private XHTMLDocument ownerElement = null;

	DOM4JDocumentCSSStyleSheet(byte origin) {
		super(null, origin);
	}

	protected DOM4JDocumentCSSStyleSheet(String medium, byte origin) {
		super(medium, origin);
	}

	@Override
	public String getHref() {
		return ownerElement != null ? ownerElement.getBaseURI() : null;
	}

	@Override
	public XHTMLDocument getOwnerNode() {
		return ownerElement;
	}

	@Override
	public void setOwnerDocument(CSSDocument owner) {
		this.ownerElement = (XHTMLDocument) owner;
	}

	@Override
	abstract protected DOM4JComputedStyle createComputedCSSStyle();

	@Override
	abstract public DOM4JCSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Gets the computed style for the given DOM4J element and pseudo-element.
	 * 
	 * @param elm       the element.
	 * @param pseudoElt the pseudo-element.
	 * @return the computed style declaration.
	 */
	@Override
	public ComputedCSSStyle getComputedStyle(CSSElement elm, Condition pseudoElt) {
		// Get the inline style
		InlineStyle inline = (InlineStyle) elm.getStyle();
		// Compute style
		DOM4JComputedStyle styledecl = createComputedCSSStyle();
		styledecl.setOwnerNode(elm);
		ComputedCSSStyle style = computeStyle(styledecl, elm.getSelectorMatcher(), pseudoElt, inline);
		return style;
	}

	/**
	 * Creates and returns a copy of this style sheet.
	 * <p>
	 * The copy is a shallow copy (the rule list is new, but the referenced rules
	 * are the same as in the cloned object.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	public DOM4JDocumentCSSStyleSheet clone() {
		DOM4JDocumentCSSStyleSheet myClone = getStyleSheetFactory().createDocumentStyleSheet(getTargetMedium(),
				getOrigin());
		myClone.setOwnerDocument(ownerElement);
		copyAllTo(myClone);
		return myClone;
	}

	@Override
	public DOM4JDocumentCSSStyleSheet clone(String targetMedium) {
		DOM4JDocumentCSSStyleSheet myClone = getStyleSheetFactory().createDocumentStyleSheet(targetMedium, getOrigin());
		myClone.setOwnerDocument(ownerElement);
		copyToTarget(myClone);
		return myClone;
	}

}
