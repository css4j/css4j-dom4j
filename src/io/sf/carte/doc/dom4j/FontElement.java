/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.QName;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * Font element.
 * <p>
 * Provides equivalent CSS style for this deprecated element.
 * 
 * @author Carlos Amengual
 *
 */
class FontElement extends XHTMLElement {

	private static final long serialVersionUID = 2L;

	FontElement(String name) {
		super(name);
	}

	FontElement(QName qname) {
		super(qname);
	}

	FontElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	public boolean hasPresentationalHints() {
		return hasAttribute("face") || hasAttribute("size") || hasAttribute("color");
	}

	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
		AttributeToStyle.face(getAttribute("face"), style);
		AttributeToStyle.size(getAttribute("size"), style);
		AttributeToStyle.color(getAttribute("color"), style);
	}

}
