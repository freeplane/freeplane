package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.freeplane.features.treetable.TreeTableNavigation.Direction.DOWN;
import static org.freeplane.features.treetable.TreeTableNavigation.Direction.LEFT;
import static org.freeplane.features.treetable.TreeTableNavigation.Direction.RIGHT;
import static org.freeplane.features.treetable.TreeTableNavigation.Direction.UP;

import java.util.HashSet;
import java.util.Set;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableNavigationTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel rowA;
	private NodeModel subRow;
	private NodeModel rowB;
	private final Set<NodeModel> hidden = new HashSet<NodeModel>();
	private TreeTableNavigation navigation;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(table);
		final NodeModel header = childOf(table, "Colonnes");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		childOf(header, "Prix");
		childOf(header, "Delai");
		childOf(header, "Cout");
		rowA = childOf(table, "A");
		subRow = childOf(rowA, "A.1");
		rowB = childOf(table, "B");
		navigation = new TreeTableNavigation(node -> !hidden.contains(node));
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	private NodeModel cellOf(final NodeModel row, final String key) {
		final NodeModel cell = childOf(row, key);
		TreeTableCellModel.markAsCell(cell);
		return cell;
	}

	@Test
	public void goesFromRowToItsFirstFilledCell() {
		final NodeModel delay = childOf(cellOf(rowA, "Delai"), "3 jours");
		cellOf(rowA, "Prix");

		assertThat(navigation.target(rowA, RIGHT)).isSameAs(delay);
	}

	@Test
	public void movesBetweenShownRowsInDisplayOrder() {
		assertThat(navigation.target(rowA, DOWN)).isSameAs(subRow);
		assertThat(navigation.target(subRow, DOWN)).isSameAs(rowB);
		assertThat(navigation.target(rowB, UP)).isSameAs(subRow);
		assertThat(navigation.target(rowA, UP)).isNull();

		hidden.add(subRow);

		assertThat(navigation.target(rowA, DOWN)).isSameAs(rowB);
	}

	@Test
	public void movesBetweenCellsOfSameRowSkippingEmptyOnes() {
		final NodeModel price = childOf(cellOf(rowA, "Prix"), "12");
		cellOf(rowA, "Delai");
		final NodeModel cost = childOf(cellOf(rowA, "Cout"), "5");

		assertThat(navigation.target(price, RIGHT)).isSameAs(cost);
		assertThat(navigation.target(cost, LEFT)).isSameAs(price);
		assertThat(navigation.target(price, LEFT)).isSameAs(rowA);
		assertThat(navigation.target(cost, RIGHT)).isNull();
	}

	@Test
	public void leavesNavigationInsideCellTreeToFreeplane() {
		final NodeModel delay = cellOf(rowA, "Delai");
		final NodeModel days = childOf(delay, "3 jours");
		final NodeModel workDays = childOf(days, "ouvres");
		childOf(delay, "ou 4 jours");

		assertThat(navigation.target(days, RIGHT)).isNull();
		assertThat(navigation.target(workDays, LEFT)).isNull();
		assertThat(navigation.target(days, DOWN)).isNull();
		assertThat(navigation.target(workDays, UP)).isNull();
	}

	@Test
	public void movesToSameColumnOfAdjacentRowAtCellBorders() {
		final NodeModel delay = cellOf(rowA, "Delai");
		final NodeModel days = childOf(delay, "3 jours");
		final NodeModel alternative = childOf(delay, "ou 4 jours");
		final NodeModel nextDelay = childOf(cellOf(subRow, "Delai"), "1 semaine");

		assertThat(navigation.target(alternative, DOWN)).isSameAs(nextDelay);
		assertThat(navigation.target(nextDelay, UP)).isSameAs(alternative);
		assertThat(navigation.target(days, UP)).isNull();
		assertThat(navigation.target(nextDelay, DOWN)).isSameAs(rowB);
	}

	@Test
	public void ignoresNodesOutsideTables() {
		assertThat(navigation.target(mapFake.getRoot(), RIGHT)).isNull();
	}
}
