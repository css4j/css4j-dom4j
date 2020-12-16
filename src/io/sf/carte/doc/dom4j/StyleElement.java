/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.dom4j.QName;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.MediaFactory;

/**
 * <code>style</code> element.
 * 
 * @author Carlos Amengual
 * 
 */
class StyleElement extends StyleDefinerElement {

	private static final long serialVersionUID = 2L;

	StyleElement(String name) {
		super(name);
	}

	StyleElement(QName qname) {
		super(qname);
	}

	StyleElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	@Override
	public void normalize() {
		if (!containsCSS()) {
			super.normalize();
		} else {
			// Local reference to sheet, to avoid race conditions.
			final AbstractCSSStyleSheet sheet = getSheet();
			if (sheet == null) {
				super.normalize();
			} else {
				super.setText(sheet.toString());
			}
		}
	}

	private boolean containsCSS() {
		if (linkedSheet != null) {
			// Local reference to sheet, to avoid race conditions.
			final AbstractCSSStyleSheet sheet;
			String type = attributeValue("type");
			if ("text/css".equalsIgnoreCase(type) || ((type == null || type.length() == 0)
					&& (sheet = getSheet()) != null && sheet.getCssRules().getLength() != 0)) {
				return true;
			}
		}
		/*
		 * If the sheet has not been processed yet, we return false as well.
		 */
		return false;
	}

	/**
	 * Gets the associated style sheet for the node.
	 * 
	 * @return the associated style sheet for the node, or <code>null</code> if the
	 *         sheet is not CSS or the media attribute was not understood. If the
	 *         element is empty or the sheet could not be parsed, the returned sheet
	 *         will be empty.
	 */
	@Override
	public AbstractCSSStyleSheet getSheet() {
		if (needsUpdate) {
			String nsuri = getNamespaceURI();
			if (nsuri != null && nsuri.length() != 0 && !nsuri.equals(XHTMLDocument.XHTML_NAMESPACE_URI)) {
				return null;
			}
			String type = attributeValue("type");
			if (type != null && !"text/css".equalsIgnoreCase(type) && type.length() != 0) {
				return null;
			}
			String media = attributeValue("media");
			MediaQueryList mediaList;
			if (media == null || media.trim().length() == 0) {
				mediaList = MediaFactory.createAllMedia();
			} else {
				try {
					mediaList = getDocumentFactory().getStyleSheetFactory().createImmutableMediaQueryList(media, this);
				} catch (CSSBudgetException e) {
					getErrorHandler().linkedStyleError(this, e.getMessage());
					return null;
				}
				if (mediaList.isNotAllMedia() && mediaList.hasErrors()) {
					return null;
				}
			}
			String title = attributeValue("title");
			if (title != null && (title = title.trim()).length() == 0) {
				title = null;
			}
			if (linkedSheet == null) {
				linkedSheet = getDocumentFactory().createLinkedStyleSheet(this, title, mediaList);
			} else {
				DOM4JCSSStyleSheet mysheet = (DOM4JCSSStyleSheet) linkedSheet;
				mysheet.setMedia(mediaList);
				mysheet.setTitle(title);
				linkedSheet.getCssRules().clear();
			}
			String styleText = getText();
			if (styleText.length() != 0) {
				linkedSheet.setHref(getBaseURI());
				Reader re = new StringReader(styleText);
				try {
					linkedSheet.parseStyleSheet(re);
				} catch (DOMException e) {
					getErrorHandler().linkedSheetError(e, linkedSheet);
				} catch (IOException e) {
					getErrorHandler().linkedSheetError(e, linkedSheet);
				}
			}
			needsUpdate = false;
		}
		return linkedSheet;
	}

	@Override
	void resetLinkedSheet() {
		super.resetLinkedSheet();
		if (linkedSheet != null) {
			getSheet();
		}
	}

}
