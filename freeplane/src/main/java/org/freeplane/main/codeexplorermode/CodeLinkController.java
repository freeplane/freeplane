/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

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
import org.freeplane.view.swing.map.MapView;

public class CodeLinkController extends LinkController {

    private static final Color VISIBLE_CONNECTOR_COLOR = Color.GREEN;

    private static final Point backwardsConnectorStartInclination = new Point(100, 10);
    private static final Point upwardsConnectorStartInclination = new Point(-backwardsConnectorStartInclination.x, -backwardsConnectorStartInclination.y);
    private static final Point backwardsConnectorEndInclination = new Point(backwardsConnectorStartInclination.x, -backwardsConnectorStartInclination.y);
    private static final Point upwardsConnectorEndInclination = new Point(upwardsConnectorStartInclination.x, -upwardsConnectorStartInclination.y);


    public CodeLinkController(ModeController modeController) {
        super(modeController);
    }

    @Override
    public Color getColor(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ? VISIBLE_CONNECTOR_COLOR : Color.BLACK;
    }

    @Override
    public int[] getDashArray(ConnectorModel connector) {
        return getStandardDashArray();

    }

    @Override
    public int getWidth(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ?
                1 + (int) Math.log10(((CodeConnectorModel)connector).weight())
                : 1;

    }

    @Override
    public int getOpacity(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ? 255 : 30;
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
        return ((CodeNodeModel)node).getOutgoingLinks(((MapView)component).getMapSelection());
    }

    @Override
    public Component getPopupForModel(Object obj) {
        if(obj instanceof CodeConnectorModel)
            return new JLabel("To be done");
        else
            return null;

    }

    @Override
    public Icon getLinkIcon(Hyperlink link, NodeModel model) {
        return null;
    }

    @Override
    public Point getStartInclination(ConnectorModel connector) {
        return ((CodeConnectorModel)connector).goesUp() ? upwardsConnectorStartInclination : backwardsConnectorStartInclination;
    }

    @Override
    public Point getEndInclination(ConnectorModel connector) {
        return ((CodeConnectorModel)connector).goesUp() ? upwardsConnectorEndInclination : backwardsConnectorEndInclination;
    }

}