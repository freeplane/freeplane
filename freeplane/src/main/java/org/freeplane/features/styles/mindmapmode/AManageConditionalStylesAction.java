package org.freeplane.features.styles.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableModel;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.FilterConditionEditor.Variant;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.MapStyleModel;

abstract public class AManageConditionalStylesAction extends AFreeplaneAction {
	private static final int BUTTON_GAP = 15;
	private static final String WINDOW_CONFIG_PROPERTY = "conditional_style_window_configuration";
	/**
     *
     */
    private static final long serialVersionUID = 1L;

	public AManageConditionalStylesAction(final String name) {
	    super(name);
    }

    protected void createAndShowDialog(final ModeController modeController, final ConditionalStyleModel conditionalStyleModel, Component pane, String title) {
        final JDialog dialog = new JDialog(UITools.getCurrentFrame());
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);
        dialog.setTitle(title);
        dialog.getContentPane().add(pane, BorderLayout.CENTER);

        JButton okButton = new JButton();
        LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
        JButton cancelButton = new JButton();
        LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleOkAction(modeController, conditionalStyleModel);
                dialog.dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
        dialog.pack();
        final WindowConfigurationStorage windowConfigurationStorage = new WindowConfigurationStorage(WINDOW_CONFIG_PROPERTY);
        windowConfigurationStorage.setBounds(dialog);
        dialog.setVisible(true);
    }

    protected abstract void handleOkAction(ModeController modeController, ConditionalStyleModel conditionalStyleModel);


    protected Component createConditionalStylePane(final MapModel map, final ConditionalStyleModel conditionalStyleModel, Variant variant) {
		final JPanel pane = new JPanel(new BorderLayout());
	    final MapStyleModel styles = MapStyleModel.getExtension(map);
		final TableModel tableModel = MLogicalStyleController.getController().getConditionalStyleModelAsTableModel(map, conditionalStyleModel);
		final ConditionalStyleTable conditionalStyleTable = new ConditionalStyleTable(styles, conditionalStyleModel, tableModel, variant);
		conditionalStyleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		if(conditionalStyleTable.getRowCount() > 0){
			conditionalStyleTable.setRowSelectionInterval(0, 0);
		}
	    JScrollPane scrollPane = new JScrollPane(conditionalStyleTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
				conditionalStyleModel.addCondition(true, null, MapStyleModel.DEFAULT_STYLE, false);
				conditionalStyleTable.setRowSelectionInterval(row, row);
			}

		});

	    JButton edit = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(edit, TextUtils.getRawText("edit"));
	    edit.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				FilterComposerDialog filterComposerDialog = conditionalStyleTable.filterComposer();
				final ASelectableCondition value = (ASelectableCondition) conditionalStyleTable.getValueAt(selectedRow, 1);
				final ASelectableCondition newCondition = filterComposerDialog.editCondition(value);
				conditionalStyleTable.setValueAt(newCondition, selectedRow, 1);
			}
		});

	    JButton delete = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(delete, TextUtils.getRawText("delete"));
	    delete.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    delete.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
				conditionalStyleModel.removeCondition(selectedRow);
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
			@Override
            public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow <= 0){
					return;
				}
				conditionalStyleModel.moveUp(selectedRow);
				selectedRow--;
				conditionalStyleTable.setRowSelectionInterval(selectedRow, selectedRow);
			}
		});
	    JButton down = new JButton();
	    LabelAndMnemonicSetter.setLabelAndMnemonic(down, TextUtils.getRawText("down"));
	    down.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    down.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
				int selectedRow = conditionalStyleTable.getSelectedRow();
				if(selectedRow == -1 || selectedRow == conditionalStyleTable.getRowCount() - 1){
					return;
				}
				conditionalStyleModel.moveDown(selectedRow);
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
