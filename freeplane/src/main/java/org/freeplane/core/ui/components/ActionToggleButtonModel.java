package org.freeplane.core.ui.components;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.JToggleButton;

import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.util.Compat;

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
		if(Compat.isMacOsX()) {
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
			if (currentEvent instanceof ItemEvent
					&& (((ItemEvent) currentEvent).getStateChange() == ItemEvent.SELECTED)
					== isSelected()) {
				return;
			}
		}
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