/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.attribute.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.TypedListCellRenderer;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.collection.IListModel;

class ListDialog extends JDialog {
	private class AddAction implements ActionListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(final ActionEvent e) {
			data.add(getCurrentText());
			addButton.setEnabled(false);
			selectText();
		}
	}

	private class CloseAction implements ActionListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(final ActionEvent e) {
			ListDialog.dialog.dispose();
		}
	}

	private class DeleteAction implements ActionListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = list.getSelectedValues();
			for (int i = 0; i < selectedValues.length; i++) {
				data.remove(selectedValues[i]);
			}
			if (data.getSize() == 0) {
				data.add("");
			}
			list.clearSelection();
		}
	}

	final private class ListSelectionChangeListener implements ListSelectionListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
		 * event.ListSelectionEvent)
		 */
		public void valueChanged(final ListSelectionEvent e) {
			final int minIndex = list.getMinSelectionIndex();
			final int maxIndex = list.getMaxSelectionIndex();
			if (minIndex == maxIndex && minIndex != -1) {
				textField.setText(data.getElementAt(minIndex).toString());
				selectText();
			}
			updateButtons();
		}
	}

	private class RenameAction implements ActionListener {
		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		public void actionPerformed(final ActionEvent e) {
			final Object[] selectedValues = list.getSelectedValues();
			for (int i = 0; i < selectedValues.length; i++) {
				if (!selectedValues[i].equals(getCurrentText())) {
					data.replace(selectedValues[i], getCurrentText());
				}
			}
			renameButton.setEnabled(false);
			list.clearSelection();
			selectText();
		}
	}

	final private class TextChangeListener implements DocumentListener {
		public void changedUpdate(final DocumentEvent e) {
			update();
		}

		public void insertUpdate(final DocumentEvent e) {
			update();
		}

		public void removeUpdate(final DocumentEvent e) {
			update();
		}

		private void update() {
			updateButtons();
		}
	}

	private static ListDialog dialog;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void showDialog(final Component frameComp, final Component locationComp, final String labelText,
	                              final String title, final IListModel possibleValues, final String longValue) {
		final Window window = SwingUtilities.getWindowAncestor(frameComp);
		if(window instanceof Frame)
			ListDialog.dialog = new ListDialog((Frame)window, locationComp, labelText, title, possibleValues, longValue);
		else if(window instanceof Dialog)
			ListDialog.dialog = new ListDialog((Dialog )window, locationComp, labelText, title, possibleValues, longValue);
		else{
			final Frame frame = JOptionPane.getFrameForComponent(frameComp);
			ListDialog.dialog = new ListDialog(frame, locationComp, labelText, title, possibleValues, longValue);
		}
		UITools.addEscapeActionToDialog(ListDialog.dialog);
		ListDialog.dialog.show();
	}

	private JButton addButton;
	private IListModel data = null;
	private JButton deleteButton;
	private JList list;
	private JButton renameButton;
	private JTextField textField;

	private ListDialog(final Frame frame, final Component locationComp, final String labelText, final String title,
	                   final IListModel data, final String longValue) {
		super(frame, title, true);
		init(locationComp, labelText, data, longValue);
	}

	private ListDialog(final Dialog frame, final Component locationComp, final String labelText, final String title,
	                   final IListModel data, final String longValue) {
		super(frame, title, true);
		init(locationComp, labelText, data, longValue);
	}

	public void init(final Component locationComp, final String labelText, final IListModel data, final String longValue) {
	    this.data = data;
		final JButton closeButton = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(closeButton, TextUtils.getRawText("simplyhtml.closeBtnName"));
		closeButton.addActionListener(new CloseAction());
		getRootPane().setDefaultButton(closeButton);
		addButton = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(addButton, TextUtils.getRawText("add"));
		final AddAction addAction = new AddAction();
		addButton.addActionListener(addAction);
		renameButton = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(renameButton, TextUtils.getRawText("rename"));
		renameButton.addActionListener(new RenameAction());
		deleteButton = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(deleteButton, TextUtils.getRawText("delete"));
		deleteButton.addActionListener(new DeleteAction());
		textField = new JTextField(20);
		textField.getDocument().addDocumentListener(new TextChangeListener());
		list = new JList(data) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation,
			                                      final int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0 && (row = getFirstVisibleIndex()) != -1) {
					final Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0)) {
						final Point loc = r.getLocation();
						loc.y--;
						final int prevIndex = locationToIndex(loc);
						final Rectangle prevR = getCellBounds(prevIndex, prevIndex);
						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
			}
		};
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		if (longValue != null) {
			list.setPrototypeCellValue(longValue);
		}
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					addButton.doClick();
				}
			}
		});
		list.setCellRenderer(new TypedListCellRenderer());
		list.setModel(data);
		list.addListSelectionListener(new ListSelectionChangeListener());
		final JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		final JLabel label = new JLabel(labelText);
		label.setLabelFor(list);
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0, 5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(closeButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(addButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(renameButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(deleteButton);
		final JPanel textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.LINE_AXIS));
		textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		textPane.add(textField);
		final Container contentPane = getContentPane();
		contentPane.add(textPane, BorderLayout.PAGE_START);
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		updateButtons();
		pack();
		setLocationRelativeTo(locationComp);
    }

	private String getCurrentText() {
		final Document document = textField.getDocument();
		try {
			final String text = document.getText(0, document.getLength());
			return text;
		}
		catch (final BadLocationException e) {
			LogUtils.severe(e);
			return "";
		}
	}

	private int getIndexOf(final String text) {
		for (int i = 0; i < data.getSize(); i++) {
			if (data.getElementAt(i).toString().equals(text)) {
				return i;
			}
		}
		return -1;
	}

	private void selectText() {
		textField.requestFocusInWindow();
		textField.select(0, textField.getDocument().getLength());
	}

	/**
	 *
	 */
	private void updateButtons() {
		final String text = getCurrentText();
		final boolean isNewText = -1 == getIndexOf(text);
		addButton.setEnabled(isNewText);
		final int minSelectionIndex = list.getMinSelectionIndex();
		renameButton.setEnabled(minSelectionIndex != -1);
		deleteButton.setEnabled(minSelectionIndex != -1);
	}
}
