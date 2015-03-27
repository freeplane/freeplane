package org.freeplane.features.styles.mindmapmode;

public class PatternProperty implements Cloneable {
	protected String value;

	public PatternProperty() {
	}

	public PatternProperty(final String value) {
		super();
		this.value = value;
	}

	@Override
	protected Object clone() {
		return new PatternProperty(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
