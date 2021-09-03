package org.freeplane.features.note;

import java.awt.Color;
import java.awt.Font;

import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

public class NoteStyleAccessor {
	final private String rule;
	final private Color noteForeground;
	private Color noteBackground;
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
			final int alignment = style.getHorizontalTextAlignment(noteStyleNode, StyleOption.FOR_UNSELECTED_NODE).swingConstant;
			final CssRuleBuilder cssRuleBuilder = new CssRuleBuilder();
			if(asHtmlFragment)
				cssRuleBuilder.withHTMLFont(noteFont);
			else
				cssRuleBuilder.withCSSFont(noteFont);
			cssRuleBuilder.withColor(noteForeground)
			.withBackground((noteBackground != null ? noteBackground : //
				controller.getMapViewManager().getMapViewComponent().getBackground()))
			.withAlignment(alignment);
			if(asHtmlFragment)
				cssRuleBuilder.withMaxWidthAsPt(zoom, NodeSizeModel.getMaxNodeWidth(noteStyleNode), style.getMaxWidth(node, StyleOption.FOR_UNSELECTED_NODE));
			this.rule = cssRuleBuilder.toString();
		}
		else {
			this.rule = "";
			this.noteForeground = null;
			this.noteBackground = null;
		}

	}
	public String getNoteCSSStyle() {
		return rule;
	}
	public Color getNoteForeground() {
		return noteForeground;
	}
	public Color getNoteBackground() {
		return noteBackground;
	}


}