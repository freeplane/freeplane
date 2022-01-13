package org.freeplane.core.resources.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;

final class ColorIcon implements Icon {
		
		private static final int COLOR_ICON_BORDER_SIZE = (int) (2 * UITools.FONT_SCALE_FACTOR);
		private Color color;
		private String text;
		private final Component c;
		
		int textWidth;
		int textHeight;

		public ColorIcon(Component c, Color color) {
			super();
			this.c = c;
			this.color = color;
		    if (color != null) {
		        this.text = ColorUtils.colorToString(color);
		    }
		    else {
		    	this.text = " ";
		    }

			this.textWidth = this.textHeight = 0;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
		    if (color != null) {
		    	calculateTextSize();
		        final Color backgroundColor = ColorUtils.makeNonTransparent(color);
		        g.setColor(backgroundColor);
		        g.fillRoundRect(x + COLOR_ICON_BORDER_SIZE / 2, 
		        		y + COLOR_ICON_BORDER_SIZE / 2, 
		        		getIconWidth() - COLOR_ICON_BORDER_SIZE, 
		        		getIconHeight() - COLOR_ICON_BORDER_SIZE, 5, 5);
		        final Color textColor = UITools.getTextColorForBackground(color);
		        g.setColor(textColor);
		        Graphics2D g2 = (Graphics2D) g;
				int xText = x + (getIconWidth() - textWidth) / 2;
				int yText = y + textHeight;
				g2.drawString(text, xText, yText);
		        
		    }
		}

		@Override
		public int getIconWidth() {
			return COLOR_ICON_BORDER_SIZE*100;
		}

		@Override
		public int getIconHeight() {
			calculateTextSize();
			return textHeight + COLOR_ICON_BORDER_SIZE*2;
		}

		private void calculateTextSize() {
			if(textWidth == 0) {
		        Graphics2D g = (Graphics2D) c.getGraphics();
		        if(g != null) {
		        	Rectangle2D textBounds = g.getFont().getStringBounds(text, g.getFontRenderContext());
		        	textWidth = (int) (textBounds.getWidth());
		        	textHeight = (int) (textBounds.getHeight());	
		        }
			}
		}
}