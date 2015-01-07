package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
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

	public Entry() {
		super();
		this.name = "";
	}

	ArrayList<Entry> childEntries = new ArrayList<>();
	final private Map<String, String> attributes = new HashMap<>();
	private List<String> builders;
	private Entry parent;

	public void setAttribute(final String key, String value) {
		attributes.put(key, value);
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

	public Object getParent() {
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
	
}
