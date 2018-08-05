package org.freeplane.features.explorer.mindmapmode;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.assertj.core.api.ThrowableAssert;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.junit.Test;
import org.mockito.Mockito;

public class MapExplorerSpec {

	static TextController textController = mock(TextController.class);
	static AccessedNodes accessedNodes = mock(AccessedNodes.class);


	static class ReferenceMapExplorer {
		NodeModel start;
		List<TestCommand> path;
		public ReferenceMapExplorer(NodeModel start, List<TestCommand> commands) {
			this.start = start;
			this.path = commands;
		}

	}

	static class TestCommand {
			TextController textController = MapExplorerSpec.textController;
			AccessedNodes accessedNodes = MapExplorerSpec.accessedNodes;
			String searchedString;
			ExploringStep operator;
			public TestCommand(ExploringStep operator, String searchedString) {
				super();
				this.operator = operator;
				this.searchedString = searchedString;
			}
	}

	private static TestCommand command(ExploringStep operator, String searchedString) {
		return new TestCommand(operator, searchedString);
	}

	NodeModel node = Mockito.mock(NodeModel.class);
	private void assertPath(String path, TestCommand... commands) {
		 assertThat(new MapExplorer(textController, node, path, accessedNodes)).isEqualToComparingFieldByFieldRecursively(
			new ReferenceMapExplorer(node, asList(commands)));
	}

	private void assertIllegalPath(final String path) {
		 assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				new MapExplorer(null, node, path, null);
			}
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void splitsPaths() throws Exception {
		assertPath("");
		assertPath("a", command(ExploringStep.CHILD, "a"));
		assertPath("->a", command(ExploringStep.CHILD, "a"));
		assertPath("<->a", command(ExploringStep.SIBLING, "a"));
		assertPath("->a->b", command(ExploringStep.CHILD, "a"), command(ExploringStep.CHILD, "b"));
		assertPath("-->a", command(ExploringStep.DESCENDANT, "a"));
		assertPath("<-", command(ExploringStep.PARENT, ""));

		assertIllegalPath("<-a");
		assertIllegalPath("<--");
		assertIllegalPath("<->");
		assertIllegalPath("->");

		assertPath("<--a", command(ExploringStep.ANCESTOR, "a"));
		assertPath(":", command(ExploringStep.ROOT, ""));
		assertPath(":a", command(ExploringStep.GLOBAL, "a"));
	}

}
