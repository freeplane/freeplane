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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

/**
 * A dialog for getting shortcut keys.
 */
public class GrabKeyDialog extends JDialog {
	class ActionHandler implements ActionListener {
		public void actionPerformed(final ActionEvent evt) {
			if (evt.getSource() == ok) {
				if (shortcut.keyChar == null) {
					isOK = false;
					dispose();
				}
				else if (canClose(UITools.getKeyStroke(shortcut.getText()))) {
					isOK = true;
					dispose();
				}
			}
			else if (evt.getSource() == cancel) {
				dispose();
			}
			else if (evt.getSource() == clear) {
				shortcut.keyChar = KeyEvent.CHAR_UNDEFINED;
				shortcut.setText(null);
				shortcut.requestFocus();
			}
		}
	}

	class InputPane extends JTextField {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Makes the tab key work in Java 1.4.
		 * 
		 * @since jEdit 3.2pre4
		 */
		@Override
		public boolean getFocusTraversalKeysEnabled() {
			return false;
		}

		private int getModifierMask() {
			return modifierMask;
		}

		private Character keyChar = null;

		@Override
		protected void processKeyEvent(final KeyEvent evt) {
			if (KeyEvent.KEY_PRESSED != evt.getID()) {
				return;
			}
			if ((getModifierMask() & evt.getModifiers()) != 0) {
				final KeyEvent evt2 = new KeyEvent(evt.getComponent(), evt.getID(), evt.getWhen(), ~getModifierMask()
				        & evt.getModifiers(), evt.getKeyCode(), evt.getKeyChar(), evt.getKeyLocation());
				processKeyEvent(evt2);
				if (evt2.isConsumed()) {
					evt.consume();
				}
				return;
			}
			final int keyCode = evt.getKeyCode();
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
					return;
				case '\0':
					if (evt.getKeyChar() == KeyEvent.CHAR_UNDEFINED || evt.getKeyChar() == 0) {
						return;
					}
					break;
				case KeyEvent.VK_WINDOWS:
					if (Compat.isWindowsOS()) {
						return;
					}
					break;
				case KeyEvent.VK_ALT:
					KeyEventTranslator.modifiers |= InputEvent.ALT_MASK;
					return;
				case KeyEvent.VK_ALT_GRAPH:
					KeyEventTranslator.modifiers |= InputEvent.ALT_GRAPH_MASK;
					return;
				case KeyEvent.VK_CONTROL:
					KeyEventTranslator.modifiers |= InputEvent.CTRL_MASK;
					return;
				case KeyEvent.VK_SHIFT:
					KeyEventTranslator.modifiers |= InputEvent.SHIFT_MASK;
					return;
				case KeyEvent.VK_META:
					KeyEventTranslator.modifiers |= InputEvent.META_MASK;
					return;
				default:
					if (KeyEventTranslator.ALT_KEY_PRESSED_DISABLED) {
						/* we don't handle key pressed A+ */
						/* they're too troublesome */
						if ((KeyEventTranslator.modifiers & InputEvent.ALT_MASK) != 0) {
							return;
						}
					}
					break;
			}
			evt.consume();
			final KeyEventTranslator.Key key = KeyEventTranslator.translateKeyEvent(evt);
			if (key == null) {
				return;
			}
			keyChar = key.input;
			final StringBuilder keyString = new StringBuilder(/* getText() */);
			if (key.modifiers != null) {
				keyString.append(key.modifiers).append(' ');
			}
			if (key.input == ' ') {
				keyString.append("SPACE");
			}
			else if (key.key == 0) {
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
			updateAssignedTo(keyString.toString());
		}

		public Character getKeyChar() {
			return keyChar;
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
	public final static String MODIFIER_SEPARATOR = " ";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	private static String getText(final String resourceString) {
		return TextUtils.getText("GrabKeyDialog." + resourceString);
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

	private JLabel assignedTo;
	private JButton cancel;
	private JButton clear;
	private boolean isOK;
	private int modifierMask;
	private JButton ok;
	private InputPane shortcut;

	public Character getKeyChar() {
		return shortcut.getKeyChar();
	}

	private IKeystrokeValidator validator;

	public GrabKeyDialog(final String input, final int modifierMask) {
		super((Window) UITools.getMenuComponent(), ModalityType.APPLICATION_MODAL);
		setTitle(GrabKeyDialog.getText("grab-key.title"));
		init(input, modifierMask);
	}

	public GrabKeyDialog(final String input) {
		this(input, 0);
	}

	public boolean canClose(final KeyStroke ks) {
		return validator == null || validator.isValid(ks, getKeyChar());
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
			LogUtils.severe(e);
		}
		return null;
	}

	public IKeystrokeValidator getValidator() {
		return validator;
	}

	private void init(final String inputText, final int modifierMask) {
		this.modifierMask = modifierMask;
		enableEvents(AWTEvent.KEY_EVENT_MASK);
		final JPanel content = new JPanel(new GridLayout(0, 1, 0, 6)) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
		final Box input = Box.createHorizontalBox();
		shortcut = new InputPane();
		if (inputText != null) {
			shortcut.setText(inputText);
		}
		input.add(shortcut);
		input.add(Box.createHorizontalStrut(12));
		clear = new JButton((GrabKeyDialog.getText("grab-key.clear")));
		clear.addActionListener(new ActionHandler());
		input.add(clear);
		shortcut.setPreferredSize(new Dimension(200, clear.getPreferredSize().height));
		assignedTo = new JLabel();
		updateAssignedTo(null);
		final Box buttons = Box.createHorizontalBox();
		buttons.add(Box.createGlue());
		ok = new JButton(GrabKeyDialog.getText("common.ok"));
		ok.addActionListener(new ActionHandler());
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(12));
		cancel = new JButton(GrabKeyDialog.getText("common.cancel"));
		cancel.addActionListener(new ActionHandler());
		buttons.add(cancel);
		buttons.add(Box.createGlue());
		content.add(input);
		content.add(buttons);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(getParent());
		setResizable(false);
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

	public void setValidator(final IKeystrokeValidator validator) {
		this.validator = validator;
	}

	private void updateAssignedTo(final String shortcut) {
		final String text = (GrabKeyDialog.getText("grab-key.assigned-to.none"));
		if (ok != null) {
			ok.setEnabled(true);
		}
		assignedTo.setText((GrabKeyDialog.getText("grab-key.assigned-to") + " " + text));
	}
}
