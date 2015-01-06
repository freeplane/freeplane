package org.freeplane.core.ui.menubuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Dimitry
 *
 */
public class Entry {

	ArrayList<Entry> childEntries = new ArrayList<>();
	final private Map<String, String> attributes = new HashMap<>();
	private List<String> builders;

	public void setAttribute(final String key, String value) {
		attributes.put(key, value);
	}

	public void addChild(Entry homeEntry) {
		childEntries.add(homeEntry);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public void setBuilders(List<String> builders) {
		this.builders = builders;
		
	}
	
	

}
