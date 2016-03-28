package org.freeplane.core.ui;

import javax.swing.AbstractButton;
import javax.swing.Action;

import org.dpolivaev.mnemonicsetter.ActionNameMnemonicHolder;
import org.dpolivaev.mnemonicsetter.ButtonNameMnemonicHolder;
import org.dpolivaev.mnemonicsetter.INameMnemonicHolder;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;

public class LabelAndMnemonicSetter {

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final AbstractButton btn, final String inLabel) {
		LabelAndMnemonicSetter.setLabelAndMnemonic(new ButtonNameMnemonicHolder(btn), inLabel);
	}

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final Action action, final String inLabel) {
		LabelAndMnemonicSetter.setLabelAndMnemonic(new ActionNameMnemonicHolder(action), inLabel);
	}

	static void setLabelAndMnemonic(final INameMnemonicHolder item, final String inLabel) {
		String rawLabel = inLabel;
		if (rawLabel == null) {
			rawLabel = item.getText();
		}
		if (rawLabel == null) {
			return;
		}
		item.setText(TextUtils.removeMnemonic(rawLabel));
		final int mnemoSignIndex = rawLabel.indexOf('&');
		if (mnemoSignIndex >= 0 && mnemoSignIndex + 1 < rawLabel.length()) {
			final char charAfterMnemoSign = rawLabel.charAt(mnemoSignIndex + 1);
			if (charAfterMnemoSign != ' ') {
				if (!Compat.isMacOsX()) {
					item.setMnemonic(charAfterMnemoSign);
					item.setDisplayedMnemonicIndex(mnemoSignIndex);
				}
			}
		}
	}

}
