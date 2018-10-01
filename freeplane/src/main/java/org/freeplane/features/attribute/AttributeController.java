/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.attribute;

import java.awt.Component;
import java.awt.Font;
import java.net.URI;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.SetBooleanMapPropertyAction;
import org.freeplane.features.text.TextController;

/**
 * @author Dimitry Polivaev 22.11.2008
 */
public class AttributeController implements IExtension, AttributeSelection {
	public static final String SHOW_ICON_FOR_ATTRIBUTES = "show_icon_for_attributes";
	private static final Integer ATTRIBUTE_TOOLTIP = 7;
	static private UIIcon attributeIcon = null;
	static private AttributeSelection attributeSelection;

	public static void setAttributeSelection(AttributeSelection attributeSelection) {
		if(AttributeController.attributeSelection != null)
			throw new IllegalStateException();
		AttributeController.attributeSelection = attributeSelection;
	}

	public static AttributeController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static AttributeController getController(ModeController modeController) {
		return modeController.getExtension(AttributeController.class);
	}

	public static void install( final AttributeController attributeController) {
		Controller.getCurrentModeController().addExtension(AttributeController.class, attributeController);
	}

 	final private ModeController modeController;

	public AttributeController(final ModeController modeController) {
		this.modeController = modeController;
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final MapReader mapReader = mapController.getMapReader();
		final AttributeBuilder attributeBuilder = new AttributeBuilder(this, mapReader);
		attributeBuilder.registerBy(readManager, writeManager);
		modeController.addAction(new SetBooleanMapPropertyAction(SHOW_ICON_FOR_ATTRIBUTES));
		registerTooltipProvider();
		registerStateIconProvider();
	}

	public NodeAttributeTableModel createAttributeTableModel(final NodeModel node) {
		NodeAttributeTableModel attributeModel = node
		    .getExtension(NodeAttributeTableModel.class);
		if (attributeModel != null) {
			return attributeModel;
		}
		attributeModel = new NodeAttributeTableModel(node);
		node.addExtension(attributeModel);
		if (node.areViewsEmpty()) {
			return attributeModel;
		}
		modeController.getMapController().nodeRefresh(node);
		return attributeModel;
	}

	public void performInsertRow(final NodeModel node, final NodeAttributeTableModel model, final int row, final String name,
	                             final Object value) {
		throw new UnsupportedOperationException();
	}

	public void performRegistryAttribute(MapModel map, final String name) {
		throw new UnsupportedOperationException();
	}

	public void performRegistryAttributeValue(MapModel map, final String name, final String value, boolean manual) {
		throw new UnsupportedOperationException();
	}

	public void performRegistrySubtreeAttributes(final NodeModel model) {
		throw new UnsupportedOperationException();
	}

	public void performRemoveAttribute(MapModel map, final String name) {
		throw new UnsupportedOperationException();
	}

	public void performRemoveAttributeValue(MapModel map, final String name, final Object value) {
		throw new UnsupportedOperationException();
	}

	public Attribute performRemoveRow(final NodeModel node, final NodeAttributeTableModel model, final int row) {
		throw new UnsupportedOperationException();
	}

	public void performReplaceAtributeName(MapModel map, final String oldName, final String newName) {
		throw new UnsupportedOperationException();
	}

	public void performReplaceAttributeValue(MapModel map, final String name, final Object oldO, final Object newO) {
		throw new UnsupportedOperationException();
	}

	public void performSetColumnWidth(final NodeModel node, final NodeAttributeTableModel model, final int col, final Quantity<LengthUnits> width) {
		throw new UnsupportedOperationException();
	}

	public void performSetRestriction(MapModel map, final int row, final boolean restricted) {
		throw new UnsupportedOperationException();
	}

	public void performSetValueAt(final NodeModel node, final NodeAttributeTableModel model, final Object o, final int row, final int col) {
		throw new UnsupportedOperationException();
	}

	public void performSetVisibility(MapModel map, final int index, final boolean isVisible) {
		throw new UnsupportedOperationException();
	}

