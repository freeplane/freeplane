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
import java.io.StringReader;

import javax.swing.JLabel;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlProcessor;

import com.lightdev.app.shtm.SHTMLEditorKit.SHTMLFactory;

@SuppressWarnings("serial")
public class ScaledEditorKit extends HTMLEditorKit {
	/** Shared base style for all documents created by us use. */
	private static StyleSheet defaultStyles;

    private static final ViewFactory defaultFactory = new SHTMLFactory();

    /**
     * Fetch a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.
     *
     * @return the factory
     */
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

	protected ScaledEditorKit() {}

	/**
	 * Overriden to return our own slimmed down style sheet.
	 */
	public StyleSheet getStyleSheet() {
		if (defaultStyles == null) {
			defaultStyles = new StyleSheet();
			try (StringReader r = new StringReader(ScaledHTML.styleChanges)){
				defaultStyles.loadRules(r, null);
			}
			catch (Throwable e) {
				// don't want to die in static initialization...
				// just display things wrong.
			}
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

	static public ScaledEditorKit create() {
		if (kit == null) {
			kit = new ScaledEditorKit();
		}
		return kit;
	}

	/**
	 * The source of the html renderers
	 */
	private static ScaledEditorKit kit;

	public Document createDefaultDocument(JLabel c) {
		StyleSheet defaultstyles = getStyleSheet();
		StyleSheet ss = new ScaledStyleSheet();
		ss.addStyleSheet(defaultstyles);
		Font font = c.getFont();
		Color foreground = c.getForeground();
		StyleSheet ownStyles = new StyleSheet();
		ownStyles.addRule(new StringBuffer("body {").append(new CssRuleBuilder()
				.withCSSFont(font, UITools.FONT_SCALE_FACTOR)
				.withColor(foreground)
				.withAlignment(c.getHorizontalAlignment())).append("}").toString());
		ss.addStyleSheet(ownStyles);
		StyleSheet customStyleSheet = (StyleSheet) c.getClientProperty(StyleSheet.class);
		if(customStyleSheet != null)
			ss.addStyleSheet(customStyleSheet);
		HTMLDocument doc = new HTMLDocument(ss);
		HtmlProcessor.configureUnknownTags(doc);
		doc.setParser(getParser());
		doc.setAsynchronousLoadPriority(Integer.MAX_VALUE);
		return doc;
	}
}
