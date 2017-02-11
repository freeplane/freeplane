package org.freeplane.features.styles.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

abstract public class AManageConditionalStylesAction extends AFreeplaneAction {
	private static final int BUTTON_GAP = 15;
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public AManageConditionalStylesAction(final String name) {
	    super(name);
    }

	abstract public ConditionalStyleModel getConditionalStyleModel();

	protected Component createConditionalStylePane(final MapModel map, final ConditionalStyleModel conditionalStyleModel) {
		final JPanel pane = new JPanel(new BorderLayout());
	    final MapStyleModel styles = MapStyleModel.getExtension(map);
		final TableModel tableModel = MLogicalStyleController.getController().getConditionalStyleModelAsTableModel(map, conditionalStyleModel);
		final ConditionalStyleTable conditionalStyleTable = new ConditionalStyleTable(styles, tableModel);
		if(conditionalStyleTable.getRowCount() > 0){
			conditionalStyleTable.setRowSelectionInterval(0, 0);
		}
	    JScrollPane scrollPane = new JScrollPane(conditionalStyleTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    final int tablePreferredWidth = scrollPane.getViewport().getViewSize().width;
	    scrollPane.setPreferredSize(new Dimension(tablePreferredWidth, 400));
	    pane.add(scrollPane, BorderLayout.CENTER);
	    final Box buttons = Box.createVerticalBox();
	    
	    JButton create = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(create, TextUtils.getRawText("new"));
	    create.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = conditionalStyleTable.getRowCount();
				final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
				((MLogicalStyleController)LogicalStyleController.getController()).addConditionalStyle(map, conditionalStyleModel, true, null, MapStyleModel.DEFAULT_STYLE, false);
				conditionalStyleTable.setRowSelectionInterval(row, row);
			} 
		});

	    JButton edit = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(edit, TextUtils.getRawText("edit"));
	    edit.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				final FilterComposerDialog filterComposerDialog = new FilterComposerDialog();
				filterComposerDialog.addCondition(null);
				filterComposerDialog.setConditionRenderer(ConditionalStyleTable.createConditionRenderer());
				for(int i = 0; i < conditionalStyleTable.getRowCount(); i++){
					final ASelectableCondition condition = (ASelectableCondition)conditionalStyleTable.getValueAt(i, 1);
					filterComposerDialog.addCondition(condition);
				}
				final ASelectableCondition value = (ASelectableCondition) conditionalStyleTable.getValueAt(selectedRow, 1);
				final ASelectableCondition newCondition = filterComposerDialog.editCondition(value);
				conditionalStyleTable.setValueAt(newCondition, selectedRow, 1);
			}
		});

	    JButton delete = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(delete, TextUtils.getRawText("delete"));
	    delete.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
				((MLogicalStyleController)LogicalStyleController.getController()).removeConditionalStyle(map, conditionalStyleModel, selectedRow);
				if(conditionalStyleTable.getRowCount() == selectedRow){
					selectedRow--;
				}
				if(selectedRow == -1){
					return;
				}
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton up = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(up, TextUtils.getRawText("up"));
	    up.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow <= 0){
					return;
				}
				final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
				((MLogicalStyleController)LogicalStyleController.getController()).moveConditionalStyleUp(map, conditionalStyleModel, selectedRow);
				selectedRow--;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton down = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(down, TextUtils.getRawText("down"));
	    down.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1 || selectedRow == conditionalStyleTable.getRowCount() - 1){
					return;
				}
				final ConditionalStyleModel conditionalStyleModel = getConditionalStyleModel();
				((MLogicalStyleController)LogicalStyleController.getController()).moveConditionalStyleDown(map, conditionalStyleModel, selectedRow);
				selectedRow++;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(create);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(edit);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(delete);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(up);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(down);
	    buttons.add(Box.createVerticalGlue());
	    pane.add(buttons, BorderLayout.EAST);
	    UITools.focusOn(conditionalStyleTable);
		return pane;
    }
}
