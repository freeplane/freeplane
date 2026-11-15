package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class CellTextTreeTest {
	private static final String INDENT = "    ";

	private static List<CellTextTree.Node> parse(final String text) {
		return CellTextTree.parse(RichText.plain(text));
	}

	private static String describe(final List<CellTextTree.Node> nodes) {
		final StringBuilder description = new StringBuilder();
		for (final CellTextTree.Node node : nodes) {
			description.append(node.getContent());
			if (!node.getChildren().isEmpty())
				description.append('(').append(describe(node.getChildren())).append(')');
			description.append(';');
		}
		return description.toString();
	}

	@Test
	public void singleLineBecomesOneNode() {
		assertThat(describe(parse("12"))).isEqualTo("12;");
	}

	@Test
	public void emptyTextHasNoNodes() {
		assertThat(parse("")).isEmpty();
	}

	@Test
	public void flatLinesBecomeSiblings() {
		assertThat(describe(parse("a\nb\nc"))).isEqualTo("a;b;c;");
	}

	@Test
	public void indentedLinesBecomeChildren() {
		assertThat(describe(parse("a\n" + INDENT + "b\n" + INDENT + INDENT + "c\n" + INDENT + "d\ne")))
			.isEqualTo("a(b(c;);d;);e;");
	}

	@Test
	public void readsRegularSpacesAndTabsAsIndentation() {
		assertThat(describe(parse("a\n  b\n\t\tc\n  d"))).isEqualTo("a(b(c;);d;);");
	}

	@Test
	public void usesRelativeIndentationWhateverTheUnit() {
		assertThat(describe(parse("a\n b\n      c\n b2"))).isEqualTo("a(b(c;);b2;);");
	}

	@Test
	public void lineLessIndentedThanItsPredecessorAttachesToTheClosestShallowerLine() {
		assertThat(describe(parse("a\n    b\n        c\n  d"))).isEqualTo("a(b(c;);d;);");
	}

	@Test
	public void firstLineIndentationDoesNotCreateAParent() {
		assertThat(describe(parse("  a\n  b\n    c"))).isEqualTo("a;b(c;);");
	}

	@Test
	public void skipsBlankLines() {
		assertThat(describe(parse("a\n\n" + INDENT + "\n" + INDENT + "b"))).isEqualTo("a(b;);");
	}

	@Test
	public void keepsStyleOfIndentedRuns() {
		final RichText styled = RichText.fromContent(
			"<html><body><p>a<br>" + INDENT + "<b>bold</b></p></body></html>");

		final List<CellTextTree.Node> nodes = CellTextTree.parse(styled);

		assertThat(nodes).hasSize(1);
		assertThat(nodes.get(0).getChildren()).hasSize(1);
		assertThat(nodes.get(0).getChildren().get(0).getContent()).contains("font-weight: bold").contains("bold");
	}

	@Test
	public void restoresTheTreeExportedByTreeTableCells() {
		final MapFake map = new MapFake();
		final NodeModel cell = map.createNode("Prix");
		final NodeModel a = map.createNode("a");
		final NodeModel b = map.createNode("b");
		final NodeModel c = map.createNode("c");
		final NodeModel d = map.createNode("d");
		cell.insert(a);
		a.insert(b);
		b.insert(c);
		a.insert(d);
		cell.insert(map.createNode("e"));
		final String exported = TreeTableCells.contentOf(cell);

		final List<CellTextTree.Node> restored = parse(exported);

		assertThat(describe(restored)).isEqualTo("a(b(c;);d;);e;");
	}
}
