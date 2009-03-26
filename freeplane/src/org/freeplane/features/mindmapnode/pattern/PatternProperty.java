package org.freeplane.features.mindmapnode.pattern;

// TODO rladstaetter 28.02.2009 remove: this class is obsolete since autoboxing
@Deprecated
public class PatternProperty implements Cloneable {

	// TODO ARCH rladstaetter 21.03.2009 all properties are ultimatively a string in the domain objects - bad
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
