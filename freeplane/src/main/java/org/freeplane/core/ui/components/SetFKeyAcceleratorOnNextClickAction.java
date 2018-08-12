package org.freeplane.core.ui.components;

import static org.freeplane.core.resources.ResourceController.getResourceController;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;

public class SetFKeyAcceleratorOnNextClickAction extends AbstractAction {
	private static final String IGNORE_UNASSIGNED_F_KEYS = "ignore_unassigned_f_keys";
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	final private KeyStroke accelerator;

	public SetFKeyAcceleratorOnNextClickAction(final KeyStroke accelerator) {
        this.accelerator = accelerator;
        setEnabled();
        getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			@Override
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if(propertyName.equals(IGNORE_UNASSIGNED_F_KEYS))
					setEnabled();
			}

		});
    }

	private void setEnabled() {
		final boolean enabled = !getResourceController().getBooleanProperty(IGNORE_UNASSIGNED_F_KEYS);
		setEnabled(enabled);
	}
	@Override
	public void actionPerformed(final ActionEvent e) {
		if(getResourceController().getBooleanProperty(IGNORE_UNASSIGNED_F_KEYS))
			return;
		AccelerateableAction.setNewAcceleratorOnNextClick(accelerator);
		final JCheckBox dontShowAgainBox = new JCheckBox(TextUtils.getRawText(OptionalDontShowMeAgainDialog.DONT_SHOW_AGAIN));
		final JDialog acceleratorOnNextClickActionDialog = AccelerateableAction.getAcceleratorOnNextClickActionDialog();
		acceleratorOnNextClickActionDialog.getContentPane().add(dontShowAgainBox, BorderLayout.SOUTH);
		acceleratorOnNextClickActionDialog.pack();
		dontShowAgainBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getResourceController().setProperty(IGNORE_UNASSIGNED_F_KEYS, true);
				acceleratorOnNextClickActionDialog.setVisible(false);
			}
		});
		LabelAndMnemonicSetter.setLabelAndMnemonic(dontShowAgainBox, null);
	}
}
