/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.map;

import org.freeplane.core.extension.ExtensionContainer;
import org.freeplane.core.extension.SmallExtensionMap;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.XmlUtils;

/**
 * @author  Dimitry Polivaev 05.02.2014
 */
public class SharedNodeData {
	final private ExtensionContainer extensionContainer;
	private HistoryInformationModel historyInformation;
	final private NodeIconSetModel icons;
	private Object userObject;
	private String xmlText;
	private boolean folded;

	public SharedNodeData() {
		extensionContainer = new ExtensionContainer(new SmallExtensionMap());
		icons = new NodeIconSetModel();
		folded = false;
	}

	public ExtensionContainer getExtensionContainer() {
		return extensionContainer;
	}

	public HistoryInformationModel getHistoryInformation() {
		return historyInformation;
	}

	public void setHistoryInformation(HistoryInformationModel historyInformation) {
		this.historyInformation = historyInformation;
	}

	public NodeIconSetModel getIcons() {
		return icons;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object data) {
		if (data instanceof String) {
			setText(data.toString());
		}
		else{
			xmlText = null;
			this.userObject = data;
		}
	}

	public String getXmlText() {
		return xmlText;
	}

	public void setXmlText(String content) {
		xmlText = XmlUtils.makeValidXml(content);
		userObject = HtmlUtils.toHtml(xmlText);
	}

	public void setText(String text) {
		try {
			userObject = XmlUtils.makeValidXml(text);
			xmlText = HtmlUtils.toXhtml(text);
			if (xmlText != null && !xmlText.startsWith("<")) {
				userObject = " " + text;
				xmlText = null;
			}
		} catch (Exception e) {
			userObject = " " + text;
			xmlText = null;
		}
	}

	public void setFolded(boolean folded) {
		this.folded = folded;
	}

	public boolean isFolded() {
		return folded;
	}
}