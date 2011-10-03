package org.freeplane.view.swing.map;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;

/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
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
/**
 * @author Dimitry Polivaev
 * 23.08.2009
 */
public class ZoomableLabelUI extends BasicLabelUI {
	private boolean isPainting = false;

	static ZoomableLabelUI labelUI = new ZoomableLabelUI();
	private Rectangle iconR = new Rectangle();
	private Rectangle textR = new Rectangle();
	private Rectangle viewR = new Rectangle();

	@Override
	public Dimension getPreferredSize(final JComponent c) {
		final Dimension preferredSize = super.getPreferredSize(c);
		if (preferredSize.height == 0) {
			preferredSize.height = ((ZoomableLabel) c).getFontMetrics().getHeight();
		}
		if (preferredSize.width <= 4) {
			preferredSize.width = 4;
		}
		final float zoom = ((ZoomableLabel) c).getZoom();
		if (zoom != 1f) {
			preferredSize.width = (int) (Math.ceil(zoom * preferredSize.width));
			preferredSize.height = (int) (Math.ceil(zoom * preferredSize.height));
		}
		return preferredSize;
	}

	public static ComponentUI createUI(final JComponent c) {
		return labelUI;
	}

	@Override
	protected String layoutCL(final JLabel label, final FontMetrics fontMetrics, final String text, final Icon icon,
	                          final Rectangle viewR, final Rectangle iconR, final Rectangle textR) {
		final ZoomableLabel zLabel = (ZoomableLabel) label;
		if (isPainting) {
			final Insets insets = zLabel.getInsets();
			final int width = zLabel.getWidth();
			final int height = zLabel.getHeight();
			final float zoom = zLabel.getZoom();
			viewR.x = insets.left;
			viewR.y = insets.top;
			viewR.width = (int)((width - (insets.left + insets.right)) / zoom);
			viewR.height = (int)((height - (insets.top + insets.bottom)) / zoom);
		}
		super.layoutCL(zLabel, zLabel.getFontMetrics(), text, icon, viewR, iconR, textR);
		return text;
	}

	@Override
	public void paint(final Graphics g, final JComponent label) {
		final ZoomableLabel mainView = (ZoomableLabel) label;
		if (!mainView.useFractionalMetrics()) {
			super.paint(g, label);
			return;
		}
		final Graphics2D g2 = (Graphics2D) g;
		final Object oldRenderingHintFM = g2.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
		final Object newRenderingHintFM = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		if (oldRenderingHintFM != newRenderingHintFM) {
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, newRenderingHintFM);
		}
		final AffineTransform transform = g2.getTransform();
		final float zoom = mainView.getZoom() * 0.97f;
		g2.scale(zoom, zoom);
		final boolean htmlViewSet = null != label.getClientProperty(BasicHTML.propertyKey);
		try {
			isPainting = true;
			if(htmlViewSet){
				GlyphPainterMetricResetter.resetPainter();
			}
			super.paint(g, label);
		}
		finally {
			isPainting = false;
			if(htmlViewSet){
				GlyphPainterMetricResetter.resetPainter();
			}
		}
		g2.setTransform(transform);
		if (oldRenderingHintFM != newRenderingHintFM) {
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, oldRenderingHintFM != null ? oldRenderingHintFM
			        : RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		}
	}

	@Override
    public void propertyChange(PropertyChangeEvent e) {
		GlyphPainterMetricResetter.resetPainter();
	    try {
	        super.propertyChange(e);
        }
        catch (Exception e1) {
	        e1.printStackTrace();
        }
		GlyphPainterMetricResetter.resetPainter();
    }
	
	public Rectangle getIconR(ZoomableLabel label) {
		layout(label);
    	return iconR;
    }

	public Rectangle getTextR(ZoomableLabel label) {
		layout(label);
    	return textR;
    }

	private void layout(ZoomableLabel label) {
		String text = label.getText();
		if(text == null || text.equals(""))
			text = "!";
		Icon icon = (label.isEnabled()) ? label.getIcon() :
			label.getDisabledIcon();
		boolean wasPainting = isPainting;
		try{
			isPainting = true;
			iconR.x = iconR.y = iconR.width = iconR.height = 0;
			textR.x = textR.y = textR.width = textR.height = 0;
			layoutCL(label, label.getFontMetrics(), text, icon, viewR, iconR,textR);
			final float zoom = label.getZoom();
			iconR.x = (int)(iconR.x * zoom); 
			iconR.y = (int)(iconR.y * zoom); 
			iconR.width = (int)(iconR.width * zoom); 
			iconR.height = (int)(iconR.height * zoom); 
			textR.x = (int)(textR.x * zoom); 
			textR.y = (int)(textR.y * zoom); 
			textR.width = (int)(textR.width * zoom); 
			textR.height = (int)(textR.height * zoom); 
		}
		finally{
			isPainting = wasPainting;
		}
	}
}
