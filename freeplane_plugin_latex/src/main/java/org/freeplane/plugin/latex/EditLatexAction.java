package org.freeplane.plugin.latex;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * 
 * @author Stefan Ott
 *
 * This class is called when a (legacy!) Latex formula is edited
 * @see http://freeplane.sourceforge.net/wiki/index.php/LaTeX_in_Freeplane
 */
public class EditLatexAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final LatexNodeHook nodeHook;

	public EditLatexAction(final LatexNodeHook nodeHook) {
		super("LatexEditLatexAction");
		this.nodeHook = nodeHook;
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		nodeHook.editLatexInEditor(node);
		Controller.getCurrentModeController().getMapController()
		    .nodeChanged(node, NodeModel.UNKNOWN_PROPERTY, null, null);
	}
}
