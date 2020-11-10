/*

 Copyright (c) 2005-2020, Carlos Amengual.

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
 * Href attribute of Base element.
 * 
 * @author Carlos Amengual
 *
 */
class BaseHrefAttribute extends DOMAttribute {

	private static final long serialVersionUID = 3L;

	BaseHrefAttribute(QName qname) {
		super(qname);
	}

	BaseHrefAttribute(QName qname, String value) {
		super(qname, value);
	}

	BaseHrefAttribute(Element parent, QName qname, String value) {
		super(parent, qname, value);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		XHTMLDocument doc = (XHTMLDocument) getDocument();
		if (doc != null) {
			if (value != null && value.length() != 0) {
				org.w3c.dom.Element owner = getOwnerElement();
				// We set base to null first, in case setBaseURL(owner, base) fails
				doc.setBaseURL(null);
				doc.setBaseURL(owner, value);
			} else {
				doc.setBaseURL(null);
			}
			onBaseModify(doc);
		}
	}

	static void onBaseModify(XHTMLDocument doc) {
		Iterator<LinkElement> links = doc.linkedStyle.iterator();
		while (links.hasNext()) {
			links.next().resetLinkedSheet();
		}
	}

}
