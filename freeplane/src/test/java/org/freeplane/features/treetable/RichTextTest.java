package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RichTextTest {
	private static final String FREEPLANE_HTML = "<html>\n  <body>\n    <p>\n      Du <span style=\"color: #ff0000; font-weight: bold;\">HTML </span>ici\n    </p>\n  </body>\n</html>\n";

	private static final String EXCEL_HTML = "<html xmlns:x=\"urn:schemas-microsoft-com:office:excel\">\n"
			+ "<head><meta charset=utf-8>\n<style>\n<!--\n"
			+ "td\n\t{color:black;\n\tfont-weight:400;}\n"
			+ ".font5\n\t{color:red;\n\tfont-weight:700;}\n"
			+ ".xl65\n\t{mso-style-parent:style0;\n\tfont-style:italic;}\n"
			+ "-->\n</style>\n<!--[if gte mso 9]><xml><x:ExcelWorkbook></x:ExcelWorkbook></xml><![endif]-->\n</head>\n"
			+ "<body>\n<!--StartFragment-->\n<table border=0>\n"
			+ " <col width=64>\n"
			+ " <tr height=20>\n  <td colspan=2 rowspan=2>Car analysis</td>\n  <td class=xl65>Pros</td>\n </tr>\n"
			+ " <tr>\n  <td>Du <font class=\"font5\">HTML</font><font class=\"font0\"> ici</font></td>\n </tr>\n"
			+ " <tr>\n  <td>a&amp;b</td>\n  <td>Tr&egrave;s<br style='mso-data-placement:same-cell'>bien</td>\n  <td></td>\n </tr>\n"
			+ "</table>\n<!--EndFragment-->\n</body>\n</html>";

	@Test
	public void readsFreeplaneHtmlKeepingSpacesBetweenRuns() {
		final RichText text = RichText.fromContent(FREEPLANE_HTML);

		assertThat(text.plainText()).isEqualTo("Du HTML ici");
		assertThat(text.isFormatted()).isTrue();
		assertThat(text.getRuns()).hasSize(3);
		assertThat(text.getRuns().get(1).getStyle())
			.isEqualTo(RichTextStyle.PLAIN.withBold(true).withColor("#ff0000"));
	}

	@Test
	public void keepsPlainTextAsIs() {
		final RichText text = RichText.fromContent("plain");

		assertThat(text.isFormatted()).isFalse();
		assertThat(text.toFreeplaneContent()).isEqualTo("plain");
	}

	@Test
	public void writesFormattedRunsAsExcelHtml() {
		assertThat(RichText.fromContent(FREEPLANE_HTML).toExcelHtml())
			.isEqualTo("Du <font color=\"#ff0000\"><b>HTML </b></font>ici");
	}

	@Test
	public void writesFormattedRunsAsFreeplaneHtml() {
		assertThat(RichText.fromContent(FREEPLANE_HTML).toFreeplaneContent()).isEqualTo(
			"<html><body><p>Du <span style=\"color: #ff0000; font-weight: bold;\">HTML </span>ici</p></body></html>");
	}

	@Test
	public void readsTagsAndInlineStyles() {
		final RichText text = RichText
			.fromContent("<html><body><p><b>bold</b> <i>it</i> <u>un</u> <font color=\"blue\">blue</font></p>"
					+ "<p>next <span style=\"text-decoration: line-through\">gone</span></p></body></html>");

		assertThat(text.plainText()).isEqualTo("bold it un blue\nnext gone");
		assertThat(text.getRuns()).extracting(run -> run.getStyle().toString()).containsExactly("b", "", "i", "", "u",
			"", "#0000ff", "", "", "s");
	}

	@Test
	public void readsExcelTableWithClassStylesAndSpans() {
		final TreeTableSheet sheet = TreeTableSheet.parseHtmlTable(EXCEL_HTML);

		assertThat(sheet.getRowCount()).isEqualTo(3);
		assertThat(sheet.getColumnCount()).isEqualTo(3);
		assertThat(sheet.get(0, 0)).isEqualTo("Car analysis");
		assertThat(sheet.get(0, 1)).isEmpty();
		assertThat(sheet.get(0, 2)).isEqualTo("Pros");
		assertThat(sheet.getRichText(0, 2).getRuns().get(0).getStyle()).isEqualTo(RichTextStyle.PLAIN.withItalic(true));
		assertThat(sheet.get(1, 2)).isEqualTo("Du HTML ici");
		assertThat(sheet.getRichText(1, 2).getRuns()).extracting(run -> run.getStyle().toString())
			.containsExactly("", "b#ff0000", "");
		assertThat(sheet.get(2, 0)).isEqualTo("a&b");
		assertThat(sheet.get(2, 1)).isEqualTo("Très\nbien");
		assertThat(sheet.get(2, 2)).isEmpty();
	}

	@Test
	public void ignoresHtmlWithoutTable() {
		assertThat(TreeTableSheet.parseHtmlTable("<html><body><p>text</p></body></html>")).isNull();
	}

	@Test
	public void normalizesColors() {
		assertThat(RichTextStyle.normalizedColor("#F00")).isEqualTo("#ff0000");
		assertThat(RichTextStyle.normalizedColor("rgb(0, 128, 0)")).isEqualTo("#008000");
		assertThat(RichTextStyle.normalizedColor("windowtext")).isNull();
		assertThat(RichTextStyle.normalizedColor("black")).isNull();
	}
}
