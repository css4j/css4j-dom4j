/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.net.MalformedURLException;
import java.net.URL;

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
		if (value != null) {
			URL base;
			try {
				base = new URL(value);
			} catch (MalformedURLException e) {
				XHTMLDocument doc = (XHTMLDocument) getDocument();
				if(doc != null) {
					doc.setBaseURL(null);
				}
				return;
			}
			XHTMLDocument doc = (XHTMLDocument) getDocument();
			if(doc != null) {
				doc.setBaseURL(base);
			}
		} else {
			XHTMLDocument doc = (XHTMLDocument) getDocument();
			if(doc != null) {
				doc.setBaseURL(null);
			}
		}
	}

}
