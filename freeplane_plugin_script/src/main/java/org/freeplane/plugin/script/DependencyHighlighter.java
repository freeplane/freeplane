package org.freeplane.plugin.script;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.extension.HighlightedElements;
import org.freeplane.core.util.Pair;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.HighlightedTransformedObject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class DependencyHighlighter {
	private final LinkController linkController;
	private final Configurable configurable;

	DependencyHighlighter(final LinkController linkController, final Configurable configurable) {
		this.linkController = linkController;
		this.configurable = configurable;
	}

	void showCyclicDependency(final NodeScript nodeScript) {
		final Controller controller = Controller.getCurrentController();
		if (controller.getMap() != nodeScript.node.getMap())
			return;
		final List<NodeScript> cycle = FormulaThreadLocalStack.INSTANCE.findCycle(nodeScript);
		if (cycle.isEmpty())
			return;
		final Configurable configurable = controller.getMapViewManager().getMapViewConfiguration();
		highlightAttributes(cycle);
		showConnectors(cycle);
		configurable.refresh();
	}

	private void showConnectors(final List<NodeScript> cycle) {
		final Connectors connectors = Connectors.of(configurable);
		connectors.clear();
		final Set<Pair<NodeModel, NodeModel>> connectedNodes = new LinkedHashSet<>();
		for (int i = 0; i < cycle.size() - 1; i++) {
			NodeModel first = cycle.get(i).node;
			NodeModel second = cycle.get(i + 1).node;
			connectedNodes.add(new Pair<>(first, second));
		}
		connectedNodes.stream().map(this::createConnector).forEach(connectors::add);
	}

	private ConnectorModel createConnector(final Pair<NodeModel, NodeModel> connectedNodes) {
		return createConnector(connectedNodes.first, connectedNodes.second.createID());
	}

	private ConnectorModel createConnector(final NodeModel source, final String targetId) {
		return new ConnectorModel(source, targetId,
			ConnectorArrows.FORWARD, null,
			HighlightedTransformedObject.FAILURE_COLOR,
			linkController.getStandardConnectorAlpha(),
			linkController.getStandardConnectorShape(),
			linkController.getStandardConnectorWidth(),
			linkController.getStandardLabelFontFamily(),
			linkController.getStandardLabelFontSize());
	}

	private void highlightAttributes(final List<NodeScript> cycle) {
		final HighlightedElements highlightedElements = HighlightedElements.of(configurable);
		highlightedElements.clear();
		cycle.stream().map(NodeScript::containingElements).map(RelatedElements::getElements)
			.flatMap(Collection::stream)
			.forEach(highlightedElements::add);
	}
}
