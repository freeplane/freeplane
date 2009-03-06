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
 * Created on 25.02.2008
 */
package com.inet.jortho;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Is used from CheckerMenu and CheckerPopup to handle the user events.
 * @author Volker Berlin
 */
public class CheckerListener implements PopupMenuListener, LanguageChangeListener {
	private Dictionary dictionary;
	private Locale locale;
	private final JComponent menu;
	private final SpellCheckerOptions options;

	CheckerListener(final JComponent menu, final SpellCheckerOptions options) {
		this.menu = menu;
		this.options = options == null ? SpellChecker.getOptions() : options;
		SpellChecker.addLanguageChangeLister(this);
		dictionary = SpellChecker.getCurrentDictionary();
		locale = SpellChecker.getCurrentLocale();
	}

	public void languageChanged(final LanguageChangeEvent ev) {
		dictionary = SpellChecker.getCurrentDictionary();
		locale = SpellChecker.getCurrentLocale();
	}

	public void popupMenuCanceled(final PopupMenuEvent e) {
		/* empty */
	}

	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		/* empty */
	}

	public void popupMenuWillBecomeVisible(final PopupMenuEvent ev) {
		if (SpellChecker.getCurrentDictionary() == null) {
			menu.setEnabled(false);
			return;
		}
		final JPopupMenu popup = (JPopupMenu) ev.getSource();
		final Component invoker = popup.getInvoker();
		if (invoker instanceof JTextComponent) {
			final JTextComponent jText = (JTextComponent) invoker;
			if (!jText.isEditable()) {
				// Suggestions only for editable text components
				menu.setEnabled(false);
				return;
			}
			final Caret caret = jText.getCaret();
			int offs = Math.min(caret.getDot(), caret.getMark());
			final Point p = jText.getMousePosition();
			if (p != null) {
				// use position from mouse click and not from editor cursor position 
				offs = jText.viewToModel(p);
			}
			try {
				final Document doc = jText.getDocument();
				if (offs > 0 && (offs >= doc.getLength() || Character.isWhitespace(doc.getText(offs, 1).charAt(0)))) {
					// if the next character is a white space then use the word on the left site
					offs--;
				}
				if (offs < 0) {
					// occur if there nothing under the mouse pointer
					menu.setEnabled(false);
					return;
				}
				// get the word from current position
				final int begOffs = Utilities.getWordStart(jText, offs);
				final int endOffs = Utilities.getWordEnd(jText, offs);
				final String word = jText.getText(begOffs, endOffs - begOffs);
				//find the first invalid word from current position
				final Tokenizer tokenizer = new Tokenizer(jText, dictionary, locale, offs, options);
				String invalidWord;
				do {
					invalidWord = tokenizer.nextInvalidWord();
				} while (tokenizer.getWordOffset() < begOffs);
				menu.removeAll();
				if (!word.equals(invalidWord)) {
					// the current word is not invalid
					menu.setEnabled(false);
					return;
				}
				if (dictionary == null) {
					// without dictionary it is disabled
					menu.setEnabled(false);
					return;
				}
				final List<Suggestion> list = dictionary.searchSuggestions(word);
				//Disable then menu item if there are no suggestions
				menu.setEnabled(list.size() > 0);
				final boolean needCapitalization = tokenizer.isFirstWordInSentence() && Utils.isFirstCapitalized(word);
				for (int i = 0; i < list.size() && i < options.getSuggestionsLimitMenu(); i++) {
					final Suggestion sugestion = list.get(i);
					String sugestionWord = sugestion.getWord();
					if (needCapitalization) {
						sugestionWord = Utils.getCapitalized(sugestionWord);
					}
					final JMenuItem item = new JMenuItem(sugestionWord);
					menu.add(item);
					final String newWord = sugestionWord;
					item.addActionListener(new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							jText.setSelectionStart(begOffs);
							jText.setSelectionEnd(endOffs);
							jText.replaceSelection(newWord);
						}
					});
				}
				final UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
				if (provider == null) {
					return;
				}
				final JMenuItem addToDic = new JMenuItem(Utils.getResource("addToDictionary"));
				addToDic.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
						if (provider != null) {
							provider.addWord(word);
						}
						dictionary.add(word);
						dictionary.trimToSize();
						AutoSpellChecker.refresh(jText);
					}
				});
				if (list.size() > 0) {
					if (menu instanceof JMenu) {
						((JMenu) menu).addSeparator();
					}
					else if (menu instanceof JPopupMenu) {
						((JPopupMenu) menu).addSeparator();
					}
				}
				menu.add(addToDic);
				menu.setEnabled(true);
			}
			catch (final BadLocationException ex) {
				ex.printStackTrace();
			}
		}
	}
}
