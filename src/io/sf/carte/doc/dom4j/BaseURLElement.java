/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.Attribute;
import org.dom4j.Node;
import org.dom4j.QName;

/**
 * Base URL element.
 * 
 * @author Carlos Amengual
 * 
 */
class BaseURLElement extends XHTMLElement {

	private static final long serialVersionUID = 3L;

	BaseURLElement(String name) {
		super(name);
	}

	BaseURLElement(QName qname) {
		super(qname);
	}

	BaseURLElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	protected void childAdded(Node node) {
		super.childAdded(node);
		if (node instanceof Attribute) {
			if (node.getName().equalsIgnoreCase("href")) {
				String href = ((Attribute) node).getValue();
				if (href != null) {
					XHTMLDocument doc = getOwnerDocument();
					if (doc != null) {
						doc.setBaseURL(this, href);
						HrefAttribute.onBaseModify(doc);
					}
				}
			}
		}
	}

	@Override
	protected void childRemoved(Node node) {
		if (node instanceof Attribute && node.getName().equalsIgnoreCase("href")) {
			XHTMLDocument doc = getOwnerDocument();
			if (doc != null) {
				doc.setBaseURL(null);
				HrefAttribute.onBaseModify(doc);
			}
		}
		super.childRemoved(node);
	}

}
