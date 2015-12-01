package org.freeplane.features.encrypt;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

public class SwingPasswordStrategy implements PasswordStrategy {

    private boolean isCancelled;

    public StringBuilder getPassword() {
        return getPasswordImpl(false);
    }

    public StringBuilder getPasswordWithConfirmation() {
        return getPasswordImpl(true);
    }

    private StringBuilder getPasswordImpl(boolean withConfirmation) {
        final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(UITools.getCurrentFrame(), withConfirmation);
        pwdDialog.setModal(true);
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
