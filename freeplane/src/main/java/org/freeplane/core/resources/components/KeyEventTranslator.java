/*
  *  Freeplane - mind map editor
 *  Copyright (C) 2001, 2002 Slava Pestov
 *  Copyright (C) 2009 Dimitry Polivaev
 *
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
package org.freeplane.core.resources.components;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * In conjunction with the <code>KeyEventWorkaround</code>, hides some warts in
 * the AWT key event API.
 *
 */
class KeyEventTranslator {
	static class Key {
		final public char input;
		final public int key;
		final public String modifiers;

		public Key(final String modifiers, final int key, final char input) {
			this.modifiers = modifiers;
			this.key = key;
			this.input = input;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof Key) {
				final Key k = (Key) o;
				if ((modifiers.equals(k.modifiers)) && key == k.key && input == k.input) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return key + input;
		}

		@Override
		public String toString() {
			return (modifiers == null ? "" : modifiers) + "<" + Integer.toString(key, 16) + ","
			        + Integer.toString(input, 16) + ">";
		}
	}

	static int c, a, m, s;
	private static Map<Key, Key> transMap = new HashMap<Key, Key>();
	static {
		KeyEventTranslator.setModifierMapping(InputEvent.CTRL_MASK, InputEvent.ALT_MASK, InputEvent.META_MASK,
		    InputEvent.SHIFT_MASK);
	}

	/**
	 * Returns a string containing symbolic modifier names set in the specified
	 * event.
	 *
	 * @param evt
	 *            The event
	 * @since jEdit 4.2pre3
	 */
	public static String getModifierString(final InputEvent evt) {
		final StringBuilder buf = new StringBuilder();
		if (evt.isControlDown()) {
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.CTRL_MASK));
		}
		if (evt.isAltDown()) {
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.ALT_MASK));
		}
		if (evt.isMetaDown()) {
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.META_MASK));
		}
		if (evt.isShiftDown()) {
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.SHIFT_MASK));
		}
		return (buf.length() == 0 ? null : buf.toString());
	}

	/**
	 * Returns a the symbolic modifier name for the specified Java modifier
	 * flag.
	 *
	 * @param mod
	 *            A modifier constant from <code>InputEvent</code>
	 * @since jEdit 4.2pre3
	 */
	public static String getSymbolicModifierName(final int mod) {
		if ((mod & KeyEventTranslator.c) != 0) {
			return "control";
		}
		else if ((mod & KeyEventTranslator.a) != 0) {
			return "alt";
		}
		else if ((mod & KeyEventTranslator.m) != 0) {
			return "meta";
		}
		else if ((mod & KeyEventTranslator.s) != 0) {
			return "shift";
		}
		else {
			return "";
		}
	}

	public static String modifiersToString(final int mods) {
		StringBuilder buf = null;
		if ((mods & InputEvent.CTRL_MASK) != 0) {
			buf = new StringBuilder();
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.CTRL_MASK));
		}
		if ((mods & InputEvent.ALT_MASK) != 0) {
			if (buf == null) {
				buf = new StringBuilder();
			}
			else {
				buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
			}
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.ALT_MASK));
		}
		if ((mods & InputEvent.META_MASK) != 0) {
			if (buf == null) {
				buf = new StringBuilder();
			}
			else {
				buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
			}
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.META_MASK));
		}
		if ((mods & InputEvent.SHIFT_MASK) != 0) {
			if (buf == null) {
				buf = new StringBuilder();
			}
			else {
				buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
			}
			buf.append(KeyEventTranslator.getSymbolicModifierName(InputEvent.SHIFT_MASK));
		}
		if (buf == null) {
			return null;
		}
		else {
			return buf.toString();
		}
	}

	/**
	 * Changes the mapping between symbolic modifier key names (<code>C</code>,
	 * <code>A</code>, <code>M</code>, <code>S</code>) and Java modifier flags.
	 * You can map more than one Java modifier to a symbolic modifier, for
	 * example :
	 * <p>
	 * <code><pre>
	 * 	setModifierMapping(
	 * 		InputEvent.CTRL_MASK,
	 * 		InputEvent.ALT_MASK | InputEvent.META_MASK,
	 * 		0,
	 * 		InputEvent.SHIFT_MASK);
	 * <pre></code>
	 * </p>
	 * You cannot map a Java modifer to more than one symbolic modifier.
	 *
	 * @param c
	 *            The modifier(s) to map the <code>C</code> modifier to
	 * @param a
	 *            The modifier(s) to map the <code>A</code> modifier to
	 * @param m
	 *            The modifier(s) to map the <code>M</code> modifier to
	 * @param s
	 *            The modifier(s) to map the <code>S</code> modifier to
	 * @since jEdit 4.2pre3
	 */
	public static void setModifierMapping(final int c, final int a, final int m, final int s) {
		final int duplicateMapping = ((c & a) | (c & m) | (c & s) | (a & m) | (a & s) | (m & s));
		if ((duplicateMapping & InputEvent.CTRL_MASK) != 0) {
			throw new IllegalArgumentException("CTRL is mapped to more than one modifier");
		}
		if ((duplicateMapping & InputEvent.ALT_MASK) != 0) {
			throw new IllegalArgumentException("ALT is mapped to more than one modifier");
		}
		if ((duplicateMapping & InputEvent.META_MASK) != 0) {
			throw new IllegalArgumentException("META is mapped to more than one modifier");
		}
		if ((duplicateMapping & InputEvent.SHIFT_MASK) != 0) {
			throw new IllegalArgumentException("SHIFT is mapped to more than one modifier");
		}
		KeyEventTranslator.c = c;
		KeyEventTranslator.a = a;
		KeyEventTranslator.m = m;
		KeyEventTranslator.s = s;
	}

	/**
	 * Pass this an event from
	 * {@link KeyEventWorkaround#processKeyEvent(java.awt.event.KeyEvent)}.
	 *
	 * @since jEdit 4.2pre3
	 */
	public static Key translateKeyEvent(final KeyEvent evt) {
		final int modifiers = evt.getModifiers();
		Key returnValue = null;
		switch (evt.getID()) {
			case KeyEvent.KEY_PRESSED:
				final int keyCode = evt.getKeyCode();
				if ((keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)
				        || (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)) {
					returnValue = new Key(KeyEventTranslator.modifiersToString(modifiers), '\0', Character
					    .toUpperCase((char) keyCode));
				}
				else {
					if (keyCode > 0 && keyCode <= KeyEvent.VK_SPACE
							|| keyCode == KeyEvent.VK_DELETE) {
						evt.consume();
						returnValue = new Key(KeyEventTranslator.modifiersToString(modifiers), keyCode,
						    KeyEvent.CHAR_UNDEFINED);
					}
					else {
						returnValue = new Key(KeyEventTranslator.modifiersToString(modifiers), keyCode, evt
						    .getKeyChar());
					}
				}
				break;
			default:
				return null;
		}
		/*
		 * I guess translated events do not have the 'evt' field set so
		 * consuming won't work. I don't think this is a problem as nothing uses
		 * translation anyway
		 */
		final Key trans = KeyEventTranslator.transMap.get(returnValue);
		if (trans == null) {
			return returnValue;
		}
		else {
			return trans;
		}
	}

	public static final boolean ALT_KEY_PRESSED_DISABLED = false;
	static int modifiers;
}
