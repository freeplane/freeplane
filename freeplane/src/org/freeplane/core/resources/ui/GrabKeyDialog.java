/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2001, 2002 Slava Pestov
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

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.resources.FreeplaneResourceBundle;

/**
 * A dialog for getting shortcut keys.
 */
public class GrabKeyDialog extends JDialog {
	class ActionHandler implements ActionListener {
		public void actionPerformed(final ActionEvent evt) {
			if (evt.getSource() == ok) {
				if (canClose()) {
					dispose();
				}
			}
			else if (evt.getSource() == remove) {
				shortcut.setText(null);
				isOK = true;
				dispose();
			}
			else if (evt.getSource() == cancel) {
				dispose();
			}
			else if (evt.getSource() == clear) {
				shortcut.setText(null);
				if (debugBuffer == null) {
					updateAssignedTo(null);
				}
				shortcut.requestFocus();
			}
		}

		private boolean canClose() {
			final String shortcutString = shortcut.getText();
			if (shortcutString.length() == 0 && binding.isAssigned()) {
				final int answer = JOptionPane.showConfirmDialog(GrabKeyDialog.this, getText("grab-key.remove-ask"),
				    null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					shortcut.setText(null);
					isOK = true;
				}
				else {
					return false;
				}
			}
			final KeyBinding other = getKeyBinding(shortcutString);
			if (other == null || other == binding) {
				isOK = true;
				return true;
			}
			if (other.name == binding.name) {
				JOptionPane.showMessageDialog(GrabKeyDialog.this, getText("grab-key.duplicate-alt-shortcut"));
				return false;
			}
			if (other.isPrefix) {
				JOptionPane.showMessageDialog(GrabKeyDialog.this, getText("grab-key.prefix-shortcut"));
				return false;
			}
			final int answer = JOptionPane.showConfirmDialog(GrabKeyDialog.this, getText("grab-key.duplicate-shortcut")
			        + new Object[] { other.label }, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				if (other.shortcut != null && shortcutString.startsWith(other.shortcut)) {
					other.shortcut = null;
				}
				isOK = true;
				return true;
			}
			else {
				return false;
			}
		}
	}

	private static class Buffer {
		/**
		 */
		public int getLength() {
			return 0;
		}

		/**
		 */
		public void insert(final int length, final String string) {
		}
	}

	class InputPane extends JTextField {
		/**
		 * Makes the tab key work in Java 1.4.
		 * 
		 * @since jEdit 3.2pre4
		 */
		@Override
		public boolean getFocusTraversalKeysEnabled() {
			return false;
		}

		@Override
		protected void processKeyEvent(final KeyEvent _evt) {
			if ((getModifierMask() & _evt.getModifiers()) != 0) {
				final KeyEvent evt = new KeyEvent(_evt.getComponent(), _evt.getID(), _evt.getWhen(), ~getModifierMask()
				        & _evt.getModifiers(), _evt.getKeyCode(), _evt.getKeyChar(), _evt.getKeyLocation());
				processKeyEvent(evt);
				if (evt.isConsumed()) {
					_evt.consume();
				}
				return;
			}
			final KeyEvent evt = KeyEventWorkaround.processKeyEvent(_evt);
			if (debugBuffer != null) {
				debugBuffer.insert(debugBuffer.getLength(), "Event " + GrabKeyDialog.toString(_evt)
				        + (evt == null ? " filtered\n" : " passed\n"));
			}
			if (evt == null) {
				return;
			}
			evt.consume();
			final KeyEventTranslator.Key key = KeyEventTranslator.translateKeyEvent(evt);
			if (key == null) {
				return;
			}
			if (debugBuffer != null) {
				debugBuffer.insert(debugBuffer.getLength(), "==> Translated to " + key + "\n");
			}
			final StringBuffer keyString = new StringBuffer(/* getText() */);
			if (key.modifiers != null) {
				keyString.append(key.modifiers).append(' ');
			}
			if (key.input == ' ') {
				keyString.append("SPACE");
			}
			else if (key.input != '\0') {
				keyString.append(key.input);
			}
			else {
				final String symbolicName = getSymbolicName(key.key);
				if (symbolicName == null) {
					return;
				}
				keyString.append(symbolicName);
			}
			setText(keyString.toString());
			if (debugBuffer == null) {
				updateAssignedTo(keyString.toString());
			}
		}
	}

