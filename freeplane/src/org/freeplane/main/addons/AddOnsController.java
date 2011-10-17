package org.freeplane.main.addons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.addons.AddOnProperties.AddOnType;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;

public class AddOnsController {
	private static final String ADDONS_DIR = "addons";
	private static AddOnsController addOnsController;
	private List<AddOnProperties> installedAddOns = new ArrayList<AddOnProperties>();

	public AddOnsController() {
		createAddOnsDirIfNecessary();
		registerPlugins();
	}

	private void createAddOnsDirIfNecessary() {
		final File addOnsDir = getAddOnsDir();
		// in applets the addOnsDir will be null
		if (addOnsDir != null && !addOnsDir.exists()) {
			LogUtils.info("creating user add-ons directory " + addOnsDir);
			addOnsDir.mkdirs();
		}
	}

	private void registerPlugins() {
		final File addOnsDir = getAddOnsDir();
		// in applets the addOnsDir will be null
		if (addOnsDir == null)
			return;
		File[] addonXmlFiles = addOnsDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".plugin.xml");
			}
		});
		final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		for (File file : addonXmlFiles) {
			BufferedInputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(file));
				final IXMLReader reader = new StdXMLReader(inputStream);
				parser.setReader(reader);
				registerInstalledAddOn(new AddOnProperties(AddOnType.PLUGIN, (XMLElement) parser.parse()));
			}
			catch (final Exception e) {
				LogUtils.warn("error parsing " + file, e);
			}
			finally {
				FileUtils.silentlyClose(inputStream);
			}
		}
	}

	public static AddOnsController getController() {
		if (addOnsController == null)
			addOnsController = new AddOnsController();
		return addOnsController;
	}

	public List<AddOnProperties> getInstalledAddOns() {
		return Collections.unmodifiableList(installedAddOns);
	}

	public void registerInstalledAddOn(final AddOnProperties addOn) {
		installedAddOns.add(addOn);
		final ResourceController resourceController = ResourceController.getResourceController();
		if (addOn.getDefaultProperties() != null)
			resourceController.addDefaults(addOn.getDefaultProperties());
		if (addOn.getPreferencesXml() != null) {
			final ModeController modeController = Controller.getCurrentModeController();
			if (modeController instanceof MModeController) {
				((MModeController)modeController).getOptionPanelBuilder().load(new StringReader(addOn.getPreferencesXml()));
			}
		}
		if (addOn.getTranslations() != null) {
			HashSet<String> languages = new HashSet<String>();
			languages.add(resourceController.getLanguageCode());
			languages.add(resourceController.getDefaultLanguageCode());
			for (String language : languages) {
				final Map<String, String> resources = addOn.getTranslations().get(language);
				if (resources != null) {
					resourceController.addLanguageResources(language, addOptionPanelPrefix(resources, addOn.getName()));
					resourceController.addLanguageResources(language, resources);
				}
            }
		}
	}

	/** if the add-on is configurable it's a burden for the add-on-writer that the keys in the configuration are
	 * prepended by "OptionPanel.". This code relieves the developer from taking care of that. */
	private Map<String, String> addOptionPanelPrefix(final Map<String, String> resources, final String addOnName) {
		final HashMap<String, String> result = new HashMap<String, String>(resources.size());
		for (Entry<String, String> entry : resources.entrySet()) {
	        result.put("OptionPanel." + entry.getKey(), entry.getValue());
        }
		final String nameKey = "addons." + addOnName;
		result.put("OptionPanel.separator." + nameKey, resources.get(nameKey));
	    return result;
    }

	public File getAddOnsDir() {
		// in applets the userDir will be null
		final String userDir = ResourceController.getResourceController().getFreeplaneUserDirectory();
		return userDir == null ? null : new File(userDir, ADDONS_DIR);
	}

	public void save(final AddOnProperties addOn) throws IOException {
		final File addOnsDir = getAddOnsDir();
		if (addOnsDir != null) {
			File file = addOn.getAddOnPropertiesFile();
			if (file == null) {
				file = new File(addOnsDir, addOn.getName() + "." + addOn.getAddOnType().name().toLowerCase() + ".xml");
			}
			FileUtils.dumpStringToFile(addOn.toXmlString(), file, "UTF-8");
		}
    }

	public void deInstall(AddOnProperties addOn) {
		LogUtils.severe("FIXME: implement deinstall");
    }
}
