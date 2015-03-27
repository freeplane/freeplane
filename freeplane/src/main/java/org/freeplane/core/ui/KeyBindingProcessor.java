package org.freeplane.core.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

import org.freeplane.core.extension.IExtension;

public class KeyBindingProcessor implements IExtension {
	
	private final List<IKeyStrokeProcessor> processors = new ArrayList<IKeyStrokeProcessor>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		synchronized (processors) {
			boolean consumed = false;
			for(int i = processors.size()-1; i >= 0; i--) { 
				//maybe break after the first interception?
				consumed = processors.get(i).processKeyBinding(ks, e, condition, pressed, consumed) || consumed;
			}
			return consumed;
		}
	}
	
	public void addKeyStrokeProcessor(IKeyStrokeProcessor processor) {
		synchronized (processors) {
			if(!processors.contains(processor)) {
				processors.add(processor);
			}
		}
	}
	
	public void removeKeyStrokeProcessor(IKeyStrokeProcessor processor) {
		synchronized (processors) {
			if(processors.contains(processor)) {
				processors.remove(processor);
			}
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
