package org.freeplane.features.explorer.mindmapmode;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.freeplane.features.explorer.mindmapmode.ExploringStep.Cardinality;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

class NodeMatcher {
	enum MatchedElement{
		ALIAS {
			@Override
			String matchedStringOf(String searchedString) {
				return searchedString.substring(1);
			}
		},
		COUNTER {
			@Override
			String matchedStringOf(String searchedString) {
				return searchedString.substring(1);
			}
		},
		TEXT {
			@Override
			String matchedStringOf(String searchedString) {
				return searchedString.substring(1, searchedString.length() - 1);
			}
		}, 
		START {
			@Override
			String matchedStringOf(String searchedString) {
				return searchedString.substring(1, searchedString.length() - REST_CHARACTERS.length() - 1);
			}
		}, 
		NO_CONTENT {
			@Override
			String matchedStringOf(String searchedString) {
				return "";
			}
		};
		

		static MatchedElement of(String searchedString) {
			if(searchedString.isEmpty())
				return NO_CONTENT;
			if(searchedString.startsWith("#"))
				if(StringUtils.isNumeric(searchedString.substring(1)))
					return COUNTER;
				else
					return ALIAS;
			else if(searchedString.length() >= 2) {
				if (searchedString.startsWith("\"")) {
					if (searchedString.endsWith(REST_CHARACTERS + "\""))
						return START;
					else if (searchedString.endsWith("\""))
						return TEXT;
				}
				else if (searchedString.startsWith("'")) {
					if (searchedString.endsWith(REST_CHARACTERS + "'"))
						return START;
					else if (searchedString.endsWith("'"))
						return TEXT;
				}
			}
			throw new IllegalArgumentException("invalid search string "  + searchedString);
		}
		
		abstract String matchedStringOf(String searchedString);
	}
	
	private final String matchedString;
	private final MatchedElement matchedElement;
	private final TextController textController;
	private static final String REST_CHARACTERS = "...";

	

	public NodeMatcher(TextController textController, String searchedString) {
		this.textController = textController;
		this.matchedElement = MatchedElement.of(searchedString);
		this.matchedString = matchedElement.matchedStringOf(searchedString);
	}

	public boolean matches(NodeModel node) {
		if(matchedElement == MatchedElement.COUNTER)
			throw new IllegalArgumentException("Can not match nodes by index");
		if(matchedElement == MatchedElement.ALIAS)
			return matches(node.getExtension(NodeAlias.class));
		else 
			return matches(textController.getPlainTransformedTextWithoutNodeNumber(node));
	}

	private boolean matches(NodeAlias alias) {
		return alias != null && matchedString.equals(alias.value);
	}

	private boolean matches(final String text) {
		if(matchedElement == MatchedElement.START) 
			return text.startsWith(matchedString) ;
		else 
			return text.equals(matchedString);
	}

	public List<? extends NodeModel> filterMatchingNodes(Iterable<NodeModel> iterable, Cardinality cardinality, AccessedNodes accessedNodes) {
		if(matchedElement == MatchedElement.COUNTER) {
			return getNodeByCounter(iterable, cardinality, accessedNodes);
		}
		else
			return getNodesByContent(iterable, cardinality, accessedNodes);
	}

	private List<? extends NodeModel> getNodesByContent(Iterable<NodeModel> iterable, Cardinality cardinality,
	                                                    AccessedNodes accessedNodes) {
		int counter = 0;
		List<NodeModel> nodes = null;
		for (NodeModel node : iterable) {
			accessedNodes.accessNode(node);
			if(matches(node)) {
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
	
	private List<? extends NodeModel> getNodeByCounter(Iterable<NodeModel> iterable, Cardinality cardinality, AccessedNodes accessedNodes) {
		int counter = 1;
		int requiredCounter = Integer.valueOf(matchedString);
		for(NodeModel node : iterable) {
			if(counter == requiredCounter)
				return Collections.singletonList(node);
			counter++;
		}
		assertValidNodeCount(cardinality, 0);
		return Collections.emptyList();
	}

	private void assertValidNodeCount(Cardinality cardinality, int counter) {
		if(counter != 1 && cardinality != Cardinality.SINGLE)
			throw new IllegalStateException("One and only one node matching giving string expected, " + counter + " nodes found");
	}


}