package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.util.HtmlUtils;

public final class RichText {
	static final char NON_BREAKING_SPACE = ' ';
	private static final String LINE_BREAK = "\n";
	private static final String EXCEL_LINE_BREAK = "<br style=\"mso-data-placement:same-cell;\">";
	public static final RichText EMPTY = new RichText(Collections.<Run> emptyList());

	public static final class Run {
		private final String text;
		private final RichTextStyle style;

		public Run(final String text, final RichTextStyle style) {
			this.text = text;
			this.style = style;
		}

		public String getText() {
			return text;
		}

		public RichTextStyle getStyle() {
			return style;
		}

		boolean isLineBreak() {
			return LINE_BREAK.equals(text);
		}

		@Override
		public String toString() {
			return style.isPlain() ? text : "[" + style + ":" + text + "]";
		}
	}

	private final List<Run> runs;

	private RichText(final List<Run> runs) {
		this.runs = Collections.unmodifiableList(runs);
	}

	public static RichText plain(final String text) {
		if (text == null || text.isEmpty())
			return EMPTY;
		final List<Run> runs = new ArrayList<Run>();
		final String[] lines = text.replace("\r\n", LINE_BREAK).split(LINE_BREAK, -1);
		for (int index = 0; index < lines.length; index++) {
			if (index > 0)
				runs.add(new Run(LINE_BREAK, RichTextStyle.PLAIN));
			if (!lines[index].isEmpty())
				runs.add(new Run(lines[index], RichTextStyle.PLAIN));
		}
		return new RichText(runs);
	}

	public static RichText fromContent(final String content) {
		return content != null && HtmlUtils.isHtml(content) ? RichHtmlReader.readFragment(content) : plain(content);
	}

	static RichText normalized(final List<Run> rawRuns) {
		final List<Run> runs = new ArrayList<Run>();
		boolean atLineStart = true;
		for (final Run run : rawRuns) {
			if (run.isLineBreak()) {
				trimTrailingSpaces(runs);
				runs.add(run);
				atLineStart = true;
				continue;
			}
			String text = run.text;
			if (atLineStart || endsWithSpace(runs))
				text = stripLeadingSpaces(text);
			if (text.isEmpty())
				continue;
			append(runs, new Run(text, run.style));
			atLineStart = false;
		}
		trimTrailingSpaces(runs);
		while (!runs.isEmpty() && runs.get(0).isLineBreak())
			runs.remove(0);
		while (!runs.isEmpty() && runs.get(runs.size() - 1).isLineBreak())
			runs.remove(runs.size() - 1);
		return runs.isEmpty() ? EMPTY : new RichText(runs);
	}

	private static String stripLeadingSpaces(final String text) {
		int start = 0;
		while (start < text.length() && text.charAt(start) == ' ')
			start++;
		return text.substring(start);
	}

	private static boolean endsWithSpace(final List<Run> runs) {
		if (runs.isEmpty())
			return false;
		final String text = runs.get(runs.size() - 1).text;
		return text.endsWith(" ");
	}

	private static void trimTrailingSpaces(final List<Run> runs) {
		while (!runs.isEmpty() && !runs.get(runs.size() - 1).isLineBreak()) {
			final Run last = runs.remove(runs.size() - 1);
			int end = last.text.length();
			while (end > 0 && last.text.charAt(end - 1) == ' ')
				end--;
			if (end > 0) {
				runs.add(new Run(last.text.substring(0, end), last.style));
				return;
			}
		}
	}

	private static void append(final List<Run> runs, final Run run) {
		if (!runs.isEmpty()) {
			final Run last = runs.get(runs.size() - 1);
			if (!last.isLineBreak() && last.style.equals(run.style)) {
				runs.set(runs.size() - 1, new Run(last.text + run.text, run.style));
				return;
			}
		}
		runs.add(run);
	}

	public List<Run> getRuns() {
		return runs;
	}

	public boolean isEmpty() {
		return plainText().trim().isEmpty();
	}

	public boolean isFormatted() {
		for (final Run run : runs) {
			if (!run.isLineBreak() && !run.style.isPlain())
				return true;
		}
		return false;
	}

	public String plainText() {
		final StringBuilder text = new StringBuilder();
		for (final Run run : runs)
			text.append(run.text);
		return text.toString().replace(NON_BREAKING_SPACE, ' ');
	}

	public String toFreeplaneContent() {
		return isFormatted() ? toFreeplaneHtml() : plainText();
	}

	String toFreeplaneHtml() {
		final StringBuilder html = new StringBuilder("<html><body><p>");
		for (final Run run : runs) {
			if (run.isLineBreak())
				html.append("<br>");
			else if (run.style.isPlain())
				html.append(escapeHtml(run.text));
			else
				html.append("<span style=\"").append(cssOf(run.style)).append("\">").append(escapeHtml(run.text))
					.append("</span>");
		}
		return html.append("</p></body></html>").toString();
	}

	private static String cssOf(final RichTextStyle style) {
		final StringBuilder css = new StringBuilder();
		if (style.getColor() != null)
			css.append("color: ").append(style.getColor()).append("; ");
		if (style.isBold())
			css.append("font-weight: bold; ");
		if (style.isItalic())
			css.append("font-style: italic; ");
		if (style.isUnderline() || style.isStrikethrough())
			css.append("text-decoration:").append(style.isUnderline() ? " underline" : "")
				.append(style.isStrikethrough() ? " line-through" : "").append("; ");
		return css.toString().trim();
	}

	public String toExcelHtml() {
		final StringBuilder html = new StringBuilder();
		for (final Run run : runs) {
			if (run.isLineBreak())
				html.append(EXCEL_LINE_BREAK);
			else
				appendExcelRun(html, run);
		}
		return html.toString();
	}

	private static void appendExcelRun(final StringBuilder html, final Run run) {
		final RichTextStyle style = run.style;
		final StringBuilder closing = new StringBuilder();
		if (style.getColor() != null) {
			html.append("<font color=\"").append(style.getColor()).append("\">");
			closing.insert(0, "</font>");
		}
		appendTag(html, closing, style.isBold(), "b");
		appendTag(html, closing, style.isItalic(), "i");
		appendTag(html, closing, style.isUnderline(), "u");
		appendTag(html, closing, style.isStrikethrough(), "s");
		html.append(escapeHtml(run.text)).append(closing);
	}

	private static void appendTag(final StringBuilder html, final StringBuilder closing, final boolean applies,
			final String tag) {
		if (applies) {
			html.append('<').append(tag).append('>');
			closing.insert(0, "</" + tag + ">");
		}
	}

	private static String escapeHtml(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
			.replace(String.valueOf(NON_BREAKING_SPACE), "&nbsp;");
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof RichText && runs.toString().equals(((RichText) obj).runs.toString());
	}

	@Override
	public int hashCode() {
		return runs.toString().hashCode();
	}

	@Override
	public String toString() {
		return runs.toString();
	}
}
