package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.sf.jabref.BibtexEntry;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PdfMetadataListDialog extends JPanel {
	
	private static final long serialVersionUID = -627410651667772600L;
	
	private JTable table;

	private BibtexEntryTableModel tableModel = new BibtexEntryTableModel();
	private final Collection<BibtexEntry> entries;
	
	public PdfMetadataListDialog(Collection<BibtexEntry> entries) {
		this.entries = entries;
		setPreferredSize(new Dimension(640, 200));
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("434px:grow"),},
			new RowSpec[] {
				RowSpec.decode("max(48dlu;default)"),
				RowSpec.decode("185px:grow"),}));
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("max(20dlu;default)"),}));
		
		JLabel lblNewLabel = new JLabel("New label");
		panel.add(lblNewLabel, "2, 1");
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "1, 2, fill, fill");
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(false); 
		scrollPane.setViewportView(table);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setMinWidth(40);
		table.getColumnModel().getColumn(1).setPreferredWidth(40);
		table.getColumnModel().getColumn(1).setMaxWidth(40);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			
			private static final long serialVersionUID = 4805846114365117400L;

			public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(value));
				return label;
			}
		});
		table.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				Point p = table.getMousePosition();
				if(p == null) {
					return;
				}
				int row = p.y/table.getRowHeight();
				if(table.getRowCount() > row) {
					BibtexEntry entry = tableModel.getEntry(row);
					//DOCEAR - maybe show better tooltip
				}
			} 
		});
		tableModel.fireTableDataChanged();
		table.setRowSelectionInterval(0,0);
	}
	
	public BibtexEntry getSelectedEntry() {
		if(table.getSelectedRowCount() > 0) {
			return tableModel.getEntry(table.getSelectedRow());
		}
		return null;
	}
	
	class BibtexEntryTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 2610007578887026651L;
		
		public BibtexEntryTableModel() {
			addColumn("Type");
			addColumn("Year");
			addColumn("Title");
			addColumn("Author");			
		}
	
		public BibtexEntry getEntry(int rowIndex) {
			if(rowIndex < 0 || rowIndex >= entries.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			int i=0;
			for (Iterator<BibtexEntry> iterator = entries.iterator(); iterator.hasNext(); i++) {
				if(i==rowIndex) {
					return iterator.next();
				}
				iterator.next();
			}
			return null;
		}

		public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public int getRowCount() {
			if(entries == null) {
				return 0;
			}
			return entries.size();
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			BibtexEntry entry = getEntry(rowIndex);
			if(entry == null) {
				return null;
			}
			if(columnIndex == 0) {
				return entry.getType().getName();
			}
			return entry.getField(getColumnName(columnIndex).toLowerCase());
		}
	}

}
