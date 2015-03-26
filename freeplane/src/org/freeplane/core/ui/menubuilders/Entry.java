package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.freeplane.core.ui.AFreeplaneAction;

/**
 * @author Dimitry
 *
 */
public class Entry {
	
	public static final String COMPONENT = "component";
	public static final String ACTION = "action";
	private String name;
	final private ArrayList<Entry> childEntries;
	final private Map<String, Object> attributes;
	private List<String> builders;
	private Entry parent;


	public Entry() {
		super();
		this.name = "";
		childEntries = new ArrayList<>();
		attributes = new HashMap<>();
		builders = Collections.emptyList();
	}


	public void setAttribute(final String key, Object value) {
		if(attributes.containsKey(key)){
			if(value != attributes.get(key))
				throw new AttributeAlreadySetException(key, attributes.get(key));
		}
		else
			attributes.put(key, value);
	}
	
	public Object getAttribute(final String key) {
		return attributes.get(key);
	}

	public void addChild(Entry homeEntry) {
		childEntries.add(homeEntry);
		homeEntry.setParent(this);
	}

	private void setParent(Entry parent) {
		this.parent = parent;
		
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "parent");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "parent");
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("parent").build();
	}

	public void setBuilders(List<String> builders) {
		this.builders = builders;
		
	}

	public Entry getParent() {
		return parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return (parent != null ? parent.getPath() : "") +  "/" + getName();
	}

	public String getName() {
		return name;
	}

	public Entry getChild(int index) {
		return childEntries.get(index);
	}

	public Entry getChild(int... indices) {
		Entry entry = this;
		for(int index : indices)
			entry = entry.getChild(index);
		return entry;
	}


	public Iterable<Entry> children() {
		return childEntries;
	}


	public Collection<String> builders() {
		return builders;
	}


	public Object getComponent() {
		return getAttribute(COMPONENT);
	}
	
	public void setComponent(Object component) {
		setAttribute(COMPONENT, component);
	}

	public AFreeplaneAction getAction() {
		return(AFreeplaneAction ) getAttribute(ACTION);
	}
	
	public void setAction(AFreeplaneAction action) {
		setAttribute(ACTION, action);
	}


	public Object getAncestorComponent() {
		final Entry parent = getParent();
		if(parent == null)
			return null;
		else{
			final Object parentComponent = parent.getComponent();
			if(parentComponent != null)
				return parentComponent;
			else
				return parent.getAncestorComponent();
		}
	}


	public void removeChildren() {
		childEntries.clear();
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}


	public boolean hasChildren() {
		return ! childEntries.isEmpty();
	}


	public Entry getRoot() {
		return parent == null ? this : parent.getRoot();
	}
}
