/*
 * Created on 24 Dec 2024
 *
 * author dimitry
 */
package org.freeplane.core.util.logging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.core.ui.sounds.SoundClipPlayer;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

public class ErrorLogButton {
    private static final String SHOW_STATUS_BAR_ON_ERROR_PROPERTY = "showStatusBarOnError";

	private static class LogOpener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String freeplaneLogDirectoryPath = LogUtils.getLogDirectory();
            final File file = new File(freeplaneLogDirectoryPath);
            if (file.isDirectory()) {
                final ViewController viewController = Controller.getCurrentController().getViewController();
                try {
                    viewController.openDocument(file.toURL());
                }
                catch (Exception ex) {
                }
            }
        }
    }
    private static int errorCounter = 0;
    private static JButton logButton;
    public static void showButton() {
            errorCounter++;
            Controller controller = Controller.getCurrentController();
            if(controller == null)
                return;
            final ViewController viewController = controller.getViewController();
            if(viewController == null)
                return;
            if (TextUtils.getRawText("internal_error.tooltip", null) != null) {
                if(viewController.isDispatchThread())
                    showButtonNow(viewController);
                else
                    viewController.invokeLater(() -> showButtonNow(viewController));
            }
    }

    private static void showButtonNow(final ViewController viewController) {
        try {
            if (logButton == null) {
                final Icon errorIcon = ResourceController.getResourceController()
                        .getIcon("warning_icon");
                logButton = new JButton();
                logButton.addActionListener(new LogOpener());
                logButton.setIcon(errorIcon);
                String tooltip = TextUtils.getText("internal_error.tooltip");
                logButton.setToolTipText(tooltip);
                viewController.addStatusComponent("internal_error", logButton);
            }
            logButton.setText(TextUtils.format("errornumber", errorCounter));
            final JComponent statusBar = viewController.getStatusBar();
            if (!statusBar.isVisible()) {
            	if(!ResourceController.getResourceController().getBooleanProperty(SHOW_STATUS_BAR_ON_ERROR_PROPERTY, true))
					return;
            	UIComponentVisibilityDispatcher.of(statusBar).setVisible(true);
			}
            SoundClipPlayer.playSound("error");
        }
        catch (Exception e) {
        }
    }
}