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
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.text.View;

import org.freeplane.core.ui.components.html.ScaledHTML;
import org.freeplane.core.util.TextUtils;

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

	private int maximumWidth = Integer.MAX_VALUE;


	public Dimension getPreferredSize(final ZoomableLabel c, int maximumWidth) {
		try{
			this.maximumWidth = maximumWidth;
			final Dimension preferredSize = getPreferredSize(c);
			return preferredSize;
		}
		finally{
			this.maximumWidth = Integer.MAX_VALUE;
		}
		
	}
	
	@Override
	public Dimension getPreferredSize(final JComponent c) {
		final Dimension preferredSize = super.getPreferredSize(c);
		final int fontHeight = ((ZoomableLabel) c).getFontMetrics().getHeight();
		final Insets insets = c.getInsets();
		preferredSize.width = Math.max(preferredSize.width, fontHeight/2  + insets.left + insets.right);
		preferredSize.height = Math.max(preferredSize.height, fontHeight + insets.top + insets.bottom);
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
		ScaledHTML.Renderer v = null;
		if (isPainting) {
			final Insets insets = zLabel.getInsets();
			final int width = zLabel.getWidth();
			final int height = zLabel.getHeight();
			final float zoom = zLabel.getZoom();
			viewR.x = insets.left;
			viewR.y = insets.top;
			viewR.width = (int) (width  / zoom) - (insets.left + insets.right);
			viewR.height = (int)(height / zoom) - (insets.top + insets.bottom);
			if(viewR.width < 0)
				viewR.width = 0;
			v = (ScaledHTML.Renderer) label.getClientProperty(BasicHTML.propertyKey);
		    if (v != null) {
		    	float preferredWidth = v.getPreferredSpan(View.X_AXIS);
		    	int textWidth = viewR.width;
				if(icon != null)
		    		textWidth -= icon.getIconWidth() + label.getIconTextGap();
				if(preferredWidth < textWidth){
					v.setSize(textWidth, 1);
					super.layoutCL(zLabel, zLabel.getFontMetrics(), text, icon, viewR, iconR, textR);
					v.setSize(textR.width, textR.height);
					return text;
				}
		    }
		}
		else if(maximumWidth != Integer.MAX_VALUE){
			final Insets insets = label.getInsets();
			viewR.width = maximumWidth - insets.left - insets.right;
			if(viewR.width < 0)
				viewR.width = 0;
			v = (ScaledHTML.Renderer) label.getClientProperty(BasicHTML.propertyKey);
		    if (v != null) {
		    	v.resetSize();
		    	float preferredWidth = v.getPreferredSpan(View.X_AXIS);
		    	float minimumWidth = v.getMinimumSpan(View.X_AXIS);
		    	int textWidth = viewR.width;
				if(icon != null)
		    		textWidth -= icon.getIconWidth() + label.getIconTextGap();
				if(preferredWidth > textWidth){
					if(minimumWidth > textWidth){
						viewR.width += minimumWidth - textWidth;
						textWidth = (int) minimumWidth;
					}
					v.setSize(textWidth, 1);
					super.layoutCL(zLabel, zLabel.getFontMetrics(), text, icon, viewR, iconR, textR);
					v.setSize(textR.width, textR.height);
					return text;
				}
		    }
		}
		super.layoutCL(zLabel, zLabel.getFontMetrics(), text, icon, viewR, iconR, textR);
		return text;
	}

	@Override
	public void paint(final Graphics g, final JComponent label) {
		final ZoomableLabel mainView = (ZoomableLabel) label;
		if (!mainView.useFractionalMetrics()) {
			try {
				isPainting = true;
				superPaintSafe(g, mainView);
			}
			finally {
				isPainting = false;
			}
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
			superPaintSafe(g, mainView);
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

	// Workaround for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7126361
	private void superPaintSafe(final Graphics g, final JLabel label) {
		try {
			super.paint(g, label);
		} catch (ClassCastException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					label.setText(TextUtils.format("html_problem", label.getText()));
				}
			});
		}
	}

	@Override
    public void propertyChange(PropertyChangeEvent e) {
		GlyphPainterMetricResetter.resetPainter();
	    try {
	    	String name = e.getPropertyName();
	    	if (name == "text" || "font" == name || "foreground" == name) {
	    	    JLabel lbl = ((JLabel) e.getSource());
			    String text = lbl.getText();
			    ScaledHTML.updateRenderer(lbl, text);
	    	    View v = (View) lbl.getClientProperty(BasicHTML.propertyKey);
			    if (v != null) {
			    	lbl.putClientProperty("preferredWidth", v.getPreferredSpan(View.X_AXIS));
			    }
	    	}
	    	else
		        super.propertyChange(e);

        }
	    finally{
	    	GlyphPainterMetricResetter.resetPainter();
	    }
    }
	
	@Override
    protected void installComponents(JLabel c) {
	    ScaledHTML.updateRenderer(c, c.getText());
        c.setInheritsPopupMenu(true);
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
