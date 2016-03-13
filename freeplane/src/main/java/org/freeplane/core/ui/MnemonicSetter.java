package org.freeplane.core.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;

public class MnemonicSetter {
	private final INameMnemonicHolder[] mnemonicHolders;

	private MnemonicSetter(INameMnemonicHolder[] mnemonicHolders) {
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
			return findMnemonics(usedMnemonics, holderIndex + 1, 0);
		}
		return findMnemonics(usedMnemonics, holderIndex + 1, 0);
	}

	private Map<Integer, INameMnemonicHolder> findUnsetMnemonics(final Map<Integer, INameMnemonicHolder> usedMnemonics, int holderIndex, int characterIndex) {
		final INameMnemonicHolder holder = mnemonicHolders[holderIndex];
		final String text = holder.getText();
		final char character = text.charAt(characterIndex);
		final int keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
		if(! usedMnemonics.containsKey(keyCode) && Character.isAlphabetic(character)
				&& text.toLowerCase().indexOf(Character.toLowerCase(character)) == characterIndex) {
			final Map<Integer, INameMnemonicHolder> mnemonicsWithNewCharacter = new MnemonicMap(usedMnemonics);
			mnemonicsWithNewCharacter.put(keyCode, holder);
			final Map<Integer, INameMnemonicHolder> allMnemonics = findMnemonics(mnemonicsWithNewCharacter, holderIndex + 1, 0);
			return allMnemonics;
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
		
		Arrays.sort(mnemonicHolders, new Comparator<INameMnemonicHolder>() {
			@Override
			public int compare(INameMnemonicHolder o1, INameMnemonicHolder o2) {
				boolean o1HasMnemonics = o1.getMnemonic() > 0;
				boolean o2HasMnemonics = o2.getMnemonic() > 0;
				if(o1HasMnemonics)
					if(o2HasMnemonics)
						return 0;
					else
						return -1;
				else
					if(o2HasMnemonics)
						return 1;
					else {
						boolean o1HasAccelerator = o1. hasAccelerator();
						boolean o2HasAccelerator = o2. hasAccelerator();
						if(o1HasAccelerator && ! o2HasAccelerator)
								return 1;
						else if(! o1HasAccelerator && o2HasAccelerator)
								return -1;
						else
							return o1.getText().length() - o2.getText().length();
					}
			}
		});
		
		return usedMnemonics;
	}

	final static private Pattern CAN_HAVE_MNEMONICS = Pattern.compile("\\p{L}");
	
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

@SuppressWarnings("serial")
class MnemonicMap extends LinkedHashMap<Integer, INameMnemonicHolder> {

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

