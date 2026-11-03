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
	private NodeModel header;
	private NodeModel priceColumn;
	private NodeModel delayColumn;
	private NodeModel costColumn;
	private final Set<NodeModel> hidden = new HashSet<NodeModel>();
	private TreeTableNavigation navigation;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(table);
		header = childOf(table, "Colonnes");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		priceColumn = childOf(header, "Prix");
		delayColumn = childOf(header, "Delai");
		costColumn = childOf(header, "Cout");
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
		assertThat(navigation.target(rowA, UP)).isSameAs(table);

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
		assertThat(navigation.target(cost, RIGHT)).isSameAs(cost);
	}

	@Test
	public void leavesNavigationInsideCellTreeToFreeplane() {
		final NodeModel delay = cellOf(rowA, "Delai");
		final NodeModel days = childOf(delay, "3 jours");
		final NodeModel workDays = childOf(days, "ouvres");
		childOf(delay, "ou 4 jours");

		assertThat(navigation.target(days, RIGHT)).isSameAs(days);
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
		assertThat(navigation.target(days, UP)).isSameAs(delayColumn);
		assertThat(navigation.target(nextDelay, DOWN)).isSameAs(nextDelay);
	}

	@Test
	public void ignoresNodesOutsideTables() {
		assertThat(navigation.target(mapFake.getRoot(), RIGHT)).isNull();
	}

	@Test
	public void movesFromColumnHeaderIntoFirstRowAndAlongColumns() {
		final NodeModel delay = childOf(cellOf(subRow, "Delai"), "1 semaine");

		assertThat(navigation.target(delayColumn, DOWN)).isSameAs(delay);
		assertThat(navigation.target(delayColumn, LEFT)).isSameAs(priceColumn);
		assertThat(navigation.target(delayColumn, RIGHT)).isSameAs(costColumn);
		assertThat(navigation.target(costColumn, RIGHT)).isNull();
		assertThat(navigation.target(delayColumn, UP)).isSameAs(header);
	}

	@Test
	public void movesAlongLeafColumnsAcrossColumnGroups() {
		final NodeModel net = childOf(priceColumn, "HT");
		final NodeModel gross = childOf(priceColumn, "TTC");

		assertThat(navigation.target(net, RIGHT)).isSameAs(gross);
		assertThat(navigation.target(gross, RIGHT)).isSameAs(delayColumn);
		assertThat(navigation.target(delayColumn, LEFT)).isSameAs(gross);
		assertThat(navigation.target(priceColumn, DOWN)).isSameAs(net);
		assertThat(navigation.target(net, UP)).isSameAs(priceColumn);
		assertThat(navigation.target(priceColumn, RIGHT)).isSameAs(delayColumn);
		assertThat(navigation.target(priceColumn, UP)).isSameAs(header);
	}

	@Test
	public void linksTableRootToHeaderAndRows() {
		assertThat(navigation.target(table, RIGHT)).isSameAs(header);
		assertThat(navigation.target(table, DOWN)).isSameAs(rowA);
		assertThat(navigation.target(header, LEFT)).isSameAs(table);
		assertThat(navigation.target(header, DOWN)).isSameAs(priceColumn);
		assertThat(navigation.target(header, UP)).isNull();
	}

	@Test
	public void goesToNextColumnEvenFromCellNodeWithChildren() {
		final NodeModel days = childOf(cellOf(rowA, "Delai"), "3 jours");
		childOf(days, "ouvres");
		final NodeModel cost = childOf(cellOf(rowA, "Cout"), "5");

		assertThat(navigation.target(days, RIGHT)).isSameAs(cost);
	}

	@Test
	public void entersCellAboveAtItsLastShownNode() {
		final NodeModel price = childOf(cellOf(rowA, "Prix"), "12");
		final NodeModel net = childOf(price, "HT");
		final NodeModel nextPrice = childOf(cellOf(subRow, "Prix"), "5");

		assertThat(navigation.target(nextPrice, UP)).isSameAs(net);

		hidden.add(net);

		assertThat(navigation.target(nextPrice, UP)).isSameAs(price);
	}
}
