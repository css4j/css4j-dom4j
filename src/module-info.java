/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

module io.sf.carte.css4j.dom4j {
	exports io.sf.carte.doc.dom4j;

	requires static io.sf.carte.css4j.agent.net;
	requires transitive dom4j;
}
