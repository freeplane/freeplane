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
class AccelerateableAction implements Action{
	/**
     * 
     */
    private final MenuBuilder menuBuilder;
	final private Action originalAction;

	public void actionPerformed(ActionEvent e) {
		if(! (e.getModifiers() == ActionEvent.CTRL_MASK + InputEvent.BUTTON1_MASK && e.getSource() instanceof JMenuItem)){
	        originalAction.actionPerformed(e);
	        return;
		}
		JMenuItem item = (JMenuItem) e.getSource();
		newAccelerator(item);
		
    }

	private void newAccelerator(final JMenuItem editedItem) {
        Frame frame = JOptionPane.getFrameForComponent(editedItem);
		final Object key = this.menuBuilder.getKeyByUserObject(editedItem);
		String shortcutKey = this.menuBuilder.getShortcutKey(key.toString());
		final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(frame, 
			ResourceController.getResourceController().getProperty(shortcutKey));
		grabKeyDialog.setValidator(new IKeystrokeValidator(){
			public boolean isValid(KeyStroke keystroke) {
				if(keystroke == null){
					return true;
				}
				Object menubarKey = AccelerateableAction.this.menuBuilder.getMenubar(AccelerateableAction.this.menuBuilder.get(key));
				if(menubarKey == null){
					return true;
				}
				DefaultMutableTreeNode menubarNode = AccelerateableAction.this.menuBuilder.get(menubarKey);
				if((keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK)) == Event.ALT_MASK){
					JMenuBar menuBar = (JMenuBar) menubarNode.getUserObject();
					int menuCount = menuBar.getMenuCount();
					for(int i = 0; i < menuCount; i++){
						JMenu menu = menuBar.getMenu(i);
						char c = (char) menu.getMnemonic();
						if(Character.toLowerCase(keystroke.getKeyCode()) == Character.toLowerCase(c)){
							JOptionPane.showMessageDialog (grabKeyDialog, menu.getText(), ResourceBundles.getText("used_in_menu"), JOptionPane.WARNING_MESSAGE);
							return false;
						}
					}
				}
				return isValid(menubarNode, keystroke);
            }

			private boolean isValid(DefaultMutableTreeNode menubarNode, KeyStroke keystroke) {
                Enumeration menuElements = menubarNode.children();
				while (menuElements.hasMoreElements()){
					Node menuItemNode = (Node) menuElements.nextElement();
					Object userObject = menuItemNode.getUserObject();
					if(userObject instanceof JMenuItem){
						JMenuItem menuItem = (JMenuItem) userObject;
						if(keystroke.equals(menuItem.getAccelerator())){
							if(editedItem.equals(menuItem)){
								return true;
							}
							int replace = JOptionPane.showConfirmDialog(grabKeyDialog, menuItem.getText(), ResourceBundles.getText("remove_shortcut_question"), JOptionPane.YES_NO_OPTION);
							if(replace == JOptionPane.YES_OPTION){
								menuItem.setAccelerator(null);
								String shortcutKey = AccelerateableAction.this.menuBuilder.getShortcutKey(menuItemNode.getKey().toString());
								ResourceController.getResourceController().setProperty(shortcutKey, "");
								return true;
							}
							return false;
						}
					}
					if (! isValid(menuItemNode, keystroke)) {
						return false;
					}
				}
				return true;
            }});
		grabKeyDialog.setVisible(true);
		if (grabKeyDialog.isOK()) {
			String shortcut = grabKeyDialog.getShortcut();
			KeyStroke accelerator = UITools.getKeyStroke(shortcut);
			editedItem.setAccelerator(accelerator);
			ResourceController.getResourceController().setProperty(shortcutKey, shortcut);
		}
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        originalAction.addPropertyChangeListener(listener);
    }

	public Object getValue(String key) {
        return originalAction.getValue(key);
    }

	public boolean isEnabled() {
        return originalAction.isEnabled();
    }

	public void putValue(String key, Object value) {
        originalAction.putValue(key, value);
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
        originalAction.removePropertyChangeListener(listener);
    }

	public void setEnabled(boolean b) {
        originalAction.setEnabled(b);
    }

	public AccelerateableAction(MenuBuilder menuBuilder, Action originalAction) {
        super();
		this.menuBuilder = menuBuilder;
        this.originalAction = originalAction;
    }
}