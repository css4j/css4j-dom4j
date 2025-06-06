/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.net.URL;

import org.dom4j.QName;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.DOMPolicyException;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.MediaFactory;

/**
 * LINK element.
 * 
 * @author Carlos Amengual
 * 
 */
class LinkElement extends StyleDefinerElement {

	private static final long serialVersionUID = 2L;

	LinkElement(String name) {
		super(name);
	}

	LinkElement(QName qname) {
		super(qname);
	}

	LinkElement(QName qname, int attributeCount) {
		super(qname, attributeCount);
	}

	/**
	 * Gets the associated style sheet for the node.
	 * 
	 * @return the associated style sheet for the node, or <code>null</code> if the
	 *         sheet is not CSS or the media attribute was not understood. If the
	 *         URL is invalid or the sheet could not be parsed, the returned sheet
	 *         will be empty.
	 */
	@Override
	public AbstractCSSStyleSheet getSheet() {
		if (needsUpdate) {
			String nsuri = getNamespaceURI();
			if (nsuri != null && nsuri.length() != 0 && !nsuri.equals(XHTMLDocument.XHTML_NAMESPACE_URI)) {
				return null;
			}
			String rel = getAttribute("rel");
			String type = getAttribute("type");
			int typelen = type.length();
			if (typelen == 0) {
				if (rel.length() == 0) {
					return null;
				}
			} else if (!"text/css".equalsIgnoreCase(type)) {
				return null;
			}
			byte relAttr = AbstractCSSStyleSheet.parseRelAttribute(rel);
			if (relAttr != -1) {
				String href = attributeValue("href");
				String title = attributeValue("title");
				if (title != null && (title = title.trim()).length() == 0) {
					title = null;
				}
				if (href != null && href.length() != 0) {
					if (relAttr == 0) {
						if (href != null && href.length() != 0) {
							loadStyleSheet(href, title);
						}
					} else {
						if (title != null) {
							if (href != null && href.length() != 0) {
								XHTMLDocument doc = getDocument();
								boolean disable = linkedSheet == null || ((doc = getDocument()) != null
										&& !title.equalsIgnoreCase(doc.getSelectedStyleSheetSet()));
								loadStyleSheet(href, title);
								if (disable) {
									linkedSheet.setDisabled(true);
								}
							}
						} else {
							getErrorHandler().linkedStyleError(this, "Alternate sheet without title");
						}
					}
					needsUpdate = false;
				} else {
					getErrorHandler().linkedStyleError(this, "Missing or void href attribute.");
				}
			} else {
				linkedSheet = null;
			}
		}
		return linkedSheet;
	}

	private void loadStyleSheet(String href, String title) {
		String media = attributeValue("media");
		MediaQueryList mediaList;
		if (media == null || media.trim().length() == 0) {
			mediaList = MediaFactory.createAllMedia();
		} else {
			try {
				mediaList = getDocumentFactory().getStyleSheetFactory().createImmutableMediaQueryList(media, this);
			} catch (CSSBudgetException e) {
				getErrorHandler().linkedStyleError(this, e.getMessage());
				linkedSheet = null;
				return;
			}
			if (mediaList.isNotAllMedia() && mediaList.hasErrors()) {
				linkedSheet = null;
				return;
			}
		}
		if (linkedSheet == null) {
			linkedSheet = getDocumentFactory().createLinkedStyleSheet(this, title, mediaList);
		} else {
			DOM4JCSSStyleSheet mysheet = (DOM4JCSSStyleSheet) linkedSheet;
			mysheet.setMedia(mediaList);
			mysheet.setTitle(title);
			mysheet.getCssRules().clear();
		}
		String referrerPolicy = attributeValue("referrerpolicy");
		if (referrerPolicy == null) {
			referrerPolicy = "";
		}
		try {
			URL url = getOwnerDocument().getURL(href);
			if (getOwnerDocument().isAuthorizedOrigin(url)) {
				linkedSheet.setHref(url.toExternalForm());
				linkedSheet.loadStyleSheet(url, referrerPolicy);
			} else {
				getErrorHandler().policyError(this, "Unauthorized URL: " + url.toExternalForm());
			}
		} catch (IOException e) {
			getErrorHandler().ioError(href, e);
		} catch (DOMPolicyException e) {
			// Already logged
			linkedSheet = null;
		} catch (DOMException e) {
			// Already logged
		} catch (Exception e) {
			getErrorHandler().linkedSheetError(e, linkedSheet);
		}
	}

}
