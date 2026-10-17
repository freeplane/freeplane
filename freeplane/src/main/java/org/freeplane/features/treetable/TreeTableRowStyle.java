package org.freeplane.features.treetable;

import java.util.Collection;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleFactory;

public class TreeTableRowStyle {
	public static final String NAME = "TreeNode";

	private static final int PRIORITY = (IPropertyHandler.STYLE + IPropertyHandler.AUTO) / 2;

	public static IStyle style() {
		return StyleFactory.create(NAME);
	}

	static void registerIn(final LogicalStyleController logicalStyleController) {
		logicalStyleController.addStyleGetter(PRIORITY, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			@Override
			public Collection<IStyle> getProperty(final NodeModel node, final StyleOption option,
					final Collection<IStyle> currentValue) {
				if (TreeTableNodes.isRow(node) && isDefinedIn(node))
					logicalStyleController.add(node, currentValue, style());
				return currentValue;
			}
		});
	}

	private static boolean isDefinedIn(final NodeModel node) {
		return MapStyleModel.getExtension(node.getMap()).getStyleNode(style()) != null;
	}

	private TreeTableRowStyle() {
	}
}
