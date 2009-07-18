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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IAcceleratorChangeListener;

/**
 * @author Dimitry Polivaev
 * 03.07.2009
 */
public class FButtonBar extends FreeplaneToolBar implements IAcceleratorChangeListener, KeyEventDispatcher, WindowFocusListener {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private int nextModifiers = 0;
	private int lastModifiers = -1;
	private boolean isWindowListenerInstalled;
	final private Timer timer = new Timer(500, new ActionListener(){

		public void actionPerformed(ActionEvent e) {
			onModifierChangeImpl();
		}
	});
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(ownWindowAncestor == null){
			ownWindowAncestor = SwingUtilities.getWindowAncestor(this);
			if(ownWindowAncestor != null){
				ownWindowAncestor.addWindowFocusListener(this);
			}
		}
	    final Window windowAncestor = SwingUtilities.getWindowAncestor(e.getComponent());
		if(windowAncestor == ownWindowAncestor){
	    	processDispatchedKeyEvent(e);
	    }
	    return false;
    }
	private void processDispatchedKeyEvent(final KeyEvent e) {
		
		final int keyCode = e.getKeyCode();
		switch(e.getID()){
			case KeyEvent.KEY_PRESSED:
				switch(keyCode){
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
						if(keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12 && timer.isRunning()){
							timer.stop();
							onModifierChangeImpl();
						}
					return;
				}
			default:
				return;
			case KeyEvent.KEY_RELEASED:
				switch(keyCode){
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
	private Window ownWindowAncestor;
	public FButtonBar(){
		setRollover(false);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		buttons = new HashMap<Integer, JButton[]>();
		onModifierChange();
	}
	private void onModifierChange() {
		if(lastModifiers == nextModifiers){
			return;
		}
		if(timer.isRunning()){
			timer.stop();
		}
		if(nextModifiers == 0){
			onModifierChangeImpl();
		}
		else{
			timer.start();
		}
    }
	private void onModifierChangeImpl() {
		if(lastModifiers == nextModifiers){
			return;
		}
		lastModifiers = nextModifiers;
	    removeAll();
		addSeparator();
		JButton[] buttonRow = createButtons(nextModifiers);
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
				new ImageIcon(ResourceController.getResourceController().getResource(name))){

					/**
                     * 
                     */
                    private static final long serialVersionUID = 1L;

					@Override
                    protected void configurePropertiesFromAction(Action a) {
                    }
			};
			button.setFocusable(false);
			button.setEnabled(false);
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
			button.setAction(null);
			button.setText(ResourceBundles.getText("f_button_unassigned"));
		}
		if(newButtonNumber >= 0 && newButtonNumber < BUTTON_NUMBER){
			final int modifiers = newStroke.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.ALT_GRAPH_MASK);
			JButton[] buttonRow = createButtons(modifiers);
			final JButton button = buttonRow[newButtonNumber];
			button.setText(action.getActionCommand());
			button.setAction(action.getAction());
			button.setEnabled(action.isEnabled());
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
		if((this.nextModifiers ^ modifiers) == 0){
			return;
		}
	    this.nextModifiers |= modifiers;
	    onModifierChange();
    }
	private void resetModifiers() {
		if(this.nextModifiers == 0){
			return;
		}
	    this.nextModifiers = 0;
	    onModifierChange();
    }
	private void cleanModifiers(int modifiers) {
		if((this.nextModifiers & modifiers) == 0){
			return;
		}
	    this.nextModifiers &= ~modifiers;
	    onModifierChange();
    }
	@Override
    protected void configureComponent(Component comp) {
    }
	
	public void windowGainedFocus(WindowEvent e) {
    }
	public void windowLostFocus(WindowEvent e) {
		resetModifiers();
	}
}
