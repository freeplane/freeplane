package org.freeplane.features.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.NodeModel;

public class Searcher {

	public enum Algorithm {
		BREADTH_FIRST, DEPTH_FIRST
	}

	private ICondition condition;
	private Algorithm algorithm;
	public Searcher(Algorithm algorithm) {
		super();
		this.algorithm = algorithm;
		this.condition = null;
	}

	public Searcher condition(ICondition condition) {
		this.condition = condition;
		return this;
	}

	/** finds from any node downwards.
	 * @param condition if null every node will match. */
	public List<NodeModel> find(NodeModel node) {
		return find(Collections.singletonList(node));
	}

	public List<NodeModel> find(Iterable<NodeModel> children) {
		final List<NodeModel> matches = new ArrayList<NodeModel>();
		for(NodeModel node : children) {
			final boolean nodeMatches = condition == null || condition.checkNode(node);
			// a shortcut for non-matching leaves
			if (nodeMatches) {
				matches.add(node);
			}
			if(algorithm == Algorithm.DEPTH_FIRST)
				matches.addAll(find(node.getChildren()));
		}
		if(algorithm == Algorithm.BREADTH_FIRST) {
			for(NodeModel node : children) {
				matches.addAll(find(node.getChildren()));
			}

		}
		return matches;
	}
}