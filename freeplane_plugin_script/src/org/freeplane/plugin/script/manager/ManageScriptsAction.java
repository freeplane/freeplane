package org.freeplane.plugin.script.manager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;

public class ManageScriptsAction extends AFreeplaneAction {
	private static final int BUTTON_GAP = 15;
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public ManageScriptsAction() {
	    super("ManageScriptsAction");
    }


	protected Component createPane() {
		final JPanel pane = new JPanel(new BorderLayout());
		final DefaultTableModel table = new DefaultTableModel (new String[]{("script"), ("path"), ("enabled")}, 0){

			/**
             * 
             */
            private static final long serialVersionUID = 1L;

			@Override
            public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex){
					case 0: case 1: return String.class;
					case 2: return Boolean.class;
				}
				return super.getColumnClass(columnIndex);
            }
            
			
		};
		table.addRow(new Object[]{"script1", "path1", Boolean.TRUE});
		final JTable jTable = new JTable(table);
		jTable.setCellSelectionEnabled(false);
		jTable.setRowSelectionAllowed(true);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		final JTextField textField = new JTextField();
		textField.setEditable(false);
		DefaultCellEditor stringReader = new DefaultCellEditor(textField);
		jTable.setDefaultEditor(String.class, stringReader);
		final TableColumnModel columnModel = jTable.getColumnModel();
		final TableColumn column0 = columnModel.getColumn(0);
		column0.setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(400);
		columnModel.getColumn(2).setPreferredWidth(100);

	    JScrollPane scrollPane = new JScrollPane(jTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    pane.add(scrollPane, BorderLayout.CENTER);
	    final Box installer = Box.createHorizontalBox();
	    JButton install = new JButton();
	    MenuBuilder.setLabelAndMnemonic(install, ("install"));
	    install.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    install.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = jTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
			}
		});
	    final JTextField url = new JTextField();
	    final JFileChooser fileChooser = new JFileChooser();
	    JButton selectFile = new JButton();
	    MenuBuilder.setLabelAndMnemonic(selectFile, ("selectFile"));
	    selectFile.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(pane);
				final File selectedFile = fileChooser.getSelectedFile();
				if(selectedFile != null)
					url.setText(selectedFile.getAbsolutePath());
			}
		});

	    installer.add(Box.createVerticalStrut(BUTTON_GAP));
	    installer.add(install);
	    installer.add(Box.createVerticalStrut(BUTTON_GAP));
	    url.setColumns(60);
	    installer.add(url);
	    installer.add(Box.createVerticalStrut(BUTTON_GAP));
	    installer.add(selectFile);
	    installer.add(Box.createVerticalStrut(BUTTON_GAP));
	    pane.add(installer, BorderLayout.SOUTH);
	    
	    
	    final Box buttons = Box.createVerticalBox();
	    
	    JButton configure = new JButton();
	    MenuBuilder.setLabelAndMnemonic(configure, ("configure"));
	    configure.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    configure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = jTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
			}
		});

	    JButton edit = new JButton();
	    MenuBuilder.setLabelAndMnemonic(edit, ("edit"));
	    edit.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = jTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
			}
		});
	    JButton deinstall = new JButton();
	    MenuBuilder.setLabelAndMnemonic(deinstall, ("deinstall"));
	    deinstall.setMaximumSize(UITools.MAX_BUTTON_DIMENSION);
	    deinstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = jTable.getSelectedRow();
				if(selectedRow == -1){
					return;
				}
			}
		});
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(configure);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(edit);
	    buttons.add(Box.createVerticalStrut(BUTTON_GAP));
	    buttons.add(deinstall);
	    buttons.add(Box.createVerticalGlue());
	    pane.add(buttons, BorderLayout.EAST);
	    UITools.focusOn(jTable);
		return pane;
    }
	public void actionPerformed(ActionEvent e) {
		final Component pane = createPane();
		JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), pane);
	}
}
