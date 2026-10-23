package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableNodesTest {

	private MapFake mapFake;

	@Before
	public void setUp() {
		mapFake = new MapFake();
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	@Test
	public void marksNodeCarryingModelAsTreeTableRoot() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");

		TreeTableModel.createFor(projects);

		assertThat(TreeTableModel.isTreeTableRoot(projects)).isTrue();
	}

	@Test
	public void doesNotMarkPlainNodeAsTreeTableRoot() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");

		assertThat(TreeTableModel.isTreeTableRoot(projects)).isFalse();
	}

	@Test
	public void treatsDirectChildAsRow() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel projectA = childOf(projects, "Projet A");

		assertThat(TreeTableNodes.isRow(projectA)).isTrue();
		assertThat(TreeTableNodes.treeTableRootOf(projectA)).isSameAs(projects);
	}

	@Test
	public void treatsDeepDescendantAsRow() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel projectA = childOf(projects, "Projet A");
		final NodeModel subTask = childOf(projectA, "Sous-tache");
		final NodeModel detail = childOf(subTask, "Detail");

		assertThat(TreeTableNodes.isRow(detail)).isTrue();
		assertThat(TreeTableNodes.treeTableRootOf(detail)).isSameAs(projects);
	}

	@Test
	public void doesNotTreatTreeTableRootItselfAsRow() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);

		assertThat(TreeTableNodes.isRow(projects)).isFalse();
		assertThat(TreeTableNodes.treeTableRootOf(projects)).isNull();
	}

	@Test
	public void doesNotTreatNodeOutsideTreeTableAsRow() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel unrelated = childOf(mapFake.getRoot(), "Autre");

		assertThat(TreeTableNodes.isRow(unrelated)).isFalse();
		assertThat(TreeTableNodes.treeTableRootOf(unrelated)).isNull();
	}

	@Test
	public void attachesRowToNearestEnclosingTreeTable() {
		final NodeModel outer = childOf(mapFake.getRoot(), "Externe");
		TreeTableModel.createFor(outer);
		final NodeModel inner = childOf(outer, "Interne");
		TreeTableModel.createFor(inner);
		final NodeModel innerRow = childOf(inner, "Ligne interne");

		assertThat(TreeTableNodes.treeTableRootOf(innerRow)).isSameAs(inner);
	}

	@Test
	public void findsTreeTableRootItselfAsEnclosingRoot() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);

		assertThat(TreeTableNodes.enclosingTreeTableRoot(projects)).isSameAs(projects);
	}

	@Test
	public void findsTreeTableRootAboveRowAsEnclosingRoot() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel subTask = childOf(childOf(projects, "Projet A"), "Sous-tache");

		assertThat(TreeTableNodes.enclosingTreeTableRoot(subTask)).isSameAs(projects);
	}

	@Test
	public void findsNoEnclosingRootOutsideTreeTable() {
		final NodeModel unrelated = childOf(mapFake.getRoot(), "Autre");

		assertThat(TreeTableNodes.enclosingTreeTableRoot(unrelated)).isNull();
	}

	@Test
	public void prefersNearestTreeTableRootAsEnclosingRoot() {
		final NodeModel outer = childOf(mapFake.getRoot(), "Externe");
		TreeTableModel.createFor(outer);
		final NodeModel inner = childOf(outer, "Interne");
		TreeTableModel.createFor(inner);

		assertThat(TreeTableNodes.enclosingTreeTableRoot(inner)).isSameAs(inner);
	}

	@Test
	public void listsAllDescendantsAsRows() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel projectA = childOf(projects, "Projet A");
		final NodeModel subTask = childOf(projectA, "Sous-tache");
		final NodeModel projectB = childOf(projects, "Projet B");

		assertThat(TreeTableNodes.rowsOf(projects)).containsExactly(projectA, subTask, projectB);
	}

	@Test
	public void listsNoRowsForTreeTableWithoutChildren() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);

		assertThat(TreeTableNodes.rowsOf(projects)).isEmpty();
	}

	@Test
	public void doesNotListRowsOfNestedTreeTable() {
		final NodeModel outer = childOf(mapFake.getRoot(), "Externe");
		TreeTableModel.createFor(outer);
		final NodeModel inner = childOf(outer, "Interne");
		TreeTableModel.createFor(inner);
		childOf(inner, "Ligne interne");

		assertThat(TreeTableNodes.rowsOf(outer)).containsExactly(inner);
	}

	@Test
	public void startsTreeTableWithoutColumns() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");

		TreeTableModel.createFor(projects);

		assertThat(TreeTableModel.columnsOf(projects).isEmpty()).isTrue();
	}

	@Test
	public void reportsNoColumnsForPlainNode() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");

		assertThat(TreeTableModel.columnsOf(projects).isEmpty()).isTrue();
		assertThat(TreeTableModel.of(projects)).isNull();
	}

	@Test
	public void stopsBeingTreeTableRootAfterRemoval() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);

		TreeTableModel.removeFrom(projects);

		assertThat(TreeTableModel.isTreeTableRoot(projects)).isFalse();
	}

	@Test
	public void allowsConversionOfNodeOutsideTreeTable() {
		final NodeModel unrelated = childOf(mapFake.getRoot(), "Autre");

		assertThat(TreeTableNodes.canConvertToTable(unrelated)).isTrue();
	}

	@Test
	public void refusesConversionOfTreeTableRootAndRows() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel row = childOf(projects, "Projet A");

		assertThat(TreeTableNodes.canConvertToTable(projects)).isFalse();
		assertThat(TreeTableNodes.canConvertToTable(row)).isFalse();
		assertThat(TreeTableNodes.canConvertToTable(null)).isFalse();
	}

	@Test
	public void allowsDeletionOnlyOnTreeTableRoot() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel row = childOf(projects, "Projet A");

		assertThat(TreeTableNodes.canDeleteTable(projects)).isTrue();
		assertThat(TreeTableNodes.canDeleteTable(row)).isFalse();
		assertThat(TreeTableNodes.canDeleteTable(mapFake.getRoot())).isFalse();
		assertThat(TreeTableNodes.canDeleteTable(null)).isFalse();
	}

	@Test
	public void excludesHeaderSubtreeFromRows() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel header = childOf(projects, "Colonnes");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		final NodeModel column = childOf(header, "Budget");
		final NodeModel projectA = childOf(projects, "Projet A");

		assertThat(TreeTableNodes.rowsOf(projects)).containsExactly(projectA);
		assertThat(TreeTableNodes.isRow(header)).isFalse();
		assertThat(TreeTableNodes.isRow(column)).isFalse();
		assertThat(TreeTableModel.columnsOf(projects).containsAttributeKey("Budget")).isTrue();
	}
}
