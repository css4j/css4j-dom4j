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
 * img element.
 * 
 * @author Carlos Amengual
 * 
 */
class ImgElement extends XHTMLElement {

	private static final long serialVersionUID = 1L;

	ImgElement(String name) {
		super(name);
	}

	ImgElement(QName qname) {
		super(qname);
	}

	ImgElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	public boolean hasPresentationalHints() {
		return hasAttribute("width") || hasAttribute("height") || hasAttribute("border") || hasAttribute("hspace")
				|| hasAttribute("vspace");
	}

	@Override
	public void exportHintsToStyle(CSSStyleDeclaration style) {
		AttributeToStyle.width(getAttribute("width"), style);
		AttributeToStyle.height(getAttribute("height"), style);
		AttributeToStyle.hspace(getAttribute("hspace"), style);
		AttributeToStyle.vspace(getAttribute("vspace"), style);
		if (AttributeToStyle.border(getAttribute("border"), style)) {
			style.setProperty("border-top-style", "solid", null);
			style.setProperty("border-right-style", "solid", null);
			style.setProperty("border-bottom-style", "solid", null);
			style.setProperty("border-left-style", "solid", null);
		}
	}

}
