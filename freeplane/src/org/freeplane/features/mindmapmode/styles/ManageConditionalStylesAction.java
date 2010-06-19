package org.freeplane.features.mindmapmode.styles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableModel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.styles.ConditionalStyleModel;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;

public class ManageConditionalStylesAction extends AFreeplaneAction {
	private static final int BUTTON_GAP = 15;
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public ManageConditionalStylesAction(Controller controller) {
	    super("ManageConditionalStylesAction", controller);
    }

	public void actionPerformed(ActionEvent e) {
		final Controller controller = getController();
		final MapModel map = controller.getMap();
		Component pane = createConditionalStylePane(map);
		try{
			getModeController().startTransaction();
			final int confirmed = JOptionPane.showConfirmDialog(controller.getViewController().getMapView(), pane, "", JOptionPane.OK_CANCEL_OPTION);
			if(JOptionPane.OK_OPTION == confirmed){
				LogicalStyleController.getController(getModeController()).refreshMap(map);
				getModeController().commit();
			}
			else{
				getModeController().rollback();

			}
		}
		catch(Exception ex){
			getModeController().rollback();
		}
	}

	private Component createConditionalStylePane(final MapModel map) {
		final JPanel pane = new JPanel(new BorderLayout());
	    final MapStyleModel styles = MapStyleModel.getExtension(map);
		final TableModel tableModel = MLogicalStyleController.getController(getModeController()).getConditionalStyleModelAsTableModel(map);
		final ConditionalStyleTable conditionalStyleTable = new ConditionalStyleTable(styles, tableModel);
		if(conditionalStyleTable.getRowCount() > 0){
			conditionalStyleTable.setRowSelectionInterval(0, 0);
		}
	    JScrollPane scrollPane = new JScrollPane(conditionalStyleTable);
	    scrollPane.setPreferredSize(new Dimension(600, 400));
	    pane.add(scrollPane, BorderLayout.CENTER);
	    final Box buttons = Box.createVerticalBox();
	    JButton create = new JButton(TextUtils.getText("new"));
	    create.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LogicalStyleController.getController(getModeController()).addConditionalStyle(map, true, null, MapStyleModel.DEFAULT_STYLE);
				int row = conditionalStyleTable.getRowCount() - 1;
				conditionalStyleTable.setRowSelectionInterval(row, row);
			} 
		});
	    JButton delete = new JButton(TextUtils.getText("delete"));
	    delete.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				LogicalStyleController.getController(getModeController()).removeConditionalStyle(map, selectedRow);
				if(conditionalStyleTable.getRowCount() == selectedRow){
					selectedRow--;
				}
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton up = new JButton(TextUtils.getText("up"));
	    up.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow <= 0){
					return;
				}
				LogicalStyleController.getController(getModeController()).moveConditionalStyleUp(map, selectedRow);
				selectedRow--;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton down = new JButton(TextUtils.getText("down"));
	    down.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1 || selectedRow == conditionalStyleTable.getRowCount() - 1){
					return;
				}
				LogicalStyleController.getController(getModeController()).moveConditionalStyleDown(map, selectedRow);
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
