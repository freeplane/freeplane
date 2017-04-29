package org.freeplane.features.highlight;

import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class HighlightController implements IExtension{
	final private List<NodeHighlighter> nodeHighlighters;

	public HighlightController() {
		super();
		this.nodeHighlighters = new ArrayList<>();
	} 
	
	public List<NodeHighlighter> getHighlighters(NodeModel node, boolean isPrinting){
		final ArrayList<NodeHighlighter> highlighters = new ArrayList<>();
		for(NodeHighlighter highlighter : nodeHighlighters)
			if(highlighter.isNodeHighlighted(node, isPrinting))
				highlighters.add(highlighter);
		return highlighters;
	}
	
	public void addNodeHighlighter(NodeHighlighter highlighter){
		nodeHighlighters.add(highlighter);
	}

	public void removeNodeHighlighter(NodeHighlighter highlighter){
		nodeHighlighters.remove(highlighter);
	}
}
