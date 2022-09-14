/*
 * Created on 14 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.core.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class KeystrokeDescriber {

	public static String createKeystrokeDescription(KeyStroke accelerator) {
		String acceleratorText =  null;
		if (accelerator !=  null)
		{
			acceleratorText = "";
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0)
			{
				acceleratorText += InputEvent.getModifiersExText(modifiers);
				acceleratorText += "+";
			}
			acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
		}
		return acceleratorText;
	}
	
}