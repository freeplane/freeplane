/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.styles;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.styles.ConditionalStyleModel.Item;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 9, 2009
 */
@NodeHookDescriptor(hookName = "MapStyle")
public class MapStyle extends PersistentNodeHook implements IExtension, IMapLifeCycleListener {
	private static final String NODE_CONDITIONAL_STYLES = "NodeConditionalStyles";
	public static final String RESOURCES_BACKGROUND_COLOR = "standardbackgroundcolor";
	public static final String RESOURCES_BACKGROUND_IMAGE = "backgroundImageURI";
	public static final String MAP_STYLES = "MAP_STYLES";
	public static final String FIT_TO_VIEWPORT = "fit_to_viewport";

	public static void install(boolean persistent){
		new MapStyle(persistent);
	}

	protected MapStyle( final boolean persistent) {
		super();
		ModeController modeController = Controller.getCurrentModeController();
		if (persistent) {
			final MapController mapController = modeController.getMapController();
			mapController.getWriteManager().addExtensionElementWriter(getExtensionClass(),
				new XmlWriter());
			mapController.getReadManager().addElementHandler("conditional_styles", new IElementDOMHandler() {
				@Override
				public Object createElement(Object parent, String tag, XMLElement attributes) {
					return parent;
				}

				@Override
				public void endElement(Object parent, String tag, Object element, XMLElement dom) {
					final NodeModel node = (NodeModel) parent;
					final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node);
					if(mapStyleModel != null)
						loadConditionalStyles(mapStyleModel.getConditionalStyleModel(), dom);
				}
				});

				mapController.getWriteManager().addExtensionElementWriter(ConditionalStyleModel.class,
					new IExtensionElementWriter() {
					@Override
					public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
						final ConditionalStyleModel conditionalStyleModel = (ConditionalStyleModel) extension;
						if (conditionalStyleModel.getStyleCount() == 0)
							return;
						final XMLElement hook = new XMLElement("hook");
						hook.setAttribute("NAME", NODE_CONDITIONAL_STYLES);
						saveConditionalStyles(conditionalStyleModel, hook, false);
						writer.addElement(null, hook);
					}
				});

