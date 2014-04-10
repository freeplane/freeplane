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
 *  Created on 05.11.2005
 */
package com.inet.jortho;

import java.awt.EventQueue;
import java.util.Locale;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.Highlight;

/**
 * This class check a <code>JTextComponent</code> automatically (in the background) for orthography. Spell error are
 * highlighted with a red zigzag line.
 * 
 * @author Volker Berlin
 */
class AutoSpellChecker implements DocumentListener, LanguageChangeListener {
	private static final RedZigZagPainter painter = new RedZigZagPainter();

	/**
	 * Remove the AutoSpellChecker from the given JTextComponent.
	 * 
	 * @param text
	 *            the JTextComponent
	 */
	static void disable(final JTextComponent text) {
		final AbstractDocument doc = (AbstractDocument) text.getDocument();
		for (final DocumentListener listener : doc.getDocumentListeners()) {
			if (listener instanceof AutoSpellChecker) {
				final AutoSpellChecker autoSpell = (AutoSpellChecker) listener;
				doc.removeDocumentListener(autoSpell);
				AutoSpellChecker.removeHighlights(text);
			}
		}
	}

	/**
	 * Refresh the highlighting. This can be useful if the dictionary was modify.
	 * 
	 * @param text
	 *            the JTextComponent
	 */
	static void refresh(final JTextComponent text) {
		final AbstractDocument doc = (AbstractDocument) text.getDocument();
		for (final DocumentListener listener : doc.getDocumentListeners()) {
			if (listener instanceof AutoSpellChecker) {
				final AutoSpellChecker autoSpell = (AutoSpellChecker) listener;
				autoSpell.checkAll();
			}
		}
	}

	private static void removeHighlights(final JTextComponent text) {
		final Highlighter highlighter = text.getHighlighter();
		for (final Highlight highlight : highlighter.getHighlights()) {
			if (highlight.getPainter() == painter) {
				highlighter.removeHighlight(highlight);
			}
		}
	}

	private Dictionary dictionary;
	private final JTextComponent jText;
	private Locale locale;
	private final SpellCheckerOptions options;

	public AutoSpellChecker(final JTextComponent text, final SpellCheckerOptions options) {
		jText = text;
		this.options = options == null ? SpellChecker.getOptions() : options;
		jText.getDocument().addDocumentListener(this);
		SpellChecker.addLanguageChangeLister(this);
		dictionary = SpellChecker.getCurrentDictionary();
		locale = SpellChecker.getCurrentLocale();
		checkAll();
	}

	/*====================================================================
	 * 
	 * Methods of interface DocumentListener
	 * 
	 *===================================================================*/
	/**
	 * {@inheritDoc}
	 */
	public void changedUpdate(final DocumentEvent ev) {
		//Nothing
	}

	/**
	//     * Check the completely text. Because this can consume many times with large Documents that this will do in a thread
	 * in the background step by step.
	 */
	private void checkAll() {
		if (jText == null) {
			//the needed objects does not exists
			return;
		}
		if (dictionary == null) {
			AutoSpellChecker.removeHighlights(jText);
			return;
		}
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				final Document document = jText.getDocument();
				for (int i = 0; i < document.getLength();) {
					try {
						final Element element = ((AbstractDocument) document).getParagraphElement(i);
						i = element.getEndOffset();
						checkElement(element);
					}
					catch (final java.lang.Exception ex) {
						return;
					}
				}
			}
		}, "JOrtho checkall");
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Check the spelling of the text of an element.
	 * 
	 * @param element
	 *            the to checking Element
	 */
	private void checkElement(final javax.swing.text.Element element) {
		try {
			if(! EventQueue.isDispatchThread()){
				try {
	                EventQueue.invokeAndWait(new Runnable() {
	                	public void run() {
	                		checkElement(element);
	                		return;
	                	}
	                });
                }
                catch (Exception e) {
	                e.printStackTrace();
                }
			}
			final int i = element.getStartOffset();
			final int l = ((AbstractDocument) jText.getDocument()).getLength();
			final int j = Math.min(element.getEndOffset(), l);
			if (i >= j) {
				return;
			}
			// prevent a NPE if the dictionary is currently not loaded.
			final Dictionary dic = dictionary;
			final Locale loc = locale;
			if (dic == null || loc == null) {
				return;
			}
			final Tokenizer tok = new Tokenizer(jText, dic, loc, i, j, options);
			String word;
			final Highlighter highlighter = jText.getHighlighter();
			while ((word = tok.nextInvalidWord()) != null) {
				final int wordOffset = tok.getWordOffset();
				highlighter.addHighlight(wordOffset, wordOffset + word.length(), painter);
			}
		}
		catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void removeHighlighters(final javax.swing.text.Element element) {
	    {
	    	final int i = element.getStartOffset();
	    	final int j = element.getEndOffset();
	    	final Highlighter highlighter = jText.getHighlighter();
	    	final Highlight[] highlights = highlighter.getHighlights();
	    	for (int k = highlights.length; --k >= 0;) {
	    		final Highlight highlight = highlights[k];
	    		final int hlStartOffset = highlight.getStartOffset();
	    		final int hlEndOffset = highlight.getEndOffset();
	    		if ((i <= hlStartOffset && hlStartOffset <= j) || (i <= hlEndOffset && hlEndOffset <= j)) {
	    			if (highlight.getPainter() == painter) {
	    				highlighter.removeHighlight(highlight);
	    			}
	    		}
	    	}
	    }
    }

	/**
	 * Check the Elements on the given position.
	 */
	private void checkElements(int offset, final int length) {
		final int end = offset + length;
		final Document document = jText.getDocument();
		Element element;
		do {
			try {
				// We need to use a ParagraphElement because a CharacterElement produce problems with formating in a word
				element = ((AbstractDocument) document).getParagraphElement(offset);
			}
			catch (final java.lang.Exception ex) {
				return;
			}
			removeHighlighters(element);
			checkElement(element);
			offset = element.getEndOffset();
		} while (offset <= end && offset < document.getLength());
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertUpdate(final DocumentEvent ev) {
		checkElements(ev.getOffset(), ev.getLength());
	}

	/**
	 * {@inheritDoc}
	 */
	public void languageChanged(final LanguageChangeEvent ev) {
		dictionary = SpellChecker.getCurrentDictionary();
		locale = SpellChecker.getCurrentLocale();
		checkAll();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeUpdate(final DocumentEvent ev) {
		checkElements(ev.getOffset(), 0);
	}
}
