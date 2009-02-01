/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be reworked.
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
package org.freeplane.core.resources.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Various hacks to get keyboard event handling to behave in a consistent manner
 * across Java implementations.
 *
 * @author Slava Pestov
 * @version $Id: KeyEventWorkaround.java,v 1.1.2.1 2005/05/10 20:55:31
 *          christianfoltin Exp $
 */
public class KeyEventWorkaround {
	public static final boolean ALT_KEY_PRESSED_DISABLED = false;
	public static final boolean ALTERNATIVE_DISPATCHER = false;
	private static int last;
	private static final int LAST_ALT = 2;
	private static final int LAST_NOTHING = 0;
	private static final int LAST_NUMKEYPAD = 1;
	static long lastKeyTime;
	static int modifiers;

	/**
	 * A workaround for non-working NumLock status in some Java versions.
	 *
	 * @since jEdit 4.0pre8
	 */
	public static void numericKeypadKey() {
		KeyEventWorkaround.last = KeyEventWorkaround.LAST_NOTHING;
	}

	public static KeyEvent processKeyEvent(final KeyEvent evt) {
		final int keyCode = evt.getKeyCode();
		final char ch = evt.getKeyChar();
		switch (evt.getID()) {
			case KeyEvent.KEY_PRESSED:
				KeyEventWorkaround.lastKeyTime = evt.getWhen();
				switch (keyCode) {
					case KeyEvent.VK_DEAD_GRAVE:
					case KeyEvent.VK_DEAD_ACUTE:
					case KeyEvent.VK_DEAD_CIRCUMFLEX:
					case KeyEvent.VK_DEAD_TILDE:
					case KeyEvent.VK_DEAD_MACRON:
					case KeyEvent.VK_DEAD_BREVE:
					case KeyEvent.VK_DEAD_ABOVEDOT:
					case KeyEvent.VK_DEAD_DIAERESIS:
					case KeyEvent.VK_DEAD_ABOVERING:
					case KeyEvent.VK_DEAD_DOUBLEACUTE:
					case KeyEvent.VK_DEAD_CARON:
					case KeyEvent.VK_DEAD_CEDILLA:
					case KeyEvent.VK_DEAD_OGONEK:
					case KeyEvent.VK_DEAD_IOTA:
					case KeyEvent.VK_DEAD_VOICED_SOUND:
					case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
					case '\0':
						return null;
					case KeyEvent.VK_ALT:
						KeyEventWorkaround.modifiers |= InputEvent.ALT_MASK;
						return null;
					case KeyEvent.VK_ALT_GRAPH:
						KeyEventWorkaround.modifiers |= InputEvent.ALT_GRAPH_MASK;
						return null;
					case KeyEvent.VK_CONTROL:
						KeyEventWorkaround.modifiers |= InputEvent.CTRL_MASK;
						return null;
					case KeyEvent.VK_SHIFT:
						KeyEventWorkaround.modifiers |= InputEvent.SHIFT_MASK;
						return null;
					case KeyEvent.VK_META:
						KeyEventWorkaround.modifiers |= InputEvent.META_MASK;
						return null;
					default:
						if (!evt.isMetaDown()) {
							if (evt.isControlDown() && evt.isAltDown()) {
								KeyEventWorkaround.lastKeyTime = 0L;
							}
							else if (!evt.isControlDown() && !evt.isAltDown()) {
								KeyEventWorkaround.lastKeyTime = 0L;
								if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
									return null;
								}
								if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
									return null;
								}
							}
						}
						if (KeyEventWorkaround.ALT_KEY_PRESSED_DISABLED) {
							/* we don't handle key pressed A+ */
							/* they're too troublesome */
							if ((KeyEventWorkaround.modifiers & InputEvent.ALT_MASK) != 0) {
								return null;
							}
						}
						switch (keyCode) {
							case KeyEvent.VK_NUMPAD0:
							case KeyEvent.VK_NUMPAD1:
							case KeyEvent.VK_NUMPAD2:
							case KeyEvent.VK_NUMPAD3:
							case KeyEvent.VK_NUMPAD4:
							case KeyEvent.VK_NUMPAD5:
							case KeyEvent.VK_NUMPAD6:
							case KeyEvent.VK_NUMPAD7:
							case KeyEvent.VK_NUMPAD8:
							case KeyEvent.VK_NUMPAD9:
							case KeyEvent.VK_MULTIPLY:
							case KeyEvent.VK_ADD:
								/* case KeyEvent.VK_SEPARATOR: */
							case KeyEvent.VK_SUBTRACT:
							case KeyEvent.VK_DECIMAL:
							case KeyEvent.VK_DIVIDE:
								KeyEventWorkaround.last = KeyEventWorkaround.LAST_NUMKEYPAD;
								break;
							default:
								KeyEventWorkaround.last = KeyEventWorkaround.LAST_NOTHING;
								break;
						}
						return evt;
				}
			case KeyEvent.KEY_TYPED:
				if ((ch < 0x20 || ch == 0x7f || ch == 0xff) && ch != '\b' && ch != '\t' && ch != '\n') {
					return null;
				}
				if (evt.getWhen() - KeyEventWorkaround.lastKeyTime < 750) {
					if (!KeyEventWorkaround.ALTERNATIVE_DISPATCHER) {
						if (((KeyEventWorkaround.modifiers & InputEvent.CTRL_MASK) != 0 ^ (KeyEventWorkaround.modifiers & InputEvent.ALT_MASK) != 0)
						        || (KeyEventWorkaround.modifiers & InputEvent.META_MASK) != 0) {
							return null;
						}
					}
					if (KeyEventWorkaround.last == KeyEventWorkaround.LAST_NUMKEYPAD) {
						KeyEventWorkaround.last = KeyEventWorkaround.LAST_NOTHING;
						if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '/' || ch == '*' || ch == '-' || ch == '+') {
							return null;
						}
					}
					else if (KeyEventWorkaround.last == KeyEventWorkaround.LAST_ALT) {
						KeyEventWorkaround.last = KeyEventWorkaround.LAST_NOTHING;
						switch (ch) {
							case 'B':
							case 'M':
							case 'X':
							case 'c':
							case '!':
							case ',':
							case '?':
								return null;
						}
					}
				}
				else {
					if ((KeyEventWorkaround.modifiers & InputEvent.SHIFT_MASK) != 0) {
						switch (ch) {
							case '\n':
							case '\t':
								return null;
						}
					}
					KeyEventWorkaround.modifiers = 0;
				}
				return evt;
			case KeyEvent.KEY_RELEASED:
				switch (keyCode) {
					case KeyEvent.VK_ALT:
						KeyEventWorkaround.modifiers &= ~InputEvent.ALT_MASK;
						KeyEventWorkaround.lastKeyTime = evt.getWhen();
						evt.consume();
						return null;
					case KeyEvent.VK_ALT_GRAPH:
						KeyEventWorkaround.modifiers &= ~InputEvent.ALT_GRAPH_MASK;
						return null;
					case KeyEvent.VK_CONTROL:
						KeyEventWorkaround.modifiers &= ~InputEvent.CTRL_MASK;
						return null;
					case KeyEvent.VK_SHIFT:
						KeyEventWorkaround.modifiers &= ~InputEvent.SHIFT_MASK;
						return null;
					case KeyEvent.VK_META:
						KeyEventWorkaround.modifiers &= ~InputEvent.META_MASK;
						return null;
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_PAGE_UP:
					case KeyEvent.VK_PAGE_DOWN:
					case KeyEvent.VK_END:
					case KeyEvent.VK_HOME:
						/*
						 * workaround for A+keys producing garbage on Windows
						 */
						if (KeyEventWorkaround.modifiers == InputEvent.ALT_MASK) {
							KeyEventWorkaround.last = KeyEventWorkaround.LAST_ALT;
						}
						break;
				}
				return evt;
			default:
				return evt;
		}
	}
}
