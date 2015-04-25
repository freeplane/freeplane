package org.freeplane.core.ui;

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