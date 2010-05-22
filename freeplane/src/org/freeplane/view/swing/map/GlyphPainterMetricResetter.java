/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
package org.freeplane.view.swing.map;

import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;

/**
 * @author Dimitry Polivaev
 * May 19, 2010
 */

class GlyphPainterMetricResetter extends GlyphView{

	private static final Element elem = new DummyElement();
	private static final JLabel c;
	private static final Font f;
	static {
		c= new JLabel();
		f = new Font(null, 0, 0);
		c.setFont(f);
	}
	private static GlyphPainterMetricResetter dummyGlyphView = new GlyphPainterMetricResetter(); 

	private GlyphPainterMetricResetter() {
	    super(elem);
    }

	@Override
    public Font getFont() {
		return f;
    }

	@Override
    public Container getContainer() {
		return c;
    }
	
	static void resetPainter(){
		dummyGlyphView.checkPainter();
		dummyGlyphView.getGlyphPainter().getAscent(dummyGlyphView);
	}
}
class DummyElement implements Element {
	public AttributeSet getAttributes() {
		return null;
	}

	public Document getDocument() {
		return null;
	}

	public Element getElement(int index) {
		return null;
	}

	public int getElementCount() {
		return 0;
	}

	public int getElementIndex(int offset) {
		return 0;
	}

	public int getEndOffset() {
		return 0;
	}

	public String getName() {
		return "";
	}

	public Element getParentElement() {
		return null;
	}

	public int getStartOffset() {
		return 0;
	}

	public boolean isLeaf() {
		return false;
	}
}
