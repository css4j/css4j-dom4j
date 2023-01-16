/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMAttribute;

/**
 * An attribute that fires a parent and document-wide style modify event when changed.
 * 
 * @author Carlos Amengual
 *
 */
class DocumentStyleEventAttribute extends DOMAttribute {

	private static final long serialVersionUID = 2L;

	DocumentStyleEventAttribute(QName qname) {
		super(qname);
	}

	DocumentStyleEventAttribute(QName qname, String value) {
		super(qname, value);
	}

	DocumentStyleEventAttribute(Element parent, QName qname, String value) {
		super(parent, qname, value);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		Element owner = getParent();
		if (owner != null) {
			if (owner instanceof StyleDefinerElement) {
				((StyleDefinerElement) owner).resetLinkedSheet();
			} else if (owner instanceof CachedXHTMLElement) {
				((CachedXHTMLElement) owner).onStyleModify();
			}
		}
	}

}
