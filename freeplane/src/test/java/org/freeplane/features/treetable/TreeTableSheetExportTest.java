package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleModel;
import org.junit.Before;
import org.junit.Test;

public class TreeTableSheetExportTest {

	private MapFake mapFake;
	private NodeModel table;
	private NodeModel header;

	@Before
	public void setUp() {
		mapFake = new MapFake();
		table = childOf(mapFake.getRoot(), "Projects");
		TreeTableModel.createFor(table);
		header = childOf(table, "Columns");
		LogicalStyleModel.createExtension(header).setStyle(TreeTableHeader.style());
	}

	private NodeModel childOf(final NodeModel parent, final String text) {
		final NodeModel child = mapFake.createNode(text);
		parent.insert(child);
		return child;
	}

	static void setValue(final NodeModel row, final String key, final String value) {
		NodeAttributeTableModel attributes = row.getExtension(NodeAttributeTableModel.class);
		if (attributes == null) {
			attributes = new NodeAttributeTableModel();
			row.addExtension(attributes);
		}
		attributes.silentlyAddRowNoUndo(row, new Attribute(key, value));
	}

	@Test
	public void exportsOnlySingleTreeTableRoot() {
		assertThat(TreeTableSheetExport.exportsAsSheet(Collections.singletonList(table))).isTrue();
		assertThat(TreeTableSheetExport.exportsAsSheet(Collections.singletonList(header))).isFalse();
		assertThat(TreeTableSheetExport.exportsAsSheet(Arrays.asList(table, mapFake.getRoot()))).isFalse();
	}

	@Test
	public void exportsFlatTable() {
		childOf(header, "Cost");
		childOf(header, "Duration");
		final NodeModel car = childOf(table, "Car");
		setValue(car, "Cost", "10");
		setValue(car, "Duration", "3");

		final TreeTableSheet sheet = TreeTableSheetExport.sheetOf(table);

		assertThat(sheet.toTabSeparatedText()).isEqualTo("Projects\tCost\tDuration\r\nCar\t10\t3\r\n");
	}

	@Test
	public void exportsRowHierarchyAsOneColumnPerLevel() {
		childOf(header, "Cost");
		final NodeModel phase = childOf(table, "Phase");
		setValue(phase, "Cost", "10");
		final NodeModel task = childOf(phase, "Task");
		setValue(task, "Cost", "4");
		childOf(table, "Other");

		final TreeTableSheet sheet = TreeTableSheetExport.sheetOf(table);

		assertThat(sheet.toTabSeparatedText())
			.isEqualTo("Projects\t\tCost\r\nPhase\t\t10\r\n\tTask\t4\r\nOther\t\t\r\n");
		assertThat(sheet.toHtml()).contains("<td colspan=\"2\">Projects</td>", "<td></td><td>Task</td>");
	}

	@Test
	public void exportsGroupedHeaderOnSeveralRows() {
		final NodeModel price = childOf(header, "Price");
		childOf(price, "Min");
		childOf(price, "Max");
		childOf(header, "Brand");
		final NodeModel car = childOf(table, "Car");
		setValue(car, "Min", "1");
		setValue(car, "Max", "2");
		setValue(car, "Brand", "X");

		final TreeTableSheet sheet = TreeTableSheetExport.sheetOf(table);

		assertThat(sheet.toTabSeparatedText())
			.isEqualTo("Projects\tPrice\t\tBrand\r\n\tMin\tMax\t\r\nCar\t1\t2\tX\r\n");
		assertThat(sheet.toHtml()).contains("<td rowspan=\"2\">Projects</td>", "<td colspan=\"2\">Price</td>",
			"<td rowspan=\"2\">Brand</td>");
	}

	@Test
	public void exportsHtmlTextsAsPlainText() {
		childOf(header, "Note");
		final NodeModel car = childOf(table, "<html><body><p>Fast <b>car</b></p></body></html>");
		setValue(car, "Note", "<html><body><p>good</p></body></html>");

		final TreeTableSheet sheet = TreeTableSheetExport.sheetOf(table);

		assertThat(sheet.get(1, 0)).isEqualTo("Fast car");
		assertThat(sheet.get(1, 1)).isEqualTo("good");
	}
}
