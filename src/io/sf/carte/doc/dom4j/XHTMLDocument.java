/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMDocumentType;
import org.dom4j.dom.DOMElement;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.css.CSSStyleSheet;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.CSSCanvas;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSMediaException;
import io.sf.carte.doc.style.css.CSSPropertyDefinition;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.ErrorHandler;
import io.sf.carte.doc.style.css.LinkStyle;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.SheetErrorHandler;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.InputSource;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheetFactory;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.om.DefaultErrorHandler;
import io.sf.carte.doc.style.css.om.StyleSheetList;

/**
 * XHTML-specific implementation of a DOM4J <code>Document</code>.
 * 
 * @author Carlos Amengual
 * 
 */
public class XHTMLDocument extends DOMDocument implements CSSDocument {

	public final static String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";

	private static final long serialVersionUID = 7L;

	private String documentURI = null;

	private URL baseURL = null;

	private DocumentCSSStyleSheet mergedStyleSheet = null;

	private int styleCacheSerial = Integer.MIN_VALUE;

	private Set<CSSPropertyDefinition> registeredPropertySet = null;

	Set<StyleDefinerElement> linkedStyle = new LinkedHashSet<>(4);

	Set<StyleDefinerElement> embeddedStyle = new LinkedHashSet<>(3);

	private final MyOMStyleSheetList sheets = new MyOMStyleSheetList(7);

	/*
	 * Default style set according to 'Default-Style' meta.
	 */
	private String metaDefaultStyleSet = "";

	/*
	 * Default referrer policy according to 'Referrer-Policy' header/meta.
	 */
	private String metaReferrerPolicy = "";

	private String lastStyleSheetSet = null;

	private String targetMedium = null;

	private final Map<String, CSSCanvas> canvases = new HashMap<String, CSSCanvas>(3);

	private final ErrorHandler errorHandler = createErrorHandler();

	protected XHTMLDocument() {
		super();
	}

	protected XHTMLDocument(String name) {
		super(name);
	}

	protected XHTMLDocument(DOMElement rootElement) {
		super(rootElement);
	}

	protected XHTMLDocument(DOMDocumentType docType) {
		super(docType);
	}

	protected XHTMLDocument(DOMElement rootElement, DOMDocumentType docType) {
		super(rootElement, docType);
	}

	protected XHTMLDocument(String name, DOMElement rootElement, DOMDocumentType docType) {
		super(name, rootElement, docType);
	}

	@Override
	protected String elementID(org.dom4j.Element element) {
		return element.attributeValue("id");
	}

	/**
	 * Get the compatibility mode ({@code compatMode}) attribute.
	 * 
	 * @return the string "BackCompat" if document’s mode is {@code QUIRKS},
	 *         otherwise "CSS1Compat".
	 */
	@Override
	public String getCompatMode() {
		DocumentType doctype = getDoctype();
		if (doctype != null) {
			return "CSS1Compat";
		}
		return "BackCompat";
	}

	@Override
	public CSSDocument.ComplianceMode getComplianceMode() {
		DocumentType doctype = getDoctype();
		if (doctype != null) {
			return CSSDocument.ComplianceMode.STRICT;
		}
		return CSSDocument.ComplianceMode.QUIRKS;
	}

	@Override
	public XHTMLDocument getOwnerDocument() {
		return null;
	}

	@Override
	public XHTMLElement getDocumentElement() {
		return (XHTMLElement) super.getDocumentElement();
	}

	@Override
	public XHTMLElement getElementById(String elementId) {
		return (XHTMLElement) super.getElementById(elementId);
	}

	@Override
	public XHTMLElement createElement(String name) throws DOMException {
		return (XHTMLElement) super.createElement(name);
	}

	@Override
	public XHTMLElement createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return (XHTMLElement) super.createElementNS(namespaceURI, qualifiedName);
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	/**
	 * Give the encoding used when the document was parsed, if known.
	 * 
	 * @return <code>null</code>.
	 */
	@Override
	public String getInputEncoding() {
		return null;
	}

	@Deprecated
	@Override
	public Object getFeature(String feature, String version) {
		return null;
	}

	@Deprecated
	@Override
	public boolean getStrictErrorChecking() {
		return false;
	}

	@Deprecated
	@Override
	public void setStrictErrorChecking(boolean strictErrorChecking) {
	}

	@Deprecated
	@Override
	public void normalizeDocument() {
	}

