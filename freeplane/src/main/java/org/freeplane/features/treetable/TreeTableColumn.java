package org.freeplane.features.treetable;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;

public class TreeTableColumn {
	public static final Quantity<LengthUnit> MINIMAL_WIDTH = new Quantity<LengthUnit>(20, LengthUnit.pt);

	private final String attributeKey;

	public static Quantity<LengthUnit> widthFromZoomedPixels(final int zoomedPixels, final double zoom) {
		final double width = Math.max(MINIMAL_WIDTH.toBaseUnits(), Math.round(zoomedPixels / zoom));
		return new Quantity<LengthUnit>(width, LengthUnit.px).in(LengthUnit.pt);
	}

	public TreeTableColumn(final String attributeKey) {
		if (attributeKey == null || attributeKey.trim().isEmpty())
			throw new IllegalArgumentException("tree table column requires a non blank attribute key");
		this.attributeKey = attributeKey;
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	@Override
	public int hashCode() {
		return attributeKey.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TreeTableColumn))
			return false;
		return attributeKey.equals(((TreeTableColumn) obj).attributeKey);
	}

	@Override
	public String toString() {
		return "TreeTableColumn[" + attributeKey + "]";
	}
}
