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
import javax.swing.SwingConstants;
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
	private LayoutData layoutData = new LayoutData(iconR, textR);
	
	public static class LayoutData{
		final public Rectangle iconR;
		final public Rectangle textR;
		public LayoutData(Rectangle iconR, Rectangle textR) {
			super();
			this.iconR = iconR;
			this.textR = textR;
		}
		
	}



	@Override
	public Dimension getPreferredSize(final JComponent c) {
		final Dimension preferredSize = super.getPreferredSize(c);
		final ZoomableLabel zoomableLabel = (ZoomableLabel) c;
		if(zoomableLabel.getIcon() == null){
			final int fontHeight = zoomableLabel.getFontMetrics().getHeight();
			final Insets insets = c.getInsets();
			preferredSize.width = Math.max(preferredSize.width, fontHeight/2  + insets.left + insets.right);
			preferredSize.height = Math.max(preferredSize.height, fontHeight + insets.top + insets.bottom);
		}
		final float zoom = zoomableLabel.getZoom();
		if (zoom != 1f) {
			preferredSize.width = (int) (Math.ceil(zoom * preferredSize.width));
			preferredSize.height = (int) (Math.ceil(zoom * preferredSize.height));
		}
		int minimumWidth = zoomableLabel.getMinimumWidth();
		if(minimumWidth != 0)
		preferredSize.width = Math.max(minimumWidth, preferredSize.width);
		return preferredSize;
	}

	public static ComponentUI createUI(final JComponent c) {
		return labelUI;
	}

	@Override
	protected String layoutCL(final JLabel label, final FontMetrics fontMetrics, final String text, final Icon icon,
	                          final Rectangle viewR, final Rectangle iconR, final Rectangle textR) {
		final ZoomableLabel zLabel = (ZoomableLabel) label;
		final float zoom = zLabel.getZoom();
		if (isPainting) {
			final Insets insets = zLabel.getInsets();
			final int width = zLabel.getWidth();
			final int height = zLabel.getHeight();
			viewR.x = insets.left;
			viewR.y = insets.top;
			viewR.width = (int) (width  / zoom) - (insets.left + insets.right);
			viewR.height = (int)(height / zoom) - (insets.top + insets.bottom);
			if(viewR.width < 0)
				viewR.width = 0;
		} 
		else {
			if(zLabel.getMaximumWidth() != Integer.MAX_VALUE){
				final int maximumWidth = (int) (zLabel.getMaximumWidth() / zoom);
				final Insets insets = label.getInsets();
				viewR.width = maximumWidth - insets.left - insets.right;
				if(viewR.width < 0)
					viewR.width = 0;
				ScaledHTML.Renderer v = (ScaledHTML.Renderer) label.getClientProperty(BasicHTML.propertyKey);
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
		}
		Icon textRenderingIcon = getTextRenderingIcon(zLabel);
		if(textRenderingIcon != null){
			layoutLabelWithTextIcon(textRenderingIcon, icon, viewR, iconR, textR, zLabel);
		}
		else
			super.layoutCL(zLabel, zLabel.getFontMetrics(), text, icon, viewR, iconR, textR);
		return text;
	}

	static private void layoutLabelWithTextIcon(final Icon textRenderingIcon, final Icon icon,
			final Rectangle viewR, final Rectangle iconR,
			final Rectangle textR, final ZoomableLabel zLabel) {
		JComponent c = (JComponent) zLabel;
		int horizontalAlignment = zLabel.getHorizontalAlignment();
		int horizontalTextPosition = zLabel.getHorizontalTextPosition();
		boolean orientationIsLeftToRight = true;
		int     hAlign = horizontalAlignment;
		int     hTextPos = horizontalTextPosition;
		
		if (c != null) {
		    if (!(c.getComponentOrientation().isLeftToRight())) {
		        orientationIsLeftToRight = false;
		    }
		}
		
		// Translate LEADING/TRAILING values in horizontalAlignment
		// to LEFT/RIGHT values depending on the components orientation
		switch (horizontalAlignment) {
		case SwingUtilities.LEADING: 
		    hAlign = (orientationIsLeftToRight) ? SwingUtilities.LEFT : SwingUtilities.RIGHT;
		    break;
		case SwingUtilities.TRAILING: 
		    hAlign = (orientationIsLeftToRight) ? SwingUtilities.RIGHT : SwingUtilities.LEFT;
		    break;
		}
		
		// Translate LEADING/TRAILING values in horizontalTextPosition
		// to LEFT/RIGHT values depending on the components orientation
		switch (horizontalTextPosition) {
		case SwingUtilities.LEADING: 
		    hTextPos = (orientationIsLeftToRight) ? SwingUtilities.LEFT : SwingUtilities.RIGHT;
		    break;
		case SwingUtilities.TRAILING: 
		    hTextPos = (orientationIsLeftToRight) ? SwingUtilities.RIGHT : SwingUtilities.LEFT;
		    break;
		}
		int verticalAlignment = zLabel.getVerticalAlignment();
		int verticalTextPosition = zLabel.getVerticalTextPosition();
		if (icon != null) {
		        iconR.width = icon.getIconWidth();
		        iconR.height = icon.getIconHeight();
		    }
		    else {
		        iconR.width = iconR.height = 0;
		    }
		
		    /* Initialize the text bounds rectangle textR.  If a null
		     * or and empty String was specified we substitute "" here
		     * and use 0,0,0,0 for textR.
		     */
		
		    int lsb = 0;
		    int rsb = 0;
		    /* Unless both text and icon are non-null, we effectively ignore
		     * the value of textIconGap.
		     */
		    int gap;
		
		        int availTextWidth;
		        gap = (icon == null) ? 0 : zLabel.getIconTextGap();
		
		        if (hTextPos == SwingUtilities.CENTER) {
		            availTextWidth = viewR.width;
		        }
		        else {
		            availTextWidth = viewR.width - (iconR.width + gap);
		        }
			textR.width = Math.min(availTextWidth, textRenderingIcon.getIconWidth());
			textR.height = textRenderingIcon.getIconHeight();
		
		
		    /* Compute textR.x,y given the verticalTextPosition and
		     * horizontalTextPosition properties
		     */
		
		    if (verticalTextPosition == SwingUtilities.TOP) {
		        if (hTextPos != SwingUtilities.CENTER) {
		            textR.y = 0;
		        }
		        else {
		            textR.y = -(textR.height + gap);
		        }
		    }
		    else if (verticalTextPosition == SwingUtilities.CENTER) {
		        textR.y = (iconR.height / 2) - (textR.height / 2);
		    }
		    else { // (verticalTextPosition == BOTTOM)
		        if (hTextPos != SwingUtilities.CENTER) {
		            textR.y = iconR.height - textR.height;
		        }
		        else {
		            textR.y = (iconR.height + gap);
		        }
		    }
		
		    if (hTextPos == SwingUtilities.LEFT) {
		        textR.x = -(textR.width + gap);
		    }
		    else if (hTextPos == SwingUtilities.CENTER) {
		        textR.x = (iconR.width / 2) - (textR.width / 2);
		    }
		    else { // (horizontalTextPosition == RIGHT)
		        textR.x = (iconR.width + gap);
		    }
		
		    /* labelR is the rectangle that contains iconR and textR.
		     * Move it to its proper position given the labelAlignment
		     * properties.
		     *
		     * To avoid actually allocating a Rectangle, Rectangle.union
		     * has been inlined below.
		     */
		    int labelR_x = Math.min(iconR.x, textR.x);
		    int labelR_width = Math.max(iconR.x + iconR.width,
		                                textR.x + textR.width) - labelR_x;
		    int labelR_y = Math.min(iconR.y, textR.y);
		    int labelR_height = Math.max(iconR.y + iconR.height,
		                                 textR.y + textR.height) - labelR_y;
		
		    int dx, dy;
		
		    if (verticalAlignment == SwingUtilities.TOP) {
		        dy = viewR.y - labelR_y;
		    }
		    else if (verticalAlignment == SwingUtilities.CENTER) {
		        dy = (viewR.y + (viewR.height / 2)) - (labelR_y + (labelR_height / 2));
		    }
		    else { // (verticalAlignment == BOTTOM)
		        dy = (viewR.y + viewR.height) - (labelR_y + labelR_height);
		    }
		
		    if (hAlign == SwingUtilities.LEFT) {
		        dx = viewR.x - labelR_x;
		    }
		    else if (hAlign == SwingUtilities.RIGHT) {
		        dx = (viewR.x + viewR.width) - (labelR_x + labelR_width);
		    }
		    else { // (horizontalAlignment == CENTER)
		        dx = (viewR.x + (viewR.width / 2)) -
		             (labelR_x + (labelR_width / 2));
		    }
		
		    /* Translate textR and glypyR by dx,dy.
		     */
		
		    textR.x += dx;
		    textR.y += dy;
		
		    iconR.x += dx;
		    iconR.y += dy;
		
		    if (lsb < 0) {
		        // lsb is negative. Shift the x location so that the text is
		        // visually drawn at the right location.
		        textR.x -= lsb;
		        
		        textR.width += lsb;
		    }
		    if (rsb > 0) {
		        textR.width -= rsb;
		    }
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
		final float factor = 0.97f;
		final float zoom = mainView.getZoom() * factor;
		if(mainView.getVerticalAlignment() == SwingConstants.CENTER) {
			final float translationFactorY = 0.5f;
			g2.translate(0, mainView.getHeight() * (1f - factor) * translationFactorY);
		}
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
	private void superPaintSafe(final Graphics g, final ZoomableLabel label) {
		try {
			Icon textRenderingIcon = getTextRenderingIcon(label);
			if(textRenderingIcon  != null)
				paintIcons(g, label, textRenderingIcon);
			else
				super.paint(g, label);
		} catch (ClassCastException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					label.setText(TextUtils.format("html_problem", label.getText()));
				}
			});
		}
	}

	private void paintIcons(Graphics g, ZoomableLabel label, Icon textRenderingIcon) {
        Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();
        Rectangle paintViewR = new Rectangle();
        Rectangle paintIconR = new Rectangle();
		Rectangle paintTextR = new Rectangle();
		layoutCL(label, null, null, icon, paintViewR, paintIconR, paintTextR);
        if (icon != null) {
            icon.paintIcon(label, g, paintIconR.x, paintIconR.y);
        }
        textRenderingIcon.paintIcon(label, g, paintTextR.x, paintTextR.y);
	}

	@Override
    public void propertyChange(PropertyChangeEvent e) {
	    	String name = e.getPropertyName();
	    	if (name == "text" || "font" == name || "foreground" == name 
	    			|| "ancestor" == name || "graphicsConfiguration" == name) {
	    		JLabel lbl = ((JLabel) e.getSource());
	    		if(getTextRenderingIcon(lbl) !=  null){
	    			ScaledHTML.updateRenderer(lbl, "");
	    		}
	    		else{
	    			String text = lbl.getText();
	    			GlyphPainterMetricResetter.resetPainter();
	    			try {
	    			ScaledHTML.updateRenderer(lbl, text);
	    			}
	    			finally{
	    				GlyphPainterMetricResetter.resetPainter();
	    			}
	    			View v = (View) lbl.getClientProperty(BasicHTML.propertyKey);
	    			if (v != null) {
	    				lbl.putClientProperty("preferredWidth", v.getPreferredSpan(View.X_AXIS));
	    			}
	    		}
	    	}
	    	else
		        super.propertyChange(e);

    }

	private Icon getTextRenderingIcon(JLabel lbl) {
		return (Icon) lbl.getClientProperty(ZoomableLabel.TEXT_RENDERING_ICON);
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
	
	public LayoutData getLayoutData(ZoomableLabel label) {
		layout(label);
    	return layoutData;
    }
	
	private void layout(ZoomableLabel label) {
		String text = label.getText();
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
