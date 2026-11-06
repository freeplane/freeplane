package org.freeplane.features.treetable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.freeplane.features.treetable.RichText.Run;

class RichHtmlReader {
	private static final Pattern CSS_RULE = Pattern.compile("([^{}]+)\\{([^}]*)\\}");
	private static final Pattern CSS_CLASS = Pattern.compile("\\.([\\w-]+)$");
	private static final Pattern ATTRIBUTE = Pattern
		.compile("([\\w:-]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+)))?");
	private static final Pattern WHITESPACE = Pattern.compile("[ \\t\\r\\n\\f]+");
	private static final Set<String> VOID_TAGS = new HashSet<String>(
		Arrays.asList("br", "img", "meta", "link", "col", "hr", "input", "base", "wbr"));
	private static final Set<String> HIDDEN_TAGS = new HashSet<String>(
		Arrays.asList("head", "title", "script", "xml", "style"));
	private static final Set<String> BLOCK_TAGS = new HashSet<String>(
		Arrays.asList("p", "div", "li", "h1", "h2", "h3", "h4", "h5", "h6", "tr"));
	private static class Cell {
		private final List<Run> runs = new ArrayList<Run>();
		private final int rowCount;
		private final int columnCount;

		private Cell(final int rowCount, final int columnCount) {
			this.rowCount = Math.max(1, rowCount);
			this.columnCount = Math.max(1, columnCount);
		}
	}

	private static class OpenTag {
		private final String name;
		private final RichTextStyle style;

		private OpenTag(final String name, final RichTextStyle style) {
			this.name = name;
			this.style = style;
		}
	}

	private final String html;
	private final boolean readsTable;
	private final Map<String, String> classDeclarations = new HashMap<String, String>();
	private final Deque<OpenTag> openTags = new ArrayDeque<OpenTag>();
	private final List<Run> fragmentRuns = new ArrayList<Run>();
	private final List<List<Cell>> rows = new ArrayList<List<Cell>>();
	private Cell cell;
	private int tableDepth;
	private int hiddenDepth;
	private int position;

	static RichText readFragment(final String html) {
		final RichHtmlReader reader = new RichHtmlReader(html, false);
		reader.read();
		return RichText.normalized(reader.fragmentRuns);
	}

	static TreeTableSheet readTable(final String html) {
		final RichHtmlReader reader = new RichHtmlReader(html, true);
		reader.read();
		return reader.sheet();
	}

	private RichHtmlReader(final String html, final boolean readsTable) {
		this.html = html;
		this.readsTable = readsTable;
	}

	private RichTextStyle currentStyle() {
		return openTags.isEmpty() ? RichTextStyle.PLAIN : openTags.peek().style;
	}

	private void read() {
		while (position < html.length()) {
			if (html.startsWith("<!--", position))
				skipPast("-->");
			else if (html.startsWith("<!", position) || html.startsWith("<?", position))
				skipPast(">");
			else if (html.charAt(position) == '<' && position + 1 < html.length()
					&& (Character.isLetter(html.charAt(position + 1)) || html.charAt(position + 1) == '/'))
				readTag();
			else
				readText();
		}
	}

	private void skipPast(final String end) {
		final int index = html.indexOf(end, position);
		position = index < 0 ? html.length() : index + end.length();
	}

	private void readText() {
		final int next = html.indexOf('<', position + 1);
		final int end = next < 0 ? html.length() : next;
		final String text = html.substring(position, end);
		position = end;
		if (hiddenDepth == 0)
			addText(WHITESPACE.matcher(decodeEntities(text)).replaceAll(" "));
	}

	private void readTag() {
		final int end = html.indexOf('>', position);
		final String tag = html.substring(position + 1, end < 0 ? html.length() : end);
		position = end < 0 ? html.length() : end + 1;
		if (tag.startsWith("/"))
			closeTag(tagName(tag.substring(1)));
		else
			openTag(tagName(tag), tag);
	}

	private static String tagName(final String tag) {
		int end = 0;
		while (end < tag.length() && !Character.isWhitespace(tag.charAt(end)) && tag.charAt(end) != '/'
				&& tag.charAt(end) != '>')
			end++;
		return tag.substring(0, end).toLowerCase(Locale.ENGLISH);
	}

	private void openTag(final String name, final String tag) {
		if ("style".equals(name)) {
			readStyleSheet();
			return;
		}
		final Map<String, String> attributes = attributesOf(tag.substring(name.length()));
		if ("br".equals(name))
			addLineBreak();
		else if (BLOCK_TAGS.contains(name))
			addBlockBreak();
		if ("table".equals(name))
			tableDepth++;
		if (readsTable && tableDepth == 1)
			openTableElement(name, attributes);
		if (VOID_TAGS.contains(name) || tag.endsWith("/"))
			return;
		if (HIDDEN_TAGS.contains(name))
			hiddenDepth++;
		openTags.push(new OpenTag(name, styleOf(name, attributes)));
	}

	private void openTableElement(final String name, final Map<String, String> attributes) {
		if ("tr".equals(name))
			rows.add(new ArrayList<Cell>());
		else if ("td".equals(name) || "th".equals(name)) {
			if (rows.isEmpty())
				rows.add(new ArrayList<Cell>());
			cell = new Cell(intAttribute(attributes, "rowspan"), intAttribute(attributes, "colspan"));
			rows.get(rows.size() - 1).add(cell);
		}
	}

	private static int intAttribute(final Map<String, String> attributes, final String name) {
		try {
			return Integer.parseInt(attributes.getOrDefault(name, "1").trim());
		}
		catch (final NumberFormatException e) {
			return 1;
		}
	}

