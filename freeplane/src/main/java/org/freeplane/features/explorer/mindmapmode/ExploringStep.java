package org.freeplane.features.explorer.mindmapmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.features.map.NodeModel;

enum ExploringStep {
	ROOT("^::"){
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}
		@Override
		List<? extends NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, Cardinality cardinality) {
			return Collections.singletonList(node.getMap().getRootNode());
		}

	},

	GLOBAL("^:"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}
		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality) {
			final Iterable<NodeModel> nodes = GlobalNodes.readableOf(start.getMap());
			return getNodes(start, nodeMatcher, nodes, cardinality);
		}
	},

	ANCESTOR("<--"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(final NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality) {
			final Iterable<NodeModel> nodes = new Iterable<NodeModel>() {
				NodeModel current = start;
				@Override
				public Iterator<NodeModel> iterator() {
					return new Iterator<NodeModel>() {

						@Override
						public boolean hasNext() {
							return current.getParentNode() != null;
						}

						@Override
						public NodeModel next() {
							current = current.getParentNode();
							if(current == null)
								throw new NoSuchElementException();
							return current;
						}
					};
				}
			};
			return getNodes(start, nodeMatcher, nodes, Cardinality.FIRST);
		}

	},

	SIBLING("<->"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality) {
			final NodeModel parent = start.getParentNode();
			final Iterable<NodeModel> nodes = parent == null ? Collections.<NodeModel>emptyList() : parent.getChildren();
			return getNodes(start, nodeMatcher, nodes, cardinality);
		}

	},

	PARENT("<-"){
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, Cardinality cardinality) {
			return Collections.singletonList(node.getParentNode());
		}

	},

	CHILD("->"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality) {
			final Iterable<NodeModel> nodes = start.getChildren();
			return getNodes(start, nodeMatcher, nodes, cardinality);
		}

	};

	enum Cardinality {
		SINGLE, FIRST, ALL;

		public List<NodeModel> createList(NodeModel node) {
			if(this != ALL)
				return Collections.singletonList(node);
			else {
				final ArrayList<NodeModel> list = new ArrayList<>();
				list.add(node);
				return list;
			}
		}
	}

	static final Pattern operatorRegex = createOperatorRegex();

	static private Map<String, ExploringStep> operatorByPattern = mapPatternsToOperators();

	static ExploringStep of(String pattern) {
		final ExploringStep operator = operatorByPattern.get(toOperator(pattern));
		if(operator == null)
			throw new IllegalArgumentException("Invalid operator value " + toOperator(pattern));
		return operator;
	}

	public void assertValidString(String searchedString) {
		//
	}

	void assertNonEmpty(String string) {
		if(string.isEmpty())
			throw new IllegalArgumentException("Unexpected empty string");
	}

	void assertEmpty(String string) {
		if(!string.isEmpty())
			throw new IllegalArgumentException("Unexpected non empty string: " + string);
	}

	final private String pattern;

	private ExploringStep(String pattern) {
		this.pattern = toOperator(pattern);
	}

	private static Map<String, ExploringStep> mapPatternsToOperators() {
		final HashMap<String, ExploringStep> map = new HashMap<>(3);
		final ExploringStep[] operators = ExploringStep.values();
		for(ExploringStep o : operators) {
			final String pattern = o.pattern;
			map.put(toOperator(pattern) , o);
		}
		map.put("" , ExploringStep.CHILD);
		return map;
	}

	private static String toOperator(final String pattern) {
		if(pattern.startsWith("^"))
			return pattern.substring(1);
		else
			return pattern;
	}

	private static Pattern createOperatorRegex(){
		final ExploringStep[] operators = ExploringStep.values();
		String[] patterns = new String[operators.length];
		for (int i = 0; i < operators.length; i++) {
			patterns[i] = operators[i].pattern;
		}
		return Pattern.compile(String.join("|", patterns));
	}

	public static Matcher matcher(String path) {
		return operatorRegex.matcher(toOperator(path));
	}

	abstract List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality);

	public NodeModel getSingleNode(NodeModel start, NodeMatcher nodeMatcher) {
		return getNodes(start, nodeMatcher, Cardinality.SINGLE).get(0);
	}

	public Collection<? extends NodeModel> getAllNodes(NodeModel start, NodeMatcher nodeMatcher) {
		return getNodes(start, nodeMatcher, Cardinality.ALL);
	}

	protected List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Iterable<NodeModel> iterable,
											   Cardinality cardinality) {
		int counter = 0;
		List<NodeModel> nodes = null;
		for (NodeModel node : iterable) {
			if(nodeMatcher.matches(node)) {
				counter++;
				if(counter == 1) {
					nodes = cardinality.createList(node);
					if(cardinality == Cardinality.FIRST)
						return nodes;
				}
				else {
					assertValidNodeCount(cardinality, counter);
					nodes.add(node);
				}
			}
		}
		assertValidNodeCount(cardinality, counter);
		return nodes;
	}

	private void assertValidNodeCount(Cardinality cardinality, int counter) {
		if(counter != 1 && cardinality != Cardinality.SINGLE)
			throw new IllegalStateException("One and only one node matching giving string expected, " + counter + " nodes found");
	}
}