package org.freeplane.core.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.freeplane.core.util.Compat;

public class MnemonicSetter {
	
	static class MnemonicMap extends LinkedHashMap<Integer, INameMnemonicHolder> {

		public MnemonicMap() {
			super();
		}

		public MnemonicMap(Map<? extends Integer, ? extends INameMnemonicHolder> m) {
			super(m);
		}

		@Override
		public String toString() {
			final StringBuilder stringBuilder = new StringBuilder("MnemonicMap"
					 + "(" + size() + ") {");
			for(Map.Entry<Integer, INameMnemonicHolder> entry : entrySet()){
				stringBuilder
				.append(entry.getValue().getText())
				.append(" -> ")
				.append(KeyEvent.getKeyText(entry.getKey()))
				.append("\n");
			}
			return stringBuilder.append("}").toString();
		}
		
		
		
	}
	private final INameMnemonicHolder[] mnemonicHolders;

	private MnemonicSetter(INameMnemonicHolder[] mnemonicHolders) {
		this.mnemonicHolders = mnemonicHolders;
	}

	public void setMnemonics() {
		if(Compat.isMacOsX())
			return; // Mac OS generally does not support mnemonics
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
		if(! (usedMnemonics.containsKey(keyCode) 
				|| character == '_' || Character.isSpaceChar(character) 
				|| text.toLowerCase().indexOf(Character.toLowerCase(character)) < characterIndex)) {
			final Map<Integer, INameMnemonicHolder> mnemonicsWithNewCharacter = new MnemonicMap(usedMnemonics);
			mnemonicsWithNewCharacter.put(keyCode, holder);
			final Map<Integer, INameMnemonicHolder> allMnemonics = findMnemonics(mnemonicsWithNewCharacter, holderIndex + 1, 0);
			if(allMnemonics.size() == mnemonicHolders.length) {
				return allMnemonics;
			} else {
				final Map<Integer, INameMnemonicHolder> allMnemonicsAlternative = findMnemonics(usedMnemonics, holderIndex, characterIndex + 1);
				return allMnemonics.size() >= allMnemonicsAlternative.size() ? allMnemonics : allMnemonicsAlternative;

			}
		} else {
			final Map<Integer, INameMnemonicHolder> allMnemonics = findMnemonics(usedMnemonics, holderIndex, characterIndex + 1);
			return allMnemonics;
		}
	}
	private Map<Integer, INameMnemonicHolder> extractUsedMnemonics(INameMnemonicHolder... mnemonicHolders) {
		final Map<Integer, INameMnemonicHolder> usedMnemonics = new MnemonicMap();
		for(INameMnemonicHolder holder : mnemonicHolders){
			final Integer mnemonic = holder.getMnemonic();
			if(mnemonic != 0) {
				if(! usedMnemonics.containsKey(mnemonic))
					usedMnemonics.put(mnemonic, holder);
				else {
					holder.setMnemonic(0);
					holder.setDisplayedMnemonicIndex(-1);
				}
			}
		}
		return usedMnemonics;
	}

	final static private Pattern CAN_HAVE_MNEMONICS = Pattern.compile("[^\\s_]");
	
	public static MnemonicSetter of(INameMnemonicHolder... mnemonicHolders) {
		return of(Arrays.asList(mnemonicHolders));
	}
	
	public static MnemonicSetter of(List<INameMnemonicHolder> mnemonicHolders) {
		final ArrayList<INameMnemonicHolder> validHolders = new ArrayList<INameMnemonicHolder>(mnemonicHolders.size());
		for(INameMnemonicHolder holder :mnemonicHolders)
			if(CAN_HAVE_MNEMONICS.matcher(holder.getText()).find())
				validHolders.add(holder);
		final INameMnemonicHolder[] array = validHolders.toArray(new INameMnemonicHolder[validHolders.size()]);
		return new MnemonicSetter(array);
	}

}
