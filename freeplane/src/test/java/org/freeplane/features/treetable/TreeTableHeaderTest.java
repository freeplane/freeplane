package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableHeaderTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel header;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Car analysis");
		TreeTableModel.createFor(table);
		header = childOf(table, "Columns");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	@Test
	public void recognizesStyledChildOfTableAsHeader() {
		assertThat(TreeTableHeader.isHeaderRoot(header)).isTrue();
		assertThat(TreeTableHeader.headerOf(table)).isSameAs(header);
	}

	@Test
	public void ignoresUnstyledChildOfTable() {
		final NodeModel row = childOf(table, "Type");

		assertThat(TreeTableHeader.isHeaderRoot(row)).isFalse();
		assertThat(TreeTableHeader.isInHeader(row)).isFalse();
	}

	@Test
	public void ignoresStyledNodeOutsideTable() {
		final NodeModel other = childOf(mapFake.getRoot(), "Other");
		LogicalStyleModel.createExtension(other).setStyle(TreeTableHeader.style());

		assertThat(TreeTableHeader.isHeaderRoot(other)).isFalse();
	}

	@Test
	public void findsNoHeaderInTableWithoutHeader() {
		final NodeModel other = childOf(mapFake.getRoot(), "Other");
		TreeTableModel.createFor(other);

		assertThat(TreeTableHeader.headerOf(other)).isNull();
		assertThat(TreeTableHeader.columnsOf(other).isEmpty()).isTrue();
	}

	@Test
	public void usesLeavesOfHeaderAsColumnsInOrder() {
		final NodeModel analysis = childOf(header, "Analysis");
		final NodeModel pros = childOf(analysis, "Pros");
		final NodeModel cons = childOf(analysis, "Cons");
		final NodeModel ford = childOf(header, "Ford");

		assertThat(TreeTableHeader.columnNodesOf(header)).containsExactly(pros, cons, ford);
		assertThat(TreeTableHeader.columnsOf(table).get(2).getAttributeKey()).isEqualTo("Ford");
	}

	@Test
	public void treatsHeaderDescendantsAsHeaderNodes() {
		final NodeModel analysis = childOf(header, "Analysis");
		final NodeModel pros = childOf(analysis, "Pros");

		assertThat(TreeTableHeader.isInHeader(pros)).isTrue();
		assertThat(TreeTableHeader.isBelowHeaderRoot(analysis)).isTrue();
		assertThat(TreeTableHeader.isBelowHeaderRoot(header)).isFalse();
		assertThat(TreeTableHeader.isColumnNode(pros)).isTrue();
		assertThat(TreeTableHeader.isColumnNode(analysis)).isFalse();
	}

	@Test
	public void skipsBlankAndDuplicateColumnKeys() {
		final NodeModel pros = childOf(header, "Pros");
		childOf(header, " ");
		childOf(header, "Pros");
		final NodeModel cons = childOf(header, "Cons");

		assertThat(TreeTableHeader.distinctColumnNodesOf(table)).containsExactly(pros, cons);
	}

	@Test
	public void trimsColumnKeys() {
		assertThat(TreeTableHeader.keyOf(childOf(header, "  Pros "))).isEqualTo("Pros");
	}

	@Test
	public void keepsAtLeastOneColumn() {
		final NodeModel pros = childOf(header, "Pros");

		assertThat(TreeTableHeader.canRemoveColumn(pros)).isFalse();

		final NodeModel cons = childOf(header, "Cons");

		assertThat(TreeTableHeader.canRemoveColumn(pros)).isTrue();
		assertThat(TreeTableHeader.canRemoveColumn(cons)).isTrue();
	}

	@Test
	public void movesColumnOnlyAmongItsSiblings() {
		final NodeModel analysis = childOf(header, "Analysis");
		final NodeModel pros = childOf(analysis, "Pros");
		final NodeModel cons = childOf(analysis, "Cons");

		assertThat(TreeTableHeader.canMoveColumnLeft(pros)).isFalse();
		assertThat(TreeTableHeader.canMoveColumnRight(pros)).isTrue();
		assertThat(TreeTableHeader.canMoveColumnLeft(cons)).isTrue();
		assertThat(TreeTableHeader.canMoveColumnRight(cons)).isFalse();
		assertThat(TreeTableHeader.canMoveColumnLeft(header)).isFalse();
	}

	@Test
	public void refusesRenamingToBlankOrUsedKey() {
		final NodeModel pros = childOf(header, "Pros");
		childOf(header, "Cons");

		assertThat(TreeTableHeader.acceptsRenaming(pros, "Advantages")).isTrue();
		assertThat(TreeTableHeader.acceptsRenaming(pros, " Cons ")).isFalse();
		assertThat(TreeTableHeader.acceptsRenaming(pros, " ")).isFalse();
	}

	@Test
	public void findsLowestCommonAncestorOfColumns() {
		final NodeModel analysis = childOf(header, "Analysis");
		final NodeModel pros = childOf(analysis, "Pros");
		final NodeModel cons = childOf(analysis, "Cons");
		final NodeModel ford = childOf(header, "Ford");

		assertThat(TreeTableHeader.lowestCommonAncestor(pros, cons)).isSameAs(analysis);
		assertThat(TreeTableHeader.lowestCommonAncestor(cons, ford)).isSameAs(header);
	}
}
