/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapnode.pattern;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.freeplane.core.icon.IconController;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.BooleanProperty;
import org.freeplane.core.resources.ui.ColorProperty;
import org.freeplane.core.resources.ui.ComboProperty;
import org.freeplane.core.resources.ui.FontProperty;
import org.freeplane.core.resources.ui.IPropertyControl;
import org.freeplane.core.resources.ui.IconProperty;
import org.freeplane.core.resources.ui.NextLineProperty;
import org.freeplane.core.resources.ui.PropertyBean;
import org.freeplane.core.resources.ui.SeparatorProperty;
import org.freeplane.core.resources.ui.StringProperty;
import org.freeplane.core.resources.ui.ThreeCheckBoxProperty;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.mindmapmode.icon.MIconController;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author foltin
 */
public class StylePatternPanel extends JPanel implements PropertyChangeListener {
	final private class EdgeWidthBackTransformer implements IValueTransformator {
		public String transform(final String value) {
			return transformStringToWidth(value);
		}
	}

	final private class EdgeWidthTransformer implements IValueTransformator {
		public String transform(final String value) {
			return transformEdgeWidth(value);
		}
	}

	final private class IdentityTransformer implements IValueTransformator {
		public String transform(final String value) {
			return value;
		}
	}

	private interface IValueTransformator {
		String transform(String value);
	}

	public static final class StylePatternPanelType {
		public static StylePatternPanelType WITH_NAME_AND_CHILDS = new StylePatternPanelType();
		public static StylePatternPanelType WITHOUT_NAME_AND_CHILDS = new StylePatternPanelType();

		private StylePatternPanelType() {
		}
	}

