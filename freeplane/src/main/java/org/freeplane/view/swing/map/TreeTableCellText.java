package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.treetable.TreeTableNodes;

class TreeTableCellText {
	private static final int UNWRAPPED_WIDTH = Short.MAX_VALUE;
	private static final JLabel RENDERER = new JLabel();

	static String valueOf(final NodeModel row, final String attributeKey) {
		return TreeTableNodes.cellValueOf(row, attributeKey);
	}

	static Font zoomedFontOf(final NodeView view) {
		final Font font = view.getMainView().getFont();
		return font.deriveFont(font.getSize2D() * view.getMap().getZoom());
	}

	static int height(final String value, final Font font, final int wrapWidth) {
		final View view = htmlView(value, font, Color.BLACK);
		view.setSize(wrapWidth, 0);
		return (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
	}

	static void paint(final Graphics2D graphics, final String value, final Font font, final Color color,
			final Rectangle cell, final int textPadding, final int wrapWidth) {
		final View view = htmlView(value, font, color);
		view.setSize(wrapWidth > 0 ? wrapWidth : UNWRAPPED_WIDTH, 0);
		final int width = wrapWidth > 0 ? wrapWidth : (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
		final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
		final Rectangle textBounds = new Rectangle(cell.x + textPadding, cell.y + (cell.height - height) / 2, width,
			height);
		final Shape previousClip = graphics.getClip();
		graphics.clipRect(cell.x, cell.y, cell.width, cell.height);
		view.paint(graphics, textBounds);
		graphics.setClip(previousClip);
	}

	private static View htmlView(final String value, final Font font, final Color color) {
		RENDERER.setFont(font);
		RENDERER.setForeground(color);
		return BasicHTML.createHTMLView(RENDERER, HtmlUtils.isHtml(value) ? value : HtmlUtils.plainToHTML(value));
	}

	private TreeTableCellText() {
	}
}
