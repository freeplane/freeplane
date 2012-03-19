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
package org.freeplane.features.styles.mindmapmode;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * @author Dimitry Polivaev
 * Dec 31, 2011
 */
@SuppressWarnings("serial")
public class ComboBoxRendererWithTooltip extends BasicComboBoxRenderer {
		final private JComboBox box;
		public ComboBoxRendererWithTooltip(JComboBox box){
			this.box = box;
		}
	    public Component getListCellRendererComponent(JList list, Object value,
	                                                  int index, boolean isSelected, boolean cellHasFocus) {
	    	super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    	final String text;
	    	final Container parent = list.getParent();
	    	if(parent == null){
	    		text = getText();
	    	}
	    	else {
			final int parentWidth = parent.getWidth();
			if(index == -1 || parentWidth > 0 && parentWidth < getPreferredSize().width)
	    		text = getText();
	    	else
	    		text = null;
	    	}
			if(index == -1){
	    		box.setToolTipText(text);
	    	}
	    	else{
	    		list.setToolTipText(text);
	    	}
			
			// FELIXHACK
			//setFont(getFont().deriveFont(8.0F));
			//setPreferredSize(new Dimension(Math.max(40, getPreferredSize().width), 20));
			//setBackground(Color.RED);
			
	    	return this;
	    }
}