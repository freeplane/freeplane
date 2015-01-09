package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Dimitry
 *
 */
public class Entry {
	
	private String name;
	final private ArrayList<Entry> childEntries;
	final private Map<String, String> attributes;
	private List<String> builders;
	private Entry parent;
	private Object component;

	public Entry() {
		super();
		this.name = "";
		childEntries = new ArrayList<>();
		attributes = new HashMap<>();
		builders = Collections.emptyList();
	}


	public void setAttribute(final String key, String value) {
		attributes.put(key, value);
	}
	
	public String getAttribute(final String key) {
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


	public Iterable<Entry> children() {
		return childEntries;
	}


	public Iterable<String> builders() {
		return builders;
	}


	public Object getComponent() {
		return component;
	}
	
	public void setComponent(Object component) {
		this.component = component;
	}
}