	private void registerTooltipProvider() {
		modeController.addToolTipProvider(ATTRIBUTE_TOOLTIP, new ITooltipProvider() {
			@Override
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
				final int rowCount = attributes.getRowCount();
				if (rowCount == 0) {
					return null;
				}
				final AttributeRegistry registry = AttributeRegistry.getRegistry(node.getMap());
				final TextController textController = TextController.getController(modeController);
				if (registry.getAttributeViewType().equals(AttributeTableLayoutModel.SHOW_ALL)
						&& ! textController.isMinimized(node)) {
					return null;
				}
				final NodeStyleController style = modeController.getExtension(NodeStyleController.class);
		        final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
		        final NodeModel attributeStyleNode = model.getStyleNodeSafe(MapStyleModel.ATTRIBUTE_STYLE);
		        final Font font = style.getFont(attributeStyleNode);
		        final StringBuilder tooltip = new StringBuilder();
				tooltip.append("<html><body><table style='border: 1px solid;");
				tooltip.append( new CssRuleBuilder().withHTMLFont(font)
						.withBackground(style.getBackgroundColor(attributeStyleNode))
						.withColor(style.getColor(attributeStyleNode))
						);
				tooltip.append(" ' width='100%' cellspacing='0' cellpadding='2' ");
				final int currentRowCount = attributes.getRowCount();
				for (int i = 0; i < currentRowCount; i++) {
					tooltip.append("<tr><td style='border: 1px solid;'>");
					tooltip.append(attributes.getValueAt(i, 0));
					tooltip.append("</td><td style='border: 1px solid;'>");
					final Object object = attributes.getValueAt(i, 1);
					final String text = getTransformedValue(node, textController, object);
					if(object instanceof URI){
						tooltip.append("<a");
						tooltip.append(" href=\"");
						tooltip.append(object);
						tooltip.append("\"");
                        tooltip.append(">");
                        tooltip.append(text);
						tooltip.append("</a>");
					}
					else{
						tooltip.append(text);
					}
					tooltip.append("</td></tr>");
				}
				tooltip.append("</table></body></html>");
				return tooltip.toString();
			}

			private String getTransformedValue(NodeModel node, final TextController textController, final Object value) {
				try {
					final String text = textController.getTransformedText(value, node, null);
					final boolean markTransformedText = TextController.isMarkTransformedTextSet();
					final String unicodeText = HtmlUtils.unicodeToHTMLUnicodeEntity(text);
					if (markTransformedText && text != value)
						return colorize(unicodeText, "green");
					else
						return unicodeText;
				}
				catch (Throwable e) {
					LogUtils.warn(e.getMessage(), e);
					return colorize(
						TextUtils.format("MainView.errorUpdateText", String.valueOf(value), e.getLocalizedMessage())
						.replace("\n", "<br>"), "red");
				}
			}

			private String colorize(final String text, String color) {
				return "<span style=\"color:" + color + ";font-style:italic;\">" + text + "</span>";
			}
		});
	}

	private void registerStateIconProvider() {
	    IconController.getController().addStateIconProvider(new IStateIconProvider() {
			@Override
			public UIIcon getStateIcon(NodeModel node) {
				NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);;
				if (attributes.getRowCount() == 0) {
					return null;
				}
				final String showAttributeIcon = MapStyle.getController(modeController).getPropertySetDefault(node.getMap(), SHOW_ICON_FOR_ATTRIBUTES);
				final boolean showIcon = Boolean.parseBoolean(showAttributeIcon);
				if(showIcon) {
					if (attributeIcon == null) {
						attributeIcon = IconStoreFactory.ICON_STORE.getUIIcon("showAttributes.png");
					}
					return attributeIcon;
				}
				else
					return null;
			}

			@Override
			public boolean mustIncludeInIconRegistry() {
				return true;
			}
		});
    }

	public boolean canEdit() {
	    return false;
    }

	@Override
	public NodeAttribute getSelectedAttribute() {
		return attributeSelection != null ? attributeSelection.getSelectedAttribute() : null;
	}
}
