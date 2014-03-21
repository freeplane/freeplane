/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.awt.Color;
import java.util.Collection;
import java.util.Set;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleFactory;
import org.freeplane.features.styles.StyleNamedObject;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
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

    public boolean isFloating() {
        return hasStyle(getDelegate(), StyleNamedObject.toKeyString(MapStyleModel.FLOATING_STYLE));
    }

    public int getMinNodeWidth() {
        return getStyleController().getMinWidth(getDelegate());
    }

    public int getMaxNodeWidth() {
        return getStyleController().getMaxWidth(getDelegate());
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

    public void setFloating(boolean floating) {
        if (floating) {
            setStyle(MapStyleModel.FLOATING_STYLE);
        }
        else if (MapStyleModel.FLOATING_STYLE.equals(getStyle())) {
            setStyle(null);
        }
    }

	public static boolean hasStyle(NodeModel nodeModel, String styleName) {
		final Collection<IStyle> styles = LogicalStyleController.getController().getStyles(nodeModel);
		for (IStyle style : styles) {
			if (StyleNamedObject.toKeyString(style).equals(styleName))
				return true;
		}
		return false;
    }

    public void setMinNodeWidth(int width) {
        getStyleController().setMinNodeWidth(getDelegate(), width);
    }

    public void setMaxNodeWidth(int width) {
        getStyleController().setMaxNodeWidth(getDelegate(), width);
    }

    public boolean isNumberingEnabled() {
        return NodeStyleModel.getNodeNumbering(getDelegate());
    }

    public void setNumberingEnabled(boolean enabled) {
        getStyleController().setNodeNumbering(getDelegate(), enabled);
    }
}
