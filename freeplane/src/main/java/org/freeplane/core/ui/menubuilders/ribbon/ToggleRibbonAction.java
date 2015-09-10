package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;

@SelectableAction(checkOnPopup = true)
public class ToggleRibbonAction extends AFreeplaneAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRibbon ribbon;

	public ToggleRibbonAction(JRibbon ribbon) {
		super("ToggleRibbonAction");
		this.ribbon = ribbon;
	}
	
	public void actionPerformed(ActionEvent e) {
		setMinimized(!isMinimized());
	}
	
	@Override
	public void setSelected() {
		setSelected(isMinimized());
	}
	
	public void setMinimized(boolean b) {
		ribbon.setMinimized(b);
		OneTouchCollapseResizer otcr = OneTouchCollapseResizer.findResizerFor(ribbon);
		if(otcr != null) {
			otcr.recalibrate();
		}
	}

	public boolean isMinimized() {
		return ribbon.isMinimized();
	}

}
