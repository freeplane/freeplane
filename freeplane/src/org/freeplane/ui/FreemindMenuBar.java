/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.ui;

import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;

/**
 * This is the menu bar for FreeMind. Actions are defined in MenuListener.
 * Moreover, the StructuredMenuHolder of all menus are hold here.
 */
public class FreemindMenuBar extends JMenuBar {
	private static class ActionHolder implements INameMnemonicHolder {
		final private Action action;

		public ActionHolder(final Action action) {
			super();
			this.action = action;
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return (String) action.getValue(Action.NAME);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setMnemonic(char)
		 */
		public void setMnemonic(final char charAfterMnemoSign) {
			int vk = charAfterMnemoSign;
			if (vk >= 'a' && vk <= 'z') {
				vk -= ('a' - 'A');
			}
			action.putValue(Action.MNEMONIC_KEY, new Integer(vk));
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setText(java.lang.String)
		 */
		public void setText(final String text) {
			action.putValue(Action.NAME, text);
		}
	}

	public static class ButtonHolder implements INameMnemonicHolder {
		final private AbstractButton btn;

		public ButtonHolder(final AbstractButton btn) {
			super();
			this.btn = btn;
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return btn.getText();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
			btn.setDisplayedMnemonicIndex(mnemoSignIndex);
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setMnemonic(char)
		 */
		public void setMnemonic(final char charAfterMnemoSign) {
			btn.setMnemonic(charAfterMnemoSign);
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setText(java.lang.String)
		 */
		public void setText(final String text) {
			btn.setText(text);
		}
	}

	interface INameMnemonicHolder {
		/**
		 */
		String getText();

		/**
		 */
		void setDisplayedMnemonicIndex(int mnemoSignIndex);

		/**
		 */
		void setMnemonic(char charAfterMnemoSign);

		/**
		 */
		void setText(String replaceAll);
	}

	public static final String EDIT_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/edit";
	public static final String EXTRAS_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/extras";
	public static final String FILE_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/file";
	public static final String FORMAT_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/format";
	public static final String HELP_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/help";
	public static final String INSERT_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/insert";
	public static final String MAP_POPUP_MENU = "/map_popup";
	public static final String MENU_BAR_PREFIX = "/menu_bar";
	public static final String MINDMAP_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/mindmaps";
	public static final String MODES_MENU = FreemindMenuBar.MINDMAP_MENU;
	public static final String NAVIGATE_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/navigate";
	public static final String VIEW_MENU = FreemindMenuBar.MENU_BAR_PREFIX
	        + "/view";

	static public JMenu createMenu(final String name) {
		final JMenu menu = new JMenu();
		final String text = Freeplane.getText(name);
		FreemindMenuBar.setLabelAndMnemonic(menu, text);
		return menu;
	}

	static public JMenuItem createMenuItem(final String name) {
		final JMenuItem menu = new JMenuItem();
		final String text = Freeplane.getText(name);
		FreemindMenuBar.setLabelAndMnemonic(menu, text);
		return menu;
	}

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final AbstractButton btn,
	                                       final String inLabel) {
		FreemindMenuBar.setLabelAndMnemonic(new ButtonHolder(btn), inLabel);
	}

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final Action action,
	                                       final String inLabel) {
		FreemindMenuBar.setLabelAndMnemonic(new ActionHolder(action), inLabel);
	}

	private static void setLabelAndMnemonic(final INameMnemonicHolder item,
	                                        final String inLabel) {
		String rawLabel = inLabel;
		if (rawLabel == null) {
			rawLabel = item.getText();
		}
		if (rawLabel == null) {
			return;
		}
		item.setText(Tools.removeMnemonic(rawLabel));
		final int mnemoSignIndex = rawLabel.indexOf("&");
		if (mnemoSignIndex >= 0 && mnemoSignIndex + 1 < rawLabel.length()) {
			final char charAfterMnemoSign = rawLabel.charAt(mnemoSignIndex + 1);
			if (charAfterMnemoSign != ' ') {
				if (!Tools.isMacOsX()) {
					item.setMnemonic(charAfterMnemoSign);
					item.setDisplayedMnemonicIndex(mnemoSignIndex);
				}
			}
		}
	}

	public FreemindMenuBar() {
	}

	// make method public
	@Override
	public boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,
	                                 final int condition, final boolean pressed) {
		return super.processKeyBinding(ks, e, condition, pressed);
	}
}