	/**
	 * Now deprecated, this method gave the encoding specified at the XML declaration.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public String getXmlEncoding() {
		return null;
	}

	/**
	 * Get whether this document is standalone as specified at the XML declaration.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public boolean getXmlStandalone() {
		return false;
	}

	/**
	 * Get the version for the "XML" feature.
	 * 
	 * @return <code>null</code>.
	 */
	@Deprecated
	@Override
	public String getXmlVersion() {
		return null;
	}

	@Override
	public void registerProperty(CSSPropertyDefinition definition) {
		if (registeredPropertySet == null) {
			registeredPropertySet = new HashSet<>();
		}
		registeredPropertySet.add(definition);
		mergedStyleSheet = null;
	}

	/**
	 * A list containing all the style sheets explicitly linked into or embedded
	 * in a document. For HTML documents, this includes external style sheets,
	 * included via the HTML LINK element, and inline STYLE elements. In XML,
	 * this includes external style sheets, included via style sheet processing
	 * instructions (see [XML StyleSheet]).
	 */
	@Override
	public StyleSheetList getStyleSheets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets;
	}

	private void updateStyleLists() {
		/*
		 * Add the linked and embedded styles. Must be added in this order, as
		 * mandated by the CSS spec.
		 */
		sheets.clear();
		// Add styles referenced by links
		Iterator<StyleDefinerElement> links = linkedStyle.iterator();
		while (links.hasNext()) {
			addLinkedSheet(links.next().getSheet());
		}
		// Add embedded styles
		Iterator<StyleDefinerElement> embd = embeddedStyle.iterator();
		while (embd.hasNext()) {
			addLinkedSheet(embd.next().getSheet());
		}
		sheets.setNeedsUpdate(false);
		if (lastStyleSheetSet != null) {
			setSelectedStyleSheetSet(lastStyleSheetSet);
		} else if (metaDefaultStyleSet.length() > 0) {
			setSelectedStyleSheetSet(metaDefaultStyleSet);
			lastStyleSheetSet = null;
		} else {
			setSelectedStyleSheetSet(sheets.getPreferredStyleSheetSet());
			lastStyleSheetSet = null;
		}
		if (getCanvas() != null) {
			getCanvas().reloadStyleState();
		}
	}

	private void addLinkedSheet(AbstractCSSStyleSheet linkedSheet) {
		if (linkedSheet != null) {
			sheets.add(linkedSheet);
		}
	}

	/**
	 * Gets the merged style sheet that applies to this document, resulting from
	 * the merge of the document's default style sheet, the document linked or
	 * embedded style sheets, and the non-important part of the user style
	 * sheet. Does not include overriden styles nor the 'important' part of the
	 * user-defined style sheet.
	 * <p>
	 * The style sheet is lazily built.
	 * 
	 * @return the merged style sheet that applies to this document.
	 */
	@Override
	public DocumentCSSStyleSheet getStyleSheet() {
		if (mergedStyleSheet == null) {
			mergeStyleSheets();
		}
		return mergedStyleSheet;
	}

	private void mergeStyleSheets() {
		getStyleSheets(); // Make sure that sheets are up to date
		BaseDocumentCSSStyleSheet defSheet = getDocumentFactory().getDefaultStyleSheet(getComplianceMode());
		if (targetMedium == null) {
			mergedStyleSheet = defSheet.clone();
		} else {
			mergedStyleSheet = defSheet.clone(targetMedium);
		}
		((BaseDocumentCSSStyleSheet) mergedStyleSheet).setOwnerDocument(this);
		// Add styles referenced by link and style elements
		Iterator<AbstractCSSStyleSheet> it = sheets.iterator();
		while (it.hasNext()) {
			mergedStyleSheet.addStyleSheet(it.next());
		}
		// Add DOM property definitions
		if (registeredPropertySet != null) {
			for (CSSPropertyDefinition def : registeredPropertySet) {
				mergedStyleSheet.registerProperty(def);
			}
		}
	}

	/**
	 * Adds a style sheet (contained by the given InputSource) to the global
	 * style sheet defined by the document's default style sheet and all the
	 * linked and embedded styles.
	 * 
	 * @param cssSrc
	 *            the document's InputSource.
	 * @return <code>true</code> if the parsing reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a DOM problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	public boolean addStyleSheet(InputSource cssSrc) throws DOMException, IOException {
		String media = cssSrc.getMedia();
		if (media != null && !"all".equalsIgnoreCase(media)) {
			// handle as media rule
			MediaQueryList mediaList;
			try {
				mediaList = getDocumentFactory().getStyleSheetFactory().createMediaQueryList(media, null);
			} catch (CSSBudgetException e) {
				DOMException ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
				ex.initCause(e);
				throw ex;
			}
			if (mediaList.isNotAllMedia()) {
				return false;
			}
			AbstractCSSStyleSheet sheet = getDocumentFactory().getStyleSheetFactory()
					.createStyleSheet(cssSrc.getTitle(), mediaList);
			((BaseDocumentCSSStyleSheet) sheet).setOwnerDocument(this);
			boolean result = sheet.parseStyleSheet(cssSrc.getCharacterStream());
			if (result) {
				result = !mediaList.hasErrors();
			}
			getStyleSheet().addStyleSheet(sheet);
			return result;
		} else {
			return getStyleSheet().parseStyleSheet(cssSrc.getCharacterStream());
		}
	}

	/**
	 * Gets the list of available alternate styles.
	 * 
	 * @return the list of available alternate style titles.
	 */
	@Override
	public DOMStringList getStyleSheetSets() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}
		return sheets.getStyleSheetSets();
	}

	/**
	 * Gets the title of the currently selected style sheet set.
	 * 
	 * @return the title of the currently selected style sheet, the empty string if
	 *         none is selected, or <code>null</code> if there are style sheets from
	 *         different style sheet sets that have their style sheet disabled flag
	 *         unset.
	 */
	@Override
	public String getSelectedStyleSheetSet() {
		if (sheets.needsUpdate()) {
			sheets.update();
		}

		String selectedSetName = "";

		Iterator<StyleDefinerElement> links = linkedStyle.iterator();
		while (links.hasNext()) {
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() != 0
					&& !sheet.getDisabled()) {
				if (selectedSetName.length() > 0) {
					if (!selectedSetName.equalsIgnoreCase(title)) {
						return null;
					}
				} else {
					selectedSetName = title;
				}
			}
		}

		Iterator<StyleDefinerElement> style = embeddedStyle.iterator();
		while (links.hasNext()) {
			CSSStyleSheet sheet = style.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0
					&& !sheet.getDisabled()) {
				if (selectedSetName.length() > 0) {
					if (!selectedSetName.equalsIgnoreCase(title)) {
						return null;
					}
				} else {
					selectedSetName = title;
				}
			}
		}

		return selectedSetName;
	}

	/**
	 * Selects a style sheet set, disabling the other non-persistent sheet sets.
	 * If the name is the empty string, all non-persistent sheets will be
	 * disabled. Otherwise, if the name does not match any of the sets, does
	 * nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to select.
	 */
	@Override
	public void setSelectedStyleSheetSet(String name) {
		if (name == null || (name.length() > 0 && !getStyleSheetSets().contains(name))) {
			return;
		}

		selectSheetSet(name, linkedStyle);
		selectSheetSet(name, embeddedStyle);
	}

	private void selectSheetSet(String name, Set<StyleDefinerElement> styleDefinerSet) {
		Iterator<StyleDefinerElement> links = linkedStyle.iterator();
		while (links.hasNext()) {
			String title;
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0) {
				if (title.equals(name)) {
					sheet.setDisabled(false);
					lastStyleSheetSet = name;
				} else {
					sheet.setDisabled(true);
				}
			}
		}
	}

	/**
	 * Gets the style sheet set that was last selected.
	 * 
	 * @return the last selected style sheet set, or <code>null</code> if none.
	 */
	@Override
	public String getLastStyleSheetSet() {
		return lastStyleSheetSet;
	}

	/**
	 * Enables a style sheet set. If the name does not match any of the sets,
	 * does nothing.
	 * 
	 * @param name
	 *            the case-sensitive name of the set to enable.
	 */
	@Override
	public void enableStyleSheetsForSet(String name) {
		if (name == null || name.length() == 0) {
			return;
		}
		enableStyleSheetSet(name, linkedStyle);
		enableStyleSheetSet(name, embeddedStyle);
	}

	private static void enableStyleSheetSet(String name, Set<StyleDefinerElement> styleDefinerSet) {
		Iterator<StyleDefinerElement> links = styleDefinerSet.iterator();
		while (links.hasNext()) {
			AbstractCSSStyleSheet sheet = links.next().getSheet();
			String title;
			if (sheet != null && (title = sheet.getTitle()) != null && title.length() > 0
					&& title.equals(name)) {
				sheet.setDisabled(false);
			}
		}
	}

	void onLinkStyleAdd(LinkStyle<?> element) {
		if (element instanceof LinkElement) {
			linkedStyle.add((LinkElement) element);
		} else if (element instanceof StyleElement) {
			embeddedStyle.add((StyleElement) element);
		}
		onStyleModify();
	}

	void onLinkStyleRemove(LinkStyle<?> element) {
		boolean removed;
		if (element instanceof LinkElement) {
			removed = linkedStyle.remove(element);
		} else if (element instanceof StyleElement) {
			removed = embeddedStyle.remove(element);
		} else {
			removed = false;
		}
		if (removed) {
			CSSStyleSheet sheet = element.getSheet();
			if (sheet != null) {
				String title = sheet.getTitle();
				if (title != null) {
					sheets.remove(title);
				} else {
					sheets.remove(sheet);
				}
			}
			onStyleModify();
		}
	}

	/**
	 * Notifies the document about any change in style.
	 * 
	 */
	void onStyleModify() {
		if (mergedStyleSheet != null) {
			mergedStyleSheet = null;
			styleCacheSerial++;
		}
		sheets.setNeedsUpdate(true);
	}

	/**
	 * Gets the serial number for the document-wide merged style sheet.
	 * <p>
	 * The serial number will be increased by one each time that any of the
	 * sheets that conform the style is changed.
	 * 
	 * @return the serial number for the merged style sheet.
	 */
	int getStyleCacheSerial() {
		return styleCacheSerial;
	}

	/**
	 * Gets the style database currently used to apply specific styles to this
	 * document.
	 * 
	 * @return the style database, or null if no style database has been
	 *         selected.
	 */
	@Override
	public StyleDatabase getStyleDatabase() {
		StyleDatabase sdb = null;
		if (targetMedium != null) {
			DeviceFactory df = getDocumentFactory().getStyleSheetFactory().getDeviceFactory();
			if (df != null) {
				sdb = df.getStyleDatabase(targetMedium);
			}
		}
		return sdb;
	}

	/**
	 * This document's current target medium name.
	 * 
	 * @return the target medium name of this document.
	 */
	@Override
	public String getTargetMedium() {
		return targetMedium;
	}

	/**
	 * Set the medium that will be used to compute the styles of this document.
	 * 
	 * @param medium
	 *            the name of the target medium, like 'screen' or 'print'.
	 * @throws CSSMediaException
	 *             if the document is unable to target the given medium.
	 */
	@Override
	public void setTargetMedium(String medium) throws CSSMediaException {
		if ("all".equalsIgnoreCase(medium)) {
			targetMedium = null;
		} else {
			if (medium != null) {
				medium = medium.intern();
			}
			targetMedium = medium;
		}
		onStyleModify();
	}

	/**
	 * Gets the document's canvas for the current target medium.
	 * 
	 * @return the canvas, or null if no target medium has been set, or the
	 *         DeviceFactory does not support canvas for the target medium.
	 */
	@Override
	public CSSCanvas getCanvas() {
		if (targetMedium == null) {
			return null;
		}
		if (canvases.containsKey(targetMedium)) {
			return canvases.get(targetMedium);
		}
		CSSCanvas canvas;
		DeviceFactory df = getDocumentFactory().getStyleSheetFactory().getDeviceFactory();
		if (df != null) {
			canvas = df.createCanvas(targetMedium, this);
			canvases.put(targetMedium, canvas);
		} else {
			canvas = null;
		}
		return canvas;
	}

	@Override
	public void rebuildCascade() {
		onStyleModify();
	}

	@Override
	protected XHTMLDocumentFactory getDocumentFactory() {
		return (XHTMLDocumentFactory) super.getDocumentFactory();
	}

	protected ErrorHandler createErrorHandler() {
		return new MyDefaultErrorHandler();
	}

	class MyDefaultErrorHandler extends DefaultErrorHandler {

		private static final long serialVersionUID = 1L;

		@Override
		protected AbstractCSSStyleSheetFactory getStyleSheetFactory() {
			return getDocumentFactory().getStyleSheetFactory();
		}

	}

	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Has any of the linked or embedded style sheets any error or warning ?
	 * 
	 * @return <code>true</code> if any of the linked or embedded style sheets has any NSAC or rule error
	 *         or warning, <code>false</code> otherwise.
	 */
	@Override
	public boolean hasStyleIssues() {
		return sheets.hasErrorsOrWarnings() || getErrorHandler().hasErrors() || getErrorHandler().hasWarnings();
	}

	public void onMetaAdded(String name, String attribute) {
		if ("default-style".equalsIgnoreCase(name)) {
			metaDefaultStyleSet = attribute;
			onStyleModify();
		}
	}

	public void onMetaRemoved(String name, String attribute) {
		if ("default-style".equalsIgnoreCase(name)) {
			metaDefaultStyleSet = "";
			onStyleModify();
		}
	}

	/**
	 * Gets the base URL of this Document.
	 * <p>
	 * If the Document's <code>head</code> element has a <code>base</code> child
	 * element, the base URI is computed using the value of the href attribute of
	 * the <code>base</code> element. It can also be set with the
	 * <code>setBaseURL</code> method.
	 * </p>
	 * 
	 * @return the base URL, or null if no base URL could be found.
	 */
	@Override
	public URL getBaseURL() {
		if (baseURL == null) {
			XHTMLElement elm = getDocumentElement();
			if (elm != null) {
				String attr = elm.getAttribute("xml:base");
				if (attr.length() != 0) {
					if (setBaseURL(elm, attr)) {
						return baseURL;
					}
				}
				// Look for BASE element
				org.dom4j.Element head = elm.element("head");
				if (head != null) {
					DOMElement base = (DOMElement) head.element("base");
					if (base != null) {
						String href = base.attributeValue("href");
						if (href != null && href.length() != 0 && setBaseURL(base, href)) {
							return baseURL;
						}
					}
				}
			}
			try {
				baseURL = new URL(documentURI);
			} catch (MalformedURLException e) {
			}
		}
		return baseURL;
	}

	/**
	 * Gets the absolute base URI of this node.
	 * 
	 * @return the absolute base URI of this node, or null if an absolute URI
	 *         could not be obtained.
	 */
	@Override
	public String getBaseURI() {
		URL url = getBaseURL();
		if (url == null) {
			return null;
		}
		return url.toExternalForm();
	}

	/**
	 * Sets the Base URL of this Document.
	 * 
	 * @param baseURL
	 *            the base URL.
	 */
	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
		HrefAttribute.onBaseModify(this);
	}

	/**
	 * Set the {@code BASE} URL obtained from the {@code href} attribute of the
	 * given &lt;base&gt; element.
	 * 
	 * @param element the &lt;base&gt; element.
	 * @param base    the value of the {@code href} attribute.
	 * @return {@code true} if the {@code BASE} URL was set.
	 */
	boolean setBaseURL(Element element, String base) {
		String docUri = getDocumentURI();
		if (docUri != null) {
			URL docUrl;
			try {
				docUrl = new URL(docUri);
			} catch (MalformedURLException e) {
				return setBaseForNullDocumentURI(base, element);
			}
			URL urlBase;
			try {
				urlBase = new URL(docUrl, base);
			} catch (MalformedURLException e) {
				getErrorHandler().ioError(base, e);
				return false;
			}
			String docscheme = docUrl.getProtocol();
			String bscheme = urlBase.getProtocol();
			if (!docscheme.equals(bscheme)) {
				if (!bscheme.equals("https") && !bscheme.equals("http") && !docscheme.equals("file")
						&& !docscheme.equals("jar")) {
					// Remote document wants to set a non-http base URI
					getErrorHandler().policyError(element,
							"Remote document wants to set a non-http base URL: " + urlBase.toExternalForm());
					return false;
				}
			}
			baseURL = urlBase;
			return true;
		} else {
			return setBaseForNullDocumentURI(base, element);
		}
	}

	private boolean setBaseForNullDocumentURI(String base, Element baseElement) {
		try {
			URL urlBase = new URL(base);
			String scheme = urlBase.getProtocol();
			if (scheme.equals("https") || scheme.equals("http")) {
				baseURL = urlBase;
				return true;
			}
			// Remote document wants to set a non-http base URL
			getErrorHandler().policyError(baseElement,
					"Untrusted document wants to set a non-http base URL: " + base);
		} catch (MalformedURLException e) {
			getErrorHandler().ioError(base, e);
		}
		return false;
	}

	/**
	 * Gets an URL for the given URI, taking into account the Base URL if
	 * appropriate.
	 * 
	 * @param uri
	 *            the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException
	 *             if the uri was wrong.
	 */
	@Override
	public URL getURL(String uri) throws MalformedURLException {
		if (uri.length() == 0) {
			throw new MalformedURLException("Empty URI");
		}
		URL url;
		if (uri.indexOf("://") < 0) {
			url = new URL(getBaseURL(), uri);
		} else {
			url = new URL(uri);
		}
		return url;
	}

	/**
	 * Is the provided URL a safe origin to load certain external resources?
	 * 
	 * @param linkedURL
	 *            the URL of the external resource.
	 * 
	 * @return <code>true</code> if is a safe origin, <code>false</code> otherwise.
	 */
	@Override
	public boolean isSafeOrigin(URL linkedURL) {
		URL base = getBaseURL();
		String docHost = base.getHost();
		int docPort = base.getPort();
		if (docPort == -1) {
			docPort = base.getDefaultPort();
		}
		String linkedHost = linkedURL.getHost();
		int linkedPort = linkedURL.getPort();
		if (linkedPort == -1) {
			linkedPort = linkedURL.getDefaultPort();
		}
		return (docHost.equalsIgnoreCase(linkedHost) || linkedHost.endsWith(docHost)) && docPort == linkedPort;
	}

	/**
	 * Determine whether the retrieval of the given URL is authorized.
	 * <p>
	 * If the URL's protocol is not {@code http} nor {@code https} and document's
	 * base URL's scheme is neither {@code file} nor {@code jar}, it is denied.
	 * </p>
	 * 
	 * @param url the URL to check.
	 * @return {@code true} if allowed.
	 */
	@Override
	public boolean isAuthorizedOrigin(URL url) {
		String scheme = url.getProtocol();
		if (documentURI != null) {
			URL base = getBaseURL();
			String baseScheme = base.getProtocol();
			// To try to speed things up, only the parameter's scheme is compared
			// case-insensitively
			if (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http") && !baseScheme.equals("file")
					&& !baseScheme.equals("jar")) {
				return false;
			}
		} else if (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http")) {
			return false;
		}
		return true;
	}

	/**
	 * Get the referrer policy obtained through the 'Referrer-Policy' header or a meta
	 * element.
	 * 
	 * @return the referrer policy, or the empty string if none was specified.
	 */
	@Override
	public String getReferrerPolicy() {
		NodeList nl = getElementsByTagName("meta");
		// The last one takes precedence
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Element el = (Element) nl.item(i);
			if ("referrer".equalsIgnoreCase(el.getAttribute("name"))) {
				String policy = el.getAttribute("content");
				if (policy.length() != 0) {
					metaReferrerPolicy = policy;
					break;
				}
			}
		}
		return metaReferrerPolicy;
	}

	protected void setReferrerPolicyHeader(String policy) {
		if (metaReferrerPolicy.length() == 0) {
			metaReferrerPolicy = policy;
		}
	}

	@Override
	public void setDocumentURI(String documentURI) {
		this.documentURI = documentURI;
		setBaseURL(null);
	}

	@Override
	public String getDocumentURI() {
		return documentURI;
	}

	/**
	 * Opens a connection for the given URL.
	 * 
	 * @param url
	 *            the URL to open a connection to.
	 * @return the URL connection.
	 * @throws IOException
	 *             if the connection could not be opened.
	 */
	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return url.openConnection();
	}

	/**
	 * Opens an InputStream for the given URI, taking into account the Base URL
	 * if needed.
	 * 
	 * @param uri
	 *            the uri to open a connection.
	 * @return the InputStream.
	 * @throws IOException
	 *             if the uri was wrong, or the stream could not be opened.
	 */
	public InputStream openStream(String uri) throws IOException {
		return openConnection(getURL(uri)).getInputStream();
	}

	@Override
	public boolean isVisitedURI(String href) {
		return false;
	}

	class MyOMStyleSheetList extends StyleSheetList {

		private static final long serialVersionUID = 1L;

		protected MyOMStyleSheetList(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		protected boolean hasErrorsOrWarnings() {
			boolean hasRuleErrors = false;
			Iterator<AbstractCSSStyleSheet> it = iterator();
			while (it.hasNext()) {
				AbstractCSSStyleSheet sheet = it.next();
				SheetErrorHandler eh = sheet.getErrorHandler();
				if (sheet.hasRuleErrorsOrWarnings() || eh.hasSacErrors() || eh.hasSacWarnings() || eh.hasOMErrors()
						|| eh.hasOMWarnings()) {
					hasRuleErrors = true;
					break;
				}
			}
			return hasRuleErrors;
		}

		@Override
		protected Iterator<AbstractCSSStyleSheet> iterator() {
			return super.iterator();
		}

		@Override
		protected void clear() {
			super.clear();
		}

		@Override
		protected boolean needsUpdate() {
			return super.needsUpdate();
		}

		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			super.setNeedsUpdate(needsUpdate);
		}

		@Override
		protected void update() {
			super.update();
			updateStyleLists();
		}

	}

}