	private void closeTag(final String name) {
		if (!containsOpenTag(name))
			return;
		OpenTag closed;
		do {
			closed = openTags.pop();
			if (HIDDEN_TAGS.contains(closed.name))
				hiddenDepth--;
		} while (!closed.name.equals(name));
		if (BLOCK_TAGS.contains(name))
			addBlockBreak();
		if ("td".equals(name) || "th".equals(name)) {
			if (tableDepth == 1)
				cell = null;
		}
		else if ("table".equals(name))
			tableDepth--;
	}

	private boolean containsOpenTag(final String name) {
		for (final OpenTag openTag : openTags) {
			if (openTag.name.equals(name))
				return true;
		}
		return false;
	}

	private RichTextStyle styleOf(final String name, final Map<String, String> attributes) {
		RichTextStyle style = currentStyle();
		switch (name) {
		case "b":
		case "strong":
			style = style.withBold(true);
			break;
		case "i":
		case "em":
			style = style.withItalic(true);
			break;
		case "u":
			style = style.withUnderline(true);
			break;
		case "s":
		case "strike":
		case "del":
			style = style.withStrikethrough(true);
			break;
		case "font":
			if (attributes.containsKey("color"))
				style = style.withColor(attributes.get("color"));
			break;
		default:
			break;
		}
		final String classes = attributes.get("class");
		if (classes != null) {
			for (final String className : classes.trim().split("\\s+"))
				style = style.withCss(classDeclarations.get(className.toLowerCase(Locale.ENGLISH)));
		}
		return style.withCss(attributes.get("style"));
	}

	private void readStyleSheet() {
		final int end = html.toLowerCase(Locale.ENGLISH).indexOf("</style", position);
		final String css = html.substring(position, end < 0 ? html.length() : end).replace("<!--", "")
			.replace("-->", "");
		position = end < 0 ? html.length() : end;
		skipPast(">");
		final Matcher rule = CSS_RULE.matcher(css.replaceAll("/\\*.*?\\*/", ""));
		while (rule.find()) {
			for (final String selector : rule.group(1).split(",")) {
				final Matcher className = CSS_CLASS.matcher(selector.trim());
				if (className.find())
					classDeclarations.put(className.group(1).toLowerCase(Locale.ENGLISH), rule.group(2));
			}
		}
	}

	private static Map<String, String> attributesOf(final String text) {
		final Map<String, String> attributes = new HashMap<String, String>();
		final Matcher matcher = ATTRIBUTE.matcher(text);
		while (matcher.find()) {
			String value = matcher.group(2);
			if (value == null)
				value = matcher.group(3);
			if (value == null)
				value = matcher.group(4);
			attributes.put(matcher.group(1).toLowerCase(Locale.ENGLISH), value == null ? "" : decodeEntities(value));
		}
		return attributes;
	}

	private List<Run> currentRuns() {
		if (!readsTable)
			return fragmentRuns;
		return cell == null ? null : cell.runs;
	}

	private void addText(final String text) {
		final List<Run> runs = currentRuns();
		if (runs != null && !text.isEmpty())
			runs.add(new Run(text, currentStyle()));
	}

	private void addLineBreak() {
		final List<Run> runs = currentRuns();
		if (runs != null && hiddenDepth == 0)
			runs.add(new Run("\n", RichTextStyle.PLAIN));
	}

	private void addBlockBreak() {
		final List<Run> runs = currentRuns();
		if (runs != null && !runs.isEmpty() && !runs.get(runs.size() - 1).isLineBreak())
			addLineBreak();
	}

	static String decodeEntities(final String text) {
		return text.indexOf('&') < 0 ? text : StringEscapeUtils.unescapeHtml(text);
	}

	private TreeTableSheet sheet() {
		final List<boolean[]> occupied = new ArrayList<boolean[]>();
		final List<int[]> positions = new ArrayList<int[]>();
		final List<Cell> cells = new ArrayList<Cell>();
		int columnCount = 0;
		for (int row = 0; row < rows.size(); row++) {
			int column = 0;
			for (final Cell rowCell : rows.get(row)) {
				while (isOccupied(occupied, row, column))
					column++;
				occupy(occupied, row, column, rowCell);
				positions.add(new int[] { row, column });
				cells.add(rowCell);
				column += rowCell.columnCount;
				columnCount = Math.max(columnCount, column);
			}
		}
		if (cells.isEmpty())
			return null;
		final TreeTableSheet sheet = new TreeTableSheet(rows.size(), columnCount);
		for (int index = 0; index < cells.size(); index++) {
			final Cell placed = cells.get(index);
			final int row = positions.get(index)[0];
			final int column = positions.get(index)[1];
			sheet.set(row, column, RichText.normalized(placed.runs));
			sheet.span(row, column, Math.min(placed.rowCount, rows.size() - row), placed.columnCount);
		}
		return sheet;
	}

	private static boolean isOccupied(final List<boolean[]> occupied, final int row, final int column) {
		return row < occupied.size() && column < occupied.get(row).length && occupied.get(row)[column];
	}

	private static void occupy(final List<boolean[]> occupied, final int row, final int column, final Cell cell) {
		for (int spannedRow = row; spannedRow < row + cell.rowCount; spannedRow++) {
			while (occupied.size() <= spannedRow)
				occupied.add(new boolean[0]);
			boolean[] flags = occupied.get(spannedRow);
			if (flags.length < column + cell.columnCount) {
				flags = Arrays.copyOf(flags, column + cell.columnCount);
				occupied.set(spannedRow, flags);
			}
			for (int spannedColumn = column; spannedColumn < column + cell.columnCount; spannedColumn++)
				flags[spannedColumn] = true;
		}
	}
}
