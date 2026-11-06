package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.treetable.TreeTableSheetImport.Item;
import org.junit.Test;

public class TreeTableSheetImportTest {

	private static TreeTableSheetImport importOf(final String text) {
		return TreeTableSheetImport.of(TreeTableSheet.parseTabSeparated(text), "Column");
	}

	@Test
	public void ignoresEmptyText() {
		assertThat(importOf("\r\n")).isNull();
	}

	@Test
	public void importsFlatExcelTable() {
		final TreeTableSheetImport table = importOf("Car\tCost\tDuration\r\nA\t10\t3\r\nB\t\t5\r\n");

		assertThat(table.getRootText()).isEqualTo("Car");
		assertThat(table.getColumns()).extracting(Item::getText).containsExactly("Cost", "Duration");
		assertThat(table.getRows()).extracting(Item::getText).containsExactly("A", "B");
		assertThat(table.getRows().get(0).getValues()).containsExactly(entry("Cost", "10"), entry("Duration", "3"));
		assertThat(table.getRows().get(1).getValues()).containsExactly(entry("Duration", "5"));
	}

	@Test
	public void namesUnnamedAndDuplicateColumns() {
		final TreeTableSheetImport table = importOf("Car\tCost\t\tCost\r\nA\t1\t2\t3\r\n");

		assertThat(table.getColumns()).extracting(Item::getText).containsExactly("Cost", "Column 1", "Cost 2");
		assertThat(table.getRows().get(0).getValues()).containsExactly(entry("Cost", "1"), entry("Column 1", "2"),
			entry("Cost 2", "3"));
	}

	@Test
	public void importsRowHierarchyFromLevelColumns() {
		final TreeTableSheetImport table = importOf("Projects\t\tCost\r\nPhase\t\t10\r\n\tTask\t4\r\nOther\t\t\r\n");

		assertThat(table.getColumns()).extracting(Item::getText).containsExactly("Cost");
		final List<Item> rows = table.getRows();
		assertThat(rows).extracting(Item::getText).containsExactly("Phase", "Other");
		assertThat(rows.get(0).getChildren()).extracting(Item::getText).containsExactly("Task");
		assertThat(rows.get(0).getChildren().get(0).getValues()).containsExactly(entry("Cost", "4"));
	}

	@Test
	public void attachesTooDeepRowToTopLevel() {
		final TreeTableSheetImport table = importOf("Projects\t\t\tCost\r\n\t\tTask\t4\r\n");

		assertThat(table.getRows()).extracting(Item::getText).containsExactly("Task");
	}

	@Test
	public void importsGroupedHeaderFromSeveralRows() {
		final TreeTableSheetImport table = importOf("Projects\tPrice\t\tBrand\r\n\tMin\tMax\t\r\nCar\t1\t2\tX\r\n");

		final List<Item> columns = table.getColumns();
		assertThat(columns).extracting(Item::getText).containsExactly("Price", "Brand");
		assertThat(columns.get(0).getChildren()).extracting(Item::getText).containsExactly("Min", "Max");
		assertThat(table.getRows().get(0).getValues()).containsExactly(entry("Min", "1"), entry("Max", "2"),
			entry("Brand", "X"));
	}

	@Test
	public void roundTripsExportedTreeTable() {
		final MapFake mapFake = new MapFake();
		final NodeModel tableNode = childOf(mapFake, mapFake.getRoot(), "Projects");
		TreeTableModel.createFor(tableNode);
		final NodeModel header = childOf(mapFake, tableNode, "Columns");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		final NodeModel price = childOf(mapFake, header, "Price");
		childOf(mapFake, price, "Min");
		childOf(mapFake, header, "Brand");
		final NodeModel phase = childOf(mapFake, tableNode, "Phase");
		final NodeModel task = childOf(mapFake, phase, "Task");
		TreeTableSheetExportTest.setValue(task, "Min", "7");

		final TreeTableSheetImport table = importOf(TreeTableSheetExport.sheetOf(tableNode).toTabSeparatedText());

		assertThat(table.getRootText()).isEqualTo("Projects");
		assertThat(table.getColumns()).extracting(Item::getText).containsExactly("Price", "Brand");
		assertThat(table.getColumns().get(0).getChildren()).extracting(Item::getText).containsExactly("Min");
		assertThat(table.getRows()).extracting(Item::getText).containsExactly("Phase");
		final Item importedTask = table.getRows().get(0).getChildren().get(0);
		assertThat(importedTask.getText()).isEqualTo("Task");
		assertThat(importedTask.getValues()).containsExactly(entry("Min", "7"));
	}

	@Test
	public void keepsFormattingOfExportedHtmlTable() {
		final MapFake mapFake = new MapFake();
		final NodeModel tableNode = childOf(mapFake, mapFake.getRoot(), "Projects");
		TreeTableModel.createFor(tableNode);
		final NodeModel header = childOf(mapFake, tableNode, "Columns");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
		childOf(mapFake, header, "Note");
		final NodeModel task = childOf(mapFake, tableNode, "<html><body><p><b>Task</b></p></body></html>");
		TreeTableSheetExportTest.setValue(task, "Note",
			"<html><body><p>Du <span style=\"color: #ff0000; font-weight: bold;\">HTML </span>ici</p></body></html>");

		final TreeTableSheetImport table = TreeTableSheetImport
			.of(TreeTableSheet.parseHtmlTable(TreeTableSheetExport.sheetOf(tableNode).toHtml()), "Column");

		final Item importedTask = table.getRows().get(0);
		assertThat(importedTask.getContent())
			.isEqualTo("<html><body><p><span style=\"font-weight: bold;\">Task</span></p></body></html>");
		assertThat(importedTask.getValueContents()).containsExactly(entry("Note",
			"<html><body><p>Du <span style=\"color: #ff0000; font-weight: bold;\">HTML </span>ici</p></body></html>"));
	}

	private static NodeModel childOf(final MapFake mapFake, final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}
}
