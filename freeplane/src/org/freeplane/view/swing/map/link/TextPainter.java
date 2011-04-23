/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
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
package org.freeplane.view.swing.map.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * @author Dimitry Polivaev
 * Apr 22, 2011
 */
public class TextPainter{
	private final int lineHeight;
	private final int textHeight;
	public  int getLineHeight() {
    	return lineHeight;
    }

	public int getTextHeight() {
    	return textHeight;
    }

	public int getTextWidth() {
    	return textWidth;
    }

	private final int textWidth;
	private final String[] lines;
	private final Graphics2D g;

	public TextPainter(final Graphics2D g, final String text){
		this.g = g;
		lines = text.split("\n");
		final FontMetrics fontMetrics = g.getFontMetrics();
		lineHeight = fontMetrics.getHeight();
		textHeight = lineHeight * lines.length + fontMetrics.getDescent();
		int textWidth = 0;
		for(final String line : lines){
			final int w = fontMetrics.stringWidth(line);
			textWidth = Math.max(textWidth, w);
		}
		this.textWidth = textWidth;
	}
	
	public void draw(int x, int y, Color textColor, Color bgColor){
		final Color oldColor = g.getColor();
		final Stroke oldStroke = g.getStroke();
		g.setColor(bgColor);
		g.setStroke(new BasicStroke(0.5f));
		g.fillRect(x, y, textWidth, textHeight);
		g.setColor(textColor);
		for(final String line : lines){
			y+=lineHeight;
			g.drawString(line, x, y);
		}
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}
}