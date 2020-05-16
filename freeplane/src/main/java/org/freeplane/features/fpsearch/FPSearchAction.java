package org.freeplane.features.fpsearch;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class FPSearchAction extends AFreeplaneAction {
    static final String KEY = "FPSearchAction";

    public FPSearchAction()
    {
        super(KEY);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        FPSearchDialog fpSearchDialog = new FPSearchDialog(UITools.getCurrentFrame());
    }
}
