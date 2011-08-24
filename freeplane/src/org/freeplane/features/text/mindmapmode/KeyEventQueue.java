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
package org.freeplane.features.text.mindmapmode;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * @author Dimitry Polivaev
 * Aug 23, 2011
 */
public class KeyEventQueue implements KeyEventDispatcher, FocusListener {
	private static final KeyEventQueue instance = new KeyEventQueue();
	ArrayList<KeyEvent> events = new ArrayList<KeyEvent>(100);
	private Component textComponent;
	boolean isActive = false;
	
	public boolean isActive() {
    	return isActive;
    }
	private KeyEventQueue(){}
	static public KeyEventQueue getInstance(){
		return instance;
	}

	public Component getTextComponent() {
    	return textComponent;
    }

	public void setTextComponent(Component c) {
		if(textComponent != null)
			textComponent.removeFocusListener(this);
    	this.textComponent = c;
    	if(textComponent != null)
    		textComponent.addFocusListener(this);
    }

	public boolean dispatchKeyEvent(final KeyEvent ke) {
	    if(events.contains(ke)){
	        return false;
	    }
	    if(textComponent != null){
	    	KeyEvent newEvent = new KeyEvent(textComponent, ke.getID(), ke.getWhen(), ke.getModifiers(), ke.getKeyCode(), ke.getKeyChar(), ke.getKeyLocation());
	    	events.add(newEvent);
	    }
	    else
	    	events.add(ke);
	    ke.consume();
		return true;
	}

	public void focusGained(final FocusEvent e) {
		textComponent.removeFocusListener(this);
		for (int i = 0; i < events.size(); i++) {
            final KeyEvent ke = events.get(i);
            if(ke.getComponent().equals(textComponent))
            	e.getComponent().dispatchEvent(ke);
            else{
            	KeyEvent newEvent = new KeyEvent(textComponent, ke.getID(), ke.getWhen(), ke.getModifiers(), ke.getKeyCode(), ke.getKeyChar(), ke.getKeyLocation());
            	e.getComponent().dispatchEvent(newEvent);
            }
		}
		deactivate();
	}

	public void focusLost(final FocusEvent e) {
	}
	public void activate() {
		if(isActive)
			return;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		isActive = true;
    }
	public void deactivate() {
		if(! isActive)
			return;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		isActive = false;
    	if(textComponent != null)
    		textComponent.removeFocusListener(this);
		textComponent = null;
		events.clear();
    }
	public void activate(KeyEvent e) {
	    activate();
	    dispatchKeyEvent(e);
    }
	
	public KeyEvent getFirstEvent(){
		if(events.size() == 0)
			return null;
		return events.get(0);
	}
}