package org.freeplane.core.ui;

public interface INameMnemonicHolder {
	String getText();

	void setDisplayedMnemonicIndex(int mnemoSignIndex);

	void setMnemonic(char charAfterMnemoSign);

	void setText(String replaceAll);
	
	int getMnemonic();

	void setMnemonic(int key);
	
	boolean hasAccelerator();
}