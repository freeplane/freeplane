/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Freeplane team and others
 *
 *  this file is created by Dimitry Polivaev in 2012.
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
package org.freeplane.core.ui.components.html;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.ScaledHTML.BasicHTMLViewFactory;

import com.lightdev.app.shtm.SHTMLWriter;
import com.lightdev.app.shtm.ScaledStyleSheet;

@SuppressWarnings("serial")
public class ScaledEditorKit extends HTMLEditorKit {
	/** Shared base style for all documents created by us use. */
	private static StyleSheet defaultStyles;

	private ScaledEditorKit() {
	};

	/**
	 * Overriden to return our own slimmed down style sheet.
	 */
	public StyleSheet getStyleSheet() {
		if (defaultStyles == null) {
			defaultStyles = new StyleSheet();
			StringReader r = new StringReader(ScaledHTML.styleChanges);
			try {
				defaultStyles.loadRules(r, null);
			}
			catch (Throwable e) {
				// don't want to die in static initialization... 
				// just display things wrong.
			}
			r.close();
			defaultStyles.addStyleSheet(super.getStyleSheet());
		}
		return defaultStyles;
	}

	@Override
	public Document createDefaultDocument() {
		StyleSheet styles = getStyleSheet();
		StyleSheet ss = new ScaledStyleSheet();
		ss.addStyleSheet(styles);
		HTMLDocument doc = new HTMLDocument(ss);
		doc.setParser(getParser());
		doc.setAsynchronousLoadPriority(4);
		doc.setTokenThreshold(100);
		return doc;
	}

	/**
	 * Sets the async policy to flush everything in one chunk, and
	 * to not display unknown tags.
	 */
	Document createDefaultDocument(Font defaultFont, Color foreground) {
		StyleSheet styles = getStyleSheet();
		StyleSheet ss = new ScaledStyleSheet();
		ss.addStyleSheet(styles);
		HTMLDocument doc = new HTMLDocument(ss);
		doc.setPreservesUnknownTags(false);
		doc.getStyleSheet().addRule(displayPropertiesToCSS(defaultFont, foreground));
		doc.setParser(getParser());
		doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
		doc.setPreservesUnknownTags(false);
		return doc;
	}

	private String displayPropertiesToCSS(Font font, Color fg) {
		StringBuffer rule = new StringBuffer("body {");
		if (font != null) {
			rule.append(" font-family: ");
			rule.append(font.getFamily());
			rule.append(" ; ");
			rule.append(" font-size: ");
			final int fontSize = Math.round(font.getSize() / UITools.FONT_SCALE_FACTOR);
			rule.append(fontSize);
			rule.append("pt ;");
			if (font.isBold()) {
				rule.append(" font-weight: 700 ; ");
			}
			if (font.isItalic()) {
				rule.append(" font-style: italic ; ");
			}
		}
		if (fg != null) {
			rule.append(" color: #");
			if (fg.getRed() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getRed()));
			if (fg.getGreen() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getGreen()));
			if (fg.getBlue() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getBlue()));
			rule.append(" ; ");
		}
		rule.append(" }");
		return rule.toString();
	}

	@Override
	public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
		if (doc instanceof HTMLDocument) {
			HTMLWriter w = new SHTMLWriter(out, (HTMLDocument) doc, pos, len);
			w.write();
		}
		else {
			super.write(out, doc, pos, len);
		}
	}

	/**
	 * Returns the ViewFactory that is used to make sure the Views don't
	 * load in the background.
	 */
	public ViewFactory getViewFactory() {
		return basicHTMLViewFactory;
	}

	static public ScaledEditorKit create() {
		if (kit == null) {
			basicHTMLViewFactory = new BasicHTMLViewFactory();
			kit = new ScaledEditorKit();
		}
		return kit;
	}

	/**
	 * The source of the html renderers
	 */
	private static ScaledEditorKit kit;
	/**
	 * Creates the Views that visually represent the model.
	 */
	static ViewFactory basicHTMLViewFactory;
}
