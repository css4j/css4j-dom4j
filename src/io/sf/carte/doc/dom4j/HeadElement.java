/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

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
		if(node instanceof BaseURLElement) {
			getOwnerDocument().setBaseURL(((BaseURLElement)node).base);
			((BaseURLElement)node).base = null; // help GC
		} else if("meta".equals(node.getName())) {
			Element elt = (Element)node;
			String name = elt.getAttribute("http-equiv");
			if("".equals(name)) {
				name = elt.getAttribute("name");
			}
			getOwnerDocument().onMetaAdded(name, elt.getAttribute("content"));
		}
	}

	@Override
	protected void childRemoved(Node node) {
		if("meta".equals(node.getName())) {
			Element elt = (Element)node;
			String name = elt.getAttribute("http-equiv");
			if("".equals(name)) {
				name = elt.getAttribute("name");
			}
			getOwnerDocument().onMetaRemoved(name, elt.getAttribute("content"));
		}
		super.childRemoved(node);
	}

}
