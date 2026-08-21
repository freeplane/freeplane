package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.n3.nanoxml.XMLElement;
import org.junit.Test;

public class TreeTableColumnsXmlTest {

	private static XMLElement columnElement(final String name, final String width) {
		final XMLElement element = new XMLElement();
		element.setName(TreeTableColumnsXml.XML_TREE_TABLE_COLUMN);
		if (name != null)
			element.setAttribute("NAME", name);
		if (width != null)
			element.setAttribute("WIDTH", width);
		return element;
	}

	private static XMLElement treeTableElement(final XMLElement... columns) {
		final XMLElement element = new XMLElement();
		element.setName(TreeTableColumnsXml.XML_TREE_TABLE);
		for (final XMLElement column : columns)
			element.addChild(column);
		return element;
	}

	@Test
	public void writesOneElementPerColumn() {
		final TreeTableColumns columns = TreeTableColumns.of(
			new TreeTableColumn("Pour"), new TreeTableColumn("Contre"));

		final XMLElement element = TreeTableColumnsXml.toElement(columns);

		assertThat(element.getName()).isEqualTo(TreeTableColumnsXml.XML_TREE_TABLE);
		assertThat(element.getChildrenNamed(TreeTableColumnsXml.XML_TREE_TABLE_COLUMN)).hasSize(2);
	}

	@Test
	public void writesEmptyElementForTableWithoutColumns() {
		final XMLElement element = TreeTableColumnsXml.toElement(TreeTableColumns.empty());

		assertThat(element.getChildrenNamed(TreeTableColumnsXml.XML_TREE_TABLE_COLUMN)).isEmpty();
	}

	@Test
	public void restoresColumnsWrittenBefore() {
		final TreeTableColumns columns = TreeTableColumns.of(
			new TreeTableColumn("Pour", new Quantity<LengthUnit>(50, LengthUnit.pt)),
			new TreeTableColumn("Contre", new Quantity<LengthUnit>(80, LengthUnit.pt)));

		final TreeTableColumns restored = TreeTableColumnsXml.fromElement(TreeTableColumnsXml.toElement(columns));

		assertThat(restored).isEqualTo(columns);
	}

	@Test
	public void restoresEmptyColumnsWrittenBefore() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(TreeTableColumnsXml.toElement(TreeTableColumns.empty()));

		assertThat(restored.isEmpty()).isTrue();
	}

	@Test
	public void keepsColumnOrderThroughRoundTrip() {
		final TreeTableColumns columns = TreeTableColumns.of(
			new TreeTableColumn("Un"), new TreeTableColumn("Deux"), new TreeTableColumn("Trois"));

		final TreeTableColumns restored = TreeTableColumnsXml.fromElement(TreeTableColumnsXml.toElement(columns));

		assertThat(restored.get(0).getAttributeKey()).isEqualTo("Un");
		assertThat(restored.get(1).getAttributeKey()).isEqualTo("Deux");
		assertThat(restored.get(2).getAttributeKey()).isEqualTo("Trois");
	}

	@Test
	public void readsWidthFromElement() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement("Pour", "42 pt")));

		assertThat(restored.get(0).getWidth()).isEqualTo(new Quantity<LengthUnit>(42, LengthUnit.pt));
	}

	@Test
	public void fallsBackToDefaultWidthWhenMissing() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement("Pour", null)));

		assertThat(restored.get(0).getWidth()).isEqualTo(TreeTableColumn.DEFAULT_WIDTH);
	}

	@Test
	public void fallsBackToDefaultWidthWhenUnparsable() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement("Pour", "n'importe quoi")));

		assertThat(restored.get(0).getWidth()).isEqualTo(TreeTableColumn.DEFAULT_WIDTH);
	}

	@Test
	public void skipsColumnWithoutName() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement(null, "50 pt"), columnElement("Contre", "50 pt")));

		assertThat(restored.size()).isEqualTo(1);
		assertThat(restored.get(0).getAttributeKey()).isEqualTo("Contre");
	}

	@Test
	public void skipsColumnWithBlankName() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement("   ", "50 pt")));

		assertThat(restored.isEmpty()).isTrue();
	}

	@Test
	public void skipsDuplicateColumnInsteadOfFailing() {
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(columnElement("Pour", "50 pt"), columnElement("Pour", "80 pt")));

		assertThat(restored.size()).isEqualTo(1);
		assertThat(restored.get(0).getWidth()).isEqualTo(new Quantity<LengthUnit>(50, LengthUnit.pt));
	}

	@Test
	public void ignoresUnrelatedChildElements() {
		final XMLElement stranger = new XMLElement();
		stranger.setName("something_else");
		final TreeTableColumns restored = TreeTableColumnsXml
			.fromElement(treeTableElement(stranger, columnElement("Pour", "50 pt")));

		assertThat(restored.size()).isEqualTo(1);
	}
}
