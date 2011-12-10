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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;

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
			super(data, new Object[] { TextUtils.getText(FileRevisionsDialog.key("file_name")),
			        TextUtils.getText(FileRevisionsDialog.key("file_size")),
			        TextUtils.getText(FileRevisionsDialog.key("file_last_modified")) });
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
	private final static String KEY_BASE = "FileRevisionsDialog";
	private JButton btnRestore;
	private final File file;
	private File selectedFile;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat();
	private final NumberFormat fileSizeFormat = NumberFormat.getIntegerInstance();

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			dispose();
		}
	}

	public FileRevisionsDialog(final File file, final File[] revisions) {
		super(UITools.getFrame(), TextUtils.getText(FileRevisionsDialog.key("title")), true);
		UITools.backOtherWindows();
		this.file = file;
		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		final JTable table = createTable(revisions);
		final JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.getViewport().setPreferredSize(table.getPreferredSize());
		add(scrollPane);
		add(createQuestion());
		add(createButtonBar());
		getRootPane().setDefaultButton(btnRestore);
		pack();
		setLocationRelativeTo(UITools.getFrame());
		setVisible(true);
	}

	private Component createQuestion() {
		final String text = TextUtils.format(FileRevisionsDialog.key("question"), file.getName());
		final String html = HtmlUtils.plainToHTML(text);
		final JLabel textArea = new JLabel(html);
		textArea.setAlignmentX(0.5f);
		textArea.setFont(new Font("Dialog", Font.BOLD, 12));
		textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return textArea;
	}

	private static String key(final String appendix) {
		return KEY_BASE + "." + appendix;
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
		btnRestore = createButton(FileRevisionsDialog.key("restore"),
	    FileRevisionsDialog.key("restore.tooltip"), closeAction);
		controllerBox.add(btnRestore);
		return controllerBox;
	}

	private JButton createButton(final String key, final String tooltipKey, final ActionListener closeAction) {
		final JButton button = new JButton();
		MenuBuilder.setLabelAndMnemonic(button, TextUtils.getRawText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		final String selectedFileName = getSelectedFile() == null ? null : getSelectedFile().getName();
		// arguments are only used for one button but they don't hurt for the other
		if (tooltipKey != null)
			button.setToolTipText(TextUtils.format(tooltipKey, file.getName(), selectedFileName));
		return button;
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	private void setSelectedFile(final File selectedFile) {
		this.selectedFile = selectedFile;
	}
}
