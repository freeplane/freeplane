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
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Quantity;

/**
 * @author Dimitry Polivaev
 * Dec 25, 2019
 */
public class CharIcon implements NamedIcon {
	
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(),
	  true, true);

	private final Icon icon;

	private final int codepoint;

	private final String fontFamily;

	private final Color color;

	public CharIcon(int codepoint, Font font, Color color) {
		this(codepoint, font, color, createIcon(codepoint, font, color));
	}

	private CharIcon(int codepoint, Font font, Color color, Icon icon) {
		super();
		this.icon = icon;
		this.codepoint = codepoint;
		this.color = color;
		fontFamily = font.getFamily();
	}

	private static Icon createIcon(int codepoint, Font font, Color color) {
		char[] chars = Character.toChars(codepoint);
		Rectangle2D stringBounds = font.getStringBounds(chars, 0, chars.length, FONT_RENDER_CONTEXT);
		int width = (int) Math.ceil(stringBounds.getWidth()); 
		int heigth = (int) Math.ceil(stringBounds.getHeight()); 
		Icon icon = new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Font oldFont = g.getFont();
				Color oldColor = g.getColor();
				g.setFont(font);
				g.setColor(color);
				g.drawChars(chars, 0, chars.length, 0, getIconHeight());
				g.setFont(oldFont);
				g.setColor(oldColor);
			}
			
			@Override
			public int getIconWidth() {
				return width;
			}
			
			@Override
			public int getIconHeight() {
				return heigth;
			}
		};
		return icon;
	}

	@Override
	public String getName() {
		return "unicode-" + codepoint + "-font-" + fontFamily + "-color-"+ColorUtils.colorToRGBAString(color);
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamedIcon zoom(float zoom) {
		return new CharIcon(codepoint, null, color, zoomedIcon(zoom));
	}

	private Icon zoomedIcon(float zoom) {
		return new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public int getIconWidth() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getIconHeight() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	@Override
	public Icon getIcon(Quantity<LengthUnits> iconHeight) {
		// TODO Auto-generated method stub
		return null;
	}
}
