package org.freeplane.core.ui.commandtonode;


import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionAcceleratorManager;

public class CommandToNodeHotKeyAction extends AFreeplaneAction {

    private boolean isRunning = false;

    public CommandToNodeHotKeyAction() {
        super("CommandToNodeHotKeyAction");
        CommandToNode dispatcher = new CommandToNode(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        isRunning = true;
        getAcceleratorManager().newAccelerator(this, null);
        isRunning = false;
    }


    boolean shouldInsertCommandNodeOnEvent(KeyEvent e) {
        if (isRunning)
            return false;
        KeyStroke currentAccelerator = getAcceleratorManager().getAccelerator(this);
        return areEqual(e, currentAccelerator);
    }

    private boolean areEqual(KeyEvent e, KeyStroke currentAccelerator) {
        return currentAccelerator != null
                && e.getKeyCode() == currentAccelerator.getKeyCode()
                && (e.getModifiers() | e.getModifiersEx()) == currentAccelerator.getModifiers();
    }

    private ActionAcceleratorManager getAcceleratorManager() {
        return ResourceController.getResourceController().getAcceleratorManager();
    }

}
