/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.QName;

import io.sf.carte.doc.style.css.om.ComputedCSSStyle;

/**
 * Version of CSSStylableElement that caches computed style values;
 * 
 * @author Carlos Amengual
 *
 */
class CachedXHTMLElement extends XHTMLElement {

	private static final long serialVersionUID = 3L;

	private ComputedCSSStyle cachedComputedStyle = null;

	private int cacheSerial = Integer.MIN_VALUE;

	CachedXHTMLElement(String name) {
		super(name);
	}

	CachedXHTMLElement(QName qname) {
		super(qname);
	}

	CachedXHTMLElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	public ComputedCSSStyle getComputedStyle() {
		int documentCacheSerial = getOwnerDocument().getStyleCacheSerial();
		if (cachedComputedStyle == null || cacheSerial != documentCacheSerial) {
			cacheSerial = documentCacheSerial;
			cachedComputedStyle = super.getComputedStyle();
		}
		return cachedComputedStyle;
	}

	/**
	 * Notifies the element about any change in the style attribute.
	 *
	 */
	public void onStyleModify() {
		// clear cached style
		cachedComputedStyle = null;
		// Cascade notification
		@SuppressWarnings("rawtypes")
		Iterator elements = elementIterator();
		while (elements.hasNext()) {
			Element element = (Element) elements.next();
			if (element instanceof CachedXHTMLElement) {
				((CachedXHTMLElement) element).onStyleModify();
			}
		}
		getDocument().onStyleModify();
	}

}
