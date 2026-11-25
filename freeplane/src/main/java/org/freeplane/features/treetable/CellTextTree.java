package org.freeplane.features.treetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CellTextTree {
	private static final int TAB_WIDTH = 4;

	public static class Node {
		private final RichText text;
		private final int indent;
		private final List<Node> children = new ArrayList<Node>();

		private Node(final RichText text, final int indent) {
			this.text = text;
			this.indent = indent;
		}

		public String getContent() {
			return text.toFreeplaneContent();
		}

		public List<Node> getChildren() {
			return Collections.unmodifiableList(children);
		}
	}

	public static List<Node> parse(final RichText cellText) {
		final List<Node> roots = new ArrayList<Node>();
		final List<Node> path = new ArrayList<Node>();
		for (final List<RichText.Run> line : linesOf(cellText)) {
			final Node node = nodeOf(line);
			if (node == null)
				continue;
			while (!path.isEmpty() && path.get(path.size() - 1).indent >= node.indent)
				path.remove(path.size() - 1);
			if (path.isEmpty())
				roots.add(node);
			else
				path.get(path.size() - 1).children.add(node);
			path.add(node);
		}
		return roots;
	}

	private static List<List<RichText.Run>> linesOf(final RichText cellText) {
		final List<List<RichText.Run>> lines = new ArrayList<List<RichText.Run>>();
		List<RichText.Run> line = new ArrayList<RichText.Run>();
		for (final RichText.Run run : cellText.getRuns()) {
			if (run.isLineBreak()) {
				lines.add(line);
				line = new ArrayList<RichText.Run>();
			}
			else
				line.add(run);
		}
		lines.add(line);
		return lines;
	}

	private static Node nodeOf(final List<RichText.Run> line) {
		int indent = 0;
		final List<RichText.Run> rest = new ArrayList<RichText.Run>();
		boolean inIndent = true;
		for (final RichText.Run run : line) {
			String text = run.getText();
			if (inIndent) {
				int start = 0;
				while (start < text.length() && isIndentCharacter(text.charAt(start))) {
					indent += text.charAt(start) == '\t' ? TAB_WIDTH : 1;
					start++;
				}
				text = text.substring(start);
				inIndent = text.isEmpty();
			}
			if (!text.isEmpty())
				rest.add(new RichText.Run(text, run.getStyle()));
		}
		final RichText text = RichText.ofRuns(rest);
		return text.isEmpty() ? null : new Node(text, indent);
	}

	private static boolean isIndentCharacter(final char character) {
		return character == ' ' || character == RichText.NON_BREAKING_SPACE || character == '\t';
	}

	private CellTextTree() {
	}
}
