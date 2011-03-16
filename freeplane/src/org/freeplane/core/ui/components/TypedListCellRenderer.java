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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.net.URI;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.util.FreeplaneDate;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.icon.IconController;

/**
 * @author Dimitry Polivaev
 * Mar 16, 2011
 */
public class TypedListCellRenderer extends DefaultListCellRenderer implements ListCellRenderer {

	final private static TypedListCellRenderer instance = new TypedListCellRenderer();
	static TypedListCellRenderer getInstance() {
    	return instance;
    }

	public TypedListCellRenderer() {
	    super();
    }

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	@Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    if(value instanceof FreeplaneDate){
	    	final FreeplaneDate fd = (FreeplaneDate)value;
	    	if(fd.containsTime())
	    		setIcon(ViewController.dateTimeIcon);
	    	else
	    		setIcon(ViewController.dateIcon);
	    }
	    else if(value instanceof URI)
	    	setIcon(ViewController.linkIcon);
	    else if(value instanceof String && TextUtils.isNumber((String) value))
	    	setIcon(ViewController.numberIcon);
	    
		return this;
    }
	
}
