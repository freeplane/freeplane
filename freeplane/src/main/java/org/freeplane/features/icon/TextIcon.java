/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2019 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;

/**
 * @author Dimitry Polivaev
 * Dec 25, 2019
 */
public class TextIcon implements NamedIcon {
	
    private static final class ZoomableIcon implements Icon {
		final Font font;
		final Color color;
		final String text;
		final int unscaledHeigth;
		final int heigth;
		final int width;

		private ZoomableIcon(Font font, Color color, String text, float width, float heigth, int unscaledHeigth) {
			this(font, color, text, (int)Math.ceil(width), (int) Math.ceil(heigth), unscaledHeigth);
		}
		private ZoomableIcon(Font font, Color color, String text, int width, int heigth, int unscaledHeigth) {
			this.font = font;
			this.color = color;
			this.text = text;
			this.heigth = heigth;
			this.width = width;
			this.unscaledHeigth = unscaledHeigth;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			Font oldFont = g2.getFont();
			if(! oldFont.equals(font))
				g2.setFont(font);
			Color oldColor = g2.getColor();
			if(! oldColor.equals(color))
				g2.setColor(color);
			Object oldFractionalMatricsValue = g2.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
			if(! oldFractionalMatricsValue.equals(RenderingHints.KEY_FRACTIONALMETRICS))
				g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			boolean needsScaling = heigth != unscaledHeigth;
			if (needsScaling) {
				AffineTransform oldTransform = g2.getTransform();
				g2.translate(x, y);
				double zoom = ((double)heigth)/unscaledHeigth;
				g2.scale(zoom, zoom);
				FontMetrics fontMetrics = g2.getFontMetrics();
				g2.drawString(text, fontMetrics.getLeading(), fontMetrics.getAscent());
				g2.setTransform(oldTransform);
			}
			else {
				FontMetrics fontMetrics = g2.getFontMetrics();
				g2.drawString(text, x + fontMetrics.getLeading(), y + fontMetrics.getAscent());
			}
			g2.drawRect(x, y, width, heigth);
			if(! oldFont.equals(font))
				g2.setFont(oldFont);
			if(! oldColor.equals(color))
				g2.setColor(oldColor);
			if(! oldFractionalMatricsValue.equals(RenderingHints.VALUE_FRACTIONALMETRICS_ON))
				g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, oldFractionalMatricsValue);
		}
		
		ZoomableIcon zoom(float newZoomFactor) {
			return new ZoomableIcon(font, color, text, width * newZoomFactor, heigth * newZoomFactor, unscaledHeigth);
		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public int getIconHeight() {
			return heigth;
		}
		
		ZoomableIcon withHeight(Quantity<LengthUnits> iconHeight) {
			int requiredHeight = iconHeight.toBaseUnitsRounded();
			if(heigth == requiredHeight)
				return this;
			return zoom(((float)requiredHeight)/heigth);
		}
	}

	private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(),
	  true, true);

	private final ZoomableIcon icon;

	private final String text;

	private final Color color;

	private final Font font;
	
	public TextIcon(String text, Font font, Color color) {
		this(text, font, color, createIcon(text, font, color));
	}

	private TextIcon(String text, Font font, Color color, ZoomableIcon icon) {
		super();
		this.font = font;
		this.icon = icon;
		this.text = text;
		this.color = color;
	}

	private static ZoomableIcon createIcon(String text, Font font, Color color) {
		Rectangle stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT).getBounds();
		int width = stringBounds.width; 
		int heigth = stringBounds.height; 
		ZoomableIcon icon = new ZoomableIcon(font, color, text, width, heigth, heigth);
		return icon;
	}

	@Override
	public String getName() {
		return "text-" + text + "-font-" + font.getFamily();
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public NamedIcon zoom(float zoom) {
		return new TextIcon(text, font, color, zoomedIcon(zoom));
	}

	private ZoomableIcon zoomedIcon(float zoom) {
		return icon.zoom(zoom);
	}

	@Override
	public Icon getIcon(Quantity<LengthUnits> iconHeight) {
		return icon.withHeight(iconHeight);
	}
}
