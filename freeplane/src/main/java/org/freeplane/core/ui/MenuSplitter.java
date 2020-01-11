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
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.util.Compat;

/**
 * @author Dimitry Polivaev
 * 23.01.2013
 */
public class MenuSplitter{
	private static final String EXTRA_SUBMENU = MenuSplitter.class.getName()+".extra_submenu";

	static final int MAX_HEIGHT;

	static {		
		if (!GraphicsEnvironment.isHeadless()) {
			MAX_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height - 100;
		} else {
			MAX_HEIGHT = 0;
		}
	}


	public static int count = 0;
	private final int maxMenuItemCount;

	public MenuSplitter(int maxMenuItemCount) {
		this.maxMenuItemCount = maxMenuItemCount;
	}

	public MenuSplitter() {
		this(MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT);
	}

	public void addComponent(Container container, Component component) {
		if (container instanceof JMenu)
			addMenuComponent((JMenu) container, component);
		else
			container.add(component);
	}

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
	        		submenu = new JMenu(" ");
	        		submenu.putClientProperty(EXTRA_SUBMENU, Boolean.TRUE);
	        		popupMenu.add(submenu);
	        		if(! Compat.isMacOsX())
	        			submenu.getPopupMenu().addPopupMenuListener(MnemonicSetter.INSTANCE);
	        	}
	        	addMenuComponent(submenu, component, submenu.getPopupMenu().getComponentCount());
	        }
            else
	            popupMenu.insert(component, index);
        }
    }

	public void removeMenuComponent(final Component component) {
		final Container parent = component.getParent();
		if (parent != null) {
			parent.remove(component);
			if (parent instanceof JPopupMenu) {
				final Component invoker = ((JPopupMenu) parent).getInvoker();
				if (isExtraSubMenu(invoker) && parent.getComponentCount() == 0)
					removeMenuComponent(invoker);
			}
		}
	}

	private boolean fitsOnScreen(final JPopupMenu popupMenu, final Component component) {
		final int itemCount = popupMenu.getComponentCount();
	    return itemCount < getMaxMenuItemCount() && (popupMenu.getPreferredSize().height + component.getPreferredSize().height) < MAX_HEIGHT;
    }

	protected int getMaxMenuItemCount() {
		return maxMenuItemCount;
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
		if (isExtraSubMenu(lastComponent))
			return (JMenu) lastComponent;
		else
			return null;
    }

	public void addMenuComponent(JMenu menu, Component component) {
		addMenuComponent(menu, component, menu.getPopupMenu().getComponentCount());
	}

}