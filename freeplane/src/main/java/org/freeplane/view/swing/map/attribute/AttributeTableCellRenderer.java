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
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.factory.IconFactory;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.HighlightedTransformedObject;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.map.MapView;

class AttributeTableCellRenderer extends DefaultTableCellRenderer {
	public AttributeTableCellRenderer() {
		super();
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static final float ZOOM_CORRECTION_FACTOR = 0.97F;
	private boolean isPainting;
	private float zoom;
	private boolean opaque;

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
		final Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
		    column);
		final AttributeTable attributeTable = (AttributeTable) table;
		zoom = attributeTable.getZoom();
	    final AttributeTableModel attributeTableModel = (AttributeTableModel) table.getModel();
		final String originalText = value == null ? null : value.toString();
		String text = originalText;
		Icon icon;
		Color color = null;
		URI uri = null;
		if (column == 1 && isAttributeHighlighted(attributeTable, row))
			color = FilterController.HIGHLIGHT_COLOR;
		if (column == 1 && value != null) {
			try {
				// evaluate values only
				final TextController textController = TextController.getController();
				Object transformedObject = textController.getTransformedObject(value, attributeTableModel.getNode(), null);
				text = transformedObject.toString();
				if (color == null && transformedObject instanceof HighlightedTransformedObject && TextController.isMarkTransformedTextSet()) {
					color = HighlightedTransformedObject.OK_COLOR;
				}
			}
			catch (Exception e) {
				text = TextUtils.format("MainView.errorUpdateText", originalText, e.getLocalizedMessage());
				color = HighlightedTransformedObject.FAILURE_COLOR;
			}
			uri = attributeTable.toUri(value);
			if(uri != null){
	                icon = (attributeTable).getLinkIcon(uri);
			}
			else{
				icon = null;
			}
		}
		else{
			icon = null;
		}
		configureBorder(color);
		final Icon scaledIcon;
		final IconFactory iconFactory = IconFactory.getInstance();
		if(icon != null && iconFactory.canScaleIcon(icon)){
			final int fontSize = getFont().getSize();
			scaledIcon = iconFactory.getScaledIcon(icon, new Quantity<LengthUnit>(fontSize, LengthUnit.px));
		}
		else
			scaledIcon = icon;
		if(scaledIcon != getIcon()){
			setIcon(scaledIcon);
		}
		setText(text);
		String toolTip = null;
		if(uri != null) {
			final ViewerController viewerController = Controller.getCurrentModeController().getExtension(ViewerController.class);
			if (viewerController != null && viewerController.getViewerFactory().accept(uri)) {
				toolTip = uri.toString();
}
		}
		if(toolTip == null) {
			if (text != originalText) {
				toolTip = HtmlUtils.plainToHTML(originalText);
			}
			else {
				final int prefWidth = getPreferredSize().width;
				final int width = table.getColumnModel().getColumn(column).getWidth();
				if (prefWidth > width) {
					toolTip = HtmlUtils.plainToHTML(text);
				}
			}
		}
		setToolTipText(toolTip);
		setOpaque(isSelected);
		return rendererComponent;
	}

	private boolean isAttributeHighlighted(AttributeTable attributeTable, int row) {
		NodeAttributeTableModel attributes = attributeTable.getAttributeTableModel().getNodeAttributeModel();
		if(attributes.getRowCount() <= row)
			return false;
		Attribute attribute = attributes.getAttribute(row);
		return MapView.isElementHighlighted(attributeTable, attribute);
	}

	private void configureBorder(Color color) {
		if(color != null)
			setBorder(BorderFactory.createLineBorder(color));
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
	}

	@Override
	public boolean isOpaque() {
		return opaque;
	}

	@Override
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}


}
