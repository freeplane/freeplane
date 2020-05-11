package org.freeplane.features.fpsearch;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

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
    }
}
