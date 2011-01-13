/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.util.Set;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.LogicalStyleModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.common.styles.StyleFactory;
import org.freeplane.features.common.styles.StyleNamedObject;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.styles.MLogicalStyleController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class NodeStyleProxy extends AbstractProxy<NodeModel> implements Proxy.NodeStyle {
	NodeStyleProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public IStyle getStyle() {
		return LogicalStyleModel.getStyle(getDelegate());
	}

	public String getName() {
	    final IStyle style = getStyle();
		return style == null ? null : StyleNamedObject.toKeyString(style);
    }

	public Node getStyleNode() {
		final NodeModel styleNode = MapStyleModel.getExtension(getDelegate().getMap()).getStyleNode(getStyle());
		return new NodeProxy(styleNode, getScriptContext());
	}

	public Color getBackgroundColor() {
		return getStyleController().getBackgroundColor(getDelegate());
	}

	public String getBackgroundColorCode() {
		return ColorUtils.colorToString(getBackgroundColor());
	}

	public Proxy.Edge getEdge() {
		return new EdgeProxy(getDelegate(), getScriptContext());
	}

	public Proxy.Font getFont() {
		return new FontProxy(getDelegate(), getScriptContext());
	}

	public Color getTextColor() {
		return getStyleController().getColor(getDelegate());
	}

	@Deprecated
	public Color getNodeTextColor() {
		return getTextColor();
	}

	public String getTextColorCode() {
		return ColorUtils.colorToString(getTextColor());
	}

	private MLogicalStyleController getLogicalStyleController() {
		return (MLogicalStyleController) LogicalStyleController.getController();
	}

	private MNodeStyleController getStyleController() {
		return (MNodeStyleController) NodeStyleController.getController();
	}

	public void setStyle(final IStyle key) {
		getLogicalStyleController().setStyle(getDelegate(), key);
	}

	public void setName(String styleName) {
		if (styleName == null) {
			setStyle(null);
		}
		else {
			final MapStyleModel mapStyleModel = MapStyleModel.getExtension(getDelegate().getMap());
			// actually styles is a HashSet so lookup is fast
			final Set<IStyle> styles = mapStyleModel.getStyles();
			// search for user defined styles
			final IStyle styleString = StyleFactory.create(styleName);
			if (styles.contains(styleString)) {
				setStyle(styleString);
				return;
			}
			// search for predefined styles by key
			final IStyle styleNamedObject = StyleFactory.create(new NamedObject(styleName));
			if (styles.contains(styleNamedObject)) {
				setStyle(styleNamedObject);
				return;
			}
			// search for predefined styles by their translated name (style.toString())
			for (IStyle style : styles) {
				if (style.toString().equals(styleName)) {
					setStyle(style);
					return;
				}
			}
			throw new IllegalArgumentException("style '" + styleName + "' not found");
		}
	}

	public void setBackgroundColor(final Color color) {
		getStyleController().setBackgroundColor(getDelegate(), color);
	}

	public void setBackgroundColorCode(final String rgbString) {
		setBackgroundColor(ColorUtils.stringToColor(rgbString));
	}

	public void setTextColor(final Color color) {
		getStyleController().setColor(getDelegate(), color);
	}

	@Deprecated
	public void setNodeTextColor(final Color color) {
		setTextColor(color);
	}

	public void setTextColorCode(final String rgbString) {
		setTextColor(ColorUtils.stringToColor(rgbString));
	}
}
