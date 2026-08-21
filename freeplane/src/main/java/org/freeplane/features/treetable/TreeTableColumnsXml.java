package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.n3.nanoxml.XMLElement;

public class TreeTableColumnsXml {
	public static final String XML_TREE_TABLE = "tree_table";
	public static final String XML_TREE_TABLE_COLUMN = "tree_table_column";

	private static final String ATTRIBUTE_NAME = "NAME";
	private static final String ATTRIBUTE_WIDTH = "WIDTH";

	public static XMLElement toElement(final TreeTableColumns columns) {
		final XMLElement treeTableElement = new XMLElement();
		treeTableElement.setName(XML_TREE_TABLE);
		for (final TreeTableColumn column : columns.asList()) {
			final XMLElement columnElement = new XMLElement();
			columnElement.setName(XML_TREE_TABLE_COLUMN);
			columnElement.setAttribute(ATTRIBUTE_NAME, column.getAttributeKey());
			columnElement.setAttribute(ATTRIBUTE_WIDTH, column.getWidth().toString());
			treeTableElement.addChild(columnElement);
		}
		return treeTableElement;
	}

	public static TreeTableColumns fromElement(final XMLElement treeTableElement) {
		final List<TreeTableColumn> columns = new ArrayList<TreeTableColumn>();
		for (final XMLElement columnElement : treeTableElement.getChildrenNamed(XML_TREE_TABLE_COLUMN)) {
			final TreeTableColumn column = readColumn(columnElement, columns);
			if (column != null)
				columns.add(column);
		}
		return TreeTableColumns.of(columns);
	}

	private static TreeTableColumn readColumn(final XMLElement columnElement, final List<TreeTableColumn> alreadyRead) {
		final String attributeKey = columnElement.getAttribute(ATTRIBUTE_NAME, null);
		if (attributeKey == null || attributeKey.trim().isEmpty())
			return null;
		for (final TreeTableColumn column : alreadyRead) {
			if (column.getAttributeKey().equals(attributeKey))
				return null;
		}
		return new TreeTableColumn(attributeKey, readWidth(columnElement));
	}

	private static Quantity<LengthUnit> readWidth(final XMLElement columnElement) {
		final String width = columnElement.getAttribute(ATTRIBUTE_WIDTH, null);
		if (width == null)
			return TreeTableColumn.DEFAULT_WIDTH;
		try {
			final Quantity<LengthUnit> parsed = Quantity.fromString(width, LengthUnit.pt);
			return parsed != null ? parsed : TreeTableColumn.DEFAULT_WIDTH;
		}
		catch (final RuntimeException parsingFailed) {
			return TreeTableColumn.DEFAULT_WIDTH;
		}
	}

	private TreeTableColumnsXml() {
	}
}
