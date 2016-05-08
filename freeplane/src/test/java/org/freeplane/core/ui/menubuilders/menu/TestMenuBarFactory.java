package org.freeplane.core.ui.menubuilders.menu;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.IKeyStrokeProcessor;
import org.freeplane.core.ui.components.FreeplaneMenuBar;

public class TestMenuBarFactory {

	public static FreeplaneMenuBar createFreeplaneMenuBar() {
		return new FreeplaneMenuBar(new IKeyStrokeProcessor() {
			
			@Override
			public boolean processKeyBinding(KeyStroke ks, KeyEvent e) {
				return false;
			}
		});
	}

}
