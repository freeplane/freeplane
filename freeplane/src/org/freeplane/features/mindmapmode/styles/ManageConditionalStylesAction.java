package org.freeplane.features.mindmapmode.styles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;

public class ManageConditionalStylesAction extends AFreeplaneAction {
	private static final int BUTTON_GAP = 15;
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public ManageConditionalStylesAction() {
	    super("ManageConditionalStylesAction");
    }

	public void actionPerformed(ActionEvent e) {
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		Component pane = createConditionalStylePane(map);
		try{
			Controller.getCurrentModeController().startTransaction();
			final int confirmed = JOptionPane.showConfirmDialog(controller.getViewController().getMapView(), pane, "", JOptionPane.OK_CANCEL_OPTION);
			if(JOptionPane.OK_OPTION == confirmed){
				LogicalStyleController.getController().refreshMap(map);
				Controller.getCurrentModeController().commit();
			}
			else{
				Controller.getCurrentModeController().rollback();

			}
		}
		catch(Exception ex){
			Controller.getCurrentModeController().rollback();
		}
	}

	private Component createConditionalStylePane(final MapModel map) {
		final JPanel pane = new JPanel(new BorderLayout());
	    final MapStyleModel styles = MapStyleModel.getExtension(map);
		final TableModel tableModel = MLogicalStyleController.getController().getConditionalStyleModelAsTableModel(map);
		final ConditionalStyleTable conditionalStyleTable = new ConditionalStyleTable(styles, tableModel);
		if(conditionalStyleTable.getRowCount() > 0){
			conditionalStyleTable.setRowSelectionInterval(0, 0);
		}
	    JScrollPane scrollPane = new JScrollPane(conditionalStyleTable);
	    scrollPane.setPreferredSize(new Dimension(600, 400));
	    pane.add(scrollPane, BorderLayout.CENTER);
	    final Box buttons = Box.createVerticalBox();
	    
	    JButton create = new JButton();
	    MenuBuilder.setLabelAndMnemonic(create, TextUtils.getText("new"));
	    create.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final MLogicalStyleController styleController = MLogicalStyleController.getController();
				final FilterComposerDialog filterComposerDialog = styleController.getFilterComposerDialog();
				filterComposerDialog.acceptMultipleConditions(true);
				filterComposerDialog.show();
				final List<ISelectableCondition> conditions = filterComposerDialog.getConditions();
				int row = conditionalStyleTable.getRowCount();
				for(final ISelectableCondition condition : conditions){
					LogicalStyleController.getController().addConditionalStyle(map, true, condition, MapStyleModel.DEFAULT_STYLE);
				}
				if(row < conditionalStyleTable.getRowCount()){
					conditionalStyleTable.setRowSelectionInterval(row, row);
				}
			} 
		});
	    JButton delete = new JButton();
	    MenuBuilder.setLabelAndMnemonic(delete, TextUtils.getText("delete"));
	    delete.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				LogicalStyleController.getController().removeConditionalStyle(map, selectedRow);
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
	    MenuBuilder.setLabelAndMnemonic(up, TextUtils.getText("up"));
	    up.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow <= 0){
					return;
				}
				LogicalStyleController.getController().moveConditionalStyleUp(map, selectedRow);
				selectedRow--;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton down = new JButton();
	    MenuBuilder.setLabelAndMnemonic(down, TextUtils.getText("down"));
	    down.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1 || selectedRow == conditionalStyleTable.getRowCount() - 1){
					return;
				}
				LogicalStyleController.getController().moveConditionalStyleDown(map, selectedRow);
				selectedRow++;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(create);
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
