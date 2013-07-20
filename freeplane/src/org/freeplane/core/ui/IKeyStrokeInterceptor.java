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
public interface IKeyStrokeInterceptor {
	public boolean interceptKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed);

}
