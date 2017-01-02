package org.freeplane.features.edge;

import java.awt.Color;
import java.util.ArrayList;
import java.util.WeakHashMap;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.MapStyleModel;

public class EdgeColorsConfigurationFactory {
	private final static WeakHashMap<String, EdgeColorConfiguration> configurations = new WeakHashMap<>();
	private ModeController modeController;

	public EdgeColorsConfigurationFactory(ModeController modeController) {
		this.modeController = modeController;
	}

	public EdgeColorConfiguration create(MapModel map){
		MapStyleModel mapStyle = MapStyleModel.getExtension(map);
		final String mapPropertyName = "edgeColorConfiguration";
		final String configurationString = mapStyle.getProperty(mapPropertyName);
		if(configurationString != null)
			return load(configurationString);
		else {
			final EdgeColorConfiguration newConfiguration = createNewConfiguration(map);
			String newConfigurationString = save(newConfiguration);
			mapStyle.setProperty(mapPropertyName, newConfigurationString);
			configurations.put(newConfigurationString, newConfiguration);
			return newConfiguration;
		}
	}

	private EdgeColorConfiguration createNewConfiguration(MapModel map) {
		ArrayList<Color> colors = new ArrayList<>();
		AutomaticLayoutController automaticLayoutController = modeController.getExtension(AutomaticLayoutController.class);
		EdgeController edgeController = modeController.getExtension(EdgeController.class);
		for(int levelStyleCounter = 1;;levelStyleCounter++){
			NodeModel styleNode = automaticLayoutController.getStyleNode(map, levelStyleCounter, false);
			if(styleNode == null)
				break;
			colors.add(edgeController.getColor(styleNode));
		}
		return new EdgeColorConfiguration(colors);
	}

	private EdgeColorConfiguration load(String configurationString) {
		final EdgeColorConfiguration knownConfiguration = configurations.get(configurationString);
		if(knownConfiguration != null)
			return knownConfiguration;
		final String[] colorStrings = configurationString.split(",");
		ArrayList<Color> colors = new ArrayList<>(colorStrings.length);
		for(String color : colorStrings)
			colors.add(ColorUtils.stringToColor(color));
		final EdgeColorConfiguration edgeColorConfiguration = new EdgeColorConfiguration(colors);
		configurations.put(configurationString, knownConfiguration);
		return edgeColorConfiguration;
	}
	
	public String save(EdgeColorConfiguration configuration) {
		StringBuilder sb = new StringBuilder();
		for(Color color : configuration.colors) {
			if(sb.length() > 0)
				sb.append(',');
			sb.append(ColorUtils.colorToRGBAString(color));
		}
		return sb.toString();
	}

}