	/**
	 * Create and show a new modal dialog.
	 * 
	 * @param parent
	 *            center dialog on this component.
	 * @param binding
	 *            the action/macro that should get a binding.
	 * @param allBindings
	 *            all other key bindings.
	 * @param debugBuffer
	 *            debug info will be dumped to this buffer (may be null)
	 * @since jEdit 4.1pre7
	 */
	/**
	 * A jEdit action or macro with its two possible shortcuts.
	 * 
	 * @since jEdit 3.2pre8
	 */
	public static class KeyBinding {
		public boolean isPrefix;
		public String label;
		public String name;
		public String shortcut;

		public KeyBinding(final String name, final String label, final String shortcut, final boolean isPrefix) {
			this.name = name;
			this.label = label;
			this.shortcut = shortcut;
			this.isPrefix = isPrefix;
		}

		public boolean isAssigned() {
			return shortcut != null && shortcut.length() > 0;
		}
	}

	public final static String MODIFIER_SEPARATOR = " ";

	/**
	 */
	public static boolean isMacOS() {
		return false;
	}

	public static String toString(final KeyEvent evt) {
		String id;
		switch (evt.getID()) {
			case KeyEvent.KEY_PRESSED:
				id = "KEY_PRESSED";
				break;
			case KeyEvent.KEY_RELEASED:
				id = "KEY_RELEASED";
				break;
			case KeyEvent.KEY_TYPED:
				id = "KEY_TYPED";
				break;
			default:
				id = "unknown type";
				break;
		}
		return id + ",keyCode=0x" + Integer.toString(evt.getKeyCode(), 16) + ",keyChar=0x"
		        + Integer.toString(evt.getKeyChar(), 16) + ",modifiers=0x" + Integer.toString(evt.getModifiers(), 16);
	}

	private Vector allBindings;
	private JLabel assignedTo;
	private KeyBinding binding;
	private JButton cancel;
	private JButton clear;
	private Buffer debugBuffer;
	private boolean isOK;
	final private int modifierMask;
	private JButton ok;
	private JButton remove;
	private InputPane shortcut;

	public GrabKeyDialog(final Dialog parent, final KeyBinding binding, final Vector allBindings,
	                     final Buffer debugBuffer, final int modifierMask) {
		super(parent, "grab-key.title", true);
		this.modifierMask = modifierMask;
		setTitle(getText("grab-key.title"));
		init(binding, allBindings, debugBuffer);
	}

	/**
	 * Makes the tab key work in Java 1.4.
	 * 
	 * @since jEdit 3.2pre4
	 */
	@Override
	public boolean getFocusTraversalKeysEnabled() {
		return false;
	}

	private KeyBinding getKeyBinding(final String shortcut) {
		if (shortcut == null || shortcut.length() == 0) {
			return null;
		}
		final String spacedShortcut = shortcut + " ";
		final Enumeration e = allBindings.elements();
		while (e.hasMoreElements()) {
			final KeyBinding kb = (KeyBinding) e.nextElement();
			if (!kb.isAssigned()) {
				continue;
			}
			final String spacedKbShortcut = kb.shortcut + " ";
			if (spacedShortcut.startsWith(spacedKbShortcut)) {
				return kb;
			}
			if (spacedKbShortcut.startsWith(spacedShortcut)) {
				return new KeyBinding(kb.name, kb.label, shortcut, true);
			}
		}
		return null;
	}

	private int getModifierMask() {
		return modifierMask;
	}

	/**
	 * Returns the shortcut, or null if the current shortcut should be removed
	 * or the dialog either has been cancelled. Use isOK() to determine if the
	 * latter is true.
	 */
	public String getShortcut() {
		if (isOK) {
			return shortcut.getText();
		}
		else {
			return null;
		}
	}

