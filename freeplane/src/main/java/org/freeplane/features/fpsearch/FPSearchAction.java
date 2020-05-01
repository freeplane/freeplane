package org.freeplane.features.fpsearch;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

import java.awt.*;
import java.awt.event.ActionEvent;

public class FPSearchAction extends AFreeplaneAction {
    static final String KEY = "FPSearchAction";

    public FPSearchAction()
    {
        super(KEY);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        Component root = Controller.getCurrentController().getViewController().getCurrentRootComponent();
        FPSearchDialog fpSearchDialog = new FPSearchDialog((Frame) root);
        fpSearchDialog.setVisible(true);
    }
}
