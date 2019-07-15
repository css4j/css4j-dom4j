/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.QName;
import org.w3c.dom.css.CSSStyleDeclaration;

import io.sf.carte.doc.style.css.property.AttributeToStyle;

/**
 * Table element.
 * 
 * @author Carlos Amengual
 * 
 */
class TableElement extends XHTMLElement {

	private static final long serialVersionUID = 4L;

	protected TableElement(String name) {
		super(name);
	}

	protected TableElement(QName qname) {
		super(qname);
	}

    TableElement(QName qname, int attributeCount) {
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
