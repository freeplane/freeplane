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
package org.freeplane.features.mindmapmode.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.QuitAction;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.main.application.ApplicationResourceController;

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
			getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(final ListSelectionEvent event) {
					// Update the word field if a suggestion is click
					if (!event.getValueIsAdjusting()) {
						final ListSelectionModel lsm = (ListSelectionModel) event.getSource();
						final boolean enable = !(lsm.isSelectionEmpty());
						btnReplace.setEnabled(enable);
						if (enable) {
							final FileWrapper fileWrapper = (FileWrapper) getModel().getValueAt(getSelectedRow(), 0);
							if (file.equals(fileWrapper.getFile())) {
								lsm.clearSelection();
								btnReplace.setToolTipText(null);
							}
							else {
								setSelectedFile(fileWrapper.getFile());
								btnReplace.setToolTipText(TextUtils.format(NewerFileRevisionsFoundDialog
								    .key("replace.tooltip"), file.getName(), fileWrapper.toString()));
							}
						}
						else {
							btnReplace.setToolTipText(null);
						}
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
				// disable the first line that contains the original file
				c.setEnabled(row != 0);
				return c;
			}
		};

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return false;
		}
	}

	private static final long serialVersionUID = 1L;
	private final static String KEY_BASE = "newer_revisions_found_dialog";
	private JButton btnOK;
	private JButton btnCancel;
	private JButton btnExit;
	private JButton btnReplace;
	private boolean canContinue = false;
	private final Controller controller;
	private final File file;
	private File selectedFile;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat();
	private final NumberFormat fileSizeFormat = NumberFormat.getIntegerInstance();

	private class CloseAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			canContinue = (source == btnOK //
			|| (source == btnReplace && selectedFile != null));
			dispose();
		}
	}

	public NewerFileRevisionsFoundDialog(final File file, final File[] revisions, final Controller controller) {
		super(UITools.getFrame(), TextUtils.getText(NewerFileRevisionsFoundDialog.key("title")), true);
		this.file = file;
		this.controller = controller;
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
		getRootPane().setDefaultButton(btnCancel);
		pack();
		//		LogTool.severe("hi, NewerFileRevisionsFoundDialog, thread " + Thread.currentThread(), new Exception("dummy"));
		setLocationRelativeTo(UITools.getFrame());
		setVisible(true);
	}

	private Component createQuestion() {
		final String text = TextUtils.format(NewerFileRevisionsFoundDialog.key("question"), file.getName());
		String html = HtmlUtils.plainToHTML(text);
		final JLabel textArea = new JLabel(html);
		textArea.setAlignmentX(0.5f);
		textArea.setFont(new Font("Dialog", Font.BOLD, 12));
		textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return textArea;
	}

	private static String key(final String appendix) {
		return KEY_BASE + "_" + appendix;
	}

	private JTable createTable(final File[] revisions) {
		final TreeSet<File> sortedRevisions = new TreeSet<File>(new Comparator<File>() {
			public int compare(final File f1, final File f2) {
				final long diff = f1.lastModified() - f2.lastModified();
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
		btnOK = createButton("ok", NewerFileRevisionsFoundDialog.key("ok.tooltip"), closeAction);
		btnReplace = createButton(NewerFileRevisionsFoundDialog.key("replace"), NewerFileRevisionsFoundDialog
		    .key("empty.tooltip"), closeAction);
		btnReplace.setEnabled(false);
		btnCancel = createButton("cancel", NewerFileRevisionsFoundDialog.key("cancel.tooltip"), closeAction);
		btnExit = createButton("QuitAction.text", NewerFileRevisionsFoundDialog.key("quit.tooltip"), new QuitAction(
		    controller));
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnOK);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnReplace);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnCancel);
		controllerBox.add(Box.createHorizontalGlue());
		controllerBox.add(btnExit);
		controllerBox.add(Box.createHorizontalGlue());
		return controllerBox;
	}

	private JButton createButton(final String key, final String tooltipKey, final ActionListener closeAction) {
		final JButton button = new JButton();
		MenuBuilder.setLabelAndMnemonic(button, TextUtils.getText(key));
		button.addActionListener(closeAction);
		button.setMaximumSize(new Dimension(1000, 1000));
		final String selectedFileName = getSelectedFile() == null ? null : getSelectedFile().getName();
		// arguments are only used for one button but they don't hurt for the other
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

	public static void main(final String[] args) {
		ResourceController.setResourceController(new ApplicationResourceController());
		// null Controller will lead to a NPE in QuitAction if "Exit" button is chosen
		final Controller controller = null;
		final NewerFileRevisionsFoundDialog newerFileRevisionsFoundDialog = new NewerFileRevisionsFoundDialog(new File(
		    "someMap.mm"), new File(".").listFiles(), controller);
		System.out.println("confirmContinue=" + newerFileRevisionsFoundDialog.confirmContinue());
		System.out.println("selectedFile=" + newerFileRevisionsFoundDialog.getSelectedFile());
	}
}
