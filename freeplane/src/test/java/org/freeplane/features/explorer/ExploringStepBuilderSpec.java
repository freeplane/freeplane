package org.freeplane.features.explorer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.ThrowableAssert;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.junit.Test;
import org.mockito.Mockito;

public class ExploringStepBuilderSpec {

	static TextController textController = mock(TextController.class);
	static AccessedNodes accessedNodes = mock(AccessedNodes.class);


	private static Command command(ExploringStep operator, String searchedString) {
		return new Command(operator, searchedString, accessedNodes);
	}

	NodeModel node = Mockito.mock(NodeModel.class);
	private void assertPath(String path, Command... commands) {
		 assertThat(new ExploringStepBuilder(path, accessedNodes).buildSteps())//
		 	.usingFieldByFieldElementComparator().containsExactly(commands);
	}

	private void assertIllegalPath(final String path) {
		 assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call() throws Throwable {
				new ExploringStepBuilder(path, accessedNodes).buildSteps();
			}
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void illegalSeparatorAfterSeparator() throws Exception {
		assertIllegalPath("//");
	}

	@Test
	public void illegalChildAfterChild() throws Exception {
		assertIllegalPath("~a~b");
	}

	@Test
	public void illegalUnknownElement() throws Exception {
		assertIllegalPath("???");
	}

	@Test
	public void illegalAncestorAfterAncestor() throws Exception {
		assertIllegalPath("....");
	}

	@Test
	public void empty() {
		assertPath("");
	}


	@Test
	public void root() {
		assertPath("/", command(ExploringStep.ROOT, ""));
	}

	@Test
	public void globalNode() {
		assertPath(":~a", command(ExploringStep.GLOBAL, "~a"));
	}

	@Test
	public void firstAliasChild() {
		assertPath("~a", command(ExploringStep.CHILD, "~a"));
	}

	@Test
	public void firstSingleQuoteChild() {
		assertPath("'a'", command(ExploringStep.CHILD, "'a'"));
	}

	@Test
	public void firstDoubleQuoteChild() {
		assertPath("\"a\"", command(ExploringStep.CHILD, "\"a\""));
	}

	@Test
	public void childsChild() {
		assertPath("~a/~a", command(ExploringStep.CHILD, "~a"), command(ExploringStep.CHILD, "~a"));
	}

	@Test
	public void rootChild() {
		assertPath("/~a", command(ExploringStep.ROOT, ""), command(ExploringStep.CHILD, "~a"));
	}


	@Test
	public void anyChild() {
		assertPath("*", command(ExploringStep.CHILD, "'...'"));
	}

	@Test
	public void anyDescendant() {
		assertPath("**", command(ExploringStep.DESCENDANT, "'...'"));
	}

	@Test
	public void anyRootDescendant() {
		assertPath("/**", command(ExploringStep.ROOT, ""), command(ExploringStep.DESCENDANT, "'...'"));
	}

	@Test
	public void specificDescendant() {
		assertPath("**/~a", command(ExploringStep.DESCENDANT, "~a"));
	}

	@Test
	public void parent() {
		assertPath("..", command(ExploringStep.PARENT, ""));
	}
	@Test
	public void sibling() {
		assertPath("../~a", command(ExploringStep.PARENT, ""), command(ExploringStep.CHILD, "~a"));
	}

	@Test
	public void ancestor() {
		assertPath("..~a", command(ExploringStep.ANCESTOR, "~a"));
	}

	@Test
	public void spaceInTheMiddle() {
		assertPath("~a\t\n\r/\t\n\r~a",command(ExploringStep.CHILD, "~a"),command(ExploringStep.CHILD, "~a"));
	}

	@Test
	public void spaceAround() {
		assertPath("\t\n\r~a\t\n\r",command(ExploringStep.CHILD, "~a"));
	}
}
