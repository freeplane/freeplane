package org.freeplane.main.application;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_WINDOWS;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.features.mode.Controller;

public class MenuKeyProcessor {
    static final MenuKeyProcessor INSTANCE = new MenuKeyProcessor();
	public boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
    	if(containsModifierKeyCode(ks))
    		return false;
    	final FreeplaneMenuBar freeplaneMenuBar = Controller.getCurrentController().getViewController()
    	    .getFreeplaneMenuBar();
    	return freeplaneMenuBar.processKeyBinding(ks, e, JComponent.WHEN_IN_FOCUSED_WINDOW, pressed);
    }
    private static final List<Integer> modifierKeyCodes = Arrays.asList(VK_CONTROL, VK_ALT, VK_SHIFT, VK_META, VK_WINDOWS);
	private boolean containsModifierKeyCode(final KeyStroke ks) {
		return modifierKeyCodes.contains(ks.getKeyCode());
	}
}