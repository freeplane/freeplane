package org.freeplane.plugin.script.proxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.freeplane.api.Dependencies;
import org.freeplane.api.DependencyLookup;
import org.freeplane.api.NodeRO;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.dependencies.DependenciesBuilder;
import org.freeplane.plugin.script.dependencies.DependencySearchStrategy;
import org.freeplane.plugin.script.dependencies.RelatedElements;

public class DependencyLookupProxy extends AbstractProxy<NodeModel>implements DependencyLookup {
	private final DependencySearchStrategy searchStrategy;

	public DependencyLookupProxy(NodeModel delegate, ScriptContext scriptContext,
								 DependencySearchStrategy searchStrategy) {
		super(delegate, scriptContext);
		this.searchStrategy = searchStrategy;
	}

	@Override
	public Map<? extends NodeRO, Dependencies> ofNode() {
		final RelatedElements elements = searchStrategy.find(getDelegate());
		return toMap(elements);
	}

	@Override
	public Map<? extends NodeRO, Dependencies> ofAttribute(int attributeIndex) {
		final NodeAttributeTableModel attributes = getDelegate().getExtension(NodeAttributeTableModel.class);
		if(attributes != null && attributeIndex >= 0 && attributeIndex < attributes.getRowCount()) {
			Attribute attribute = attributes.getAttribute(attributeIndex);
			final RelatedElements elements = searchStrategy.find(getDelegate(), attribute);
			return toMap(elements);
		}
		else
			return Collections.emptyMap();
	}

	@Override
	public Map<? extends NodeRO, Dependencies> ofAttribute(String attributeName) {
		final NodeAttributeTableModel attributes = getDelegate().getExtension(NodeAttributeTableModel.class);
		if(attributes != null ) {
			return ofAttribute(attributes.getAttributeIndex(attributeName));
		}
			return Collections.emptyMap();
	}

	private Map<? extends NodeRO, Dependencies> toMap(RelatedElements elements) {
		if(elements.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<NodeModel, DependenciesBuilder> builders =  new HashMap<>();
		for(Entry<Object, NodeModel> pair : elements.entrySet()) {
			final Object object = pair.getKey();
			final NodeModel node = pair.getValue();
			final DependenciesBuilder builder = builders.computeIfAbsent(node, (n) -> new DependenciesBuilder(n.getExtension(NodeAttributeTableModel.class)));
			if(object == node)
				builder.setNodeContained();
			else if(object instanceof Attribute){
				builder.addAttribute((Attribute) object);
			}
		}
		Map<NodeProxy, Dependencies> dependencies =  new HashMap<>();
		builders.forEach((n, b) -> dependencies.put(
			new NodeProxy(n, getScriptContext()), b.build()));
		return dependencies;

	}

}
