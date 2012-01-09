package org.freeplane.plugin.script.addons;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.freeplane.main.addons.AddOnProperties;

/**
 * Editor and Renderer for multiple buttons inside a table cell.
 * @author Mag. Stefan Hagmann 
 * @see http://www.bgbaden-frauen.ac.at/frauengasse20/uploads/files/Informatik/java/ButtonsInColumn.java
 */
@SuppressWarnings("serial")
class ButtonsInCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener,
        MouseListener {
	static final int BUTTON_SPACER = 4;
	private final JTable table;
	private final Border border;
	private Border fborder;
	private Object editorValue;
	private boolean isButtonColumnEditor;
	private JPanel panel;
	private final Action[] actions;
	private final JButton[] buttons;

	public ButtonsInCellRenderer(JTable table, JButton[] buttons, Action[] actions, int column) {
		this.table = table;
		this.actions = actions;
		this.buttons = buttons;
		for (JButton btn : buttons) {
			btn.setFocusPainted(false);
			btn.addActionListener(this);
		}
		border = buttons[0].getBorder();
		setFocusBorder(new LineBorder(Color.BLUE));
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
		table.addMouseListener(this);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalGlue());
		if (buttons.length > 0)
			panel.add(buttons[0]);
		for (int i = 1; i < buttons.length; i++) {
			panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
			panel.add(buttons[i]);
		}
		panel.add(Box.createHorizontalStrut(BUTTON_SPACER));
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
//FIXME: Java 6
//			final AddOnProperties addOn = model.getAddOnAt(table.convertRowIndexToModel(row));
		setButtonsVisible(table, row);
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

	protected void setButtonsVisible(JTable table, int row) {
		final ManageAddOnsPanel.AddOnTableModel model = (ManageAddOnsPanel.AddOnTableModel) table.getModel();
	    final AddOnProperties addOn = model.getAddOnAt(row);
		for (JButton btn : buttons) {
			final boolean supportsOperation = addOn.supportsOperation(btn.getName());
			btn.setVisible(supportsOperation);
		}
    }

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.editorValue = value;
		setButtonsVisible(table, row);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
//FIXME: Java 6
//		int row = table.convertRowIndexToModel(table.getEditingRow());
		int row = table.getEditingRow();
		fireEditingStopped();
		for (int i = 0; i < buttons.length; i++) {
			if (e.getSource().equals(buttons[i])) {
				final ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
				actions[i].actionPerformed(event);
				break;
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
}