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
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.GrabKeyDialog;
import org.freeplane.core.resources.ui.IKeystrokeValidator;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.IKeyBindingManager;
import org.freeplane.core.ui.components.UITools;

/**
 * @author Dimitry Polivaev
 * 20.04.2009
 */
class AccelerateableAction implements IFreeplaneAction {
	private static final boolean DISABLE_KEY_TYPE = ResourceController.getResourceController().getBooleanProperty(
	    "disable_key_type");
	/**
	 * 
	 */
	private final MenuBuilder menuBuilder;
	final private AFreeplaneAction originalAction;
	private static JDialog setAcceleratorOnNextClickActionDialog;

	static boolean isNewAcceleratorOnNextClickEnabled() {
		return setAcceleratorOnNextClickActionDialog != null;
	}

	private static final String SET_ACCELERATOR_ON_NEXT_CLICK_ACTION = "set_accelerator_on_next_click_action";

	static void setNewAcceleratorOnNextClick(final Controller controller) {
		if (AccelerateableAction.isNewAcceleratorOnNextClickEnabled()) {
			return;
		}
		final String titel = ResourceBundles.getText("SetAcceleratorOnNextClickAction.text");
		final String text = ResourceBundles.getText(SET_ACCELERATOR_ON_NEXT_CLICK_ACTION);
		final String[] options = { FpStringUtils.removeMnemonic(ResourceBundles.getText("cancel")) };
		final JOptionPane infoPane = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
		    options);
		setAcceleratorOnNextClickActionDialog = infoPane.createDialog(controller.getViewController().getFrame(), titel);
		setAcceleratorOnNextClickActionDialog.setModal(false);
		setAcceleratorOnNextClickActionDialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(final ComponentEvent e) {
				setAcceleratorOnNextClickActionDialog = null;
			}
		});
		setAcceleratorOnNextClickActionDialog.setVisible(true);
	}

	public AccelerateableAction(final MenuBuilder menuBuilder, final AFreeplaneAction originalAction) {
		super();
		this.menuBuilder = menuBuilder;
		this.originalAction = originalAction;
	}

	public void actionPerformed(final ActionEvent e) {
		final boolean newAcceleratorOnNextClickEnabled = AccelerateableAction.isNewAcceleratorOnNextClickEnabled();
		if (newAcceleratorOnNextClickEnabled) {
			setAcceleratorOnNextClickActionDialog.setVisible(false);
		}
		final Object source = e.getSource();
		if ((newAcceleratorOnNextClickEnabled || 0 != (e.getModifiers() & ActionEvent.CTRL_MASK))
		        && source instanceof IKeyBindingManager && !((IKeyBindingManager) source).isKeyBindingProcessed()
		        && source instanceof JMenuItem) {
			final JMenuItem item = (JMenuItem) source;
			newAccelerator(item);
			return;
		}
		originalAction.actionPerformed(e);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		originalAction.addPropertyChangeListener(listener);
	}

	public void afterMapChange(final Object newMap) {
		originalAction.afterMapChange(newMap);
	}

	public Object getValue(final String key) {
		return originalAction.getValue(key);
	}

	public boolean isEnabled() {
		return originalAction.isEnabled();
	}

	public boolean isSelected() {
		return originalAction.isSelected();
	}

	private void newAccelerator(final JMenuItem editedItem) {
		final Object key = menuBuilder.getKeyByUserObject(editedItem);
		final String shortcutKey = menuBuilder.getShortcutKey(key.toString());
		final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(ResourceController.getResourceController().getProperty(
		    shortcutKey));
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

			public boolean isValid(final KeyStroke keystroke, final Character keyChar) {
				if (keystroke == null) {
					return true;
				}
				final Object menubarKey = menuBuilder.getMenubar(menuBuilder.get(key));
				if (menubarKey == null) {
					return true;
				}
				if (keyChar != KeyEvent.CHAR_UNDEFINED
				        && (keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK)) == 0) {
					return DISABLE_KEY_TYPE;
				}
				final DefaultMutableTreeNode menubarNode = menuBuilder.get(menubarKey);
				if ((keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK)) == Event.ALT_MASK) {
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
				if (!isValid(menubarNode, keystroke)) {
					return false;
				}
				final KeyStroke derivedKS = FreeplaneMenuBar.derive(keystroke, keyChar);
				if (derivedKS == keystroke) {
					return true;
				}
				return isValid(menubarNode, derivedKS);
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
}
