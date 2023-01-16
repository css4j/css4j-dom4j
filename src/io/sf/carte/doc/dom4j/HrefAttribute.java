/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;

/**
 * Href attribute.
 * 
 * @author Carlos Amengual
 *
 */
class HrefAttribute extends DOMAttribute {

	private static final long serialVersionUID = 3L;

	HrefAttribute(QName qname) {
		super(qname);
	}

	HrefAttribute(QName qname, String value) {
		super(qname, value);
	}

	HrefAttribute(Element parent, QName qname, String value) {
		super(parent, qname, value);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		XHTMLDocument doc = (XHTMLDocument) getDocument();
		org.w3c.dom.Element owner;
		if (doc != null && (owner = getOwnerElement()) != null) {
			if (owner instanceof StyleDefinerElement) {
				StyleDefinerElement element = (StyleDefinerElement) owner;
				element.resetLinkedSheet();
			} else if ("base".equalsIgnoreCase(owner.getLocalName())) {
				// We set base to null unconditionally first,
				// in case setBaseURL(owner, base) fails
				doc.setBaseURL(null);
				if (value != null && value.length() != 0) {
					doc.setBaseURL(owner, value);
				}
				onBaseModify(doc);
			}
			if (owner instanceof CachedXHTMLElement) {
				((CachedXHTMLElement) owner).onStyleModify();
			}
		}
	}

	static void onBaseModify(XHTMLDocument doc) {
		Iterator<LinkElement> links = doc.linkedStyle.iterator();
		while (links.hasNext()) {
			links.next().resetLinkedSheet();
		}
	}

}
