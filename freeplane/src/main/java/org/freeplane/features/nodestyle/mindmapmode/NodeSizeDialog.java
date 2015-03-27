/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JSpinner;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.freeplane.core.util.TextUtils;

import java.awt.Color;
import javax.swing.SpinnerNumberModel;

/**
 * @author Dimitry Polivaev
 * Nov 12, 2011
 */
class NodeSizeDialog extends JDialog {
	private static final String ACTION_CANCEL = "Cancel";
	private static final String ACTION_OK = "OK";
	private final JPanel contentPanel = new JPanel();
	private JSpinner spinnerMinimumNodeWidth;
	private JSpinner spinnerMaximumNodeWidth;
	private final Action closeAction = new CloseAction();
	public boolean result;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			NodeSizeDialog dialog = new NodeSizeDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NodeSizeDialog() {
		super(JOptionPane.getFrameForComponent(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()));
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 267, 173);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, TextUtils.getText("MinNodeWidth.text"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				spinnerMinimumNodeWidth = new JSpinner();
				spinnerMinimumNodeWidth.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
				GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
				gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
				gbc_spinner_1.gridx = 0;
				gbc_spinner_1.gridy = 0;
				panel.add(spinnerMinimumNodeWidth, gbc_spinner_1);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, TextUtils.getText("MaxNodeWidth.text"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 1;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0};
			gbl_panel.rowHeights = new int[]{0, 0};
			gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				spinnerMaximumNodeWidth = new JSpinner();
				spinnerMaximumNodeWidth.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
				GridBagConstraints gbc_spinner = new GridBagConstraints();
				gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
				gbc_spinner.gridx = 0;
				gbc_spinner.gridy = 0;
				panel.add(spinnerMaximumNodeWidth, gbc_spinner);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(TextUtils.getText("ok"));
				okButton.addActionListener(closeAction);
				okButton.setActionCommand(ACTION_OK);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(TextUtils.getText("cancel"));
				cancelButton.addActionListener(closeAction);
				cancelButton.setActionCommand(ACTION_CANCEL);
				buttonPane.add(cancelButton);
			}
		}
		
		spinnerMaximumNodeWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				final Integer value = (Integer)spinnerMaximumNodeWidth.getValue();
				if((Integer)spinnerMinimumNodeWidth.getValue() > value)
					spinnerMinimumNodeWidth.setValue(value);
			}
		});
		
		spinnerMinimumNodeWidth.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				final Integer value = (Integer)spinnerMinimumNodeWidth.getValue();
				if((Integer)spinnerMaximumNodeWidth.getValue() < value)
					spinnerMaximumNodeWidth.setValue(value);
			}
		});
	}
	@SuppressWarnings("serial")
    private class CloseAction extends AbstractAction {
		public CloseAction() {
		}
		public void actionPerformed(ActionEvent e) {
			result = ACTION_OK.equals(e.getActionCommand());
			setVisible(false);
		}
	}
	
	public boolean showDialog(int minNodeWidth, int maxNodeTextWidth){
		spinnerMinimumNodeWidth.setValue(minNodeWidth);
		spinnerMaximumNodeWidth.setValue(maxNodeTextWidth);
		setVisible(true);
		return result;
	}
	
	int getMinWidth(){
		return (Integer) spinnerMinimumNodeWidth.getValue();
	}
	
	int getMaxNodeWidth(){
		return (Integer) spinnerMaximumNodeWidth.getValue();
	}
	
}