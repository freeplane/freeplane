package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TreeTableSheet {
	private static final char CELL_SEPARATOR = '\t';
	private static final char QUOTE = '"';
	private static final String LINE_SEPARATOR = "\r\n";
	private static final String HTML_LINE_BREAK = "<br style=\"mso-data-placement:same-cell;\">";

	public static class Span {
		private final int row;
		private final int column;
		private final int rowCount;
		private final int columnCount;

		public Span(final int row, final int column, final int rowCount, final int columnCount) {
			this.row = row;
			this.column = column;
			this.rowCount = rowCount;
			this.columnCount = columnCount;
		}

		boolean startsAt(final int row, final int column) {
			return this.row == row && this.column == column;
		}

		boolean covers(final int row, final int column) {
			return row >= this.row && row < this.row + rowCount && column >= this.column
					&& column < this.column + columnCount;
		}
	}

	private final String[][] cells;
	private final List<Span> spans;

	public TreeTableSheet(final int rowCount, final int columnCount) {
		cells = new String[rowCount][columnCount];
		for (final String[] row : cells)
			Arrays.fill(row, "");
		spans = new ArrayList<Span>();
	}

	public static TreeTableSheet parseTabSeparated(final String text) {
		final List<List<String>> rows = parseRows(text);
		removeTrailingEmptyRows(rows);
		int columnCount = 0;
		for (final List<String> row : rows)
			columnCount = Math.max(columnCount, row.size());
		final TreeTableSheet sheet = new TreeTableSheet(rows.size(), columnCount);
		for (int row = 0; row < rows.size(); row++) {
			for (int column = 0; column < rows.get(row).size(); column++)
				sheet.set(row, column, rows.get(row).get(column));
		}
		return sheet;
	}

	private static List<List<String>> parseRows(final String text) {
		final List<List<String>> rows = new ArrayList<List<String>>();
		List<String> row = new ArrayList<String>();
		final StringBuilder cell = new StringBuilder();
		boolean quoted = false;
		boolean cellStart = true;
		for (int index = 0; index < text.length(); index++) {
			final char character = text.charAt(index);
			if (quoted) {
				if (character != QUOTE)
					cell.append(character);
				else if (index + 1 < text.length() && text.charAt(index + 1) == QUOTE) {
					cell.append(QUOTE);
					index++;
				}
				else
					quoted = false;
			}
			else if (character == QUOTE && cellStart) {
				quoted = true;
				cellStart = false;
			}
			else if (character == CELL_SEPARATOR) {
				row.add(cell.toString());
				cell.setLength(0);
				cellStart = true;
			}
			else if (character == '\n' || character == '\r') {
				if (character == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n')
					index++;
				row.add(cell.toString());
				cell.setLength(0);
				rows.add(row);
				row = new ArrayList<String>();
				cellStart = true;
			}
			else {
				cell.append(character);
				cellStart = false;
			}
		}
		if (cell.length() > 0 || !row.isEmpty()) {
			row.add(cell.toString());
			rows.add(row);
		}
		return rows;
	}

	private static void removeTrailingEmptyRows(final List<List<String>> rows) {
		while (!rows.isEmpty() && isEmpty(rows.get(rows.size() - 1)))
			rows.remove(rows.size() - 1);
	}

	private static boolean isEmpty(final List<String> row) {
		for (final String cell : row) {
			if (!cell.trim().isEmpty())
				return false;
		}
		return true;
	}

	public int getRowCount() {
		return cells.length;
	}

	public int getColumnCount() {
		return cells.length == 0 ? 0 : cells[0].length;
	}

	public String get(final int row, final int column) {
		return cells[row][column];
	}

	public boolean isEmpty(final int row, final int column) {
		return cells[row][column].trim().isEmpty();
	}

	public void set(final int row, final int column, final String value) {
		cells[row][column] = value == null ? "" : value;
	}

	public void span(final int row, final int column, final int rowCount, final int columnCount) {
		if (rowCount > 1 || columnCount > 1)
			spans.add(new Span(row, column, rowCount, columnCount));
	}

	List<Span> getSpans() {
		return Collections.unmodifiableList(spans);
	}

	public String toTabSeparatedText() {
		final StringBuilder text = new StringBuilder();
		for (final String[] row : cells) {
			for (int column = 0; column < row.length; column++) {
				if (column > 0)
					text.append(CELL_SEPARATOR);
				text.append(quotedIfNeeded(row[column]));
			}
			text.append(LINE_SEPARATOR);
		}
		return text.toString();
	}

	private static String quotedIfNeeded(final String value) {
		if (value.indexOf(CELL_SEPARATOR) < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0
				&& value.indexOf(QUOTE) < 0)
			return value;
		return QUOTE + value.replace("\"", "\"\"") + QUOTE;
	}

	public String toHtml() {
		final StringBuilder html = new StringBuilder("<html><body><table border=\"1\">");
		for (int row = 0; row < getRowCount(); row++) {
			html.append("<tr>");
			for (int column = 0; column < getColumnCount(); column++)
				appendCell(html, row, column);
			html.append("</tr>");
		}
		return html.append("</table></body></html>").toString();
	}

	private void appendCell(final StringBuilder html, final int row, final int column) {
		final Span span = spanAt(row, column);
		if (span != null && !span.startsAt(row, column))
			return;
		html.append("<td");
		if (span != null) {
			if (span.rowCount > 1)
				html.append(" rowspan=\"").append(span.rowCount).append('"');
			if (span.columnCount > 1)
				html.append(" colspan=\"").append(span.columnCount).append('"');
		}
		html.append('>').append(escapeHtml(cells[row][column])).append("</td>");
	}

	private Span spanAt(final int row, final int column) {
		for (final Span span : spans) {
			if (span.covers(row, column))
				return span;
		}
		return null;
	}

	private static String escapeHtml(final String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
			.replace("\r\n", "\n").replace("\n", HTML_LINE_BREAK);
	}
}
