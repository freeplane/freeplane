package org.freeplane.features.treetable;

import java.util.Collection;
import java.util.function.Predicate;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleFactory;

public class TreeTableAutoStyle {
	public static final TreeTableAutoStyle ROW = new TreeTableAutoStyle("TreeNode", TreeTableNodes::isRow);
	public static final TreeTableAutoStyle TABLE = new TreeTableAutoStyle("TreeTable", TreeTableModel::isTreeTableRoot);

	private static final int PRIORITY = (IPropertyHandler.STYLE + IPropertyHandler.AUTO) / 2;

	private final String name;
	private final Predicate<NodeModel> appliesTo;

	private TreeTableAutoStyle(final String name, final Predicate<NodeModel> appliesTo) {
		this.name = name;
		this.appliesTo = appliesTo;
	}

	public String getName() {
		return name;
	}

	public IStyle style() {
		return StyleFactory.create(name);
	}

	static void registerAllIn(final LogicalStyleController logicalStyleController) {
		ROW.registerIn(logicalStyleController);
		TABLE.registerIn(logicalStyleController);
	}

	private void registerIn(final LogicalStyleController logicalStyleController) {
		logicalStyleController.addStyleGetter(PRIORITY, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			@Override
			public Collection<IStyle> getProperty(final NodeModel node, final StyleOption option,
					final Collection<IStyle> currentValue) {
				if (appliesTo.test(node) && isDefinedIn(node))
					logicalStyleController.add(node, currentValue, style());
				return currentValue;
			}
		});
	}

	private boolean isDefinedIn(final NodeModel node) {
		return MapStyleModel.getExtension(node.getMap()).getStyleNode(style()) != null;
	}
}
