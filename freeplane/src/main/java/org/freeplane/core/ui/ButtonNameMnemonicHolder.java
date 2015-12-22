package org.freeplane.core.ui;

import javax.swing.AbstractButton;

class ButtonNameMnemonicHolder implements INameMnemonicHolder {
	final private AbstractButton btn;

	public ButtonNameMnemonicHolder(final AbstractButton btn) {
		super();
		this.btn = btn;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#getText()
	 */
	public String getText() {
		return btn.getText();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
	 */
	public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
		btn.setDisplayedMnemonicIndex(mnemoSignIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#setMnemonic(char)
	 */
	public void setMnemonic(final char charAfterMnemoSign) {
		btn.setMnemonic(charAfterMnemoSign);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.main.Tools.IAbstractButton#setText(java.lang.String)
	 */
	public void setText(final String text) {
		btn.setText(text);
	}
}