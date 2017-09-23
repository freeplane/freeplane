/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Freeplane team and others
 *
 *  this file is created by Dimitry Polivaev in 2012.
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
package org.freeplane.core.ui.components.html;

import java.awt.Font;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.ui.components.UITools;


public class ScaledStyleSheet extends StyleSheet{
	
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
	public Font getFont(AttributeSet a) {
	    final Font font = super.getFont(a);
	    final float fontScaleFactor = getFontScaleFactor(a);
	    return super.getFont(font.getFamily(), font.getStyle(), Math.round(font.getSize2D() * fontScaleFactor));
    }

	private float getFontScaleFactor(AttributeSet a) {
		if(a == null)
			return UITools.FONT_SCALE_FACTOR;
		final Object attribute = a.getAttribute(CSS.Attribute.FONT_SIZE);
		if(attribute == null)
			return UITools.FONT_SCALE_FACTOR;
		final String fontSize = attribute.toString();
		final int fsLength = fontSize.length();
		if(fsLength <= 1 
				|| Character.isDigit(fontSize.charAt(fsLength-1))
				|| fontSize.endsWith("pt"))
			return UITools.FONT_SCALE_FACTOR;
		if(fontSize.endsWith("px"))
			return 1/1.3f;
		if(fontSize.endsWith("%") || fontSize.endsWith("em") || fontSize.endsWith("ex")
				|| fontSize.endsWith("er"))
			return getFontScaleFactor(a.getResolveParent());
		return UITools.FONT_SCALE_FACTOR;
    }


}