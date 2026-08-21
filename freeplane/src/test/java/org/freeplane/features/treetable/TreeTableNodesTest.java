package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
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

	private static TreeTableColumn column(final String attributeKey) {
		return new TreeTableColumn(attributeKey, new Quantity<LengthUnit>(50, LengthUnit.pt));
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
	public void reportsDepthOfDirectChildAsOne() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel projectA = childOf(projects, "Projet A");

		assertThat(TreeTableNodes.rowDepth(projectA)).isEqualTo(1);
	}

	@Test
	public void reportsIncreasingDepthForDeeperRows() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects);
		final NodeModel projectA = childOf(projects, "Projet A");
		final NodeModel subTask = childOf(projectA, "Sous-tache");

		assertThat(TreeTableNodes.rowDepth(subTask)).isEqualTo(2);
	}

	@Test
	public void reportsNoDepthOutsideTreeTable() {
		final NodeModel unrelated = childOf(mapFake.getRoot(), "Autre");

		assertThat(TreeTableNodes.rowDepth(unrelated)).isEqualTo(-1);
	}

	@Test
	public void attachesRowToNearestEnclosingTreeTable() {
		final NodeModel outer = childOf(mapFake.getRoot(), "Externe");
		TreeTableModel.createFor(outer);
		final NodeModel inner = childOf(outer, "Interne");
		TreeTableModel.createFor(inner);
		final NodeModel innerRow = childOf(inner, "Ligne interne");

		assertThat(TreeTableNodes.treeTableRootOf(innerRow)).isSameAs(inner);
		assertThat(TreeTableNodes.rowDepth(innerRow)).isEqualTo(1);
	}

	@Test
	public void readsColumnsOfEnclosingTreeTable() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		TreeTableModel.createFor(projects).setColumns(TreeTableColumns.of(column("Pour"), column("Contre")));
		final NodeModel subTask = childOf(childOf(projects, "Projet A"), "Sous-tache");

		assertThat(TreeTableNodes.columnsFor(subTask).size()).isEqualTo(2);
		assertThat(TreeTableNodes.columnsFor(subTask).indexOfAttributeKey("Contre")).isEqualTo(1);
	}

	@Test
	public void readsNoColumnsOutsideTreeTable() {
		final NodeModel unrelated = childOf(mapFake.getRoot(), "Autre");

		assertThat(TreeTableNodes.columnsFor(unrelated).isEmpty()).isTrue();
	}

	@Test
	public void startsTreeTableWithoutColumns() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");

		assertThat(TreeTableModel.createFor(projects).getColumns().isEmpty()).isTrue();
	}

	@Test
	public void keepsExistingModelOnRepeatedCreation() {
		final NodeModel projects = childOf(mapFake.getRoot(), "Projets");
		final TreeTableModel model = TreeTableModel.createFor(projects);
		model.setColumns(TreeTableColumns.of(column("Pour")));

		assertThat(TreeTableModel.createFor(projects)).isSameAs(model);
		assertThat(TreeTableModel.columnsOf(projects).size()).isEqualTo(1);
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
}
