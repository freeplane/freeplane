package org.freeplane.plugin.formula.dependencies;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.extension.HighlightedElements;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.Pair;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.Connectors;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.AccessedValues;
import org.freeplane.plugin.script.FormulaUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

class FormulaDependencyTracer implements IExtension {
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

	public void tracePrecedence() {
		final Collection<Pair<NodeModel, AccessedValues>> accessedValues;
		highlighedElements = configurable.computeIfAbsent(HighlightedElements.class, HighlightedElements::new);
		connectors = configurable.computeIfAbsent(Connectors.class, Connectors::new);
		if (tracedValues == null) {
			highlighedElements.clear();
			configurable.computeIfAbsent(Connectors.class, Connectors::new).clear();
			final NodeAttribute attribute = AttributeController.getSelectedAttribute();
			if (attribute != null) {
				highlighedElements.add(attribute.attribute);
				accessedValues = findPrecedences(attribute);
			} else {
				final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
				highlighedElements.add(node);
				accessedValues = findPrecedences(node);
			}
		} else {
			accessedValues = tracedValues.stream().map(this::findPrecedences).flatMap(Collection::stream).collect(Collectors.toSet());
		}
		accessedValues.forEach(this::highlightPrecedent);
		configurable.refresh();
		highlighedElements = null;
		connectors = null;
	}

	private void highlightPrecedent(final Pair<NodeModel, AccessedValues> v) {
		tracedValues = v.second.getAccessedValues();
		tracedValues.stream().map(a -> a instanceof NodeAttribute ? ((NodeAttribute) a).attribute : a).forEach(highlighedElements::add);
		v.second.getAccessedNodes().stream()
				.filter(n -> v.first != n)
				.forEach(n ->
						connectors.add(new ConnectorModel(v.first, n.getID(),
								ConnectorArrows.BACKWARD, null,
								FilterController.HIGHLIGHT_COLOR,
								linkController.getStandardConnectorAlpha(),
								linkController.getStandardConnectorShape(),
								linkController.getStandardConnectorWidth(),
								linkController.getStandardLabelFontFamily(),
								linkController.getStandardLabelFontSize())));
	}

	private Collection<Pair<NodeModel, AccessedValues>> findPrecedences(final Object value) {
		if (value instanceof NodeAttribute)
			return findPrecedences((NodeAttribute) value);
		else if (value instanceof NodeModel)
			return findPrecedences((NodeModel) value);
		else
			return Collections.emptySet();

	}

	private Collection<Pair<NodeModel, AccessedValues>> findPrecedences(final NodeModel node) {
		final Object userObject = node.getUserObject();
		if (FormulaUtils.containsFormula(userObject)) {
			return Collections.singleton(new Pair<>(node, FormulaUtils.getAccessedValues(node, ((String) userObject).substring(1))));
		} else
			return Collections.emptySet();
	}

	private Collection<Pair<NodeModel, AccessedValues>> findPrecedences(final NodeAttribute attribute) {
		final Object value = attribute.value();
		if (FormulaUtils.containsFormula(value)) {
			return Collections.singleton(new Pair<>(attribute.node, FormulaUtils.getAccessedValues(attribute.node, (String) value)));
		} else
			return Collections.emptySet();
	}

	void clear() {
		configurable.removeExtension(HighlightedElements.class);
		configurable.removeExtension(Connectors.class);
		configurable.removeExtension(this);
		configurable.refresh();
		tracedValues = null;
	}
}
