package org.freeplane.core.ui.components;

import javax.swing.JToggleButton;

import org.freeplane.core.ui.IFreeplaneAction;

class ActionToggleButtonModel extends JToggleButton.ToggleButtonModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final IFreeplaneAction action;

	ActionToggleButtonModel(final IFreeplaneAction action) {
		this.action = action;
		setSelected(action.isSelected());
	}

	@Override
	public void setSelected(boolean b) {
		super.setSelected(b);
		if(b != action.isSelected())
			action.setSelected(b);
	}
}