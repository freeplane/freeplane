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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;


/**
 * @author Dimitry Polivaev
 * Apr 23, 2011
 */
@SuppressWarnings("serial") 
public class ContainerMenuItem extends Box implements MenuElement{
	class MenuItemFocusListener implements FocusListener{

		public void focusGained(FocusEvent e) {
			ContainerMenuItem box = (ContainerMenuItem) SwingUtilities.getAncestorOfClass(ContainerMenuItem.class, e.getComponent());
			box.select();
		}

		public void focusLost(FocusEvent e) {
	    }
		
	}	public ContainerMenuItem(String label, JComponent component) {
        super(BoxLayout.X_AXIS);
		add(Box.createHorizontalStrut(10));
		final JLabel jlabel = new JLabel(label);
		add(jlabel);
		add(Box.createHorizontalStrut(10));
		add(component);
		UITools.addFocusListenerToDescendants(component, new MenuItemFocusListener());
   }

	public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
    }

	public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {
    }

	public void menuSelectionChanged(boolean isIncluded) {
		final Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
		if(! isIncluded && focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, this)){
			requestFocus();
		}
    }

	public MenuElement[] getSubElements() {
        return new MenuElement[]{};
    }

	public Component getComponent() {
        return this;
    }

	public void select() {
		MenuSelectionManager msm = MenuSelectionManager.defaultManager();
		if (msm.isComponentPartOfCurrentMenu(this)){
			MenuElement path[] = new MenuElement[2];
			path[0] = msm.getSelectedPath()[0];
			path[1] = this;
			msm.setSelectedPath(path);        
		 }
    }
}