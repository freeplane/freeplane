/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.map.attribute;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.attribute.IAttributeTableModel;
import org.freeplane.features.common.text.TextController;

class AttributeTableCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final float ZOOM_CORRECTION_FACTOR = 0.97F;
	private static final boolean DONT_MARK_FORMULAS = Controller.getCurrentController().getResourceController()
	.getBooleanProperty("formula_dont_mark_formulas");
	private boolean isPainting;
	private float zoom;
	private Color borderColor;

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		if (isPainting) {
			if (zoom != 1F) {
				return (int) (super.getHeight() / zoom);
			}
		}
		return super.getHeight();
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	                                               final boolean hasFocus, final int row, final int column) {
		final Component rendererComponent = super.getTableCellRendererComponent(table, value, hasFocus, isSelected, row,
		    column);
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		}
		zoom = ((AttributeTable) table).getZoom();
	    final IAttributeTableModel attributeTableModel = (IAttributeTableModel) table.getModel();
		final String originalText = value.toString();
		String text = originalText;
		borderColor = null;
		if (column == 1) {
			try {
				// evaluate values only
				final TextController textController = TextController.getController();
				text = textController.getTransformedText(originalText, attributeTableModel.getNode());
				if (!DONT_MARK_FORMULAS && text != originalText) {
					borderColor = Color.GREEN;
				}
			}
			catch (Exception e) {
				text = TextUtils.format("MainView.errorUpdateText", originalText, e.getLocalizedMessage());
				borderColor = Color.RED;
			}
		}
		setText(text);
		final int prefWidth = getPreferredSize().width;
		final int width = table.getColumnModel().getColumn(column).getWidth();
		if (prefWidth > width) {
			final String toolTip = HtmlUtils.isHtmlNode(text) ? text : HtmlUtils.plainToHTML(text);
			setToolTipText(toolTip.replace("\n", "<br>"));
		}
		else {
			setToolTipText(null);
		}
		return rendererComponent;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		if (isPainting) {
			if (zoom != 1F) {
				return (int) (0.99f + super.getWidth() / zoom);
			}
		}
		return super.getWidth();
	}

	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		if (zoom != 1F) {
			zoom *= AttributeTableCellRenderer.ZOOM_CORRECTION_FACTOR;
			final AffineTransform transform = g2.getTransform();
			g2.scale(zoom, zoom);
			isPainting = true;
			super.paint(g);
			isPainting = false;
			g2.setTransform(transform);
		}
		else {
			super.paint(g);
		}
		if(borderColor != null){
			final Color color = g.getColor();
			g.setColor(borderColor);
			g.drawRect(0, 0, getWidth()-1, getHeight()-1);
			g.setColor(color);
		}
	}
}
