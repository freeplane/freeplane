/**
 * author: Marcel Genzmehr
 * 17.01.2012
 */
package org.freeplane.core.ui;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 * 
 */
public interface IKeyStrokeProcessor {
	public boolean processKeyBinding(KeyStroke ks, KeyEvent e);

}
