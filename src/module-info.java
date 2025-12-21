/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-2-Clause OR BSD-3-Clause

/**
 * Brings CSS functionality to dom4j.
 */
module io.sf.carte.css4j.dom4j {
	exports io.sf.carte.doc.dom4j;

	requires transitive io.sf.carte.css4j;
	requires static io.sf.carte.css4j.agent.net;
	requires static io.sf.carte.xml.dtd;
	requires transitive org.dom4j;
	requires static org.xmlpull.mxp1;
	requires static org.xmlpull.v1;
	requires java.xml;
}
