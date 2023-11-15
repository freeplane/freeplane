/*
 * Created on 9 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeRelativePath;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

class CodeLinkController extends LinkController {

    private static final Color VISIBLE_CONNECTOR_COLOR = Color.GREEN;

    private static final Point backwardsConnectorStartInclination = new Point(150, 15);
    private static final Point upwardsConnectorStartInclination = new Point(-backwardsConnectorStartInclination.x, -backwardsConnectorStartInclination.y);
    private static final Point backwardsConnectorEndInclination = new Point(backwardsConnectorStartInclination.x, -backwardsConnectorStartInclination.y);
    private static final Point upwardsConnectorEndInclination = new Point(upwardsConnectorStartInclination.x, -upwardsConnectorStartInclination.y);


    CodeLinkController(ModeController modeController) {
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
        return areConnectorNodesSelected(connector) ? 128 : 30;
    }

    @Override
    public String getMiddleLabel(ConnectorModel connector) {
        return areConnectorNodesSelected(connector) ?
                Integer.toString(((CodeConnectorModel)connector).weight()) :
                    "";
    }

    private boolean areConnectorNodesSelected(ConnectorModel connector) {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        return areConnectorNodesSelected((CodeConnectorModel)connector, selection);
    }

    private boolean areConnectorNodesSelected(CodeConnectorModel connector, IMapSelection selection) {
         CodeNodeModel source = (CodeNodeModel) connector.getSource();
         CodeNodeModel target = (CodeNodeModel) connector.getTarget();
         return isConnectorSelected(selection, source, target);
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
        return 8;
    }

    @Override
    public int getLabelFontStyle(ConnectorModel connector) {
       return Font.BOLD;
    }

    @Override
    public Color getLabelColor(ConnectorModel connector) {
       return Color.BLACK;
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
        IMapSelection selection = ((MapView)component).getMapSelection();
        if (node.isLeaf() || selection.isFolded(node)) {
            Stream<Dependency> dependencies = ((CodeNodeModel)node).getOutgoingDependencies();
            List<CodeConnectorModel> connectors = toConnectors(node, dependencies, Dependency::getTargetClass, selection);
            return connectors;
        }
        else
            return Collections.emptyList();
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


    private List<CodeConnectorModel> toConnectors(NodeModel source, Stream<Dependency> dependencies,
            Function<Dependency, JavaClass> dependencyToJavaClass,
            IMapSelection mapSelection) {
        DependencySelection dependencySelection = new DependencySelection(mapSelection);
        Map<String, Long> countedDependencies = dependencies
                .map(dep -> dependencySelection.getVisibleNodeId(dependencyToJavaClass.apply(dep)))
                .filter(name -> name != null && ! name.equals(source.getID()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<CodeConnectorModel> connectors = countedDependencies.entrySet().stream()
            .map(e -> createConnector(source, e.getKey(), e.getValue().intValue()))
            .collect(Collectors.toList());
        return connectors;
    }

    private CodeConnectorModel createConnector(NodeModel source, String targetId, int weight) {
        NodeModel target = source.getMap().getNodeForID(targetId);
        NodeRelativePath nodeRelativePath = new NodeRelativePath(source, target);
        return new CodeConnectorModel(source, targetId, weight, nodeRelativePath.compareNodePositions() > 0);
    }

    private boolean isConnectorSelected(IMapSelection selection, CodeNodeModel source, CodeNodeModel target) {
        if(selection.getSelection().contains(source.getMap().getRootNode()))
            return true;
        List<NodeModel> selectedNodes = selection.getSortedSelection(true);
        Optional<NodeModel> selectedSourceDescendantOrSource = findSelectedDescendantOrSelf(source, selectedNodes);
        boolean isSourceSelected = selectedSourceDescendantOrSource.isPresent();
        if (selectedNodes.size() == 1 && isSourceSelected)
            return ! target.isDescendantOf(selectedSourceDescendantOrSource.get());
        Optional<NodeModel> selectedTargetDescendantOrTarget = findSelectedDescendantOrSelf(target, selectedNodes);
        boolean isTargetSelected = selectedTargetDescendantOrTarget.isPresent();
        if (selectedNodes.size() == 1)
            return isTargetSelected && ! source.isDescendantOf(selectedTargetDescendantOrTarget.get());
        else if (selectedNodes.size() > 1 && (isSourceSelected && isTargetSelected))
            return selectedSourceDescendantOrSource.get() != selectedTargetDescendantOrTarget.get();
        else
            return false;
    }

     private Optional<NodeModel> findSelectedDescendantOrSelf(NodeModel node, List<NodeModel> selectedNodes) {
         return selectedNodes.stream()
                 .filter(n -> node == n || node.isDescendantOf(n))
                 .findAny();
     }

}
