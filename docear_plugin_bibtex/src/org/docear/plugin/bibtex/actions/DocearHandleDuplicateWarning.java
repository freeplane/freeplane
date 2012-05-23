package org.docear.plugin.bibtex.actions;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

import net.sf.jabref.BasePanel;
import net.sf.jabref.Globals;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;

public class DocearHandleDuplicateWarning implements PostOpenAction {

	@Override
	public boolean isActionNecessary(ParserResult pr) {
		return pr.hasDuplicateKeys();
	}

	@Override
	public void performAction(BasePanel panel, ParserResult pr) {
		Object[] options = {TextUtils.getText("yes"),TextUtils.getText("dialog.resolve_duplicate_keys.4"),TextUtils.getText("no")};
		int answer = JOptionPane.showOptionDialog(null,
                "<html><p>"+TextUtils.getText("dialog.resolve_duplicate_keys.1")
                +"</p><p>"+TextUtils.getText("dialog.resolve_duplicate_keys.2"),
                TextUtils.getText("dialog.resolve_duplicate_keys.2"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (answer == JOptionPane.YES_OPTION) {
            panel.runCommand("resolveDuplicateKeys");
        }
        if (answer == JOptionPane.NO_OPTION) {
        	ResourceController.getResourceController().setProperty("docear.reference_manager.resolve_duplicate_keys", true);
        	new HandleDuplicateKeys().performAction(panel, pr);
        }
	}

}
