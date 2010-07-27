package org.freeplane.features.common.text;

import org.freeplane.features.common.map.NodeModel;


public interface ITextTransformer {
	String transform(String nodeText, NodeModel nodeModel);
}
