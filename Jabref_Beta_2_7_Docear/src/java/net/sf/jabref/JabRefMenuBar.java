/**
 * author: Marcel Genzmehr
 * 23.01.2012
 */
package net.sf.jabref;

import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

public class JabRefMenuBar extends JMenuBar {

	private static final long serialVersionUID = 1L;
	
	public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		return super.processKeyBinding(ks, e, condition, pressed); 
	}

	
}