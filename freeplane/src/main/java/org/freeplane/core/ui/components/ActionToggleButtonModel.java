package org.freeplane.core.ui.components;

import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

import org.freeplane.core.ui.IFreeplaneAction;

class ActionToggleButtonModel extends JToggleButton.ToggleButtonModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean ignoreSetSelected = false;

	ActionToggleButtonModel(final IFreeplaneAction action) {
	}

	@Override
	public void setSelected(boolean b) {
		if(ignoreSetSelected)
			return;
		super.setSelected(b);
	}

	@Override
    public void setPressed(boolean b) {
		ignoreSetSelected = true;
	    try {
	        super.setPressed(b);
        }
        finally {
        	ignoreSetSelected = false;
        }
    }

	@Override
    protected void fireActionPerformed(ActionEvent e) {
		boolean setSelectedWasIgnored = ignoreSetSelected;
		ignoreSetSelected = false;
	    try {
		    super.fireActionPerformed(e);
        }
        finally {
        	ignoreSetSelected = setSelectedWasIgnored;
        }
    }
	
	
}