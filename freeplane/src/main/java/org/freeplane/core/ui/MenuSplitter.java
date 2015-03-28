/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.core.ui;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * 23.01.2013
 */
public class MenuSplitter{
	private static final String EXTRA_SUBMENU = MenuBuilder.class.getName()+".extra_submenu";
	private static final int MAX_MENU_ITEM_COUNT = ResourceController.getResourceController().getIntProperty("max_menu_item_count");
	
	public static int count = 0;
	public void addMenuComponent(JMenu menu, final Component component, final int index) {
	    final JPopupMenu popupMenu = menu.getPopupMenu();
	    final int itemCount = popupMenu.getComponentCount();
	    if(index == 0 || index < itemCount)
	    	popupMenu.insert(component, index);
        else {
        	final Component lastMenuItem = popupMenu.getComponent(itemCount - 1);
        	final boolean extraSubMenu = isExtraSubMenu(lastMenuItem);
	        if (extraSubMenu || !fitsOnScreen(popupMenu, component)) {
	        	final JMenu submenu;
				if(extraSubMenu) {
	        		submenu = (JMenu) lastMenuItem;
	        	}
                else {
	        		if (component instanceof JPopupMenu.Separator)
	        			return;
	        		submenu = new JMenu("");
	        		submenu.putClientProperty(EXTRA_SUBMENU, Boolean.TRUE);
	        		popupMenu.add(submenu);
	        	}
	        	addMenuComponent(submenu, component, submenu.getPopupMenu().getComponentCount());
	        }
            else
	            popupMenu.insert(component, index);
        }
    }

	private boolean fitsOnScreen(final JPopupMenu popupMenu, final Component component) {
		final int itemCount = popupMenu.getComponentCount();
	    return itemCount < MAX_MENU_ITEM_COUNT && (popupMenu.getPreferredSize().height + component.getPreferredSize().height) < MenuBuilder.MAX_HEIGHT;
    }

	public boolean hasExtraSubMenu(final JMenu menu) {
		final Component lastComponent = menu.getComponent(menu.getComponentCount()-1);
	    return isExtraSubMenu(lastComponent);
    }
	
	public boolean isExtraSubMenu(final Component c) {
	    return (c instanceof JMenu) &&  (Boolean.TRUE.equals(((JMenu)c).getClientProperty(EXTRA_SUBMENU)));
    }

	public JMenu getExtraSubMenu(JMenu parentComponent) {
		final Component lastComponent = parentComponent.getComponent(parentComponent.getComponentCount()-1);
		if(new MenuSplitter().isExtraSubMenu(lastComponent))
			return (JMenu) lastComponent;
		else
			return null;
    }


	
}