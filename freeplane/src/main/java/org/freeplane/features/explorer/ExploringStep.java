package org.freeplane.features.explorer;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeStream;

enum ExploringStep {
	ROOT{
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}
		@Override
		List<NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
			final NodeModel rootNode = node.getMap().getRootNode();
			accessedNodes.accessNode(rootNode);
			return Collections.singletonList(rootNode);
		}

	},

	GLOBAL{
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}
		@Override
		List<NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
			final Iterable<NodeModel> nodes = GlobalNodes.readableOf(start.getMap());
			accessedNodes.accessGlobalNode();
			return nodeMatcher.filterMatchingNodes(nodes, accessedNodes);
		}
	},

	ANCESTOR{
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<NodeModel> getNodes(final NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
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
			return nodeMatcher.filterMatchingNodes(nodes, accessedNodes);
		}

	},

		PARENT{
		@Override
		public void assertValidString(String searchedString) {
			assertEmpty(searchedString);
		}

		@Override
		List<NodeModel> getNodes(NodeModel node, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
			final NodeModel parentNode = node.getParentNode();
			accessedNodes.accessNode(parentNode);
			return Collections.singletonList(parentNode);
		}

	},

	CHILD{
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
			final Iterable<NodeModel> nodes = start.getChildren();
			return nodeMatcher.filterMatchingNodes(nodes, accessedNodes);
		}

	},

	DESCENDANT{
		@Override
		public void assertValidString(String searchedString) {
			assertNonEmpty(searchedString);
		}

		@Override
		List<NodeModel> getNodes(NodeModel start, final NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
			accessedNodes.accessBranch(start);
			return NodeStream.of(start).skip(1).filter(nodeMatcher::matches).collect(Collectors.toList());
		}
	};

	public enum Cardinality {
		SINGLE, FIRST, ALL;
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

	abstract List<NodeModel> getNodes(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes);


	NodeModel getSingleNode(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
		final List<NodeModel> nodes = getNodes(start, nodeMatcher, accessedNodes);
		return nodes.get(0);
	}

	Collection<NodeModel> getAllNodes(NodeModel start, NodeMatcher nodeMatcher, AccessedNodes accessedNodes) {
		return getNodes(start, nodeMatcher, accessedNodes);
	}

}