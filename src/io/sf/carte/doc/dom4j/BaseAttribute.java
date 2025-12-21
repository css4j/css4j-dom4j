/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;

/**
 * Base attribute.
 * 
 * @author Carlos Amengual
 *
 */
class BaseAttribute extends DOMAttribute {

	private static final long serialVersionUID = 1L;

	BaseAttribute(QName qname) {
		super(qname);
	}

	BaseAttribute(QName qname, String value) {
		super(qname, value);
	}

	BaseAttribute(Element parent, QName qname, String value) {
		super(parent, qname, value);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		XHTMLDocument doc = (XHTMLDocument) getDocument();

		// Check if we are at the root element (no element parent)
		Element owner;
		if (doc != null && (owner = getParent()) != null && owner.getParent() == null) {
			// We set base to null unconditionally first,
			// in case setBaseURL(owner, base) fails
			doc.setBaseURL(null);
			if (value != null && value.length() != 0) {
				doc.setBaseURL((org.w3c.dom.Element) owner, value);
			}
			HrefAttribute.onBaseModify(doc);

			if (owner instanceof CachedXHTMLElement) {
				((CachedXHTMLElement) owner).onStyleModify();
			}
		}
	}

	@Deprecated
	@Override
	public Object getFeature(String feature, String version) {
		return null;
	}

}
