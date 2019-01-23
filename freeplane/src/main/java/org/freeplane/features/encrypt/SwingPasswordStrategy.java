package org.freeplane.features.encrypt;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class SwingPasswordStrategy implements PasswordStrategy {

    private boolean isCancelled;

    public StringBuilder getPassword(NodeModel node) {
        final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(UITools.getCurrentFrame(), false);
		return getPassword(pwdDialog, node);
    }

    public StringBuilder getPasswordWithConfirmation(NodeModel node) {
        final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(UITools.getCurrentFrame(), true);
		return getPassword(pwdDialog, node);
    }

    private StringBuilder getPassword(final EnterPasswordDialog pwdDialog, NodeModel node) {
    	UITools.setDialogLocationUnder(pwdDialog, node);
		pwdDialog.setVisible(true);
        if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
            isCancelled = true;
            return null;
        }
        return pwdDialog.getPassword();
	}

    public void onWrongPassword() {
        final Controller controller = Controller.getCurrentController();
        JOptionPane.showMessageDialog(controller.getViewController().getCurrentRootComponent(), TextUtils
            .getText("accessories/plugins/EncryptNode.properties_wrong_password"), "Freeplane",
            JOptionPane.ERROR_MESSAGE);
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}
