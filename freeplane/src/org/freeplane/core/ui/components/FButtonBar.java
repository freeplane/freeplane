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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IAcceleratorChangeListener;

/**
 * @author Dimitry Polivaev
 * 03.07.2009
 */
public class FButtonBar extends FreeplaneToolBar implements IAcceleratorChangeListener, KeyEventDispatcher {
	private int modifiers;
	public boolean dispatchKeyEvent(KeyEvent e) {
	    final Window windowAncestor = SwingUtilities.getWindowAncestor(e.getComponent());
		if(windowAncestor instanceof Dialog){
			resetModifiers();
		}
		else{
	    	processDispatchedKeyEvent(e);
	    }
	    return false;
    }
	private void processDispatchedKeyEvent(KeyEvent e) {
		
		switch(e.getID()){
			case KeyEvent.KEY_PRESSED:
				switch(e.getKeyCode()){
					case KeyEvent.VK_CONTROL:
						setModifiers(KeyEvent.CTRL_MASK);
						break;
					case KeyEvent.VK_SHIFT:
						setModifiers(KeyEvent.SHIFT_MASK);
						break;
					case KeyEvent.VK_ALT:
						setModifiers(KeyEvent.ALT_MASK);
						break;
					case KeyEvent.VK_ALT_GRAPH:
						setModifiers(KeyEvent.ALT_GRAPH_MASK);
						break;
					default:
						return;
				}
			default:
				return;
			case KeyEvent.KEY_RELEASED:
				switch(e.getKeyCode()){
					case KeyEvent.VK_CONTROL:
						cleanModifiers(KeyEvent.CTRL_MASK);
						break;
					case KeyEvent.VK_SHIFT:
						cleanModifiers(KeyEvent.SHIFT_MASK);
						break;
					case KeyEvent.VK_ALT:
						cleanModifiers(KeyEvent.ALT_MASK);
					case KeyEvent.VK_ALT_GRAPH:
						cleanModifiers(KeyEvent.ALT_GRAPH_MASK);
						break;
					default:
						return;
				}
				break;
		}
	}
	
	private static final int BUTTON_NUMBER = 12;
	final private Map<Integer, JButton[]> buttons;
	public FButtonBar(){
		setRollover(false);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		buttons = new HashMap<Integer, JButton[]>();
		onModifierChange();
	}
	private void onModifierChange() {
		removeAll();
		addSeparator();
		JButton[] buttonRow = createButtons(modifiers);
		for(JButton button:buttonRow){
			add(button);
		}
		revalidate();
		repaint();
	    
    }
	private JButton[] createButtons() {
	    JButton[] buttons = new JButton[BUTTON_NUMBER];
		for(int i = 0; i < BUTTON_NUMBER; i++){
			final String name = "/images/f" + (i+1) + ".png";
			final JButton button = buttons[i] = new JButton(ResourceBundles.getText("f_button_unassigned"), 
				new ImageIcon(ResourceController.getResourceController().getResource(name)));
			button.setFocusable(false);
			button.setMargin(FreeplaneToolBar.nullInsets);
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				button.setBorderPainted(false);
			}
			button.setContentAreaFilled(false);

		}
		return buttons;
    }
	public void acceleratorChanged(JMenuItem action, KeyStroke oldStroke, KeyStroke newStroke) {
		final int oldButtonNumber = oldStroke != null ? oldStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		final int newButtonNumber = newStroke != null ? newStroke.getKeyCode() - KeyEvent.VK_F1 : -1;
		if(oldButtonNumber >= 0 && oldButtonNumber < BUTTON_NUMBER){
			final int modifiers = oldStroke.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			final JButton[] buttonRow = buttons.get(modifiers);
			final JButton button = buttonRow[oldButtonNumber];
			final ActionListener[] actionListeners = button.getActionListeners();
			assert(actionListeners.length <= 1);
			if(actionListeners.length == 1){
				button.removeActionListener(actionListeners[0]);
			}
			button.setText(ResourceBundles.getText("f_button_unassigned"));
		}
		if(newButtonNumber >= 0 && newButtonNumber < BUTTON_NUMBER){
			final int modifiers = newStroke.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			JButton[] buttonRow = createButtons(modifiers);
			final JButton button = buttonRow[newButtonNumber];
			button.setText(action.getActionCommand());
			button.addActionListener(action.getAction());
		}
    }
	private JButton[] createButtons(final int modifiers) {
	    JButton[] buttonRow = buttons.get(modifiers);
	    if(buttonRow == null){
	    	buttonRow = createButtons();
	    	buttons.put(modifiers, buttonRow);
	    }
	    return buttonRow;
    }
	private void setModifiers(int modifiers) {
	    this.modifiers |= modifiers;
	    onModifierChange();
    }
	private void resetModifiers() {
		if(this.modifiers == 0){
			return;
		}
	    this.modifiers = 0;
	    onModifierChange();
    }
	private void cleanModifiers(int modifiers) {
	    this.modifiers &= ~modifiers;
	    onModifierChange();
    }
	@Override
    protected void configureComponent(Component comp) {
    }
}
