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

import org.freeplane.features.filter.Searcher;
import org.freeplane.features.filter.Searcher.Algorithm;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.NodeModel;

enum ExploringStep {
	ROOT("^::"){
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}
		@Override
		List<? extends NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			final NodeModel rootNode = node.getMap().getRootNode();
			accessedNodes.accessNode(rootNode);
			return Collections.singletonList(rootNode);
		}

	},

	GLOBAL("^:"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}
		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			final Iterable<NodeModel> nodes = GlobalNodes.readableOf(start.getMap());
			accessedNodes.accessGlobalNode();
			return nodeMatcher.filterMatchingNodes(nodes, cardinality, accessedNodes);
		}
	},

	ANCESTOR("<--"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(final NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
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

						@Override
						public void remove() {
					        throw new UnsupportedOperationException("remove");
					    }
					};
				}
			};
			return nodeMatcher.filterMatchingNodes(nodes, Cardinality.FIRST, accessedNodes);
		}

	},

	SIBLING("<->"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			final NodeModel parent = start.getParentNode();
			accessedNodes.accessNode(parent);
			final Iterable<NodeModel> nodes = parent == null ? Collections.<NodeModel>emptyList() : parent.getChildren();
			return nodeMatcher.filterMatchingNodes(nodes, cardinality, accessedNodes);
		}

	},

	PARENT("<-"){
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			final NodeModel parentNode = node.getParentNode();
			accessedNodes.accessNode(parentNode);
			return Collections.singletonList(parentNode);
		}

	},

	CHILD("->"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			final Iterable<NodeModel> nodes = start.getChildren();
			return nodeMatcher.filterMatchingNodes(nodes, cardinality, accessedNodes);
		}

	},

	DESCENDANT("-->"){
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<? extends NodeModel> getNodes(NodeModel start, final NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes) {
			accessedNodes.accessBranch(start);
			final ICondition condition = new ICondition() {
				@Override
				public boolean checkNode(NodeModel node) {
					return nodeMatcher.matches(node);
				}
			};
			return new Searcher(Algorithm.DEPTH_FIRST).condition(condition).find(start.getChildren());
		}
	};

	public enum Cardinality {
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
		StringBuilder patterns = new StringBuilder();
		for (int i = 0; i < operators.length; i++) {
			if(i > 0)
				patterns.append('|');
			patterns.append(operators[i].pattern);
		}
		return Pattern.compile(patterns.toString());
	}

	public static Matcher matcher(String path) {
		return operatorRegex.matcher(toOperator(path));
	}

	abstract List<? extends NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, Cardinality cardinality, AccessedNodes accessedNodes);


	NodeModel getSingleNode(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
		return getNodes(start, nodeMatcher, Cardinality.SINGLE, accessedNodes).get(0);
	}

	Collection<? extends NodeModel> getAllNodes(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
		return getNodes(start, nodeMatcher, Cardinality.ALL, accessedNodes);
	}

}