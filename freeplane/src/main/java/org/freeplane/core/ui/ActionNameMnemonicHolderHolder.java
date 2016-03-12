package org.freeplane.core.ui;

import java.awt.event.KeyEvent;

import javax.swing.Action;

class ActionNameMnemonicHolderHolder implements INameMnemonicHolder {
	final private Action action;

	public ActionNameMnemonicHolderHolder(final Action action) {
		super();
		this.action = action;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#getText()
	 */
	public String getText() {
		return (String) action.getValue(Action.NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
	 */
	public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
		action.putValue("SwingDisplayedMnemonicIndexKey", mnemoSignIndex);

	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#setMnemonic(char)
	 */
	public void setMnemonic(final char charAfterMnemoSign) {
		int vk = charAfterMnemoSign;
		action.putValue(Action.MNEMONIC_KEY,  KeyEvent.getExtendedKeyCodeForChar(vk));
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#setText(java.lang.String)
	 */
	public void setText(final String text) {
		action.putValue(Action.NAME, text);
	}
}