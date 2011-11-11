package org.freeplane.plugin.script.addons;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.main.addons.AddOnsController;

public class ManageAddOnsAction extends AFreeplaneAction {
    private static final long serialVersionUID = 1L;
	private ManageAddOnsDialog dialog;

	public ManageAddOnsAction() {
	    super("ManageAddOnsAction");
    }

	public void actionPerformed(ActionEvent e) {
		getDialog().setVisible(true);
	}

	public ManageAddOnsDialog getDialog() {
		if (dialog == null)
			dialog = new ManageAddOnsDialog(AddOnsController.getController().getInstalledAddOns());
    	return dialog;
    }
}
