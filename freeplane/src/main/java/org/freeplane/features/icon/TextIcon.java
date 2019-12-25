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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Dec 25, 2019
 */
public class TextIcon implements NamedIcon{
	
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(),
	  true, true);

	private final String text;
	private final Font font;
	private final Icon icon;

	public TextIcon(String text, Font font) {
		super();
		this.text = text;
		this.font = font;
		Rectangle2D stringBounds = font.getStringBounds(text, 0, text.length(), FONT_RENDER_CONTEXT);
		int width = (int) Math.ceil(stringBounds.getWidth()); 
		int heigth = (int) Math.ceil(stringBounds.getHeight()); 
		icon = new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Font oldFont = g.getFont();
				g.setFont(font);
				g.drawString(text, 0, getIconHeight());
				g.setFont(oldFont);
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
	}

	@Override
	public String getName() {
		return "text-" + text;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public Icon getIcon(NodeModel node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamedIcon zoom(float zoom) {
		// TODO Auto-generated method stub
		return null;
	}
}
