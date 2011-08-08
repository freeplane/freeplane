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
import java.awt.Window;
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
public class NewerFileRevisionsFoundDialog extends JDialog {
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

		public RevisionTable(final Object[][] data) {
			super(data, new Object[] { TextUtils.getText(NewerFileRevisionsFoundDialog.key("file_name")),
			        TextUtils.getText(NewerFileRevisionsFoundDialog.key("file_size")),
			        TextUtils.getText(NewerFileRevisionsFoundDialog.key("file_last_modified")) });
			final Dimension dim = this.getPreferredSize();
			getColumnModel().getColumn(0).setPreferredWidth((int) (dim.width * 0.62));
			getColumnModel().getColumn(1).setPreferredWidth((int) (dim.width * 0.13));
			getColumnModel().getColumn(2).setPreferredWidth((int) (dim.width * 0.25));
			setRowHeight(20);
			setRowSelectionAllowed(true);
			setFocusable(false);
			setDefaultRenderer(Object.class, renderer);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setRowSelectionInterval(0, 0);
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
							btnOpen.doClick();
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
				if (row == 0)
					c.setFont(getFont().deriveFont(Font.BOLD));
				return c;
			}
		};

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}

		private void setButtonOpenDefault() {
			MenuBuilder.setLabelAndMnemonic(btnOpen, TextUtils.getRawText(NewerFileRevisionsFoundDialog.key("open")));
			btnOpen.setToolTipText(TextUtils.format(NewerFileRevisionsFoundDialog.key("open.tooltip")));
		}

		private void setButtonOpenRestore(final FileWrapper fileWrapper) {
			MenuBuilder.setLabelAndMnemonic(btnOpen, TextUtils.getRawText(NewerFileRevisionsFoundDialog.key("restore")));
			btnOpen.setToolTipText(TextUtils.format(NewerFileRevisionsFoundDialog.key("restore.tooltip"),
			    file.getName(), fileWrapper.toString()));
		}
	}

	private static final long serialVersionUID = 1L;
	private final static String KEY_BASE = "NewerFileRevisionsFoundDialog";
	private JButton btnOpen;
	private JButton btnSkip;
	private boolean canContinue = false;
	private final File file;
	private File selectedFile;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat();
	private final NumberFormat fileSizeFormat = NumberFormat.getIntegerInstance();

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			canContinue = (source == btnOpen);
			dispose();
		}
	}

	public NewerFileRevisionsFoundDialog(final File file, final File[] revisions) {
		super(UITools.getFrame(), TextUtils.getText(NewerFileRevisionsFoundDialog.key("title")), true);
		if(getOwner() != null){
			final Window[] ownedWindows = getOwner().getOwnedWindows();
			for(Window w : ownedWindows){
				if(w.isVisible()){
					w.toBack();
				}
			}
		}
		this.file = file;
		setBackground(Color.white);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		UITools.addEscapeActionToDialog(this);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		final JTable table = createTable(revisions);
		final JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(Color.white);
		scrollPane.getViewport().setPreferredSize(new Dimension(460, 120));
		add(scrollPane);
		add(createQuestion());
		add(createButtonBar());
		getRootPane().setDefaultButton(btnOpen);
		pack();
		setLocationRelativeTo(UITools.getFrame());
		setVisible(true);
	}

	private Component createQuestion() {
		final String text = TextUtils.format(NewerFileRevisionsFoundDialog.key("question"), file.getName());
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
				return diff > 0 ? -1 : (diff > 0 ? 1 : 0);
			}
		});
		sortedRevisions.addAll(Arrays.asList(revisions));
		final Object[][] data = new Object[sortedRevisions.size() + 1][];
		int i = 0;
		data[i++] = createRow(file);
		for (final File f : sortedRevisions) {
			data[i++] = createRow(f);
		}
		sortedRevisions.addAll(Arrays.asList(revisions));
		return new RevisionTable(data);
	}

	private Object[] createRow(final File file) {
		return new Object[] { new FileWrapper(file), fileSizeFormat.format(file.length()),
		        dateFormat.format(file.lastModified()) };
	}

	private Box createButtonBar() {
		final Box controllerBox = Box.createHorizontalBox();
		controllerBox.setBorder(new EmptyBorder(5, 0, 5, 0));
		final CloseAction closeAction = new CloseAction();
		btnOpen = createButton(NewerFileRevisionsFoundDialog.key("open"),
		    NewerFileRevisionsFoundDialog.key("open.tooltip"), closeAction);
		btnSkip = createButton(NewerFileRevisionsFoundDialog.key("skip"),
		    NewerFileRevisionsFoundDialog.key("skip.tooltip"), closeAction);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOpen);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnSkip);
		controllerBox.add(Box.createHorizontalGlue());
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

	public boolean confirmContinue() {
		return canContinue;
	}
}
