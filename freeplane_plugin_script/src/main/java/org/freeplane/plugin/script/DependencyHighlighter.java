package org.freeplane.plugin.script;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

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
import org.freeplane.plugin.script.dependencies.RelatedElements;

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
		final List<NodeScript> cycle = FormulaThreadLocalStacks.INSTANCE.findCycle(nodeScript);
		if (cycle.isEmpty())
			return;
		showCycle(cycle);
	}

	private void showCycle(final List<NodeScript> cycle) {
		final HighlightedElements highlightedElements = HighlightedElements.of(configurable);
		highlightedElements.clear();
		Stream<Object> relatedElements = cycle.stream().map(NodeScript::containingElements).map(RelatedElements::getElements)
				.flatMap(Collection::stream);
		final Connectors connectors = Connectors.of(configurable);
		connectors.clear();
		final Set<Pair<NodeModel, NodeModel>> connectedNodes = new LinkedHashSet<>();
		for (int i = 0; i < cycle.size() - 1; i++) {
			final NodeModel first = cycle.get(i).node;
			final NodeModel second = cycle.get(i + 1).node;
			connectedNodes.add(new Pair<>(first, second));
		}
		SwingUtilities.invokeLater(new Runnable() {
			private ConnectorModel createConnector(final Pair<NodeModel, NodeModel> connectedNodes) {
				return createConnector(connectedNodes.first, connectedNodes.second.createID());
			}

			private ConnectorModel createConnector(final NodeModel source, final String targetId) {
 				ConnectorModel connectorModel = new ConnectorModel(source, targetId);
                connectorModel.setColor(Optional.of(HighlightedTransformedObject.FAILURE_COLOR));
                connectorModel.setStartArrow(Optional.of(ConnectorArrows.FORWARD.start));
                connectorModel.setEndArrow(Optional.of(ConnectorArrows.FORWARD.end));
                return connectorModel;
			}
			@Override
			public void run() {
				relatedElements.forEach(highlightedElements::add);
				connectedNodes.stream().map(this::createConnector).forEach(connectors::add);
				configurable.refresh();
			}
		});
	}
	public void clear() {
		configurable.removeExtension(HighlightedElements.class);
		configurable.removeExtension(Connectors.class);
	}
}
