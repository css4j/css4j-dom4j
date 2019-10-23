/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.QName;

import io.sf.carte.doc.style.css.ExtendedCSSStyleDeclaration;
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
		return hasAttribute("bgcolor");
	}

	@Override
	public void exportHintsToStyle(ExtendedCSSStyleDeclaration style) {
		AttributeToStyle.bgcolor(getAttribute("bgcolor"), style);
	}

}
