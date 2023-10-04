package org.freeplane.features.note;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.html.StyleSheet;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeCss;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;

public class NoteStyleAccessor {
	final private String rule;
	final private Color noteForeground;
	final private NodeCss noteCss;
	final private Color noteBackground;
	final private HorizontalTextAlignment horizontalAlignment;
	public NoteStyleAccessor(ModeController modeController, NodeModel node, float zoom, boolean asHtmlFragment) {
		final Controller controller = modeController.getController();
		MapModel map = controller.getMap();
		if(map != null){
			final MapStyleModel model = MapStyleModel.getExtension(map);
			final NodeModel noteStyleNode = model.getStyleNodeSafe(MapStyleModel.NOTE_STYLE);
			final NodeStyleController style = Controller.getCurrentModeController().getExtension(
				NodeStyleController.class);
			final Font noteFont = style.getFont(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE);
			this.noteBackground = style.getBackgroundColor(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE);
			this.noteForeground = style.getColor(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE);
			this.noteCss = style.getStyleSheet(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE);
			this.horizontalAlignment = style.getHorizontalTextAlignment(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE);
			final CssRuleBuilder cssRuleBuilder = new CssRuleBuilder();
			if(asHtmlFragment)
				cssRuleBuilder.withHTMLFont(noteFont);
			else
				cssRuleBuilder.withCSSFont(noteFont);
			cssRuleBuilder.withColor(noteForeground)
			.withBackground((noteBackground != null ? noteBackground : //
				controller.getMapViewManager().getMapViewComponent().getBackground()))
			.withAlignment(horizontalAlignment.swingConstant);
			if(asHtmlFragment)
				cssRuleBuilder.withMaxWidthAsPt(zoom, NodeSizeModel.getMaxNodeWidth(noteStyleNode), style.getMaxWidth(node, StyleOption.FOR_UNSELECTED_NODE));
			this.rule = cssRuleBuilder.toString();
		}
		else {
			this.rule = "";
			this.noteForeground = null;
			this.noteBackground = null;
			this.noteCss = NodeCss.EMPTY;
			this.horizontalAlignment = HorizontalTextAlignment.DEFAULT;
		}

	}
	public String getNoteCSSStyle() {
		return rule;
	}

	public StyleSheet getNoteStyleSheet() {
		return noteCss.getStyleSheet();
	}

	public Color getNoteForeground() {
		return noteForeground;
	}
	public Color getNoteBackground() {
		return noteBackground;
	}
    public HorizontalTextAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }



}