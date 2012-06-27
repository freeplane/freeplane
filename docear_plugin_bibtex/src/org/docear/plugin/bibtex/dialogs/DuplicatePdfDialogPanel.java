package org.docear.plugin.bibtex.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

public class DuplicatePdfDialogPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private List<BibtexEntry> entries;	
	private JTable table;
	private URI uri;	
	private File file;
	
	
	public DuplicatePdfDialogPanel(final List<BibtexEntry> entries, File file) {
		this.entries = entries;
		this.uri = file.toURI();
		this.file = file;
		init();
	}
	/**
	 * Create the panel.
	 */
	private void init() {		
		table = new JTable(getTableModel());		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFont(GUIGlobals.CURRENTFONT);
        table.setRowHeight(GUIGlobals.TABLE_ROW_PADDING + GUIGlobals.CURRENTFONT.getSize());
        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getSelectionModel().setSelectionInterval(0, 0);
		
		JScrollPane scrollPane = new JScrollPane();		
		scrollPane.setPreferredSize(new Dimension(800, 300));		
		setLayout(new BorderLayout());
		 
		Component message = new MultiLineActionLabel(TextUtils.format("docear.reference.duplicate_file.message", file.getName()));
		((MultiLineActionLabel) message).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("open_pdf".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(uri);
					}
					catch (IOException ex) {
						LogUtils.warn(ex.getMessage());
					}
				}
			}
		});
		Component mail = new MultiLineActionLabel(TextUtils.getText("docear.reference.duplicate_file.mail"));
		((MultiLineActionLabel) mail).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				if("send_mail".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("mailto:help@docear.org"));
					}					
					catch (Exception ex) {
						LogUtils.warn(ex.getMessage());
					}
				}
			}
		});
		
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
				
//		p.add(new JLabel(TextUtils.format("docear.reference.duplicate_file.message", this.fileName)), BorderLayout.NORTH);
		p.add(message, BorderLayout.NORTH);
		p.add(mail, BorderLayout.SOUTH);
		
		
		
		add(p, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		
		scrollPane.setViewportView(table);
	}

	private AbstractTableModel getTableModel() {
		final JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper(); 
		AbstractTableModel model = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			final static int columns = 5;
			final String[] columnNames = {wrapper.getLocalizedColumnName("key"), 
					wrapper.getLocalizedColumnName("author"), 
					wrapper.getLocalizedColumnName("title"), 
					wrapper.getLocalizedColumnName("year"), 
					wrapper.getLocalizedColumnName("journal")};
			
			Object[][] rowData = new Object[entries.size()][columns];
			
			public String getColumnName(int col) {
			    return columnNames[col];
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}
			
			public void setValueAt(Object value, int row, int col) {
		        rowData[row][col] = value;
		        fireTableCellUpdated(row, col);
		    }

			public Object getValueAt(int row, int col) {
		        return rowData[row][col];
		    }

			public int getRowCount() {
				return this.rowData.length;
			}

			public int getColumnCount() {
				return columns;
			}
		};
		
		Iterator<BibtexEntry> iter = entries.iterator();
		for (int i=0; iter.hasNext(); i++) {
			BibtexEntry entry = iter.next();
			model.setValueAt(entry.getCiteKey(), i, 0);
			model.setValueAt(entry.getField("author"), i, 1);
			model.setValueAt(entry.getField("title"), i, 2);
			model.setValueAt(entry.getField("year"), i, 3);
			model.setValueAt(entry.getField("journal"), i, 4);			
		}

		return model;
	}	
	
	public BibtexEntry getSelectedEntry() {
		int index = table.getSelectedRow();
		return entries.get(index);
	}	

}
