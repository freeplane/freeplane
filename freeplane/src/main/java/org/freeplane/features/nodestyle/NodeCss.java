package org.freeplane.features.nodestyle;

import javax.swing.text.html.StyleSheet;

import org.freeplane.core.extension.IExtension;

public class NodeCss implements IExtension{
	public static final NodeCss EMPTY = new NodeCss("");
	public final String css;
	public final StyleSheet styleSheet;

	public NodeCss(String css) {
		this.css = css;
		this.styleSheet = new StyleSheet();
		styleSheet.addRule(css);
    }

}