				mapController.getReadManager().addElementHandler("hook", new IElementDOMHandler() {
					@Override
					public Object createElement(Object parent, String tag, XMLElement attributes) {
						if (attributes == null
								|| !NODE_CONDITIONAL_STYLES.equals(attributes.getAttribute("NAME", null))) {
							return null;
					}
					final ConditionalStyleModel conditionalStyleModel = new ConditionalStyleModel();
					((NodeModel)parent).addExtension(conditionalStyleModel);
					return conditionalStyleModel;
				}

				@Override
				public void endElement(Object parent, String tag, Object element, XMLElement dom) {
					loadConditionalStyles((ConditionalStyleModel) element, dom);
				}
			});
			mapController.getReadManager().addElementHandler("map_styles",  new IElementContentHandler() {
				@Override
				public Object createElement(Object parent, String tag, XMLElement attributes) {
	                return parent;
                }
				@Override
				public void endElement(final Object parent, final String tag, final Object userObject,
				                       final XMLElement attributes, final String content) {
					// bugfix
					if(isContentEmpty(content))
						return;
					final NodeModel node = (NodeModel) userObject;
					final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node);
					if (mapStyleModel == null) {
						return;
					}
					final MapModel map = node.getMap();
					mapStyleModel.createStyleMap(map, content);
					map.getIconRegistry().addIcons(mapStyleModel.getStyleMap());
				}
				private boolean isContentEmpty(final String content) {
					return content.indexOf('<') == -1;
				}

                @Override
                public boolean containsXml(XMLElement element) {
                    return true;
                }

			}
			);

		}
		final MapController mapController = modeController.getMapController();
		mapController.addMapLifeCycleListener(this);
	}

	protected class XmlWriter implements IExtensionElementWriter {
		@Override
		public void writeContent(final ITreeWriter writer, final Object object, final IExtension extension)
		        throws IOException {
			final MapStyleModel mapStyleModel = (MapStyleModel) extension;
			final MapModel styleMap = mapStyleModel.getStyleMap();
			if (styleMap == null) {
				return;
			}
			final MapWriter mapWriter = Controller.getCurrentModeController().getMapController().getMapWriter();
			final StringWriter sw = new StringWriter();
			final String el = System.getProperty("line.separator");
			sw.append(el);
			sw.append("<map_styles>");
			sw.append(el);
			final NodeModel rootNode = styleMap.getRootNode();
			final boolean forceFormatting = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
			try {
				mapWriter.writeNodeAsXml(sw, rootNode, Mode.STYLE, true, true, forceFormatting);
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			sw.append("</map_styles>");
			sw.append(el);
			final XMLElement element = new XMLElement("hook");
			saveExtension(extension, element);
			writer.addElement(sw.toString(), element);
		}
	}

	@Override
	protected XmlWriter createXmlWriter() {
		return null;
	}

	protected class MyXmlReader extends XmlReader{
		@Override
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (null == super.createElement(parent, tag, attributes)){
				return null;
			}
			super.endElement(parent, tag, parent, attributes);
			return parent;
		}

		@Override
        public void endElement(Object parent, String tag, Object userObject, XMLElement xml) {
			// do nothing for not root nodes
			final XMLElement parentNodeElement = xml.getParent().getParent();
			if (parentNodeElement == null || !parentNodeElement.getName().equals("map")) {
				return;
			}
			NodeModel node = (NodeModel) userObject;
			loadMapStyleProperties(MapStyleModel.getExtension(node), xml);
       }

		private void loadMapStyleProperties(MapStyleModel model, XMLElement xml) {
			final Vector<XMLElement> propertyXml = xml.getChildrenNamed("properties");
			if(propertyXml != null && propertyXml.size() >= 1){
				final Map<String, String> properties = model.getProperties();
				final Properties attributes = propertyXml.get(0).getAttributes();
				for(Entry<Object, Object> attribute:attributes.entrySet()){
					properties.put(attribute.getKey().toString(), attribute.getValue().toString());
				}
			}
        }

	}

	@Override
	protected IElementHandler createXmlReader() {
		return new MyXmlReader();
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final MapStyleModel model = new MapStyleModel();
		final String colorString = element.getAttribute("background", null);
		final String alphaString = element.getAttribute("background_alpha", null);
		Color bgColor;
		if (colorString != null) {
			bgColor = ColorUtils.stringToColor(colorString);
		}
		else {
			bgColor = null;
		}
		if (alphaString != null) {
			bgColor = ColorUtils.makeNonTransparent(ColorUtils.alphaToColor(alphaString, bgColor));
		}
		model.setBackgroundColor(bgColor);
		final String zoomString = element.getAttribute("zoom", null);
		if (zoomString != null) {
			final float zoom = Float.valueOf(zoomString);
			model.setZoom(zoom);
		}
		final String layoutString = element.getAttribute("layout", null);
		try {
			if (layoutString != null) {
				final MapViewLayout layout = MapViewLayout.valueOf(layoutString);
				model.setMapViewLayout(layout);
			}
		}
		catch (final Exception e) {
		}
		return model;
	}

	private void loadConditionalStyles(ConditionalStyleModel conditionalStyleModel, XMLElement conditionalStylesRoot) {
		final ConditionFactory conditionFactory = FilterController.getCurrentFilterController().getConditionFactory();
		final Vector<XMLElement> styleElements = conditionalStylesRoot.getChildrenNamed("conditional_style");
		for(XMLElement styleElement : styleElements){
			final boolean isActive = Boolean.valueOf(styleElement.getAttribute("ACTIVE", "false"));
			final boolean isLast = Boolean.valueOf(styleElement.getAttribute("LAST", "false"));
			String styleText = styleElement.getAttribute("LOCALIZED_STYLE_REF", null);
			final IStyle style;
			if(styleText != null){
				style = StyleFactory.create(TranslatedObject.format(styleText));
			}
			else {
				style = StyleFactory.create(styleElement.getAttribute("STYLE_REF", null));
			}
			final ASelectableCondition condition;
			if(styleElement.getChildrenCount() == 1){
				final XMLElement conditionElement = styleElement.getChildAtIndex(0);
				try {
	                condition = conditionFactory.loadCondition(conditionElement);
                }
                catch (Exception e) {
	                e.printStackTrace();
	                continue;
                }
			}
			else{
				condition = null;
			}
			conditionalStyleModel.addCondition(isActive, condition, style, isLast);
		}
    }
	private void saveConditionalStyles(ConditionalStyleModel conditionalStyleModel, XMLElement parent, boolean createRoot) {
		final int styleCount = conditionalStyleModel.getStyleCount();
		if(styleCount == 0){
			return;
		}
		final XMLElement conditionalStylesRoot;
		if(createRoot){
			conditionalStylesRoot = parent.createElement("conditional_styles");
		parent.addChild(conditionalStylesRoot);
		}
		else
			conditionalStylesRoot = parent;
		for(final Item item : conditionalStyleModel){
			item.toXml(conditionalStylesRoot);
		}

    }

	public Color getBackground(final MapModel map) {
		MapStyleModel styleModel = map.getExtension(MapStyleModel.class);
		if(styleModel == null)
		    styleModel = map.getRootNode().getExtension(MapStyleModel.class);
		final Color backgroundColor = styleModel != null ? styleModel.getBackgroundColor() : null;
		if (backgroundColor != null) {
			return backgroundColor;
		}
		final String stdcolor = ResourceController.getResourceController().getProperty(
		    MapStyle.RESOURCES_BACKGROUND_COLOR);
		final Color standardMapBackgroundColor = ColorUtils.stringToColor(stdcolor);
		return standardMapBackgroundColor;
	}

	public URI getBackgroundImage(MapModel map) {
	    MapStyleModel styleModel = map.getRootNode().getExtension(MapStyleModel.class);
	    String uriString = styleModel.getProperty(MapStyle.RESOURCES_BACKGROUND_IMAGE);
	    if(uriString != null) {
	        try {
	            return UrlManager.getAbsoluteUri(map, new URI(uriString));
	        }
	        catch (final URISyntaxException e) {
	            LogUtils.severe(e);
	        }
	        catch (final MalformedURLException e) {
	            LogUtils.severe(e);
	        }
	    }
	    return null;
	}

	@Override
	protected Class<MapStyleModel> getExtensionClass() {
	    return MapStyleModel.class;
	}

	@Override
	public void onCreate(final MapModel map) {
	    final NodeModel rootNode = map.getRootNode();
	    final MapStyleModel mapStyleModel = MapStyleModel.getExtension(rootNode);
	    if (mapStyleModel.getStyleMap() != null) {
	        copyMapStylesNoUndoNoRefresh(map);
	    }
	    else {
	        createDefaultStyleMap(map);
	    }
	}

	private void createDefaultStyleMap(final MapModel map) {
		UrlManager loader = UrlManager.getController();
		final File file = loader.defaultTemplateFile();
		if (file != null) {
			try {
				MapModel styleMapContainer = new MapModel(map.getNodeDuplicator());
				loader.load(Compat.fileToUrl(file), styleMapContainer);
				if (null != MapStyleModel.getExtension(styleMapContainer)){
				    new StyleExchange(styleMapContainer, map).moveStyle(false);
					return;
				}
			}
			catch (Exception e) {
				LogUtils.warn(e);
				UITools.errorMessage(TextUtils.format("error_in_template", file));
			}
		};
		MapModel styleMapContainer = new MapModel(map.getNodeDuplicator());
		try {
			loader.load(ResourceController.getResourceController().getResource("/styles/viewer_standard.mm"), styleMapContainer);
			new StyleExchange(styleMapContainer, map).moveStyle(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }



	@Override
    protected HookAction createHookAction() {
	    return null;
	}


	private Optional<MapModel> loadStyleMapContainer(final URL source) {
	    final ModeController modeController = Controller.getCurrentModeController();
	    final UrlManager urlManager = modeController.getExtension(UrlManager.class);
	    final MapModel styleMapContainer = new MapModel(modeController.getMapController().duplicator());
	    if (urlManager.loadCatchExceptions(source, styleMapContainer))
	        return Optional.of(styleMapContainer);
	    else
	        return Optional.empty();

	}


	public void replaceStyle(final File file, final MapModel targetMap) throws MalformedURLException {
        final URL url = file.toURI().toURL();
	    loadStyleMapContainer(url).ifPresent(styleMapContainer ->
	        new StyleExchange(styleMapContainer, targetMap).replaceMapStylesAndAutomaticStyle());
	}

    public void copyStyles(final File file, final MapModel targetMap) throws MalformedURLException {
        final URL url = file.toURI().toURL();
        loadStyleMapContainer(url).ifPresent(styleMapContainer ->
            new StyleExchange(styleMapContainer, targetMap).copyMapStyles());
    }

    private void copyMapStylesNoUndoNoRefresh(final MapModel targetMap) {
        MapStyleModel mapStyleModel = MapStyleModel.getExtension(targetMap);
        String followedMap = mapStyleModel.getProperty(MapStyleModel.FOLLOWED_MAP_LOCATION_PROPERTY);
        String lastUpdateTimeString = mapStyleModel.getProperty(MapStyleModel.FOLLOWED_MAP_LAST_TIME);
        long lastUpdateTime = lastUpdateTimeString != null ? Long.parseLong(lastUpdateTimeString) : 0;
        String followedMapPath; 
        if(followedMap != null) {
            try {
                URI source = new URI(followedMap);
                boolean shouldUpdate;
                long sourceLastModificationTime ;
                if(source.getScheme().equalsIgnoreCase("file")) {
                    File file = Paths.get(source).toFile();
                    sourceLastModificationTime = file.lastModified();
                    shouldUpdate = sourceLastModificationTime > lastUpdateTime;
                    followedMapPath = file.getAbsolutePath();
                } else {
                    sourceLastModificationTime = lastUpdateTime;
                    shouldUpdate = true;
                    followedMapPath = followedMap;
                }
                if(shouldUpdate) {
                    loadStyleMapContainer(source.toURL()).ifPresent(styleMapContainer ->
                    {
                        new StyleExchange(styleMapContainer, targetMap).copyMapStylesNoUndoNoRefresh();
                        UITools.showMessage(TextUtils.format("stylesUpdated", targetMap.getTitle(), followedMapPath), JOptionPane.INFORMATION_MESSAGE);
                    });
                }
                if(sourceLastModificationTime != lastUpdateTime)
                    MapStyleModel.getExtension(targetMap).setProperty(MapStyleModel.FOLLOWED_MAP_LAST_TIME, Long.toString(sourceLastModificationTime));
            }
            catch (URISyntaxException | MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final MapStyleModel mapStyleModel = (MapStyleModel) extension;
		super.saveExtension(extension, element);
		final Color backgroundColor = mapStyleModel.getBackgroundColor();
		if (backgroundColor != null) {
			element.setAttribute("background", ColorUtils.colorToString(backgroundColor));
		}
		final float zoom = mapStyleModel.getZoom();
		if (zoom != 1f) {
			element.setAttribute("zoom", Float.toString(zoom));
		}
		final MapViewLayout layout = mapStyleModel.getMapViewLayout();
		if (!layout.equals(MapViewLayout.MAP)) {
			element.setAttribute("layout", layout.toString());
		}
		saveConditionalStyles(mapStyleModel.getConditionalStyleModel(), element, true);
		saveProperties(mapStyleModel.getProperties(), element);
	}

	private void saveProperties(Map<String, String> properties, XMLElement element) {
		if(properties.isEmpty()){
			return;
		}
	    final XMLElement xmlElement = new XMLElement("properties");
	    for (Entry<String, String>  entry: properties.entrySet()){
	    	xmlElement.setAttribute(entry.getKey(), entry.getValue());
	    }
	    element.addChild(xmlElement);
    }

	public void setZoom(final MapModel map, final float zoom) {
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		if (zoom == mapStyleModel.getZoom()) {
			return;
		}
		mapStyleModel.setZoom(zoom);
		Controller.getCurrentModeController().getMapController().setSaved(map, false);
	}


	public void setMapViewLayout(final MapModel map, final MapViewLayout layout) {
		final MapStyleModel mapStyleModel = MapStyleModel.getExtension(map);
		if (layout.equals(mapStyleModel.getMapViewLayout())) {
			return;
		}
		mapStyleModel.setMapViewLayout(layout);
		Controller.getCurrentModeController().getMapController().setSaved(map, false);
	}

	public void setBackgroundColor(final MapStyleModel model, final Color color) {
		final Color actionColor = ColorUtils.makeNonTransparent(color);
		final Color oldColor = model.getBackgroundColor();
		if (actionColor == oldColor || actionColor != null && actionColor.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				model.setBackgroundColor(actionColor);
				Controller.getCurrentModeController().getMapController().fireMapChanged(
				    new MapChangeEvent(MapStyle.this, Controller.getCurrentController().getMap(), MapStyle.RESOURCES_BACKGROUND_COLOR,
				        oldColor, actionColor));
			}

			@Override
			public String getDescription() {
				return "MapStyle.setBackgroundColor";
			}

			@Override
			public void undo() {
				model.setBackgroundColor(oldColor);
				Controller.getCurrentModeController().getMapController().fireMapChanged(
				    new MapChangeEvent(MapStyle.this, Controller.getCurrentController().getMap(), MapStyle.RESOURCES_BACKGROUND_COLOR,
				        actionColor, oldColor));
			}
		};
		Controller.getCurrentModeController().execute(actor, Controller.getCurrentController().getMap());
	}

	public static  MapStyle getController(final ModeController modeController) {
        return modeController.getExtension(MapStyle.class);
    }

	public static MapStyle getController() {
		return getController(Controller.getCurrentModeController());
    }

	public void setProperty(final MapModel model, final String key, final String newValue) {
		final MapStyleModel styleModel = MapStyleModel.getExtension(model);
		final String oldValue = styleModel.getProperty(key);
		if(oldValue == newValue || oldValue != null && oldValue.equals(newValue)) {
			return;
		}
		IActor actor = new  IActor() {
			@Override
			public void undo() {
				setPropertyWithoutUndo(model, key, oldValue);
			}

			@Override
			public String getDescription() {
				return "set map style property";
			}

			@Override
			public void act() {
				setPropertyWithoutUndo(model, key, newValue);
			}

			private void setPropertyWithoutUndo(final MapModel model, final String key, final String newValue) {
				styleModel.setProperty(key, newValue);
	    		Controller.getCurrentModeController().getMapController().fireMapChanged(
	    		    new MapChangeEvent(MapStyle.this, model, key, oldValue, newValue));
            }
		};
		Controller.getCurrentModeController().execute(actor, model);
	}

	public String getPropertySetDefault(final MapModel model, final String key) {
			final MapStyleModel styleModel = MapStyleModel.getExtension(model);
			final String oldValue = styleModel.getProperty(key);
			if(oldValue != null){
				return oldValue;
			}
			final String value = ResourceController.getResourceController().getProperty(key);
			styleModel.setProperty(key, value);
			return value;
	}

	public String getProperty(final MapModel model, final String key) {
	    return MapStyleModel.getExtension(model).getProperty(key);
	}

    public Map<String, String> getPropertiesReadOnly(final MapModel model) {
        return Collections.unmodifiableMap(MapStyleModel.getExtension(model).getProperties());
    }

}
