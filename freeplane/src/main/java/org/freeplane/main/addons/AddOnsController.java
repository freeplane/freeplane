package org.freeplane.main.addons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringEscapeUtils;
import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.main.addons.AddOnProperties.AddOnType;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;

public class AddOnsController {
	private static final String PATH_SEPARATOR = File.pathSeparator;
	private static final String FILES_TO_DELETE_ON_NEXT_START_PROPERTY = "addons.filesToDeleteOnNextStart";
	private static final String ADDONS_DIR = "addons";
	private static AddOnsController addOnsController;
	private List<AddOnProperties> installedAddOns = new ArrayList<AddOnProperties>();
	private boolean autoInstall;
    public static final String LATEST_VERSION_FILE = "version.properties";

	private AddOnsController() {
		deleteOldFiles();
		createAddOnsDirIfNecessary();
		registerPlugins();
		autoInstall = true;
	}

	private void deleteOldFiles() {
		String[] filesToDelete = ResourceController.getResourceController()
				.getProperty(FILES_TO_DELETE_ON_NEXT_START_PROPERTY, "")
				.split(PATH_SEPARATOR);
		for(String path : filesToDelete) {
			if(! path.isEmpty())
				deleteFile(path);
		}
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
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".plugin.xml");
			}
		});
		if (addonXmlFiles == null) {
			LogUtils.severe("Can not read addon directory " + addOnsDir);
			return;
		}
		final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
		for (File file : addonXmlFiles) {
			try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))){
				final IXMLReader reader = new StdXMLReader(inputStream);
				parser.setReader(reader);
				registerInstalledAddOn(new AddOnProperties(AddOnType.PLUGIN, (XMLElement) parser.parse()));
			}
			catch (final Exception e) {
				LogUtils.warn("error parsing " + file, e);
			}
		}
	}

	public static AddOnsController getController() {
		if (addOnsController == null)
			addOnsController = new AddOnsController();
		return addOnsController;
	}

	public List<AddOnProperties> getInstalledAddOns() {
		return Collections.unmodifiableList(new ArrayList<AddOnProperties>(installedAddOns));
	}

	public void registerInstalledAddOn(final AddOnProperties addOn) {
		installedAddOns.add(addOn);
		final ResourceController resourceController = ResourceController.getResourceController();
		if (addOn.getDefaultProperties() != null)
			resourceController.addDefaults(addOn.getDefaultProperties());
		if (addOn.getTranslations() != null)
			registerAddOnResources(addOn, resourceController);
		if (addOn.getPreferencesXml() != null) {
			final ModeController modeController = Controller.getCurrentModeController();
			if (modeController instanceof MModeController) {
				((MModeController)modeController).getOptionPanelBuilder().load(new StringReader(addOn.getPreferencesXml()));
			}
		}
	}

	/** make the translations of this add-on known system-wide.
	 * Translations of add-ons will always be much worse than of Freeplane itself. By copying translation
	 * for the default language to the selected language we avoid ugly "[translate me]" strings. */
	public static void registerAddOnResources(final AddOnProperties addOn, final ResourceController rc) {
		LinkedHashMap<String, Map<String, String>> translations = new LinkedHashMap<>();
		String defaultLanguage = rc.getDefaultLanguageCode();
		translations.put(defaultLanguage, getLanguageResources(addOn, defaultLanguage));
		if (!defaultLanguage.equals(rc.getLanguageCode())) {
			translations.put(rc.getLanguageCode(), getLanguageResources(addOn, rc.getLanguageCode()));
			completeResourcesFromDefault(translations.get(defaultLanguage), translations.get(rc.getLanguageCode()));
		}
		for (Entry<String, Map<String, String>> entry : translations.entrySet()) {
			rc.addLanguageResources(entry.getKey(), entry.getValue());
			rc.addLanguageResources(entry.getKey(), addOptionPanelPrefix(entry.getValue(), addOn.getName()));
		}
	}

	private static void completeResourcesFromDefault(Map<String, String> defaultResources,
	                                                 Map<String, String> selectedLanguageResources) {
		for (Entry<String, String> entry : defaultResources.entrySet()) {
			if (selectedLanguageResources.get(entry.getKey()) == null) {
				selectedLanguageResources.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private static Map<String, String> getLanguageResources(AddOnProperties addOn, String language) {
		final Map<String, String> resources = addOn.getTranslations().get(language);
		if (resources != null) {
			return unescapeStrings(new LinkedHashMap<>(resources));
		}
		else {
			return new LinkedHashMap<String, String>();
		}
	}

	private static Map<String, String> unescapeStrings(final Map<String, String> resources) {
		for (Entry<String, String> entry : resources.entrySet()) {
		    if (entry.getValue().indexOf('\\') != -1) {
		        // convert \uFFFF sequences
		        entry.setValue(StringEscapeUtils.unescapeJava(entry.getValue()));
		    }
		}
		return resources;
	}

	/** if the add-on is configurable it's a burden for the add-on-writer that the keys in the configuration are
	 * prepended by "OptionPanel.". This code relieves the developer from taking care of that. */
	private static Map<String, String> addOptionPanelPrefix(final Map<String, String> resources, final String addOnName) {
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

	public void deinstall(AddOnProperties addOn) {
		String previousFilesToDeleteOnNextStart = ResourceController.getResourceController().getProperty(FILES_TO_DELETE_ON_NEXT_START_PROPERTY, "");
		StringBuilder filesToDeleteOnNextStart = new StringBuilder(previousFilesToDeleteOnNextStart);
		LogUtils.info("deinstalling " + addOn);
		for (String[] rule : addOn.getDeinstallationRules()) {
			if (rule[0].equals("delete")) {
				final String path = expandVariables(rule);
				boolean deleted = deleteFile(path);
				if (!deleted) {
					filesToDeleteOnNextStart.append(PATH_SEPARATOR);
					filesToDeleteOnNextStart.append(path);
				}
			}
		}
		installedAddOns.remove(addOn);
		if(filesToDeleteOnNextStart.length() > previousFilesToDeleteOnNextStart.length()) {
			ResourceController.getResourceController().setProperty(FILES_TO_DELETE_ON_NEXT_START_PROPERTY,
				filesToDeleteOnNextStart.toString());
		}
	}

	private boolean deleteFile(final String path) {
		final File file = new File(path);
		if (!file.exists()) {
			LogUtils.warn("file " + path + " should be deleted but does not exist");
		}
		else {
			if (file.delete())
				LogUtils.info("deleted " + path);
			else {
				LogUtils.warn("could not delete file " + path);
			}
		}
		boolean deleted = !file.exists();
		return deleted;
	}

	private String expandVariables(String[] rule) {
		return rule[1].replace("${installationbase}", ResourceController.getResourceController()
		    .getFreeplaneUserDirectory());
	}

	/** returns true if the url is an add-on package and the user decided to install it. */
	public boolean installIfAppropriate(final URL url) {
		if(! autoInstall)
			return false;
		if (url.getFile().endsWith(UrlManager.FREEPLANE_ADD_ON_FILE_EXTENSION)) {
			AddOnInstaller installer = Controller.getCurrentModeController().getExtension(
			    AddOnInstaller.class);
			if (installer == null) {
				LogUtils.warn("no AddOnInstaller registered. Cannot install " + url);
				return false;
			}
			UITools.backOtherWindows();
			final int selection = UITools.showConfirmDialog(null,
				TextUtils.format("newmap.install.addon.question", new File(url.getFile()).getName()),
			    TextUtils.getText("newmap.install.addon.title"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (selection == JOptionPane.OK_OPTION) {
				installer.install(url);
				return true;
			}
		}
		return false;
	}

	public void setAutoInstallEnabled(boolean autoInstall) {
	   this.autoInstall = autoInstall;

    }

	public boolean isAutoInstallEnabled() {
    	return autoInstall;
    }

	public AddOnProperties getInstalledAddOn(final String name) {
		// Performance consideration: list is small -> iteration over list is OK.
		for (AddOnProperties addOn : installedAddOns) {
	        if (addOn.getName().equals(name))
	        	return addOn;
        }
		return null;
	}
}
