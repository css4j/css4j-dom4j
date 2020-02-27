/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.QName;
import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * Cached table element.
 * 
 * @author Carlos Amengual
 * 
 */
class CachedTableElement extends CachedXHTMLElement {

	private static final long serialVersionUID = 4L;

	protected CachedTableElement(String name) {
		super(name);
	}

	protected CachedTableElement(QName qname) {
		super(qname);
	}

    CachedTableElement(QName qname, int attributeCount) {
        super(qname, attributeCount);
    }

	@Override
	public boolean hasPresentationalHints() {
		return hasAttribute("width") || hasAttribute("height") || hasAttribute("cellspacing")
				|| hasAttribute("border") || hasAttribute("bordercolor") || hasAttribute("bgcolor")
				|| hasAttribute("background");
	}

	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
		AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
		AttributeToStyle.cellSpacing(getAttribute("cellspacing"), style);
		AttributeToStyle.width(getAttribute("width"), style);
		AttributeToStyle.height(getAttribute("height"), style);
		AttributeToStyle.border(getAttribute("border"), style);
		AttributeToStyle.borderColor(getAttribute("bordercolor"), style);
		AttributeToStyle.background(getAttribute("background"), style);
	}

}
