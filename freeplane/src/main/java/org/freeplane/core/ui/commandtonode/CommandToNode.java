package org.freeplane.core.ui.commandtonode;

import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

public class CommandToNode {
    private static final int MAX_TEXT_LENGTH = 50;
    private static final String LINK_STARTING_TEXT = "menuitem:_";

    public static void insertNode(AFreeplaneAction action) {
        if (action == null) {
            showMessageInStatusLine("NewNodeLinkedToMenu.NoCommandFound");
            return;
        }
        MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
        NodeModel parent = mapController.getSelectedNode();
        String nodeText = getCommandText(action);
        NodeModel newNodeModel = new NodeModel(nodeText, parent.getMap());
        if (parent.isRoot())
            newNodeModel.setSide(MapController.suggestNewChildSide(parent, NodeModel.Side.DEFAULT));
        mapController.insertNode(newNodeModel, parent, parent.getChildCount());
        String nodeLink = LINK_STARTING_TEXT + action.getKey();
        try {
            ((MLinkController) LinkController.getController()).setLink(newNodeModel, new URI(nodeLink), LinkController.LINK_ABSOLUTE);
            showMessageInStatusLine("NewNodeLinkedToMenu.NodeInserted");
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getCommandText(AFreeplaneAction action) {
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

    private static String getActionText(String key, String type) {
        String texto = TextUtils.getText(key + "." + type, null);
        String resp;
        if (texto != null && !texto.isEmpty() && !texto.equals("null")){
            resp = TextUtils.getShortText(texto, MAX_TEXT_LENGTH,".");
        } else {
            resp = null;
        }
        return resp;
    }

    private static void showMessageInStatusLine(String key) {
        String msg = TextUtils.getRawText(key);
        Controller.getCurrentController().getViewController().out(msg);
    }

}
