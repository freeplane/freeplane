package org.freeplane.features.treetable;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;

public class TreeTableColumn {
	public static final Quantity<LengthUnit> DEFAULT_WIDTH = new Quantity<LengthUnit>(60, LengthUnit.pt);
	public static final Quantity<LengthUnit> MINIMAL_WIDTH = new Quantity<LengthUnit>(20, LengthUnit.pt);

	private final String attributeKey;
	private final Quantity<LengthUnit> width;

	public TreeTableColumn(final String attributeKey) {
		this(attributeKey, DEFAULT_WIDTH);
	}

	public TreeTableColumn(final String attributeKey, final Quantity<LengthUnit> width) {
		if (attributeKey == null || attributeKey.trim().isEmpty())
			throw new IllegalArgumentException("tree table column requires a non blank attribute key");
		if (width == null)
			throw new IllegalArgumentException("tree table column requires a width");
		width.assertNonNegative();
		this.attributeKey = attributeKey;
		this.width = width;
	}

	public static Quantity<LengthUnit> widthFromZoomedPixels(final int zoomedPixels, final double zoom) {
		final double width = Math.max(MINIMAL_WIDTH.toBaseUnits(), Math.round(zoomedPixels / zoom));
		return new Quantity<LengthUnit>(width, LengthUnit.px).in(LengthUnit.pt);
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	public Quantity<LengthUnit> getWidth() {
		return width;
	}

	public TreeTableColumn withAttributeKey(final String newAttributeKey) {
		return new TreeTableColumn(newAttributeKey, width);
	}

	public TreeTableColumn withWidth(final Quantity<LengthUnit> newWidth) {
		return new TreeTableColumn(attributeKey, newWidth);
	}

	@Override
	public int hashCode() {
		return 31 * attributeKey.hashCode() + width.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TreeTableColumn))
			return false;
		final TreeTableColumn other = (TreeTableColumn) obj;
		return attributeKey.equals(other.attributeKey) && width.equals(other.width);
	}

	@Override
	public String toString() {
		return "TreeTableColumn[" + attributeKey + ", " + width + "]";
	}
}
