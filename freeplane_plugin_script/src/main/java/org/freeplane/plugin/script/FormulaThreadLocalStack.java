package org.freeplane.plugin.script;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;

public class FormulaThreadLocalStack {

	private FormulaThreadLocalStack() {

	}
	private final ThreadLocal<UniqueStack<NodeScript>> stack =
			new ThreadLocal<UniqueStack<NodeScript>>() {
		@Override protected UniqueStack<NodeScript> initialValue() {
			return new UniqueStack<NodeScript>();
		}
	};
	public static final FormulaThreadLocalStack INSTANCE = new FormulaThreadLocalStack();

	private UniqueStack<NodeScript> stack() {
		return stack.get();
	}

	public boolean push(NodeModel nodeModel, String script) {
		final boolean success = stack().push(new NodeScript(nodeModel, script));
		if (!success) {
			LogUtils.warn("Circular reference detected! Traceback (innermost last):\n " //
			        + stackTrace(nodeModel, script));
		}
		return success;
	}

	public void pop() {
		stack().pop();
	}

	private String stackTrace(NodeModel nodeModel, String script) {
		ArrayList<String> entries = new ArrayList<String>(stack().size());
		for (NodeScript node : stack()) {
			entries.add(format(node.nodeModel, node.script, nodeModel));
		}
		entries.add(format(nodeModel, script, nodeModel));
		return StringUtils.join(entries.iterator(), "\n -> ");
	}
	private String format(NodeModel nodeModel, String script, NodeModel nodeToHighlight) {
		return (nodeToHighlight.equals(nodeModel) ? "* " : "") + nodeModel.createID() + " "
		        + limitLength(deformat(nodeModel.getText()), 30) //
		        + " -> " + limitLength(script, 60);
	}

	private String deformat(String string) {
		return HtmlUtils.htmlToPlain(string).replaceAll("\\s+", " ");
	}

	private String limitLength(final String string, int maxLenght) {
		if (string == null || maxLenght >= string.length())
			return string;
		maxLenght = maxLenght > 3 ? maxLenght - 3 : maxLenght;
		return string.substring(0, maxLenght) + "...";
	}

}