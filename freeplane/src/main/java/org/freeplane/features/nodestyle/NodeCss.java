package org.freeplane.features.nodestyle;

import javax.swing.text.html.StyleSheet;

import org.freeplane.core.extension.IExtension;

public class NodeCss implements IExtension{
	public static final NodeCss EMPTY = new NodeCss("");
	public final String css;
	private StyleSheet styleSheet;

	public NodeCss(String css) {
		this.css = css;
		this.styleSheet = null;
    }

	public StyleSheet getStyleSheet() {
		if(styleSheet == null) {
			styleSheet = new StyleSheet();
			styleSheet.addRule(css);
		}
		return styleSheet;
	}

}