package org.freeplane.view.swing.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.UITools;

class TreeTableLineStyle {
	static final float MINIMAL_PAINTED_WIDTH = 1f;

	private final Color color;
	private final float unzoomedWidth;
	private final Dash dash;

	static TreeTableLineStyle ofBorder(final MainView mainView) {
		return new TreeTableLineStyle(mainView.getBorderColor(), mainView.getUnzoomedBorderWidth(), mainView.getDash());
	}

	TreeTableLineStyle(final Color color, final float unzoomedWidth, final Dash dash) {
		this.color = color;
		this.unzoomedWidth = unzoomedWidth;
		this.dash = dash;
	}

	Color getColor() {
		return color;
	}

	float paintedWidth(final float zoom) {
		return Math.max(unzoomedWidth * zoom, MINIMAL_PAINTED_WIDTH);
	}

	Stroke stroke(final float zoom) {
		return UITools.createStroke(paintedWidth(zoom), dash.pattern, BasicStroke.JOIN_MITER);
	}
}
