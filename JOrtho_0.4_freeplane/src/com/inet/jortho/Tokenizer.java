/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  Created on 07.11.2005
 */
package com.inet.jortho;

import java.text.BreakIterator;
import java.util.Locale;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Break the text and words and search for misspelling.
 * @author Volker Berlin
 */
public class Tokenizer {
	private final Dictionary dictionary;
	private final Document doc;
	/** end offset of current paragraph */
	private int endOffset;
	private boolean isFirstWordInSentence;
	private final SpellCheckerOptions options;
	/** start offset of current paragraph */
	private int paragraphOffset;
	private String phrase;
	private String sentence;
	private final BreakIterator sentences;
	private int startSentence, endSentence, startWord, endWord;
	private int wordOffset;
	private final LetterBasedBreakIterator words;

	/**
	* Create a tokenizer for the selected range.
	*/
	Tokenizer(final JTextComponent jText, final Dictionary dictionary, final Locale locale, final int startOffset,
	          final int endOffset, final SpellCheckerOptions options) {
		this.dictionary = dictionary;
		doc = jText.getDocument();
		this.options = options == null ? SpellChecker.getOptions() : options;
		sentences = BreakIterator.getSentenceInstance(locale);
		words = new LetterBasedBreakIterator();
		paragraphOffset = startOffset;
		this.endOffset = endOffset;
		//loadSentences();
		setSentencesText();
		endSentence = sentences.first();
		endWord = BreakIterator.DONE;
	}

	/**
	 * Create a Tokenizer for the current paragraph
	 * @param jText the checking JTextComponent
	 * @param dictionary the used Dictionary
	 * @param locale the used Locale, is needed for the word and sentence breaker
	 * @param offset the current offset.
	 */
	Tokenizer(final JTextComponent jText, final Dictionary dictionary, final Locale locale, final int offset,
	          final SpellCheckerOptions options) {
		this(jText, dictionary, locale, Utilities.getParagraphElement(jText, offset).getStartOffset(), Utilities
		    .getParagraphElement(jText, offset).getEndOffset(), options);
	}

	/**
	 * Create a tokenizer for the completely text document. 
	 */
	Tokenizer(final JTextComponent jText, final Dictionary dictionary, final Locale locale,
	          final SpellCheckerOptions options) {
		this(jText, dictionary, locale, 0, jText.getDocument().getLength(), options);
	}

	/**
	 * Get start offset of the last misspelling in the JTextComponent.
	 */
	int getWordOffset() {
		return paragraphOffset + wordOffset;
	}

	/**
	 * Was the last invalid word the first word in a sentence.
	 * 
	 * @return true if it was the first word.
	 */
	boolean isFirstWordInSentence() {
		return isFirstWordInSentence;
	}

	/**
	 * Check if the word is a web address. This means a email address or web page address.
	 * 
	 * @param word
	 *            the word that should be check. It can not be null and can not include any whitespace.
	 * @return true if it is a web address.
	 */
	private boolean isWebAddress(String word) {
		if (startWord + 2 >= sentence.length()) {
			return false;
		}
		final char char0 = sentence.charAt(startWord);
        final char char1 = sentence.charAt(startWord + 1);
        if (char0 == '@' && (Character.isLetter(char1) ||Character.isDigit(char1))
		    || startWord + 3 < sentence.length() && char0 == ':'
		        && char1 == '/' && sentence.charAt(startWord + 2) == '/') {
			while (startWord < endWord) {
				final String next = sentence.substring(startWord, endWord).trim();
				if (next.length() > 0) {
					word += next;
					startWord = endWord;
					endWord = words.next();
				}
				else {
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Loads the sentences of the current paragraph.
	 */
	private void loadSentences() {
		setSentencesText();
		startSentence = sentences.first();
		endSentence = sentences.next();
		nextSentence();
	}

	/**
	 * Get the next misspelling word. If not found then it return null.
	 */
	public String nextInvalidWord() {
		isFirstWordInSentence = false;
		while (true) {
			if (endWord == BreakIterator.DONE) {
				startSentence = endSentence;
				endSentence = sentences.next();
				if (endSentence == BreakIterator.DONE) {
					if (!nextParagraph()) {
						return null;
					}
				}
				else {
					nextSentence();
				}
			}
			while (endWord != BreakIterator.DONE) {
				final String word = sentence.substring(startWord, endWord).trim();
				wordOffset = startSentence + startWord;
				startWord = endWord;
				endWord = words.next();
				//only words with 2 or more characters are checked
				if (word.length() > 1 && Character.isLetter(word.charAt(0))) {
					boolean exist = dictionary.exist(word);
					if (!exist && !options.isCaseSensitive()) {
						exist = dictionary.exist(Utils.getInvertedCapitalizion(word));
					}
					else if (!exist && (isFirstWordInSentence || options.getIgnoreCapitalization())
					        && Character.isUpperCase(word.charAt(0))) {
						// Uppercase check on starting of sentence
						final String capitalizeWord = word.substring(0, 1).toLowerCase() + word.substring(1);
						exist = dictionary.exist(capitalizeWord);
					}
					if (!exist && options.isIgnoreAllCapsWords() && Utils.isAllCapitalized(word)) {
						exist = true;
					}
					if (!exist && options.isIgnoreWordsWithNumbers() && Utils.isIncludeNumbers(word)) {
						exist = true;
					}
					if (!exist && !isWebAddress(word)) {
						return word;
					}
					isFirstWordInSentence = false;
				}
			}
		}
	}

	/**
	 * Initialize the variables for the next paragraph.
	 * @return true, if there is a next paragraph
	 */
	private boolean nextParagraph() {
		if (doc instanceof AbstractDocument) {
			paragraphOffset = ((AbstractDocument) doc).getParagraphElement(paragraphOffset).getEndOffset();
			if (paragraphOffset >= endOffset) {
				return false;
			}
		}
		else {
			return false;
		}
		loadSentences();
		return true;
	}

	/**
	 * Load the next Sentence in the word breaker.
	 */
	private void nextSentence() {
		sentence = phrase.substring(startSentence, endSentence);
		words.setText(sentence);
		startWord = words.first();
		endWord = words.next();
		isFirstWordInSentence = true;
	}

	/**
	 * Call sentences.setText( String ) based on the current value of paragraphOffset.
	 */
	private void setSentencesText() {
		int end = endOffset;
		if (doc instanceof AbstractDocument) {
			end = ((AbstractDocument) doc).getParagraphElement(paragraphOffset).getEndOffset();
		}
		try {
			phrase = doc.getText(paragraphOffset, end - paragraphOffset);
		}
		catch (final BadLocationException e) {
			e.printStackTrace();
		}
		sentences.setText(phrase);
	}

	/**
	 * Update the text after a word was replaced. The changes in the text should be only after the current word offset.
	 */
	void updatePhrase() {
		endOffset = doc.getLength();
		setSentencesText();
		endSentence = sentences.following(startSentence);
		sentence = phrase.substring(startSentence, endSentence);
		words.setText(sentence);
		startWord = words.following(wordOffset);
		endWord = words.next();
	}
}
