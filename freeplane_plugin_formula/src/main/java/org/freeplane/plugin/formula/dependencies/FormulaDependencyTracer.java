package org.freeplane.plugin.formula.dependencies;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.extension.HighlightedElements;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeSelection;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.dependencies.DependencySearchStrategy;
import org.freeplane.plugin.script.dependencies.RelatedElements;

class FormulaDependencyTracer implements IExtension {

	private DependencySearchStrategy searchStrategy;

	public static void clear(Configurable configurable) {
		configurable.removeExtension(HighlightedElements.class);
		configurable.removeExtension(Connectors.class);
		configurable.removeExtension(FormulaDependencyTracer.class);
		configurable.refresh();

	}

	private static final long serialVersionUID = 1L;
	private final Configurable configurable;
	private final LinkController linkController;
	private Collection<Object> tracedValues;
	private HighlightedElements highlighedElements;
	private Connectors connectors;

	public FormulaDependencyTracer(final Configurable configurable, final LinkController linkController) {
		this.configurable = configurable;
		this.linkController = linkController;
	}

	public void findPrecedents() {
		findDependencies(DependencySearchStrategy.PRECEDENTS);
	}

	public void findDependents() {
		findDependencies(DependencySearchStrategy.DEPENDENTS);
	}

	private void findDependencies(DependencySearchStrategy strategy) {
		final Collection<Pair<NodeModel, RelatedElements>> accessedValues;
		highlighedElements = HighlightedElements.of(configurable);
		if (tracedValues != null && searchStrategy != strategy)
			tracedValues = highlighedElements.getElements();
		searchStrategy = strategy;
		connectors = Connectors.of(configurable);
		final boolean dependenciesFoundInFirstSearchIteration;
		final boolean isFirstSearchIteration = tracedValues == null;
		if (isFirstSearchIteration) {
			highlighedElements.clear();
			configurable.computeIfAbsent(Connectors.class, Connectors::new).clear();
			final AttributeSelection attributeSelection = AttributeController.getAttributeSelection();
			if (! attributeSelection.isEmpty()) {
				attributeSelection.nodeAttributeStream().map(NodeAttribute::attribute).forEach(highlighedElements::add);
				accessedValues = attributeSelection.nodeAttributeStream().map(this::findDependencies).flatMap(Collection::stream).collect(Collectors.toSet());
			} else {
				final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
				highlighedElements.add(node);
				accessedValues = findDependencies(node);
			}
			dependenciesFoundInFirstSearchIteration = ! accessedValues.isEmpty();
		} else {
			accessedValues = tracedValues.stream().map(this::findDependencies).flatMap(Collection::stream).collect(Collectors.toSet());
			dependenciesFoundInFirstSearchIteration = true;
		}
		saveAccessedValuesForNextIteration(accessedValues);
		highlightAttributes(accessedValues);
		showConnectors(accessedValues);
		configurable.refresh();
		if(! dependenciesFoundInFirstSearchIteration)
			tracedValues = null;
		highlighedElements = null;
		connectors = null;
	}

	private void highlightAttributes(final Collection<Pair<NodeModel, RelatedElements>> accessedValues) {
		accessedValues.forEach(this::highlightAttributes);
	}

	private void saveAccessedValuesForNextIteration(final Collection<Pair<NodeModel, RelatedElements>> accessedValues) {
		tracedValues = new HashSet<Object>();
		accessedValues.forEach(v -> tracedValues.addAll(v.second.getElements()));
	}

	private Collection<Pair<NodeModel, RelatedElements>> findDependencies(final Object value) {
		if (value instanceof NodeAttribute)
			return findDependencies((NodeAttribute) value);
		else if (value instanceof NodeModel)
			return findDependencies((NodeModel) value);
		else
			return Collections.emptySet();

	}

	private Collection<Pair<NodeModel, RelatedElements>> findDependencies(final NodeModel node) {
		final RelatedElements relatedElements = searchStrategy.find(node);
		return toCollection(node, relatedElements);
	}

	private Collection<Pair<NodeModel, RelatedElements>> findDependencies(final NodeAttribute nodeAttribute) {
		final RelatedElements relatedElements = searchStrategy.find(nodeAttribute.node, nodeAttribute.attribute);
		return toCollection(nodeAttribute.node, relatedElements);
	}

	private Collection<Pair<NodeModel, RelatedElements>> toCollection(final NodeModel node,
																	  final RelatedElements relatedElements) {
		if (!relatedElements.isEmpty()) {
			return Collections.singleton(new Pair<>(node, relatedElements));
		} else
			return Collections.emptySet();
	}

	private void showConnectors(final Collection<Pair<NodeModel, RelatedElements>> accessedValues) {
		final Stream<Pair<NodeModel, NodeModel>> connectedNodes = accessedValues.stream()
			.<Pair> flatMap(pair -> pair.second.getRelatedNodes().stream().distinct()
				.map(node -> new Pair<>(pair.first, node))).distinct().map(searchStrategy::inConnectionOrder);
		connectedNodes.forEach(this::showConnectors);
	}

	private void showConnectors(final Pair<NodeModel, NodeModel> v) {
		connectors.add(createConnector(v.first, v.second.createID()));
	}

	private ConnectorModel createConnector(final NodeModel source, final String id) {
		return new ConnectorModel(source, id,
			ConnectorArrows.FORWARD, null,
			FilterController.HIGHLIGHT_COLOR,
			linkController.getStandardConnectorAlpha(),
			linkController.getStandardConnectorShape(),
			linkController.getStandardConnectorWidth(),
			linkController.getStandardLabelFontFamily(),
			linkController.getStandardLabelFontSize());
	}

	private void highlightAttributes(final Pair<NodeModel, RelatedElements> v) {
		v.second.getElements().stream().map(a -> a instanceof NodeAttribute ? ((NodeAttribute) a).attribute : a)
			.forEach(highlighedElements::add);
	}
}
