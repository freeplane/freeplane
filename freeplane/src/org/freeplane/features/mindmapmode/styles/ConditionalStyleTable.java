package org.freeplane.features.mindmapmode.styles;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.freeplane.features.common.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.common.styles.MapStyleModel;

public class ConditionalStyleTable extends JTable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	private MapStyleModel styleModel;
	private DefaultConditionRenderer conditionRenderer;
	
	@SuppressWarnings("serial")
    private class ConditionEditor extends AbstractCellEditor
    implements TableCellEditor{
		
		JButton btn = new JButton();
		private Object cellEditorValue;
		public Component getTableCellEditorComponent(final JTable table, final Object value, boolean isSelected, int row, int column) {
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final MLogicalStyleController styleController = MLogicalStyleController.getController();
					final FilterComposerDialog filterComposerDialog = styleController.getFilterComposerDialog();
					filterComposerDialog.show();
					cellEditorValue = filterComposerDialog.getCondition();
					if(cellEditorValue == null){
						cellEditorValue = value;
					}
					btn.removeActionListener(this);
					fireEditingStopped();
				}
			});
	        return btn;
        }

		public Object getCellEditorValue() {
	        return cellEditorValue;
        }
		
		@Override
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				return ((MouseEvent) anEvent).getClickCount() >= 2;
			}
			return true;
		}
	}

	public ConditionalStyleTable(MapStyleModel styleModel, TableModel tableModel) {
	    super(tableModel);
	    this.styleModel = styleModel;
	    setCellSelectionEnabled(false);
	    setRowSelectionAllowed(true);
	    final TableColumnModel columnModel = getColumnModel();
	    setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
	    setSelectionBackground(DefaultConditionRenderer.SELECTED_BACKGROUND);
	    setRowHeight(20);
		conditionRenderer = new DefaultConditionRenderer("");
		columnModel.getColumn(1).setCellRenderer(conditionRenderer);
		columnModel.getColumn(1).setCellEditor(new ConditionEditor());
		final JComboBox styleBox = new JComboBox();
		styleBox.setEditable(false);
		columnModel.getColumn(2).setCellEditor(new DefaultCellEditor(styleBox){

			/**
             * 
             */
            private static final long serialVersionUID = 1L;
            {
            	setClickCountToStart(2);
            }

			@Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                         int column) {
				final Collection<Object> styles = ConditionalStyleTable.this.styleModel.getStyles();
				final DefaultComboBoxModel boxContent = new DefaultComboBoxModel(styles.toArray());
				styleBox.setModel(boxContent);
	            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
			
		});
		columnModel.getColumn(1).setPreferredWidth(300);
		columnModel.getColumn(2).setCellRenderer(new DefaultStyleRenderer());
    }

	public MapStyleModel getStyles() {
	    return styleModel;
    }
    
}