	private String getSymbolicName(final int keyCode) {
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return null;
			/*
			 * else if(keyCode == KeyEvent.VK_OPEN_BRACKET) return "["; else
			 * if(keyCode == KeyEvent.VK_CLOSE_BRACKET) return "]";
			 */
		}
		if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
			return String.valueOf(Character.toLowerCase((char) keyCode));
		}
		try {
			final Field[] fields = KeyEvent.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				final Field field = fields[i];
				final String name = field.getName();
				if (name.startsWith("VK_") && field.getInt(null) == keyCode) {
					return name.substring(3);
				}
			}
		}
		catch (final Exception e) {
		}
		return null;
	}

	/**
	 */
	private String getText(final String resourceString) {
		return FreeplaneResourceBundle.getByKey("GrabKeyDialog." + resourceString);
	}

	private void init(final KeyBinding binding, final Vector allBindings, final Buffer debugBuffer) {
		this.binding = binding;
		this.allBindings = allBindings;
		this.debugBuffer = debugBuffer;
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		final JPanel content = new JPanel(new GridLayout(0, 1, 0, 6)) {
			/**
			 * Makes the tab key work in Java 1.4.
			 * 
			 * @since jEdit 3.2pre4
			 */
			@Override
			public boolean getFocusTraversalKeysEnabled() {
				return false;
			}

			/**
			 * Returns if this component can be traversed by pressing the Tab
			 * key. This returns false.
			 */
			@Override
			public boolean isManagingFocus() {
				return false;
			}
		};
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);
		new JLabel(debugBuffer == null ? (getText("grab-key.caption") + " " + binding.label)
		        : (getText("grab-key.keyboard-test")));
		final Box input = Box.createHorizontalBox();
		shortcut = new InputPane();
		input.add(shortcut);
		input.add(Box.createHorizontalStrut(12));
		clear = new JButton((getText("grab-key.clear")));
		clear.addActionListener(new ActionHandler());
		input.add(clear);
		assignedTo = new JLabel();
		if (debugBuffer == null) {
			updateAssignedTo(null);
		}
		final Box buttons = Box.createHorizontalBox();
		buttons.add(Box.createGlue());
		if (debugBuffer == null) {
			ok = new JButton(getText("common.ok"));
			ok.addActionListener(new ActionHandler());
			buttons.add(ok);
			buttons.add(Box.createHorizontalStrut(12));
			if (binding.isAssigned()) {
				remove = new JButton((getText("grab-key.remove")));
				remove.addActionListener(new ActionHandler());
				buttons.add(Box.createHorizontalStrut(12));
			}
		}
		cancel = new JButton(getText("common.cancel"));
		cancel.addActionListener(new ActionHandler());
		buttons.add(cancel);
		buttons.add(Box.createGlue());
		content.add(input);
		content.add(buttons);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(getParent());
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Returns if this component can be traversed by pressing the Tab key. This
	 * returns false.
	 */
	public boolean isManagingFocus() {
		return false;
	}

	/**
	 * Returns true, if the dialog has not been cancelled.
	 * 
	 * @since jEdit 3.2pre9
	 */
	public boolean isOK() {
		return isOK;
	}

	@Override
	protected void processKeyEvent(final KeyEvent evt) {
		shortcut.processKeyEvent(evt);
	}

	private void updateAssignedTo(final String shortcut) {
		String text = (getText("grab-key.assigned-to.none"));
		final KeyBinding kb = getKeyBinding(shortcut);
		if (kb != null) {
			if (kb.isPrefix) {
				text = getText("grab-key.assigned-to.prefix") + " " + shortcut;
			}
			else {
				text = kb.label;
			}
		}
		if (ok != null) {
			ok.setEnabled(kb == null || !kb.isPrefix);
		}
		assignedTo.setText((getText("grab-key.assigned-to") + " " + text));
	}
}
