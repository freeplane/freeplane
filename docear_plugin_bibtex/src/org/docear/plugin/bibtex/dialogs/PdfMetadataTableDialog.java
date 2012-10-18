package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.export.ExportFormats;
import net.sf.jabref.export.layout.LayoutHelper;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PdfMetadataTableDialog extends JPanel {
	
	private static final long serialVersionUID = -627410651667772600L;
	
	private JTable table;

	private final BibtexEntryTableModel tableModel;
	private final Collection<BibtexEntry> entries;
	
	public PdfMetadataTableDialog(Collection<BibtexEntry> entries) {
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
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText("docear.metadata.import.help"));
		panel.add(lblNewLabel, "2, 1");
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "1, 2, fill, fill");
		
		tableModel = new BibtexEntryTableModel();		
		table = new JTable(tableModel);
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
		int cols = tableModel.getColumnCount();
		for (int i = 4; i < cols; i++) {
			int width = SwingUtilities.computeStringWidth(table.getFontMetrics(table.getTableHeader().getFont()), tableModel.getColumnName(i));
			table.getColumnModel().getColumn(i).setMinWidth(width);
			table.getColumnModel().getColumn(i).setPreferredWidth(width);
		}
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
//				Point p = table.getMousePosition();
//				if(p == null) {
//					return;
//				}
//				int row = p.y/table.getRowHeight();
//				if(table.getRowCount() > row) {
//					BibtexEntry entry = tableModel.getEntry(row);
//					//DOCEAR - maybe show better tooltip
//				}
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
			
			for (Iterator<BibtexEntry> iterator = entries.iterator(); iterator.hasNext();) {
				BibtexEntry entry = iterator.next();
				Set<String> fields = entry.getAllFields();
				for (String field : fields) {
					field = capitilize(field.toLowerCase());
					if(this.findColumn(field) == -1) {
						addColumn(field);
					}
				}
			}			
		}
	
		private String capitilize(String text) {
			CharBuffer buffer = CharBuffer.allocate(text.length());
			buffer.append(text);
			buffer.rewind();
			buffer.mark();			
			String c = String.valueOf(buffer.get());
			buffer.reset();
			buffer.put(c.toUpperCase().charAt(0));			
			return buffer.rewind().toString();
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
			StringBuffer sb = new StringBuffer();
			StringReader sr = new StringReader(Globals.prefs.get("preview0").replaceAll("__NEWLINE__", "\n"));
	        ExportFormats.entryNumber = 1; // Set entry number in case that is included in the preview layout.
			try {
				sb.append(new LayoutHelper(sr).getLayoutFromText(Globals.FORMATTER_PACKAGE).doLayout(entry,ReferencesController.getController().getJabrefWrapper().getDatabase()));
			} 
			catch (Exception e) {
			}
			
			return entry.getField(getColumnName(columnIndex).toLowerCase());
		}
	}

}
