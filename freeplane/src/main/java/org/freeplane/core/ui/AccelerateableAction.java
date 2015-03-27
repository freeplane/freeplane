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

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.components.IKeyBindingManager;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 20.04.2009
 */
public class AccelerateableAction implements IFreeplaneAction {
//	private class KeystrokeValidator implements IKeystrokeValidator {
//        private final Component parentComponent;
//        private final Object key;
//        private final JMenuItem editedItem;
//
//		private KeystrokeValidator(Component parentComponent, Object key, JMenuItem editedItem) {
//			this.parentComponent = parentComponent;
//			this.key = key;
//			this.editedItem = editedItem;
//		}
//
//		private boolean checkForOverwriteShortcut(final DefaultMutableTreeNode menubarNode, final KeyStroke keystroke) {
//			final Node priorAssigned = MenuUtils.findAssignedMenuItemNodeRecursively(menubarNode, keystroke);
//			if (priorAssigned == null || editedItem.equals(priorAssigned.getUserObject())) {
//				return true;
//			}
//			return replaceOrCancel(priorAssigned, ((JMenuItem) priorAssigned.getUserObject()).getText());
//		}
//
//		private boolean replaceOrCancel(Node menuItemNode, String oldMenuItemTitle) {
//			if (askForReplaceShortcutViaDialog(oldMenuItemTitle, parentComponent)) {
//				menuBuilder.setAccelerator(menuItemNode, null);
//				final String shortcutKey = menuBuilder.getShortcutKey(menuItemNode.getKey().toString());
//				ResourceController.getResourceController().setProperty(shortcutKey, "");
//				return true;
//			} else {
//				return false;
//			}
//		}
//
//		public boolean isValid(final KeyStroke keystroke, final Character keyChar) {
//        	if (keystroke == null) {
//        		return true;
//        	}
//        	final Node menuBarNode = menuBuilder.getMenuBar(menuBuilder.get(key));
//        	if (menuBarNode == null) {
//        		return true;
//        	}
//        	if (keyChar != KeyEvent.CHAR_UNDEFINED
//        	        && (keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK)) == 0) {
//        		final String keyTypeActionString = ResourceController.getResourceController().getProperty("key_type_action", FirstAction.EDIT_CURRENT.toString());
//        		FirstAction keyTypeAction = FirstAction.valueOf(keyTypeActionString);
//        		return FirstAction.IGNORE.equals(keyTypeAction);
//        	}
//        	if ((keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK)) == Event.ALT_MASK) {
//        		final JMenuBar menuBar = (JMenuBar) menuBarNode.getUserObject();
//        		final int menuCount = menuBar.getMenuCount();
//        		for (int i = 0; i < menuCount; i++) {
//        			final JMenu menu = menuBar.getMenu(i);
//        			final char c = (char) menu.getMnemonic();
//        			if (Character.toLowerCase(keystroke.getKeyCode()) == Character.toLowerCase(c)) {
//        				JOptionPane.showMessageDialog(parentComponent, menu.getText(), TextUtils
//        				    .getText("used_in_menu"), JOptionPane.WARNING_MESSAGE);
//        				return false;
//        			}
//        		}
//        	}
//        	if (!checkForOverwriteShortcut(menuBarNode, keystroke)) {
//        		return false;
//        	}
//        	final KeyStroke derivedKS = FreeplaneMenuBar.derive(keystroke, keyChar);
//        	if (derivedKS == keystroke) {
//        		return true;
//        	}
//        	return checkForOverwriteShortcut(menuBarNode, derivedKS);
//        }
//    }

	final private AFreeplaneAction originalAction;
	private ActionAcceleratorManager acceleratorManager;
    private static JDialog setAcceleratorOnNextClickActionDialog;
    private static KeyStroke acceleratorForNextClickedAction;

	public static boolean isNewAcceleratorOnNextClickEnabled() {
		return setAcceleratorOnNextClickActionDialog != null;
	}

	private static final String SET_ACCELERATOR_ON_NEXT_CLICK_ACTION = "set_accelerator_on_next_click_action";

