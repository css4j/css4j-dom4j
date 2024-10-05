/*

 Copyright (c) 2020-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom4j;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class PermissiveErrorHandler implements ErrorHandler {

	public PermissiveErrorHandler() {
		super();
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		throw exception;
	}

}
