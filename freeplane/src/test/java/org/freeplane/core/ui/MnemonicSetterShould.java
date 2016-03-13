package org.freeplane.core.ui;

import static org.junit.Assert.assertThat;

import java.awt.event.KeyEvent;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class MnemonicSetterShould {
	static class MnemonicHolder implements INameMnemonicHolder{

		private String text;
		private int keyCode;

		public MnemonicHolder(String text, char character) {
			super();
			setText(text);
			setMnemonic(character);
		}

		public MnemonicHolder(String text) {
			setText(text);
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public void setDisplayedMnemonicIndex(int mnemoSignIndex) {
		}

		@Override
		public void setMnemonic(char character) {
			final int keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
			setMnemonic(keyCode);
		}

		@Override
		public void setMnemonic(final int keyCode) {
			this.keyCode = keyCode;
		}

		@Override
		public void setText(String text) {
			this.text = text;
		}

		@Override
		public int getMnemonic() {
			return keyCode;
		}

		@Override
		public boolean hasAccelerator() {
			return false;
		}

	}

	private void assertMnemonic(final MnemonicHolder mnemonicHolder, char character) {
		final int keyCode = character != 0 ? KeyEvent.getExtendedKeyCodeForChar(character) : 0;
		final int mnemonic = mnemonicHolder.getMnemonic();
		if(keyCode == 0)
			assertThat(mnemonic, CoreMatchers.equalTo(keyCode));
		else {
			final String mnemonicCharacter = KeyEvent.getKeyText(mnemonic);
			final String expectedCharacter = KeyEvent.getKeyText(keyCode);
			assertThat(mnemonicCharacter, CoreMatchers.equalTo(expectedCharacter));
		}
	}


	@Test
	public void assignsFirstLetter() throws Exception {
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder("A");
		MnemonicSetter.of(mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderA, 'A');
	}


	@Test
	public void skipsSpaces() throws Exception {
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder(" A");
		MnemonicSetter.of(mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderA, 'A');
	}
	
	@Test
	public void assignsSecondLetter_IfFirstLetterIsAlreadyAssigned() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("AB");
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder("A", 'A');
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderA, 'A');
		assertMnemonic(mnemonicHolderAB, 'B');
	}


	@Test
	public void assignsSecondLetter_IfFirstLetterLowerCaseIsAlreadyAssigned() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("ab");
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder("A", 'A');
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderA, 'A');
		assertMnemonic(mnemonicHolderAB, 'B');
	}
	
	@Test
	public void assignsFirstLetter_ifItIsNotAssignedYet() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("AB");
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder("C");
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderAB, 'A');
	}
	
	@Test
	public void assignsFirstLetter_toSecondHolder() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("AB");
		final MnemonicHolder mnemonicHolderC = new MnemonicHolder("C");
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderC).setMnemonics();
		assertMnemonic(mnemonicHolderAB, 'A');
		assertMnemonic(mnemonicHolderC, 'C');
	}
	
	@Test
	public void assignsSecondLetter_toSecondHolder() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("AB");
		final MnemonicHolder mnemonicHolderAC = new MnemonicHolder("AC");
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderAC).setMnemonics();
		assertMnemonic(mnemonicHolderAB, 'A');
		assertMnemonic(mnemonicHolderAC, 'C');
	}
	
	@Test
	public void assignsNoLetter_ifAllLettersAreAlreadyAssigned() throws Exception {
		final MnemonicHolder mnemonicHolderA1 = new MnemonicHolder("a");
		final MnemonicHolder mnemonicHolderA2 = new MnemonicHolder("a", 'A');
		MnemonicSetter.of(mnemonicHolderA1, mnemonicHolderA2).setMnemonics();
		assertMnemonic(mnemonicHolderA1, '\0');
		assertMnemonic(mnemonicHolderA2, 'A');
	}
	
	@Test
	public void assignsSecondLetter_toFirstHolder() throws Exception {
		final MnemonicHolder mnemonicHolderAB = new MnemonicHolder("AB");
		final MnemonicHolder mnemonicHolderA = new MnemonicHolder("A");
		MnemonicSetter.of(mnemonicHolderAB, mnemonicHolderA).setMnemonics();
		assertMnemonic(mnemonicHolderAB, 'B');
		assertMnemonic(mnemonicHolderA, 'A');
	}

	@Test
	public void assignsNoLetter_toSecondHolder() throws Exception {
		final MnemonicHolder mnemonicHolderA1 = new MnemonicHolder("a");
		final MnemonicHolder mnemonicHolderA2 = new MnemonicHolder("a");
		MnemonicSetter.of(mnemonicHolderA1, mnemonicHolderA2).setMnemonics();
		assertMnemonic(mnemonicHolderA1, 'A');
		assertMnemonic(mnemonicHolderA2, '\0');
	}
}
