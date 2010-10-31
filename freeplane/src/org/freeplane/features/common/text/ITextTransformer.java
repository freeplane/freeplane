package org.freeplane.features.common.text;

import org.freeplane.features.common.map.NodeModel;


public interface ITextTransformer {
	String transformText(String nodeText, NodeModel nodeModel);
}
