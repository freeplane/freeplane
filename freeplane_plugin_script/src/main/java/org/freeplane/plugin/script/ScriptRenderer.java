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
package org.freeplane.plugin.script;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import org.freeplane.core.util.HtmlUtils;

/**
 * @author Dimitry Polivaev
 * Jul 23, 2011
 */
public class  ScriptRenderer extends DefaultListCellRenderer{
    private static final long serialVersionUID = 1L;

	@Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
	    final JComponent renderer = (JComponent) super.getListCellRendererComponent(list, firstLine(value), index, isSelected, cellHasFocus);
	    final String script = value.toString();
		if(script.contains("\n")) {
	    	renderer.setToolTipText(HtmlUtils.plainToHTML(script));
	    }

		return renderer;
    }

	private Object firstLine(Object value) {
		if(! (value instanceof String) )
			return value;
		String script = ((String) value).trim();
		return script.substring(0, Math.min(40, script.length())).trim().replaceAll("\\s+", " ");
		
    }

	@Override
    public Dimension getPreferredSize() {
	    final Dimension preferredSize = super.getPreferredSize();
		if(! isPreferredSizeSet())
			preferredSize.width = 100;
		return preferredSize;
    }
	
}