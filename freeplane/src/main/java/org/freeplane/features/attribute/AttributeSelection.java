package org.freeplane.features.attribute;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface AttributeSelection {
	public static final AttributeSelection EMPTY = new AttributeSelection() {
		@Override
		public List<SelectedAttribute> getSelectedAttributes() {
			return Collections.emptyList();
		}
	};

	public static class SelectedAttribute {
    	public enum SelectedPart{NAME, VALUE, BOTH}
    	private final NodeAttribute selectedAttribute;
    	private final SelectedPart selectedPart;
		public SelectedAttribute(NodeAttribute selectedAttribute, SelectedPart selectedPart) {
			super();
			this.selectedAttribute = selectedAttribute;
			this.selectedPart = selectedPart;
		}
		public NodeAttribute getSelectedAttribute() {
			return selectedAttribute;
		}
		public SelectedPart getSelectedPart() {
			return selectedPart;
		}
	}

	List<SelectedAttribute> getSelectedAttributes();

	default boolean isEmpty() {
		return getSelectedAttributes().isEmpty();
	}

	default Stream<NodeAttribute> nodeAttributeStream() {
		return getSelectedAttributes().stream().map(SelectedAttribute::getSelectedAttribute);
	}
}
