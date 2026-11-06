package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
		Mockito.when(table.getMap().registryNode(Mockito.any()))
			.thenAnswer(invocation -> "ID_" + System.identityHashCode(invocation.getArgument(0)));
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
	public void passesKeyOfSplitColumnToItsFirstSubColumn() {
		final NodeModel price = childOf(header, "Price");
		final NodeModel net = childOf(price, "Net");
		childOf(price, "Gross");

		assertThat(TreeTableHeader.heirOf(price)).isSameAs(net);
		assertThat(TreeTableHeader.keyInheritedFrom(price)).isEqualTo("Price");
	}

	@Test
	public void passesKeyOfSplitColumnToFirstLeafOfInsertedSubtree() {
		final NodeModel price = childOf(header, "Price");
		final NodeModel net = childOf(childOf(price, "Amounts"), "Net");

		assertThat(TreeTableHeader.heirOf(price)).isSameAs(net);
		assertThat(TreeTableHeader.keyInheritedFrom(price)).isEqualTo("Price");
	}

	@Test
	public void passesNoKeyStillUsedByAnotherColumn() {
		final NodeModel price = childOf(header, "Price");
		childOf(price, "Net");
		childOf(header, "Price");

		assertThat(TreeTableHeader.keyInheritedFrom(price)).isNull();
	}

	@Test
	public void passesNoKeyToSubColumnNamedLikeAnotherColumn() {
		final NodeModel price = childOf(header, "Price");
		childOf(price, "Net");
		childOf(header, "Net");

		assertThat(TreeTableHeader.keyInheritedFrom(price)).isNull();
	}

	@Test
	public void passesNoKeyFromColumnWithoutSubColumnOrFromHeader() {
		final NodeModel price = childOf(header, "Price");

		assertThat(TreeTableHeader.keyInheritedFrom(price)).isNull();
		assertThat(TreeTableHeader.keyInheritedFrom(header)).isNull();
	}

	@Test
	public void returnsKeyOfLastRemovedSubColumnToParent() {
		final NodeModel cons = childOf(header, "Cons");
		final NodeModel financial = childOf(cons, "Financial");
		cons.remove(cons.getIndex(financial));

		assertThat(TreeTableHeader.keyReturnedTo(cons, financial)).isEqualTo("Financial");
	}

	@Test
	public void returnsFirstLeafKeyOfRemovedSubColumnGroup() {
		final NodeModel cons = childOf(header, "Cons");
		final NodeModel group = childOf(cons, "Group");
		childOf(group, "Financial");
		childOf(group, "Planning");
		cons.remove(cons.getIndex(group));

		assertThat(TreeTableHeader.keyReturnedTo(cons, group)).isEqualTo("Financial");
	}

	@Test
	public void returnsNoKeyWhileParentKeepsSubColumns() {
		final NodeModel cons = childOf(header, "Cons");
		childOf(cons, "Financial");
		final NodeModel planning = childOf(cons, "Planning");
		cons.remove(cons.getIndex(planning));

		assertThat(TreeTableHeader.keyReturnedTo(cons, planning)).isNull();
	}

	@Test
	public void returnsNoKeyWhenRemovingTopLevelColumn() {
		childOf(header, "Pros");
		final NodeModel cons = childOf(header, "Cons");
		header.remove(header.getIndex(cons));

		assertThat(TreeTableHeader.keyReturnedTo(header, cons)).isNull();
	}

	@Test
	public void returnsNoKeyStillShownByAnotherColumn() {
		final NodeModel cons = childOf(header, "Cons");
		final NodeModel financial = childOf(cons, "Financial");
		childOf(header, "Financial");
		cons.remove(cons.getIndex(financial));

		assertThat(TreeTableHeader.keyReturnedTo(cons, financial)).isNull();
	}

	@Test
	public void keepsUnnamedColumnsButSkipsDuplicateKeys() {
		final NodeModel pros = childOf(header, "Pros");
		final NodeModel unnamed = childOf(header, " ");
		childOf(header, "Pros");
		final NodeModel cons = childOf(header, "Cons");

		assertThat(TreeTableHeader.distinctColumnNodesOf(table)).containsExactly(pros, unnamed, cons);
	}

	@Test
	public void identifiesUnnamedColumnByItsNode() {
		final NodeModel first = childOf(header, "");
		final NodeModel second = childOf(header, "");

		assertThat(TreeTableHeader.keyOf(first)).isNotEmpty().isNotEqualTo(TreeTableHeader.keyOf(second))
			.isEqualTo(TreeTableHeader.keyOf(first, "  "));
		assertThat(TreeTableHeader.keyOf(first, "Named")).isEqualTo("Named");
		assertThat(TreeTableHeader.keyOf(table, "")).isEmpty();
	}

	@Test
	public void passesKeyOfSplitColumnToSubColumnStillWithoutName() {
		final NodeModel price = childOf(header, "Price");
		final NodeModel unnamed = childOf(price, "");

		assertThat(TreeTableHeader.keyInheritedFrom(price)).isEqualTo("Price");
		assertThat(TreeTableHeader.keyOf(TreeTableHeader.heirOf(price))).isEqualTo(TreeTableHeader.keyOf(unnamed));
	}

	@Test
	public void returnsValuesOfRemovedUnnamedSubColumnToParent() {
		final NodeModel cons = childOf(header, "Cons");
		final NodeModel unnamed = childOf(cons, "");
		final String unnamedKey = TreeTableHeader.keyOf(unnamed);
		cons.remove(cons.getIndex(unnamed));

		assertThat(TreeTableHeader.keyReturnedTo(cons, unnamed)).isEqualTo(unnamedKey);
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
