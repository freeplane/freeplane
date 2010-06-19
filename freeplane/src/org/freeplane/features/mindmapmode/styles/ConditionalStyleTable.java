package org.freeplane.features.mindmapmode.styles;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.AFilterComposerDialog;
import org.freeplane.features.common.filter.condition.DefaultConditionRenderer;
import org.freeplane.features.common.styles.ConditionalStyleModel;
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
		private class FilterComposerDialog extends AFilterComposerDialog{

			public FilterComposerDialog(Controller controller) {
		        super(controller, TextUtils.getText("filter_dialog"), true);
	        }

			protected DefaultComboBoxModel createModel() {
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				return model;
		    }
			
			protected boolean applyModel(DefaultComboBoxModel model) {
				cellEditorValue = model.getSelectedItem();
			    return cellEditorValue != null;
		    }
					
		}
		private Object cellEditorValue ;
		JButton btn = new JButton();
		public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
			cellEditorValue = null;
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Controller c = UITools.getController(table);
					final FilterComposerDialog filterComposerDialog = new FilterComposerDialog(c);
					filterComposerDialog.show();
					fireEditingStopped();
					btn.removeActionListener(this);
				}
			});
	        return btn;
        }

		public Object getCellEditorValue() {
	        return cellEditorValue;
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
		conditionRenderer = new DefaultConditionRenderer();
		columnModel.getColumn(1).setCellRenderer(conditionRenderer);
		columnModel.getColumn(1).setCellEditor(new ConditionEditor());
		final JComboBox styleBox = new JComboBox();
		styleBox.setEditable(false);
		columnModel.getColumn(2).setCellEditor(new DefaultCellEditor(styleBox){

			/**
             * 
             */
            private static final long serialVersionUID = 1L;

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
