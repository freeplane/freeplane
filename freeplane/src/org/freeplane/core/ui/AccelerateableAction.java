/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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

import java.awt.Event;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.GrabKeyDialog;
import org.freeplane.core.resources.ui.IKeystrokeValidator;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 * 20.04.2009
 */
class AccelerateableAction implements IFreeplaneAction {
	/**
	 * 
	 */
	private final MenuBuilder menuBuilder;
	final private AFreeplaneAction originalAction;

	public AccelerateableAction(final MenuBuilder menuBuilder, final AFreeplaneAction originalAction) {
		super();
		this.menuBuilder = menuBuilder;
		this.originalAction = originalAction;
	}

	public void actionPerformed(final ActionEvent e) {
		if (!(e.getModifiers() == ActionEvent.CTRL_MASK + InputEvent.BUTTON1_MASK && e.getSource() instanceof JMenuItem)) {
			originalAction.actionPerformed(e);
			return;
		}
		final JMenuItem item = (JMenuItem) e.getSource();
		newAccelerator(item);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		originalAction.addPropertyChangeListener(listener);
	}

	public Object getValue(final String key) {
		return originalAction.getValue(key);
	}

	public boolean isEnabled() {
		return originalAction.isEnabled();
	}

	private void newAccelerator(final JMenuItem editedItem) {
		final Frame frame = JOptionPane.getFrameForComponent(editedItem);
		final Object key = menuBuilder.getKeyByUserObject(editedItem);
		final String shortcutKey = menuBuilder.getShortcutKey(key.toString());
		final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(frame, ResourceController.getResourceController()
		    .getProperty(shortcutKey));
		grabKeyDialog.setValidator(new IKeystrokeValidator() {
			private boolean isValid(final DefaultMutableTreeNode menubarNode, final KeyStroke keystroke) {
				final Enumeration menuElements = menubarNode.children();
				while (menuElements.hasMoreElements()) {
					final Node menuItemNode = (Node) menuElements.nextElement();
					final Object userObject = menuItemNode.getUserObject();
					if (userObject instanceof JMenuItem) {
						final JMenuItem menuItem = (JMenuItem) userObject;
						if (keystroke.equals(menuItem.getAccelerator())) {
							if (editedItem.equals(menuItem)) {
								return true;
							}
							final int replace = JOptionPane.showConfirmDialog(grabKeyDialog, menuItem.getText(),
							    ResourceBundles.getText("remove_shortcut_question"), JOptionPane.YES_NO_OPTION);
							if (replace == JOptionPane.YES_OPTION) {
								menuBuilder.setAccelerator(menuItemNode, null);
								final String shortcutKey = menuBuilder.getShortcutKey(menuItemNode.getKey().toString());
								ResourceController.getResourceController().setProperty(shortcutKey, "");
								return true;
							}
							return false;
						}
					}
					if (!isValid(menuItemNode, keystroke)) {
						return false;
					}
				}
				return true;
			}

			public boolean isValid(final KeyStroke keystroke) {
				if (keystroke == null) {
					return true;
				}
				final Object menubarKey = menuBuilder.getMenubar(menuBuilder.get(key));
				if (menubarKey == null) {
					return true;
				}
				final DefaultMutableTreeNode menubarNode = menuBuilder.get(menubarKey);
				if ((keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK)) == Event.ALT_MASK) {
					final JMenuBar menuBar = (JMenuBar) menubarNode.getUserObject();
					final int menuCount = menuBar.getMenuCount();
					for (int i = 0; i < menuCount; i++) {
						final JMenu menu = menuBar.getMenu(i);
						final char c = (char) menu.getMnemonic();
						if (Character.toLowerCase(keystroke.getKeyCode()) == Character.toLowerCase(c)) {
							JOptionPane.showMessageDialog(grabKeyDialog, menu.getText(), ResourceBundles
							    .getText("used_in_menu"), JOptionPane.WARNING_MESSAGE);
							return false;
						}
					}
				}
				return isValid(menubarNode, keystroke);
			}
		});
		grabKeyDialog.setVisible(true);
		if (grabKeyDialog.isOK()) {
			final String shortcut = grabKeyDialog.getShortcut();
			final KeyStroke accelerator = UITools.getKeyStroke(shortcut);
			menuBuilder.setAccelerator((Node) menuBuilder.get(key), accelerator);
			ResourceController.getResourceController().setProperty(shortcutKey, shortcut);
		}
	}

	public void putValue(final String key, final Object value) {
		originalAction.putValue(key, value);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		originalAction.removePropertyChangeListener(listener);
	}

	public void setEnabled(final boolean b) {
		originalAction.setEnabled(b);
	}

	public boolean isSelected() {
	    return originalAction.isSelected();
    }

	public void afterMapChange(Object newMap) {
		originalAction.afterMapChange(newMap);
    }
}
