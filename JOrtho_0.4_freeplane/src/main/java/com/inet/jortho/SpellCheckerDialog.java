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
 *  Created on 10.11.2005
 */
package com.inet.jortho;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

/**
 * The Dialog for continues checking the orthography.
 * @author Volker Berlin
 */
class SpellCheckerDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private JButton addToDic = new JButton(Utils.getResource("addToDictionary"));
	final private JButton change = new JButton(Utils.getResource("change"));
	final private JButton changeAll = new JButton(Utils.getResource("changeAll"));
	/** Map of change all words */
	final private HashMap<String, String> changeWords = new HashMap<String, String>();
	final private JButton close = new JButton(Utils.getResource("close"));
	private Dictionary dictionary;
	final private JButton editDic = new JButton(Utils.getResource("editDictionary"));
	final private JButton ignore = new JButton(Utils.getResource("ignore"));
	final private JButton ignoreAll = new JButton(Utils.getResource("ignoreAll"));
	/** List of ignore all words */
	final private ArrayList<String> ignoreWords = new ArrayList<String>();
	private boolean isDictionaryModify;
	private JTextComponent jText;
	final private JLabel notFound = new JLabel();
	private final SpellCheckerOptions options;
	final private JList suggestionsList = new JList();
	private Tokenizer tok;
	final private JTextField word = new JTextField();

	SpellCheckerDialog(final Dialog owner) throws HeadlessException {
		this(owner, false, null);
	}

	SpellCheckerDialog(final Dialog owner, final boolean modal, final SpellCheckerOptions options) {
		super(owner, modal);
		this.options = options == null ? SpellChecker.getOptions() : options;
		init();
	}

	SpellCheckerDialog(final Frame owner) {
		this(owner, false, null);
	}

	SpellCheckerDialog(final Frame owner, final boolean modal, final SpellCheckerOptions options) {
		super(owner, modal);
		this.options = options == null ? SpellChecker.getOptions() : options;
		init();
	}

	public void actionPerformed(final ActionEvent ev) {
		final Object source = ev.getSource();
		if (source == ignore) {
			searchNext();
		}
		else if (source == close) {
			dispose();
		}
		else {
			final String newWord = word.getText();
			final String oldWord = notFound.getText();
			if (source == ignoreAll) {
				ignoreWords.add(oldWord);
				searchNext();
			}
			else if (source == addToDic) {
				final UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
				if (provider != null) {
					provider.addWord(oldWord);
				}
				dictionary.add(oldWord);
				dictionary.trimToSize();
				isDictionaryModify = true;
				searchNext();
			}
			else if (source == editDic) {
				new DictionaryEditDialog(this).setVisible(true);
			}
			else if (source == change) {
				replaceWord(oldWord, newWord);
				searchNext();
			}
			else if (source == changeAll) {
				changeWords.put(oldWord, newWord);
				replaceWord(oldWord, newWord);
				searchNext();
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (isDictionaryModify) {
			AutoSpellChecker.refresh(jText);
		}
	}

	final private void init() {
		try {
			final Image image = ImageIO.read(getClass().getResourceAsStream("icon.png"));
			// setIconImage appeared in Java 6.0 so use reflection to be compatible
			// with earlier JVMs. Equivalent to calling setIcomImage(image);
			final Class<Dialog> cls = Dialog.class;
			final java.lang.reflect.Method m = cls.getMethod("setIconImage", new Class[] { Image.class });
			m.invoke(this, new Object[] { image });
		}
		catch (final Throwable e1) {
			// can occur in Java 5 or if the icon was removed, then use the default
		}
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final Container cont = getContentPane();
		cont.setLayout(new GridBagLayout());
		final Insets insetL = new Insets(8, 8, 0, 8);
		final Insets insetR = new Insets(8, 0, 0, 8);
		cont.add(new JLabel(Utils.getResource("notInDictionary") + ":"), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
		    GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, insetL, 0, 0));
		notFound.setForeground(Color.RED);
		notFound.setText("xxxxxxxxxx");
		cont.add(notFound, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST,
		    GridBagConstraints.NONE, insetL, 0, 0));
		cont.add(word, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetL, 0, 0));
		cont.add(new JLabel(Utils.getResource("suggestions") + ":"), new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
		    GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, insetL, 0, 0));
		final JScrollPane scrollPane = new JScrollPane(suggestionsList);
		cont.add(scrollPane, new GridBagConstraints(1, 4, 2, 5, 1.0, 1.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.BOTH, new Insets(8, 8, 8, 8), 0, 0));
		cont.add(ignore, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(ignoreAll, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(addToDic, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(editDic, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(change, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(changeAll, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(close, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		cont.add(new JLabel(), new GridBagConstraints(3, 8, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
		    GridBagConstraints.HORIZONTAL, insetR, 0, 0));
		ignore.addActionListener(this);
		ignoreAll.addActionListener(this);
		addToDic.addActionListener(this);
		editDic.addActionListener(this);
		change.addActionListener(this);
		changeAll.addActionListener(this);
		close.addActionListener(this);
		//ESCAPE Taste
		close.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
		    "ESCAPE");
		close.getActionMap().put("ESCAPE", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		word.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(final DocumentEvent ev) {
				// disable "Add To Dictionary" if word was changed, not this word would added else the original misspelled word
				addToDic.setEnabled(false);
			}

			public void insertUpdate(final DocumentEvent ev) {
				// disable "Add To Dictionary" if word was changed, not this word would added else the original misspelled word
				addToDic.setEnabled(false);
			}

			public void removeUpdate(final DocumentEvent ev) {
				// disable "Add To Dictionary" if word was changed, not this word would added else the original misspelled word
				addToDic.setEnabled(false);
			}
		});
		suggestionsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent ev) {
				// Update the word field if a suggestion is click
				if (!ev.getValueIsAdjusting() && suggestionsList.getSelectedIndex() >= 0) {
					word.setText((String) suggestionsList.getSelectedValue());
					addToDic.setEnabled(true);
				}
			}
		});
		final boolean isUserDictionary = SpellChecker.getUserDictionaryProvider() != null;
		addToDic.setEnabled(isUserDictionary);
		editDic.setEnabled(isUserDictionary);
		pack();
	}

	private void replaceWord(final String oldWord, final String newWord) {
		jText.setSelectionStart(tok.getWordOffset());
		jText.setSelectionEnd(tok.getWordOffset() + oldWord.length());
		jText.replaceSelection(newWord);
		tok.updatePhrase();
	}

	/**
	 * Search the next misspelling word. If found it then refresh the dialog with the new information.
	 * ignoreWords and changeWords will handle automatically.
	 * @return true, if found a spell error.
	 */
	private boolean searchNext() {
		String wordStr;
		while (true) {
			wordStr = tok.nextInvalidWord();
			if (wordStr == null) {
				dispose();
				String title = SpellChecker.getApplicationName();
				if (title == null) {
					title = this.getTitle();
				}
				JOptionPane.showMessageDialog(getParent(), Utils.getResource("msgFinish"), title,
				    JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			if (ignoreWords.contains(wordStr)) {
				continue;
			}
			final String changeTo = changeWords.get(wordStr);
			if (changeTo != null) {
				replaceWord(wordStr, changeTo);
				continue;
			}
			break;
		}
		word.setText(wordStr);
		notFound.setText(wordStr);
		final List<Suggestion> list = dictionary.searchSuggestions(wordStr);
		final boolean needCapitalization = tok.isFirstWordInSentence() && Utils.isFirstCapitalized(wordStr);
		final Vector<String> suggestionsVector = new Vector<String>();
		for (int i = 0; i < list.size() && i < options.getSuggestionsLimitDialog(); i++) {
			final Suggestion sugestion = list.get(i);
			String newWord = sugestion.getWord();
			if (needCapitalization) {
				newWord = Utils.getCapitalized(newWord);
			}
			if (i == 0) {
				word.setText(newWord);
			}
			suggestionsVector.add(newWord);
		}
		suggestionsList.setListData(suggestionsVector);
		addToDic.setEnabled(true);
		return true;
	}

	public void show(final JTextComponent jTextComponent, final Dictionary dic, final Locale loc) {
		jText = jTextComponent;
		dictionary = dic;
		change.requestFocus();
		setTitle(Utils.getResource("spelling") + ": " + loc.getDisplayLanguage());
		tok = new Tokenizer(jTextComponent, dic, loc, options);
		if (searchNext()) {
			// if the JTextComponent is large and has a scrollpane then Java use the bounds
			// and not the visible rect. This is bad
			Container parent = jTextComponent;
			while (parent != null && !(parent instanceof JScrollPane)) {
				if (parent instanceof JComponent) {
					final JComponent jcomp = (JComponent) parent;
					if (jcomp.getVisibleRect().height == jcomp.getBounds().height) {
						break;
					}
				}
				if (parent.getParent() != null) {
					parent = parent.getParent();
				}
				else {
					break;
				}
			}
			setLocationRelativeTo(parent);
			setVisible(true);
		}
	}
}
