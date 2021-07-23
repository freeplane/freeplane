package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyle;

public class RedefineStyleUpdateTemplateAction extends AFreeplaneAction {
	public static final String NAME = "RedefineStyleUpdateTemplateAction";
    private static final long serialVersionUID = 1L;
	
	public RedefineStyleUpdateTemplateAction() {
		super(NAME);
	}

	public void actionPerformed(final ActionEvent e) {
	    final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
        MapStyle.getController().redefineStyle(node, true);
	}
}
