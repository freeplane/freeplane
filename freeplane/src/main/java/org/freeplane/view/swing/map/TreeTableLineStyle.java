package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.UITools;

class TreeTableLineStyle {
	private final Color color;
	private final int paintedWidth;
	private final Dash dash;

	static TreeTableLineStyle ofBorder(final NodeView view) {
		final MainView mainView = view.getMainView();
		return new TreeTableLineStyle(mainView.getBorderColor(), mainView.getPaintedBorderWidth(), mainView.getDash());
	}

	TreeTableLineStyle(final Color color, final int paintedWidth, final Dash dash) {
		this.color = color;
		this.paintedWidth = paintedWidth;
		this.dash = dash;
	}

	Color getColor() {
		return color;
	}

	Stroke stroke() {
		return UITools.createStroke(paintedWidth, dash.pattern, BasicStroke.JOIN_MITER);
	}
}
