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
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Dimitry Polivaev
 * Aug 23, 2011
 */
public class EventBuffer implements KeyEventDispatcher, FocusListener {
	ArrayList<KeyEvent> events = new ArrayList<KeyEvent>(100);
	private Component textComponent;
	boolean isActive = false;
	private InputEvent firstEvent;
	private KeyEvent dispatchedEvent = null;

	public boolean isActive() {
		return isActive;
	}
	EventBuffer(){}
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
		if(ke.equals(dispatchedEvent)){
			return false;
		}
		if(textComponent != null){
			KeyEvent newEvent = new KeyEvent(textComponent, ke.getID(), ke.getWhen(), ke.getModifiers(), ke.getKeyCode(), ke.getKeyChar(), ke.getKeyLocation());
			events.add(newEvent);
		}
        else {
	        events.add(ke);
        }
		
		// Prevent Freeplane freeze
		if(ke.getKeyCode() == KeyEvent.VK_ESCAPE 
				&& ke.getID() == KeyEvent.KEY_RELEASED){
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(EventBuffer.this);
				}
			});
		}
		ke.consume();
		return true;
	}
	
	public void focusGained(final FocusEvent e) {
		try{
			textComponent.removeFocusListener(this);
			for (int i = 0; i < events.size(); i++) {
				final KeyEvent ke = events.get(i);
				if(ke.getComponent().equals(textComponent))
					dispatchedEvent = ke;
				else{
					dispatchedEvent = new KeyEvent(textComponent, ke.getID(), ke.getWhen(), ke.getModifiers(), ke.getKeyCode(), ke.getKeyChar(), ke.getKeyLocation());
				}
				e.getComponent().dispatchEvent(dispatchedEvent);
				dispatchedEvent = null;
			}
		}
		finally{
			deactivate();
		}
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
		firstEvent = null;
		dispatchedEvent = null;
	}
	public void activate(InputEvent e) {
		activate();
		if(e instanceof KeyEvent)
			dispatchKeyEvent((KeyEvent) e);
		else if(e instanceof MouseEvent)
			setFirstEvent(e);
	}
	public void setFirstEvent(InputEvent e) {
		firstEvent = e;
	}

	public KeyEvent getFirstEvent(){
		if(firstEvent instanceof KeyEvent)
			return (KeyEvent) firstEvent;
		if(events.size() == 0)
			return null;
		return events.get(0);
	}

	public MouseEvent getMouseEvent() {
		if(firstEvent instanceof MouseEvent)
			return (MouseEvent) firstEvent;
		else
			return null;
	}
}