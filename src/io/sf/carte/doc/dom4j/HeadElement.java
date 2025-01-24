/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.dom4j.Node;
import org.dom4j.QName;
import org.w3c.dom.Element;

/**
 * XHTML head element.
 * 
 * @author Carlos Amengual
 * 
 */
class HeadElement extends XHTMLElement {

	private static final long serialVersionUID = 3L;

	HeadElement(String name) {
		super(name);
	}

	HeadElement(QName qname) {
		super(qname);
	}

	HeadElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	protected void childAdded(Node node) {
		super.childAdded(node);
		if (node instanceof BaseURLElement) {
			XHTMLDocument doc = getOwnerDocument();
			if (doc != null) {
				String href = ((BaseURLElement) node).getAttribute("href").trim();
				if (href.length() != 0) {
					doc.setBaseURL(this, href);
				}
			}
		} else if ("meta".equalsIgnoreCase(node.getName())) {
			XHTMLDocument doc = getOwnerDocument();
			if (doc != null) {
				Element elt = (Element) node;
				String name = elt.getAttribute("http-equiv");
				if (name.length() == 0) {
					name = elt.getAttribute("name");
				}
				doc.onMetaAdded(name, elt.getAttribute("content"));
			}
		}
	}

	@Override
	protected void childRemoved(Node node) {
		String nName = node.getName();
		if ("meta".equalsIgnoreCase(nName)) {
			Element elt = (Element) node;
			String name = elt.getAttribute("http-equiv");
			if (name.length() == 0) {
				name = elt.getAttribute("name");
			}
			getOwnerDocument().onMetaRemoved(name, elt.getAttribute("content"));
		} else if (node instanceof BaseURLElement) {
			XHTMLDocument doc = getOwnerDocument();
			if (doc != null) {
				doc.setBaseURL(null);
				HrefAttribute.onBaseModify(doc);
			}
		}
		super.childRemoved(node);
	}

}
