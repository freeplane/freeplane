package org.freeplane.core.ui;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

public class UITextChanger implements KeyEventDispatcher {

	public static final String TRANSLATIONKEY = "org.freeplane.translationkey";
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		final int modifiers = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK;
		if(((e.getModifiersEx() & modifiers) == modifiers) && e.getKeyCode() == KeyEvent.VK_F10) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				replaceComponentText();
			}
			return true;
		}
		return false;
	}

	private void replaceComponentText() {
		for (Window window : Window.getWindows()) {
	        final Point mousePosition = window.getMousePosition(true);
			if (mousePosition != null) {
				final Component componentUnderMouse = SwingUtilities.getDeepestComponentAt(window, mousePosition.x, mousePosition.y);
				replaceComponentText(componentUnderMouse);
			}
	    }
	}

	private void replaceComponentText(Component component) {
		if(! (component instanceof JComponent))
			return;
		final String translationKey = (String)((JComponent) component).getClientProperty(TRANSLATIONKEY);
		if(translationKey == null)
			return;
		String newText = JOptionPane.showInputDialog(component, "replace text", TextUtils.getRawText(translationKey));
		if(newText != null) {
			if(newText.isEmpty())
				newText = null;
			ResourceController.getResourceController().putUserResourceString(translationKey, newText);
			if(newText == null)
				newText = TextUtils.getRawText(translationKey);
			if(component instanceof AbstractButton)
				((AbstractButton) component).setText(newText);
			else if(component instanceof JLabel)
				((JLabel) component).setText(newText);
					
		}
	}
}

