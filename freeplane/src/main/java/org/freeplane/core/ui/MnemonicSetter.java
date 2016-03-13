package org.freeplane.core.ui;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class MnemonicSetter {
	private final INameMnemonicHolder[] mnemonicHolders;

	public MnemonicSetter(INameMnemonicHolder... mnemonicHolders) {
		this.mnemonicHolders = mnemonicHolders;
	}

	public void setMnemonics() {
		final Map<Integer, INameMnemonicHolder> usedMnemonics = extractUsedMnemonics(mnemonicHolders);
		if(usedMnemonics.size() < mnemonicHolders.length) {
			final Map<Integer, INameMnemonicHolder> mnemonicSet = findMnemonics(usedMnemonics, 0, 0);
			setMnemonics(mnemonicSet);
		}
	}

	private void setMnemonics(final Map<Integer, INameMnemonicHolder> setMnemonics) {
		for(Map.Entry<Integer, INameMnemonicHolder> holderMnemonic : setMnemonics.entrySet())
			holderMnemonic.getValue().setMnemonic(holderMnemonic.getKey());
	}

	private Map<Integer, INameMnemonicHolder> findMnemonics(final Map<Integer, INameMnemonicHolder> usedMnemonics, int holderIndex, int characterIndex) {
		if(holderIndex >= mnemonicHolders.length)
			return usedMnemonics;
		final INameMnemonicHolder holder = mnemonicHolders[holderIndex];
		final boolean holderHasNoMnemonics = ! usedMnemonics.containsKey(holder.getMnemonic());
		if(holderHasNoMnemonics) {
			final String text = holder.getText();
			if(text.length() > characterIndex) {
				return findUnsetMnemonics(usedMnemonics, holderIndex, characterIndex);
			}
		}
		return findMnemonics(usedMnemonics, holderIndex + 1, 0);
	}

	private Map<Integer, INameMnemonicHolder> findUnsetMnemonics(final Map<Integer, INameMnemonicHolder> usedMnemonics, int holderIndex, int characterIndex) {
		final INameMnemonicHolder holder = mnemonicHolders[holderIndex];
		final String text = holder.getText();
		final char character = text.charAt(characterIndex);
		final int keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
		if(! usedMnemonics.containsKey(keyCode)) {
			final Map<Integer, INameMnemonicHolder> mnemonicsWithNewCharacter = new HashMap<Integer, INameMnemonicHolder>(usedMnemonics);
			mnemonicsWithNewCharacter.put(keyCode, holder);
			final Map<Integer, INameMnemonicHolder> allMnemonics = findMnemonics(mnemonicsWithNewCharacter, holderIndex + 1, 0);
			if(allMnemonics.size() == mnemonicHolders.length) {
				return allMnemonics;
			} else {
				final Map<Integer, INameMnemonicHolder> newMnemonicsAlternative = findMnemonics(usedMnemonics, holderIndex, characterIndex + 1);
				final Map<Integer, INameMnemonicHolder> allMnemonicsAlternative = findMnemonics(newMnemonicsAlternative, holderIndex + 1, 0);
				return allMnemonics.size() >= allMnemonicsAlternative.size() ? allMnemonics : allMnemonicsAlternative;

			}
		} else {
			final Map<Integer, INameMnemonicHolder> allMnemonics = findMnemonics(usedMnemonics, holderIndex, characterIndex + 1);
			return findMnemonics(allMnemonics, holderIndex + 1, 0);
		}
	}
	private Map<Integer, INameMnemonicHolder> extractUsedMnemonics(INameMnemonicHolder... mnemonicHolders) {
		final Map<Integer, INameMnemonicHolder> usedMnemonics = new HashMap<Integer, INameMnemonicHolder>();
		for(INameMnemonicHolder holder : mnemonicHolders){
			final int mnemonic = holder.getMnemonic();
			if(mnemonic != 0) {
				usedMnemonics.put(mnemonic, holder);
			}
		}
		return usedMnemonics;
	}

}
