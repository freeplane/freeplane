/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.codeexplorermode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class CodeLinkController extends LinkController {

    private static final Color VISIBLE_CONNECTOR_COLOR = Color.GREEN;

    public CodeLinkController(ModeController modeController) {
        super(modeController);
    }

    @Override
    public Color getColor(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ? VISIBLE_CONNECTOR_COLOR : Color.LIGHT_GRAY;
    }

    @Override
    public int[] getDashArray(ConnectorModel connector) {
        return getStandardDashArray();

    }

    @Override
    public int getWidth(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ?
                1 + (int) Math.log(((CodeConnectorModel)connector).weight())
                : 1;

    }

    @Override
    public int getOpacity(ConnectorModel connector) {
        return getStandardConnectorOpacity();
    }

    @Override
    public String getMiddleLabel(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ?
                Integer.toString(((CodeConnectorModel)connector).weight()) :
                    "";
    }

    private boolean areConnectorNodesSelected(ConnectorModel connector) {
        Set<NodeModel> selection = Controller.getCurrentController().getSelection().getSelection();
        return selection.size() == 1 && (selection.contains(connector.getSource()) || selection.contains(connector.getTarget()))
                || selection.size() > 1 && (selection.contains(connector.getSource()) && selection.contains(connector.getTarget()));
    }

    @Override
    public String getSourceLabel(ConnectorModel connector) {
       return "";
    }

    @Override
    public String getTargetLabel(ConnectorModel connector) {
        return "";
    }

    @Override
    public String getLabelFontFamily(ConnectorModel connector) {
        return getStandardLabelFontFamily();

    }

    @Override
    public int getLabelFontSize(ConnectorModel connector) {
        return getStandardLabelFontSize();
    }

    @Override
    public ConnectorShape getShape(ConnectorModel connector) {
        return ConnectorShape.CUBIC_CURVE;
    }

    @Override
    public ConnectorArrows getArrows(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ? ConnectorArrows.FORWARD : ConnectorArrows.NONE;
    }

    @Override
    public String getLinkShortText(NodeModel node) {
        return null;
    }



    @Override
    public boolean hasNodeLinks(MapModel map, JComponent component) {
       return true;
    }

    @Override
    public Collection<? extends NodeLinkModel> getLinksTo(NodeModel node, Configurable component) {
       return Collections.emptyList();
    }

    @Override
    public Collection<? extends NodeLinkModel> getLinksFrom(NodeModel node,
            Configurable component) {
        return ((CodeNodeModel)node).getOutgoingLinks(component);

    }

    @Override
    public Component getPopupForModel(Object obj) {
        return new JLabel("To be done");

    }

    @Override
    public Icon getLinkIcon(Hyperlink link, NodeModel model) {
        return null;
    }

    @Override
    public Point getStartInclination(ConnectorModel connector) {
        return null;
    }

    @Override
    public Point getEndInclination(ConnectorModel connector) {
        return null;
    }

}
