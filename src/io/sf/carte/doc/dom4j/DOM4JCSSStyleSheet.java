/*

 Copyright (c) 2005-2026, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

package io.sf.carte.doc.dom4j;

import java.net.URL;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.dom4j.XHTMLDocumentFactory.DOM4JCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.om.AbstractCSSRule;
import io.sf.carte.doc.style.css.om.BaseCSSStyleSheet;

/**
 * CSS Style Sheet for DOM4J.
 */
abstract class DOM4JCSSStyleSheet extends BaseCSSStyleSheet implements Cloneable {

	private static final long serialVersionUID = 2L;

	Node ownerNode;

	public DOM4JCSSStyleSheet(String title, Node ownerNode, MediaQueryList media, AbstractCSSRule ownerRule,
			int origin) {
		super(title, media, ownerRule, origin);
		this.ownerNode = ownerNode;
	}

	@Override
	public String getHref() {
		String href = super.getHref();
		if (href == null) {
			CSSDocument document = (CSSDocument) ownerNode.getOwnerDocument();
			URL url = document.getBaseURL();
			if (url != null) {
				href = url.toExternalForm();
			}
		}
		return href;
	}

	@Override
	public Node getOwnerNode() {
		return ownerNode;
	}

	@Override
	protected void setMedia(MediaQueryList media) throws DOMException {
		super.setMedia(media);
	}

	@Override
	protected void setTitle(String title) {
		super.setTitle(title);
	}

	@Override
	protected String getTargetMedium() {
		if (ownerNode != null) {
			return ((CSSDocument) ownerNode.getOwnerDocument()).getStyleSheet().getTargetMedium();
		}
		return null;
	}

	/**
	 * Creates and returns a copy of this style sheet.
	 * <p>
	 * The copy is a shallow copy (the rule list is new, but the referenced rules
	 * are the same as in the cloned object.
	 * 
	 * @return a clone of this instance.
	 */
	@Override
	public DOM4JCSSStyleSheet clone() {
		DOM4JCSSStyleSheet myClone = ((DOM4JCSSStyleSheetFactory) getStyleSheetFactory())
				.createCSSStyleSheet(getTitle(), getOwnerNode(), getMedia(), getOwnerRule(), getOrigin());
		myClone.setHref(getHref());
		copyAllTo(myClone);
		return myClone;
	}

}
