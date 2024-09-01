package org.freeplane.core.ui.commandtonode;

import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

public class CommandToNode {
    private static final int MAX_TEXT_LENGTH = 50;
    private static final String LINK_STARTING_TEXT = "menuitem:_";

    public static void insertNode(AFreeplaneAction action, JDialog newNodeLinkedToMenuItemDialog) {
        if (action == null) {
            showMessageInStatusLine("NewNodeLinkedToMenu.NoCommandFound");
            return;
        }
        MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
        NodeModel parent = mapController.getSelectedNode();
        String nodeText = getCommandText(action);
        NodeModel menuItemNode = new NodeModel(nodeText, parent.getMap());
        if (parent.isRoot())
            menuItemNode.setSide(MapController.suggestNewChildSide(parent, NodeModel.Side.DEFAULT));
        mapController.insertNode(menuItemNode, parent, parent.getChildCount());
        Controller.getCurrentController().getMapViewManager().displayOnCurrentView(menuItemNode);
        SwingUtilities.invokeLater(() -> {
            IMapSelection selection = Controller.getCurrentController().getSelection();
            selection.scrollNodeToVisible(menuItemNode);
            UITools.setDialogLocationRelativeTo(newNodeLinkedToMenuItemDialog, menuItemNode);
        });
        String nodeLink = LINK_STARTING_TEXT + action.getKey();
        try {
            ((MLinkController) LinkController.getController()).setLink(menuItemNode, new URI(nodeLink), LinkController.LINK_ABSOLUTE);
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
