/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.features.url.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;

/**
 * @author vboerchers
 */
class FileRevisionsDialog extends JDialog {
	public static class FileWrapper {
		private final File file;

		public FileWrapper(final File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}

		@Override
		public String toString() {
			return file.getName();
		}
	}

	private class RevisionTable extends JTable {
		private static final long serialVersionUID = 1L;

		public RevisionTable(final Object[][] data, int selectedRow) {
			super(data, new Object[] { TextUtils.getText(key("file_name")),
			        TextUtils.getText(key("file_size")),
			        TextUtils.getText(key("file_last_modified")) });
			int width = Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3;
			getColumnModel().getColumn(0).setPreferredWidth((int) (width * 0.7));
			getColumnModel().getColumn(1).setPreferredWidth((int) (width * 0.1));
			getColumnModel().getColumn(2).setPreferredWidth((int) (width * 0.2));
			setRowHeight(20);
			setRowSelectionAllowed(true);
			setFocusable(false);
			setDefaultRenderer(Object.class, renderer);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setRowSelectionInterval(selectedRow, selectedRow);
			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent event) {
					// Update the word field if a suggestion is click
					if (!event.getValueIsAdjusting()) {
						final ListSelectionModel lsm = (ListSelectionModel) event.getSource();
						final boolean enable = !(lsm.isSelectionEmpty());
						if (enable) {
							final FileWrapper fileWrapper = (FileWrapper) getModel().getValueAt(getSelectedRow(), 0);
							if (file.equals(fileWrapper.getFile())) {
								setButtonOpenDefault();
							}
							else {
								setButtonOpenRestore(fileWrapper);
							}
							setSelectedFile(fileWrapper.getFile());
						}
						else {
							setButtonOpenDefault();
						}
					}
				}
			});
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() >= 2) {
						final FileWrapper fileWrapper = (FileWrapper) getModel().getValueAt(getSelectedRow(), 0);
						setSelectedFile(fileWrapper.getFile());
						if (fileWrapper != null)
							btnRestore.doClick();
					}
				}
			});
		}

		private void setButtonOpenDefault() {
			LabelAndMnemonicSetter.setLabelAndMnemonic(btnRestore, TextUtils.getRawText(key("open")));
			btnRestore.setToolTipText(TextUtils.format(key("open.tooltip")));
		}

		private void setButtonOpenRestore(final FileWrapper fileWrapper) {
			LabelAndMnemonicSetter.setLabelAndMnemonic(btnRestore, TextUtils.getRawText(key("restore")));
			btnRestore.setToolTipText(TextUtils.format(key("restore.tooltip"),
			    file.getName(), fileWrapper.toString()));
		}
		private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
			                                               final boolean isSelected, final boolean hasFocus,
			                                               final int row, final int column) {
				final Component c = super
				    .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				// change the font of the first line which contains the original file
				if (containsOriginalFile(table, row))
					c.setFont(getFont().deriveFont(Font.BOLD));
				return c;
			}

			private boolean containsOriginalFile(JTable table, int row) {
				FileWrapper fileHolder = (FileWrapper) table.getValueAt(row, 0);
				return fileHolder.getFile().equals(file);
            }
		};

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}

	}

	private static final long serialVersionUID = 1L;
	private final static String ALL_KEY_BASE = "FileRevisionsDialog";
	private final static String AUTOSAVE_KEY_BASE = "NewerFileRevisionsFoundDialog";
	private String keyBase;
	private JButton btnRestore;
	private JButton btnSkip;
	private boolean cancelled;
	private final File file;
	private File selectedFile;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat();
	private final NumberFormat fileSizeFormat = NumberFormat.getIntegerInstance();

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			cancelled = (source == btnSkip);
			dispose();
		}
	}


	@SuppressWarnings("serial")
	private class EscapeAction extends AbstractAction {
		public void actionPerformed(final ActionEvent e) {
			cancelled = true;
			dispose();
		}
	}
	
	public FileRevisionsDialog(final File file, final File[] revisions, AlternativeFileMode mode) {
		super((Frame) UITools.getMenuComponent(), true);
		if(mode == AlternativeFileMode.ALL)
			keyBase = ALL_KEY_BASE;
		else if(mode == AlternativeFileMode.AUTOSAVE)
			keyBase = AUTOSAVE_KEY_BASE;
		setTitle(TextUtils.getText(key("title")));
		UITools.backOtherWindows();
		this.selectedFile = this.file = file;
		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		final JTable table = createTable(revisions);
		final JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.white);
		final Dimension tablePreferredSize = table.getPreferredSize();
		int maxHeight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 2 / 3;
		scrollPane.getViewport().setPreferredSize(new Dimension(tablePreferredSize.width, Math.min(maxHeight, tablePreferredSize.height)));
		add(scrollPane);
		add(createQuestion());
		add(createButtonBar());
		getRootPane().setDefaultButton(btnRestore);
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		getRootPane().getActionMap().put("up", new AbstractAction() {
			
			public void actionPerformed(ActionEvent e) {
				int newSelectedRow = table.getSelectedRow() - 1;
				if(newSelectedRow >= 0)
					table.setRowSelectionInterval(newSelectedRow, newSelectedRow);
				
			}
		});
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		getRootPane().getActionMap().put("down", new AbstractAction() {
			
			public void actionPerformed(ActionEvent e) {
				int newSelectedRow = table.getSelectedRow() + 1;
				if(newSelectedRow < table.getRowCount())
					table.setRowSelectionInterval(newSelectedRow, newSelectedRow);
			}
		});
		UITools.addEscapeActionToDialog(this, new EscapeAction());

		pack();
		UITools.showFrame();
		setLocationRelativeTo(UITools.getMenuComponent());
		setVisible(true);
	}

	private Component createQuestion() {
		final String text = TextUtils.format(key("question"), file.getName());
		final String html = HtmlUtils.plainToHTML(text);
		final JLabel textArea = new JLabel(html);
		textArea.setAlignmentX(0.5f);
		textArea.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return textArea;
	}

	private String key(final String appendix) {
		return keyBase + "." + appendix;
	}

	private JTable createTable(final File[] revisions) {
		final TreeSet<File> sortedRevisions = new TreeSet<File>(new Comparator<File>() {
			public int compare(final File f1, final File f2) {
				final long diff = f1.lastModified() - f2.lastModified();
				if (diff == 0)
					return f1.getName().compareTo(f2.getName());
				return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
			}
		});
		sortedRevisions.add(file);
		sortedRevisions.addAll(Arrays.asList(revisions));
		final Object[][] data = new Object[sortedRevisions.size()][];
		int i = 0;
		int selectedRow = 0;
		for (final File f : sortedRevisions) {
			data[i] = createRow(f);
			if(f == file)
				selectedRow = i;
			i++;
		}
		final RevisionTable revisionTable = new RevisionTable(data, selectedRow);
		return revisionTable;
	}

	private Object[] createRow(final File file) {
		return new Object[] { new FileWrapper(file), fileSizeFormat.format(file.length()),
		        dateFormat.format(file.lastModified()) };
	}

	private Box createButtonBar() {
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		final CloseAction closeAction = new CloseAction();
		controllerBox.add(Box.createHorizontalGlue());
		btnSkip = createButton(key("cancel"), null, closeAction);
		btnRestore = createButton(key("open"), key("open.tooltip"), closeAction);
		controllerBox.add(btnRestore);
		controllerBox.add(Box.createHorizontalStrut(10));
		controllerBox.add(btnSkip);
		controllerBox.add(Box.createHorizontalStrut(10));
		return controllerBox;
	}

	private JButton createButton(final String key, final String tooltipKey, final ActionListener closeAction) {
		final JButton button = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(button, TextUtils.getRawText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		final String selectedFileName = getSelectedFile() == null ? null : getSelectedFile().getName();
		// arguments are only used for one button but they don't hurt for the other
		if (tooltipKey != null)
			button.setToolTipText(TextUtils.format(tooltipKey, file.getName(), selectedFileName));
		return button;
	}
	
	/** returns null on cancel */
	public File getSelectedFile() {
		if(cancelled)
			return null;
		return selectedFile;
	}

	private void setSelectedFile(final File selectedFile) {
		this.selectedFile = selectedFile;
	}
}
