package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.mindmapmode.MModeController;

@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/scripting" })
public class ExecuteScriptForAllNodes extends AFreeplaneAction {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	final private ScriptingEngine engine;


	public ExecuteScriptForAllNodes(final Controller controller, ScriptingEngine engine) {
	    super("ExecuteScriptForAllNodes", controller);
	    this.engine = engine;
    }


    public void actionPerformed(ActionEvent e) {
		final NodeModel node = getController().getMap().getRootNode();
		getController().getViewController().setWaitingCursor(true);
		try{
			engine.performScriptOperationRecursive((MModeController) getModeController(), node);
		}
		finally{
			getController().getViewController().setWaitingCursor(false);
		}
    }

}
