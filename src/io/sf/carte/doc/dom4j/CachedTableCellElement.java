/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.QName;

import io.sf.carte.doc.style.css.CSSStyleDeclaration;
import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * Cached table cell element.
 * 
 * @author Carlos Amengual
 * 
 */
class CachedTableCellElement extends CachedXHTMLElement {

	private static final long serialVersionUID = 4L;

	protected CachedTableCellElement(String name) {
		super(name);
	}

	protected CachedTableCellElement(QName qname) {
		super(qname);
	}

	CachedTableCellElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	public boolean hasPresentationalHints() {
		return hasAttribute("bgcolor") || hasAttribute("width") || hasAttribute("height") || hasAttribute("background")
				|| hasAttribute("align");
	}

	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
		AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
		AttributeToStyle.width(getAttribute("width"), style);
		AttributeToStyle.height(getAttribute("height"), style);
		AttributeToStyle.background(getAttribute("background"), style);
		AttributeToStyle.align(getAttribute("align"), style);
	}

}
