package org.freeplane.core.resources.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;

import com.formdev.flatlaf.util.HiDPIUtils;

class JColorButton extends JButton {

	private static final long serialVersionUID = 1L;
	private static final int COLOR_ICON_BORDER_SIZE = (int) (4 * UITools.FONT_SCALE_FACTOR);
	private Color color;
	private String text;
	int textWidth;
	int textHeight;
	public JColorButton() {
		super(" ");
		this.color = null;
		this.text = " ";
	}
	void setColor(Color color) {
		this.color = color;
	    if (color != null) {
	        this.text = ColorUtils.colorToString(color);
	    }
	    else {
	    	this.text = " ";
	    }

		this.textWidth = this.textHeight = 0;
		repaint();
	}
	
	
	
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if(isPreferredSizeSet())
			return preferredSize;
		Dimension newSize = new Dimension(preferredSize);
		newSize.width  += COLOR_ICON_BORDER_SIZE * 2;
		newSize.height += COLOR_ICON_BORDER_SIZE * 2;
		return newSize;
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintColor(g);
	}
	private void paintColor(Graphics g) {
	    if (color != null) {
	    	calculateTextSize();
	        final Color backgroundColor = ColorUtils.makeNonTransparent(color);
	        g.setColor(backgroundColor);
	        g.setFont(getFont());
	        g.fillRect(COLOR_ICON_BORDER_SIZE , 
	        		COLOR_ICON_BORDER_SIZE, 
	        		getWidth() - COLOR_ICON_BORDER_SIZE*2, 
	        		getHeight() - COLOR_ICON_BORDER_SIZE*2);
	        Graphics2D g2 = (Graphics2D) g;
	        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int xText = (getWidth() - textWidth) / 2;
			int yText = (getHeight() + textHeight - COLOR_ICON_BORDER_SIZE) / 2;
	        /* Draw the Text */
	        if(! getModel().isEnabled()) {
	            g.setColor(color.brighter());
	            HiDPIUtils.drawStringUnderlineCharAtWithYCorrection( this, g2, text, -1, xText, yText);
	            g.setColor(color.darker());
	            HiDPIUtils.drawStringUnderlineCharAtWithYCorrection( this, g2, text, -1, xText - 1, yText - 1);
	        } else {
	            /*** paint the text normally */
		        final Color textColor = UITools.getTextColorForBackground(color);
		        g.setColor(textColor);
		        HiDPIUtils.drawStringUnderlineCharAtWithYCorrection( this, g2, text, -1, xText, yText);
	        }

	        
	    }
	}
	private void calculateTextSize() {
		if(textWidth == 0) {
	        Graphics2D g = (Graphics2D) getGraphics();
	        if(g != null) {
	        	Rectangle2D textBounds = g.getFont().getStringBounds(text, g.getFontRenderContext());
	        	textWidth = (int) (textBounds.getWidth());
	        	textHeight = (int) (textBounds.getHeight());	
	        }
		}
	}
}