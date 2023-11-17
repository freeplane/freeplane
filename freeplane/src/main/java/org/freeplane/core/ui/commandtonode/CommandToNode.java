package org.freeplane.core.ui.commandtonode;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

public class CommandToNode implements KeyEventDispatcher {

    private CommandToNodeHotKeyAction commandToNodeHotKeyAction;
    private static final int MAX_TEXT_LENGTH = 50;
    private static final String LINK_STARTING_TEXT = "menuitem:_";


    public CommandToNode(CommandToNodeHotKeyAction commandToNodeHotKeyAction) {
        this.commandToNodeHotKeyAction = commandToNodeHotKeyAction;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (commandToNodeHotKeyAction.shouldInsertCommandNodeOnEvent(e)) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                insertNode();
            }
            return true;
        }
       return false;
    }

    private void insertNode() {

        // get Component under mouse
        Component component = getComponentUnderMouse();

        // get its AbstractButton
        AbstractButton abstractButton = null;
        if (!(component instanceof AbstractButton)){
            showMessageInStatusLine("CommandToNode.NoCommandFound");
            return;
        }
        abstractButton = ((AbstractButton) component);

        // get button's Action
        AFreeplaneAction action = getAction(abstractButton);
        if(action==null){
            showMessageInStatusLine("CommandToNode.NoCommandFound");
            return;
        }

        // get selected node
        MMapController mapController = getMapController();
        NodeModel parent = mapController.getSelectedNode();

        // get command text from action
        String nodeText = getCommandText(action);

        // create child node
        NodeModel newNodeModel = new NodeModel(nodeText, parent.getMap());
        if(parent.isRoot())
            newNodeModel.setSide( MapController.suggestNewChildSide(parent, NodeModel.Side.DEFAULT));
        mapController.insertNode(newNodeModel, parent, parent.getChildCount());

        // get link string from action
        String nodeLink = LINK_STARTING_TEXT + action.getKey();

        // set node's link
        try {
            getLinkController().setLink(newNodeModel, new URI(nodeLink), LinkController.LINK_ABSOLUTE);
            showMessageInStatusLine("CommandToNode.NodeInserted");
        }
        catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getCommandText(AFreeplaneAction action) {
        String texto = getActionText(action.getKey(),"text");
        if(texto != null && !texto.isEmpty())
            return texto;

        texto = getActionText(action.getKey(),"tooltip");
        if(texto != null && !texto.isEmpty())
            return texto;

        texto = action.getRawText();
        if(texto != null && !texto.isEmpty())
            return texto;

        return action.getKey() ;
    }

    private String getActionText(String key, String type) {
        String texto = TextUtils.getText(key + "." + type, null);
        String resp;
        if (texto != null && !texto.isEmpty() && !texto.equals("null")){
            resp = TextUtils.getShortText(texto, MAX_TEXT_LENGTH,".");
        } else {
            resp = null;
        }
        return resp;
    }

    private Component getComponentUnderMouse(){
        for (Window window : Window.getWindows()) {
            final Point mousePosition = window.getMousePosition(true);
            if (mousePosition != null) {
                final Component componentUnderMouse = SwingUtilities.getDeepestComponentAt(window, mousePosition.x,
                        mousePosition.y);
                return componentUnderMouse;
            }
        }
        return null;
    }

    private AFreeplaneAction getAction(AbstractButton abstractButton){
        Action action = abstractButton.getAction();
        if(action instanceof AccelerateableAction){
            action = ((AccelerateableAction) action).getOriginalAction();
        }
        return (AFreeplaneAction) action;

    }

    private void showMessageInStatusLine(String key) {
        String msg = TextUtils.getRawText(key);
        Controller.getCurrentController().getViewController().out(msg);
    }

    private MLinkController getLinkController() {
        return (MLinkController) LinkController.getController();
    }

    private MMapController getMapController() {
        return (MMapController) Controller.getCurrentModeController().getMapController();
    }

}
