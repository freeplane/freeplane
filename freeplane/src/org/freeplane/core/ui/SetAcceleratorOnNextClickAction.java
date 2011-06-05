package org.freeplane.core.ui;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

public class SetAcceleratorOnNextClickAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private KeyStroke accelerator;

	public SetAcceleratorOnNextClickAction(final KeyStroke accelerator) {
        super("SetAcceleratorOnNextClickAction");
        this.accelerator = accelerator;
    }

    public SetAcceleratorOnNextClickAction() {
		this(null);
	}

	public void actionPerformed(final ActionEvent e) {
		AccelerateableAction.setNewAcceleratorOnNextClick(accelerator);
	}
}
