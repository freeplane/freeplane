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
 *  Created on 24.12.2007
 */
package com.inet.jortho;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.WindowConstants;

/**
 * Implements edit dialog for the user dictionary.
 * @author Volker Berlin
 */
class DictionaryEditDialog extends JDialog {
	private class DeleteAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Delete the selected entries. The "Delete" Button it the only Listener.
		 */
		public void actionPerformed(final ActionEvent e) {
			final int[] selected = list.getSelectedIndices();
			Arrays.sort(selected);
			for (int i = selected.length - 1; i >= 0; i--) {
				((DefaultListModel) list.getModel()).remove(selected[i]);
				isModify = true;
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton delete;
	private boolean isModify;
	private final JList list;

	DictionaryEditDialog(final JDialog parent) {
		super(parent, Utils.getResource("userDictionary"), true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		final DefaultListModel data = new DefaultListModel();
		loadWordList(data);
		list = new JList(data);
		content.add(new JScrollPane(list), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
		    GridBagConstraints.BOTH, new Insets(8, 8, 8, 8), 0, 0));
		delete = new JButton(Utils.getResource("delete"));
		content.add(delete, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH,
		    GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
		final DeleteAction deleteAction = new DeleteAction();
		delete.addActionListener(deleteAction);
		// DELETE Key
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), "DELETE");
		getRootPane().getActionMap().put("DELETE", deleteAction);
		//ESCAPE Key
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent e) {
				dispose();
			}
		});
		pack();
		setLocationRelativeTo(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isModify) {
			final UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
			if (provider != null) {
				final ListModel model = list.getModel();
				final StringBuilder builder = new StringBuilder();
				for (int i = 0; i < model.getSize(); i++) {
					if (builder.length() != 0) {
						builder.append('\n');
					}
					builder.append(model.getElementAt(i));
				}
				provider.setUserWords(builder.toString());
			}
		}
	}

	/**
	 * A hack for the layout manger to prevent that the dialog is to small to show the title line. The problem occur
	 * only if there are small words in the list. With a empty list there are no problems.
	 */
	@Override
	public Dimension getPreferredSize() {
		final Dimension dim = super.getPreferredSize();
		final String title = getTitle();
		final int titleWidth = getFontMetrics(getFont()).stringWidth(title) + 80;
		if (dim.width < titleWidth) {
			dim.width = titleWidth;
		}
		return dim;
	}

	/**
	 * Load all words from the user dictionary if available
	 * @param data
	 */
	private void loadWordList(final DefaultListModel data) {
		try {
			final UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
			if (provider != null) {
				final String userWords = provider.getUserWords(SpellChecker.getCurrentLocale());
				if (userWords != null) {
					final BufferedReader input = new BufferedReader(new StringReader(userWords));
					final ArrayList<String> wordList = new ArrayList<String>();
					String word = input.readLine();
					while (word != null) {
						if (word.length() > 1) {
							wordList.add(word);
						}
						word = input.readLine();
					}
					// Liste alphabetical sorting with the user language
					Collections.sort(wordList, Collator.getInstance());
					for (final String str : wordList) {
						data.addElement(str);
					}
				}
			}
		}
		catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
}