	private static final String CHILD_PATTERN = "childpattern";
	private static final String CLEAR_ALL_SETTERS = "clear_all_setters";
	private static final String CLOUD = "cloud";
	private static final String CLOUD_COLOR = "cloudcolor";
	private static final String EDGE_COLOR = "edgecolor";
	private static final String EDGE_STYLE = "edgestyle";
	private static final String[] EDGE_STYLES = initializeEdgeStyles();
	private static final String EDGE_WIDTH = "edgewidth";
	private static final String[] EDGE_WIDTHS = new String[] { "EdgeWidth_parent", "EdgeWidth_thin", "EdgeWidth_1",
	        "EdgeWidth_2", "EdgeWidth_4", "EdgeWidth_8" };
	private static final String ICON = "icon";
	private static final String NODE_BACKGROUND_COLOR = "nodebackgroundcolor";
	private static final String NODE_COLOR = "nodecolor";
	private static final String NODE_FONT_BOLD = "nodefontbold";
	private static final String NODE_FONT_ITALIC = "nodefontitalic";
	private static final String NODE_FONT_NAME = "nodefontname";
	private static final String NODE_FONT_SIZE = "nodefontsize";
	private static final String NODE_NAME = "patternname";
	private static final String NODE_STYLE = "nodeshape";
	private static final String NODE_TEXT = "nodetext";
	private static final String SCRIPT = "script";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SET_CHILD_PATTERN = StylePatternPanel.SET_RESOURCE;
	private static final String SET_CLOUD = StylePatternPanel.SET_RESOURCE;
	private static final String SET_CLOUD_COLOR = StylePatternPanel.SET_RESOURCE;
	private static final String SET_EDGE_COLOR = StylePatternPanel.SET_RESOURCE;
	private static final String SET_EDGE_STYLE = StylePatternPanel.SET_RESOURCE;
	private static final String SET_EDGE_WIDTH = StylePatternPanel.SET_RESOURCE;
	private static final String SET_ICON = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_BACKGROUND_COLOR = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_COLOR = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_BOLD = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_ITALIC = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_NAME = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_SIZE = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_STYLE = StylePatternPanel.SET_RESOURCE;
	private static final String SET_NODE_TEXT = StylePatternPanel.SET_RESOURCE;
	private static final String SET_RESOURCE = "set_property_text";
	private static final String SET_SCRIPT = "setscript";
	private ComboProperty mChildPattern;
	private ThreeCheckBoxProperty mClearSetters;
	private BooleanProperty mCloud;
	private ColorProperty mCloudColor;
	private List<IPropertyControl> mControls;
	private ColorProperty mEdgeColor;
	private ComboProperty mEdgeStyle;
	private ComboProperty mEdgeWidth;
	private IconProperty mIcon;
	private List<MindIcon> mIconInformationVector;
	private final ModeController mMindMapController;
	private StringProperty mName;
	private ColorProperty mNodeBackgroundColor;
	private ColorProperty mNodeColor;
	private BooleanProperty mNodeFontBold;
	private BooleanProperty mNodeFontItalic;
	private FontProperty mNodeFontName;
	private ComboProperty mNodeFontSize;
	private ComboProperty mNodeStyle;
	private StringProperty mNodeText;
	private List<Pattern> mPatternList;
	/**
	 * Denotes pairs property -> ThreeCheckBoxProperty such that the boolean
	 * property can be set, when the format property is changed.
	 */
	private final Map<ThreeCheckBoxProperty, PropertyBean> mPropertyChangePropagation = new HashMap<ThreeCheckBoxProperty, PropertyBean>();
	private ScriptEditorProperty mScriptPattern;
	private ThreeCheckBoxProperty mSetChildPattern;
	private ThreeCheckBoxProperty mSetCloud;
	private ThreeCheckBoxProperty mSetCloudColor;
	private ThreeCheckBoxProperty mSetEdgeColor;
	private ThreeCheckBoxProperty mSetEdgeStyle;
	private ThreeCheckBoxProperty mSetEdgeWidth;
	private ThreeCheckBoxProperty mSetIcon;
	private ThreeCheckBoxProperty mSetNodeBackgroundColor;
	private ThreeCheckBoxProperty mSetNodeColor;
	private ThreeCheckBoxProperty mSetNodeFontBold;
	private ThreeCheckBoxProperty mSetNodeFontItalic;
	private ThreeCheckBoxProperty mSetNodeFontName;
	private ThreeCheckBoxProperty mSetNodeFontSize;
	private ThreeCheckBoxProperty mSetNodeStyle;
	private ThreeCheckBoxProperty mSetNodeText;
	private ThreeCheckBoxProperty mSetScriptPattern;
	final private StylePatternPanelType mType;
	final private String[] sizes = new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24",
	        "30", "36", "48", "72" };

	/**
	 * @throws HeadlessException
	 */
	public StylePatternPanel(final ModeController pMindMapController, final StylePatternPanelType pType)
	        throws HeadlessException {
		super();
		mMindMapController = pMindMapController;
		mType = pType;
	}

	private static String[] initializeEdgeStyles() {
	    final EdgeStyle[] enumConstants = EdgeStyle.class.getEnumConstants();
	    final String[] strings = new String[enumConstants.length];
	    for(int i = 0; i < enumConstants.length; i++){
	    	strings[i] = enumConstants[i].toString();
	    }
	    return strings;
    }

	public void addListeners() {
		for (final IPropertyControl control : mControls) {
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				bean.addPropertyChangeListener(this);
			}
		}
		mClearSetters.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent pEvt) {
				for (final ThreeCheckBoxProperty booleanProp : mPropertyChangePropagation.keySet()) {
					booleanProp.setValue(mClearSetters.getValue());
				}
			}
		});
	}

	private List<IPropertyControl> getControls() {
		final List<IPropertyControl> controls = new ArrayList<IPropertyControl>();
		controls.add(new SeparatorProperty("OptionPanel.separator.General"));
		mClearSetters = new ThreeCheckBoxProperty(StylePatternPanel.CLEAR_ALL_SETTERS);
		mClearSetters.setValue(ThreeCheckBoxProperty.TRUE_VALUE);
		controls.add(mClearSetters);
		if (StylePatternPanelType.WITH_NAME_AND_CHILDS.equals(mType)) {
			mName = new StringProperty(StylePatternPanel.NODE_NAME);
			controls.add(mName);
			mSetChildPattern = new ThreeCheckBoxProperty(StylePatternPanel.SET_CHILD_PATTERN);
			controls.add(mSetChildPattern);
			final List<String> childNames = new ArrayList<String>();
			mChildPattern = new ComboProperty(StylePatternPanel.CHILD_PATTERN, childNames, childNames);
			controls.add(mChildPattern);
		}
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeColors"));
		mSetNodeColor = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_COLOR);
		controls.add(mSetNodeColor);
		mNodeColor = new ColorProperty(StylePatternPanel.NODE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(NodeStyleController.RESOURCES_NODE_TEXT_COLOR));
		controls.add(mNodeColor);
		mSetNodeBackgroundColor = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_BACKGROUND_COLOR);
		controls.add(mSetNodeBackgroundColor);
		mNodeBackgroundColor = new ColorProperty(StylePatternPanel.NODE_BACKGROUND_COLOR, ResourceController
		    .getResourceController().getDefaultProperty(NODE_BACKGROUND_COLOR));
		controls.add(mNodeBackgroundColor);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeStyles"));
		mSetNodeStyle = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_STYLE);
		controls.add(mSetNodeStyle);
		mNodeStyle = new ComboProperty(StylePatternPanel.NODE_STYLE, new String[] { "fork", "bubble", "as_parent",
		        "combined" });
		controls.add(mNodeStyle);
		mIconInformationVector = new ArrayList<MindIcon>();
		final ModeController controller = mMindMapController;
		final Collection<MindIcon> mindIcons = ((MIconController) IconController.getController(controller)).getMindIcons();
		mIconInformationVector.addAll(mindIcons);
		mSetIcon = new ThreeCheckBoxProperty(StylePatternPanel.SET_ICON);
		controls.add(mSetIcon);
		mIcon = new IconProperty(StylePatternPanel.ICON, mIconInformationVector);
		controls.add(mIcon);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeFont"));
		mSetNodeFontName = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_FONT_NAME);
		controls.add(mSetNodeFontName);
		mNodeFontName = new FontProperty(StylePatternPanel.NODE_FONT_NAME);
		controls.add(mNodeFontName);
		mSetNodeFontSize = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_FONT_SIZE);
		controls.add(mSetNodeFontSize);

		final List<String> sizesVector = new ArrayList<String>(Arrays.asList(sizes));
		mNodeFontSize = new ComboProperty(StylePatternPanel.NODE_FONT_SIZE, sizesVector, sizesVector);
		
		controls.add(mNodeFontSize);
		mSetNodeFontBold = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_FONT_BOLD);
		controls.add(mSetNodeFontBold);
		mNodeFontBold = new BooleanProperty(StylePatternPanel.NODE_FONT_BOLD);
		controls.add(mNodeFontBold);
		mSetNodeFontItalic = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_FONT_ITALIC);
		controls.add(mSetNodeFontItalic);
		mNodeFontItalic = new BooleanProperty(StylePatternPanel.NODE_FONT_ITALIC);
		controls.add(mNodeFontItalic);
		/* **** */
		mSetNodeText = new ThreeCheckBoxProperty(StylePatternPanel.SET_NODE_TEXT);
		controls.add(mSetNodeText);
		mNodeText = new StringProperty(StylePatternPanel.NODE_TEXT);
		controls.add(mNodeText);
		/* **** */
		controls.add(new SeparatorProperty("OptionPanel.separator.EdgeControls"));
		mSetEdgeWidth = new ThreeCheckBoxProperty(StylePatternPanel.SET_EDGE_WIDTH);
		controls.add(mSetEdgeWidth);
		mEdgeWidth = new ComboProperty(StylePatternPanel.EDGE_WIDTH, EDGE_WIDTHS);
		controls.add(mEdgeWidth);
		/* **** */
		mSetEdgeStyle = new ThreeCheckBoxProperty(StylePatternPanel.SET_EDGE_STYLE);
		controls.add(mSetEdgeStyle);
		mEdgeStyle = new ComboProperty(StylePatternPanel.EDGE_STYLE, EDGE_STYLES);
		controls.add(mEdgeStyle);
		/* **** */
		mSetEdgeColor = new ThreeCheckBoxProperty(StylePatternPanel.SET_EDGE_COLOR);
		controls.add(mSetEdgeColor);
		mEdgeColor = new ColorProperty(StylePatternPanel.EDGE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(EdgeController.RESOURCES_EDGE_COLOR));
		controls.add(mEdgeColor);
		/* **** */
		controls.add(new SeparatorProperty("OptionPanel.separator.CloudControls"));
		mSetCloud = new ThreeCheckBoxProperty(StylePatternPanel.SET_CLOUD);
		controls.add(mSetCloud);
		mCloud = new BooleanProperty(StylePatternPanel.CLOUD);
		controls.add(mCloud);
		/* **** */
		mSetCloudColor = new ThreeCheckBoxProperty(StylePatternPanel.SET_CLOUD_COLOR);
		controls.add(mSetCloudColor);
		mCloudColor = new ColorProperty(StylePatternPanel.CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		controls.add(mCloudColor);
		/* **** */
		controls.add(new SeparatorProperty("OptionPanel.separator.ScriptingControl"));
		mSetScriptPattern = new ThreeCheckBoxProperty(StylePatternPanel.SET_SCRIPT);
		controls.add(mSetScriptPattern);
		mScriptPattern = new ScriptEditorProperty(StylePatternPanel.SCRIPT, mMindMapController);
		controls.add(mScriptPattern);
		mPropertyChangePropagation.put(mSetNodeColor, mNodeColor);
		mPropertyChangePropagation.put(mSetNodeBackgroundColor, mNodeBackgroundColor);
		mPropertyChangePropagation.put(mSetNodeStyle, mNodeStyle);
		mPropertyChangePropagation.put(mSetNodeFontName, mNodeFontName);
		mPropertyChangePropagation.put(mSetNodeFontSize, mNodeFontSize);
		mPropertyChangePropagation.put(mSetNodeFontBold, mNodeFontBold);
		mPropertyChangePropagation.put(mSetNodeFontItalic, mNodeFontItalic);
		mPropertyChangePropagation.put(mSetNodeText, mNodeText);
		mPropertyChangePropagation.put(mSetCloud, mCloud);
		mPropertyChangePropagation.put(mSetCloudColor, mCloudColor);
		mPropertyChangePropagation.put(mSetEdgeColor, mEdgeColor);
		mPropertyChangePropagation.put(mSetEdgeStyle, mEdgeStyle);
		mPropertyChangePropagation.put(mSetEdgeWidth, mEdgeWidth);
		mPropertyChangePropagation.put(mSetIcon, mIcon);
		mPropertyChangePropagation.put(mSetScriptPattern, mScriptPattern);
		if (StylePatternPanelType.WITH_NAME_AND_CHILDS.equals(mType)) {
			mPropertyChangePropagation.put(mSetChildPattern, mChildPattern);
		}
		return controls;
	}

	private HashMap<String, Integer> getEdgeWidthTransformation() {
		final HashMap<String, Integer> transformator = new HashMap<String, Integer>(StylePatternPanel.EDGE_WIDTHS.length);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[0], EdgeModel.WIDTH_PARENT);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[1], EdgeModel.WIDTH_THIN);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[2], 1);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[3], 2);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[4], 4);
		transformator.put(StylePatternPanel.EDGE_WIDTHS[5], 8);
		return transformator;
	}

	private List<String> getPatternNames() {
		final List<String> childNames = new ArrayList<String>();
		for (final Pattern pattern : mPatternList) {
			childNames.add(pattern.getName());
		}
		return childNames;
	}

	private PatternProperty getPatternResult(final PatternProperty baseProperty,
	                                         final ThreeCheckBoxProperty threeCheckBoxProperty,
	                                         final PropertyBean property) {
		final IValueTransformator transformer = new IdentityTransformer();
		return getPatternResult(baseProperty, threeCheckBoxProperty, property, transformer);
	}

	/**
	 */
	private PatternProperty getPatternResult(final PatternProperty baseProperty,
	                                         final ThreeCheckBoxProperty threeCheckBoxProperty,
	                                         final PropertyBean property, final IValueTransformator transformer) {
		final String checkboxResult = threeCheckBoxProperty.getValue();
		if (checkboxResult == null) {
			return null;
		}
		if (checkboxResult.equals(ThreeCheckBoxProperty.DON_T_TOUCH_VALUE)) {
			return null;
		}
		if (checkboxResult.equals(ThreeCheckBoxProperty.FALSE_VALUE)) {
			return baseProperty;
		}
		baseProperty.setValue(transformer.transform(property.getValue()));
		return baseProperty;
	}

	public Pattern getResultPattern() {
		final Pattern pattern = new Pattern();
		return getResultPattern(pattern);
	}

	public Pattern getResultPattern(final Pattern pattern) {
		pattern.setPatternNodeColor(getPatternResult(new PatternProperty(), mSetNodeColor, mNodeColor));
		pattern.setPatternNodeBackgroundColor(getPatternResult(new PatternProperty(), mSetNodeBackgroundColor,
		    mNodeBackgroundColor));
		pattern.setPatternNodeStyle(getPatternResult(new PatternProperty(), mSetNodeStyle, mNodeStyle));
		pattern.setPatternNodeText(getPatternResult(new PatternProperty(), mSetNodeText, mNodeText));
		/* edges */
		pattern.setPatternEdgeColor(getPatternResult(new PatternProperty(), mSetEdgeColor, mEdgeColor));
		pattern.setPatternEdgeStyle(getPatternResult(new PatternProperty(), mSetEdgeStyle, mEdgeStyle));
		pattern.setPatternEdgeWidth(getPatternResult(new PatternProperty(), mSetEdgeWidth, mEdgeWidth,
		    new EdgeWidthBackTransformer()));
		/* clouds */
		pattern.setPatternCloud(getPatternResult(new PatternProperty(), mSetCloud, mCloud));
		pattern.setPatternCloudColor(getPatternResult(new PatternProperty(), mSetCloudColor, mCloudColor));
		/* font */
		pattern.setPatternNodeFontName(getPatternResult(new PatternProperty(), mSetNodeFontName, mNodeFontName));
		pattern.setPatternNodeFontSize(getPatternResult(new PatternProperty(), mSetNodeFontSize, mNodeFontSize));
		pattern.setPatternNodeFontBold(getPatternResult(new PatternProperty(), mSetNodeFontBold, mNodeFontBold));
		pattern.setPatternNodeFontItalic(getPatternResult(new PatternProperty(), mSetNodeFontItalic, mNodeFontItalic));
		pattern.setPatternIcon(getPatternResult(new PatternProperty(), mSetIcon, mIcon));
		pattern.setPatternScript(getPatternResult(new PatternProperty(), mSetScriptPattern, mScriptPattern));
		if (StylePatternPanelType.WITH_NAME_AND_CHILDS.equals(mType)) {
			pattern.setName(mName.getValue());
			pattern.setPatternChild(getPatternResult(new PatternProperty(), mSetChildPattern, mChildPattern));
		}
		return pattern;
	}

	/**
	 * Creates all controls and adds them to the frame.
	 */
	public void init() {
		final CardLayout cardLayout = new CardLayout();
		final JPanel rightStack = new JPanel(cardLayout);
		final String form = "right:max(40dlu;p), 4dlu, 20dlu, 7dlu,right:max(40dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder rightBuilder = new DefaultFormBuilder(rightLayout);
		rightBuilder.setDefaultDialogBorder();
		mControls = getControls();
		for (final IPropertyControl control : mControls) {
			control.layout(rightBuilder);
		}
		rightStack.add(rightBuilder.getPanel(), "testTab");
		add(rightStack, BorderLayout.CENTER);
	}

	/**
	 * Used to enable/disable the attribute controls, if the check boxes are
	 * changed.
	 */
	public void propertyChange(final PropertyChangeEvent pEvt) {
		if (mPropertyChangePropagation.containsKey(pEvt.getSource())) {
			final ThreeCheckBoxProperty booleanProp = (ThreeCheckBoxProperty) pEvt.getSource();
			final IPropertyControl bean = (IPropertyControl) mPropertyChangePropagation.get(booleanProp);
			bean.setEnabled(ThreeCheckBoxProperty.TRUE_VALUE.equals(booleanProp.getValue()));
			return;
		}
	}

	public void setPattern(final Pattern pattern) {
		setPatternControls(pattern.getPatternNodeColor(), mSetNodeColor, mNodeColor, ResourceController
		    .getResourceController().getDefaultProperty(NodeStyleController.RESOURCES_NODE_TEXT_COLOR));
		setPatternControls(pattern.getPatternNodeBackgroundColor(), mSetNodeBackgroundColor, mNodeBackgroundColor,
		    ResourceController.getResourceController().getDefaultProperty(NODE_BACKGROUND_COLOR));
		setPatternControls(pattern.getPatternNodeStyle(), mSetNodeStyle, mNodeStyle, NodeStyleModel.SHAPE_AS_PARENT);
		setPatternControls(pattern.getPatternNodeText(), mSetNodeText, mNodeText, "");
		setPatternControls(pattern.getPatternEdgeColor(), mSetEdgeColor, mEdgeColor, ResourceController
		    .getResourceController().getDefaultProperty(EdgeController.RESOURCES_EDGE_COLOR));
		setPatternControls(pattern.getPatternEdgeStyle(), mSetEdgeStyle, mEdgeStyle, StylePatternPanel.EDGE_STYLES[0]);
		setPatternControls(pattern.getPatternEdgeWidth(), mSetEdgeWidth, mEdgeWidth, StylePatternPanel.EDGE_WIDTHS[0],
		    new EdgeWidthTransformer());
		setPatternControls(pattern.getPatternCloud(), mSetCloud, mCloud, Boolean.TRUE.toString());
		setPatternControls(pattern.getPatternCloudColor(), mSetCloudColor, mCloudColor, ResourceController
		    .getResourceController().getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		setPatternControls(pattern.getPatternNodeFontName(), mSetNodeFontName, mNodeFontName, ResourceController
		    .getResourceController().getDefaultFontFamilyName());
		setPatternControls(pattern.getPatternNodeFontSize(), mSetNodeFontSize, mNodeFontSize, sizes[0]);
		setPatternControls(pattern.getPatternNodeFontBold(), mSetNodeFontBold, mNodeFontBold, Boolean.TRUE.toString());
		setPatternControls(pattern.getPatternNodeFontItalic(), mSetNodeFontItalic, mNodeFontItalic, Boolean.TRUE
		    .toString());
		final MindIcon firstInfo = mIconInformationVector.get(0);
		setPatternControls(pattern.getPatternIcon(), mSetIcon, mIcon, firstInfo.getName());
		setPatternControls(pattern.getPatternScript(), mSetScriptPattern, mScriptPattern, "");
		if (StylePatternPanelType.WITH_NAME_AND_CHILDS.equals(mType)) {
			mName.setValue(pattern.getName());
			setPatternControls(pattern.getPatternChild(), mSetChildPattern, mChildPattern,
			    (mPatternList.size() > 0) ? ((Pattern) mPatternList.get(0)).getName() : null);
		}
		for (final Iterator iter = mPropertyChangePropagation.keySet().iterator(); iter.hasNext();) {
			final ThreeCheckBoxProperty prop = (ThreeCheckBoxProperty) iter.next();
			propertyChange(new PropertyChangeEvent(prop, prop.getName(), null, prop.getValue()));
		}
	}

	private void setPatternControls(final PatternProperty patternProperty, final PropertyBean threeCheckBoxProperty,
	                                final PropertyBean property, final String defaultValue) {
		setPatternControls(patternProperty, threeCheckBoxProperty, property, defaultValue, new IdentityTransformer());
	}

	/**
	 */
	private void setPatternControls(final PatternProperty patternProperty, final PropertyBean threeCheckBoxProperty,
	                                final PropertyBean property, final String defaultValue,
	                                final IValueTransformator transformer) {
		if (patternProperty == null) {
			property.setValue(defaultValue);
			threeCheckBoxProperty.setValue(ThreeCheckBoxProperty.DON_T_TOUCH_VALUE);
			return;
		}
		if (patternProperty.getValue() == null) {
			property.setValue(defaultValue);
			threeCheckBoxProperty.setValue(ThreeCheckBoxProperty.FALSE_VALUE);
			return;
		}
		property.setValue(transformer.transform(patternProperty.getValue()));
		threeCheckBoxProperty.setValue(ThreeCheckBoxProperty.TRUE_VALUE);
	}

	/**
	 * For the child pattern box, the list is set here.
	 */
	public void setPatternList(final List<Pattern> patternList) {
		mPatternList = patternList;
		final List<String> childNames = getPatternNames();
		mChildPattern.updateComboBoxEntries(childNames, childNames);
	}

	private String transformEdgeWidth(final String pEdgeWidth) {
		if (pEdgeWidth == null) {
			return null;
		}
		final int edgeWidth = ApplyPatternAction.edgeWidthStringToInt(pEdgeWidth);
		final HashMap<String, Integer> transformator = getEdgeWidthTransformation();
		for(Entry<String, Integer> transformatorEntry : transformator.entrySet()) {
			final Integer width = transformatorEntry.getValue();
			if (edgeWidth == width.intValue()) {
				return transformatorEntry.getKey();
			}
		}
		return null;
	}

	private String transformStringToWidth(final String value) {
		final HashMap transformator = getEdgeWidthTransformation();
		final int intWidth = ((Integer) transformator.get(value)).intValue();
		return ApplyPatternAction.edgeWidthIntToString(intWidth);
	}
}
