package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TreeTableSheetTest {

	@Test
	public void writesTabSeparatedRows() {
		final TreeTableSheet sheet = new TreeTableSheet(2, 2);
		sheet.set(0, 0, "Name");
		sheet.set(0, 1, "Cost");
		sheet.set(1, 0, "Car");
		sheet.set(1, 1, "10");

		assertThat(sheet.toTabSeparatedText()).isEqualTo("Name\tCost\r\nCar\t10\r\n");
	}

	@Test
	public void quotesCellsContainingSeparatorsOrQuotes() {
		final TreeTableSheet sheet = new TreeTableSheet(1, 2);
		sheet.set(0, 0, "two\nlines");
		sheet.set(0, 1, "say \"hi\"");

		assertThat(sheet.toTabSeparatedText()).isEqualTo("\"two\nlines\"\t\"say \"\"hi\"\"\"\r\n");
	}

	@Test
	public void parsesExcelTabSeparatedText() {
		final TreeTableSheet sheet = TreeTableSheet
			.parseTabSeparated("Name\tCost\r\n\"two\nlines\"\t\"say \"\"hi\"\"\"\r\nCar\r\n\r\n");

		assertThat(sheet.getRowCount()).isEqualTo(3);
		assertThat(sheet.getColumnCount()).isEqualTo(2);
		assertThat(sheet.get(1, 0)).isEqualTo("two\nlines");
		assertThat(sheet.get(1, 1)).isEqualTo("say \"hi\"");
		assertThat(sheet.get(2, 0)).isEqualTo("Car");
		assertThat(sheet.get(2, 1)).isEmpty();
	}

	@Test
	public void parsesTextWithoutFinalLineBreak() {
		final TreeTableSheet sheet = TreeTableSheet.parseTabSeparated("a\tb\nc\td");

		assertThat(sheet.getRowCount()).isEqualTo(2);
		assertThat(sheet.get(1, 1)).isEqualTo("d");
	}

	@Test
	public void roundTripsThroughTabSeparatedText() {
		final TreeTableSheet sheet = new TreeTableSheet(2, 2);
		sheet.set(0, 0, "a\tb");
		sheet.set(1, 1, "\"x\"\r\ny");

		final TreeTableSheet parsed = TreeTableSheet.parseTabSeparated(sheet.toTabSeparatedText());

		assertThat(parsed.get(0, 0)).isEqualTo("a\tb");
		assertThat(parsed.get(0, 1)).isEmpty();
		assertThat(parsed.get(1, 1)).isEqualTo("\"x\"\r\ny");
	}

	@Test
	public void writesSpansAsMergedHtmlCells() {
		final TreeTableSheet sheet = new TreeTableSheet(2, 3);
		sheet.set(0, 0, "Root");
		sheet.span(0, 0, 2, 2);
		sheet.set(0, 2, "A & B");
		sheet.set(1, 2, "<1>");

		assertThat(sheet.toHtml()).isEqualTo("<html><body><table border=\"1\">"
				+ "<tr><td rowspan=\"2\" colspan=\"2\">Root</td><td>A &amp; B</td></tr>"
				+ "<tr><td>&lt;1&gt;</td></tr>"
				+ "</table></body></html>");
	}
}
