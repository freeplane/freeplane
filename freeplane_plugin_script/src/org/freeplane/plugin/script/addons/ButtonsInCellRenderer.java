package org.freeplane.plugin.script.addons;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Editor and Renderer for multiple buttons inside a table cell.
 * @author Mag. Stefan Hagmann 
 * @see http://www.bgbaden-frauen.ac.at/frauengasse20/uploads/files/Informatik/java/ButtonsInColumn.java
 */
@SuppressWarnings("serial")
class ButtonsInColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener,
        MouseListener {
	private static final int BUTTON_SPACER = 3;
	private final JTable table;
	private final Border border;
	private Border fborder;
	private Object editorValue;
	private boolean isButtonColumnEditor;
	//die Buttons sind in diesem Container
	private JPanel panel;
	private final Action[] actions;
	private final JButton[] buttons;

	public ButtonsInColumn(JTable table, JButton[] buttons, Action[] actions, int column) {
		this.table = table;
		this.actions = actions;
		this.buttons = buttons;
		for (JButton button : buttons) {
			button.setFocusPainted(false);
			button.addActionListener(this);
		}
		border = buttons[0].getBorder();
		setFocusBorder(new LineBorder(Color.BLUE));
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
		table.addMouseListener(this);
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		for (JButton btn : buttons) {
			panel.add(btn);
			panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
		}
	}

	private void setFocusBorder(Border focusBorder) {
		this.fborder = focusBorder;
		for (JButton btn : buttons) {
			btn.setBorder(focusBorder);
		}
	}

	public Object getCellEditorValue() {
		return editorValue;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	                                               int row, int column) {
		for (JButton btn : buttons) {
			if (isSelected) {
				btn.setForeground(table.getSelectionForeground());
				btn.setBackground(table.getSelectionBackground());
				panel.setBackground(table.getSelectionBackground());
			}
			else {
				btn.setForeground(table.getForeground());
				btn.setBackground(UIManager.getColor("Button.background"));
				panel.setBackground(table.getBackground());
			}
			if (hasFocus) {
				btn.setBorder(fborder);
			}
			else {
				btn.setBorder(border);
			}
		}
		return panel;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.editorValue = value;
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
//FIXME: Java 6
//		int row = table.convertRowIndexToModel(table.getEditingRow());
		int row = table.getEditingRow();
		fireEditingStopped();
		ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
		for (int i = 0; i < buttons.length; i++) {
			if (e.getSource().equals(buttons[i])) {
				actions[i].actionPerformed(event);
			}
		}
	}

	/*
	 *  When the mouse is pressed the editor is invoked. If you then then drag
	 *  the mouse to another cell before releasing it, the editor is still
	 *  active. Make sure editing is stopped when the mouse is released.
	 */
	public void mousePressed(MouseEvent e) {
		if (table.isEditing() && table.getCellEditor() == this)
			isButtonColumnEditor = true;
	}

	public void mouseReleased(MouseEvent e) {
		if (isButtonColumnEditor && table.isEditing())
			table.getCellEditor().stopCellEditing();
		isButtonColumnEditor = false;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		JTable table = new JTable(2, 2);
		table.setRowHeight(36);
//FIXME: Java 6
//		table.setAutoCreateRowSorter(true);
		final TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(200);
		columnModel.getColumn(1).setPreferredWidth(400);
		JButton[] btns = new JButton[] { new JButton("Deactivate"), new JButton("Configure") };
		Action[] actions = new Action[] { new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Deactivating");
			}
		}, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Configuring");
			}
		} };
		new ButtonsInColumn(table, btns, actions, 1);
		JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), table);
	}
}