	static void setNewAcceleratorOnNextClick(KeyStroke accelerator) {
		if (AccelerateableAction.isNewAcceleratorOnNextClickEnabled()) {
			return;
		}
        acceleratorForNextClickedAction = accelerator;
        String title = TextUtils.getText("SetAccelerator.dialogTitle");
        String text = TextUtils.getText(SET_ACCELERATOR_ON_NEXT_CLICK_ACTION);
		if(accelerator != null)
			text = TextUtils.format("SetAccelerator.keystrokeDetected", toString(accelerator)) + "\n" + text;
		final Frame frame = Controller.getCurrentController().getViewController().getFrame();
		setAcceleratorOnNextClickActionDialog = UITools.createCancelDialog(frame, title, text);
		getAcceleratorOnNextClickActionDialog().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(final ComponentEvent e) {
				setAcceleratorOnNextClickActionDialog = null;
				acceleratorForNextClickedAction = null;
			}
		});
		getAcceleratorOnNextClickActionDialog().setVisible(true);
	}

	public AccelerateableAction(final ActionAcceleratorManager acceleratorManager, final AFreeplaneAction originalAction) {
		super();
		this.acceleratorManager = acceleratorManager;
		this.originalAction = originalAction;
	}

	public void actionPerformed(final ActionEvent e) {
		final boolean newAcceleratorOnNextClickEnabled = AccelerateableAction.isNewAcceleratorOnNextClickEnabled();
		final KeyStroke newAccelerator = acceleratorForNextClickedAction;
		if (newAcceleratorOnNextClickEnabled) {
			getAcceleratorOnNextClickActionDialog().setVisible(false);
		}
		final Object source = e.getSource();
		if ((newAcceleratorOnNextClickEnabled || 0 != (e.getModifiers() & ActionEvent.CTRL_MASK))
		        && source instanceof IKeyBindingManager && !((IKeyBindingManager) source).isKeyBindingProcessed()
		        && source instanceof JMenuItem) {
			final JMenuItem item = (JMenuItem) source;
			acceleratorManager.newAccelerator(getOriginalAction(), newAccelerator);
			return;
		}
		originalAction.actionPerformed(e);
	}
	 
	public static JDialog getAcceleratorOnNextClickActionDialog() {
		return setAcceleratorOnNextClickActionDialog;
	}
	
	public static KeyStroke getAcceleratorForNextClick() {
		return acceleratorForNextClickedAction;
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

	public void setSelected(boolean newValue) {
		originalAction.setSelected(newValue);
	}
	
	public AFreeplaneAction getOriginalAction() {
		return originalAction;
	}

//	public void newAccelerator(final JMenuItem editedItem, final KeyStroke newAccelerator) {
//		final Object key = menuBuilder.getKeyByUserObject(editedItem);
//		final String shortcutKey = menuBuilder.getShortcutKey(key.toString());
//		final String oldShortcut = ResourceController.getResourceController().getProperty(shortcutKey);
//		if (newAccelerator == null
//		        || !new KeystrokeValidator(editedItem, key, editedItem).isValid(newAccelerator,
//		            newAccelerator.getKeyChar())) {
//            final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(oldShortcut);
//		    final IKeystrokeValidator validator = new KeystrokeValidator(grabKeyDialog, key, editedItem);
//		    grabKeyDialog.setValidator(validator);
//		    grabKeyDialog.setVisible(true);
//		    if (grabKeyDialog.isOK()) {
//		        final String shortcut = grabKeyDialog.getShortcut();
//		        final KeyStroke accelerator = UITools.getKeyStroke(shortcut);
//		        menuBuilder.setAccelerator((Node) menuBuilder.get(key), accelerator);
//		        ResourceController.getResourceController().setProperty(shortcutKey, shortcut);
//                LogUtils.info("created shortcut '" + shortcut + "' for menuitem '" + key + "', shortcutKey '"
//                        + shortcutKey + "' (" + editedItem.getText() + ")");
//		    }
//		}
//		else{
//		    if(oldShortcut != null){
//                final int replace = JOptionPane.showConfirmDialog(
//                    editedItem, 
//                    oldShortcut,
//                    TextUtils.getText("remove_shortcut_question"), JOptionPane.YES_NO_OPTION);
//                if (replace != JOptionPane.YES_OPTION) {
//                    return;
//                }
//		    }
//            menuBuilder.setAccelerator((Node) menuBuilder.get(key), newAccelerator);
//            ResourceController.getResourceController().setProperty(shortcutKey, toString(newAccelerator));
//            LogUtils.info("created shortcut '" + toString(newAccelerator) + "' for menuitem '" + key
//                    + "', shortcutKey '" + shortcutKey + "' (" + editedItem.getText() + ")");
//		}
//	}

    private static String toString(final KeyStroke newAccelerator) {
        return newAccelerator.toString().replaceFirst("pressed ", "");
    }

	private static boolean askForReplaceShortcutViaDialog(String oldMenuItemTitle, Component parentComponent) {
		final int replace = JOptionPane.showConfirmDialog(parentComponent,
		    TextUtils.format("replace_shortcut_question", oldMenuItemTitle),
		    TextUtils.format("replace_shortcut_title"), JOptionPane.YES_NO_OPTION);
		return replace == JOptionPane.YES_OPTION;
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

	public String getIconKey() {
		return originalAction.getIconKey();
	}
}
