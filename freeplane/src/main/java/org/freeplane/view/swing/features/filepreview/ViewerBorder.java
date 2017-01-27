package org.freeplane.view.swing.features.filepreview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial") class ViewerBorder extends EmptyBorder {
	private final int borderWidth;
	private final Color borderColor;
	ViewerBorder(int width, Color borderColor) {
		super(0, 0, 0, 0);
		this.borderWidth = width;
		this.borderColor = borderColor;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if (c.getCursor().getType() == Cursor.SE_RESIZE_CURSOR) {
			Color oldColor = g.getColor();
			g.translate(x, y);
			g.setColor(borderColor);
			g.fillRect(0, 0, width - borderWidth, borderWidth);
			g.fillRect(0, borderWidth, borderWidth, height - borderWidth);
			g.fillRect(borderWidth, height - borderWidth, width - borderWidth, borderWidth);
			g.fillRect(width - borderWidth, 0, borderWidth, height - borderWidth);

			g.translate(-x, -y);
			g.setColor(oldColor);
		}
	}

	public static void repaintBorder(JComponent component) {
		final int borderWidth = ((ViewerBorder)component.getBorder()).borderWidth;
		component.paintImmediately(0, 0, component.getWidth(), borderWidth);
		component.paintImmediately(0, component.getHeight() - borderWidth, component.getWidth(), borderWidth);
		component.paintImmediately(0, 0, borderWidth, component.getHeight());
		component.paintImmediately(component.getWidth() - borderWidth, 0, borderWidth, component.getHeight());
	}
	
}