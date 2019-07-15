/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.net.MalformedURLException;
import java.net.URL;

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

	transient URL base = null;

	private static final long serialVersionUID = 2L;

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
		if(node instanceof Attribute){
			if(node.getName().equals("href")){
				String href = ((Attribute) node).getValue();
				if (href != null) {
					try {
						base = new URL(href);
					} catch (MalformedURLException e) {
						XHTMLDocument doc = getOwnerDocument();
						if(doc != null) {
							doc.setBaseURL(null);
						}
						return;
					}
					XHTMLDocument doc = getOwnerDocument();
					if(doc != null) {
						doc.setBaseURL(base);
					}
				}
			}
		}
	}

	@Override
	protected void childRemoved(Node node) {
		super.childRemoved(node);
		if(node instanceof Attribute && node.getName().equals("href")){
			base = null;
			getOwnerDocument().setBaseURL(null);
		}
	}

}
