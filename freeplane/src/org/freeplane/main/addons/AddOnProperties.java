package org.freeplane.main.addons;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.CdataContentXmlWriter;
import org.freeplane.n3.nanoxml.XMLElement;

public class AddOnProperties {
	public enum AddOnType {
		SCRIPT, PLUGIN
	}
	public static final String OP_CONFIGURE = "configure";
	public static final String OP_DEACTIVATE = "deactivate";
	public static final String OP_ACTIVATE = "activate";
	public static final String OP_DEINSTALL = "deinstall";

	private final AddOnType addOnType;
	private boolean active = true;
	private String name;
	private String version;
	private FreeplaneVersion freeplaneVersionFrom;
	private FreeplaneVersion freeplaneVersionTo;
	private URL sourceUrl;
	private String description;
	private Map<String, Map<String, String>> translations;
	private String preferencesXml;
	private Map<String, String> defaultProperties;
	private List<String[/*action, file*/]> deinstallationRules;
	private File addOnPropertiesFile;

	public AddOnProperties(AddOnType addOnType) {
		this.addOnType = addOnType;
	}

	public AddOnProperties(AddOnType addOnType, XMLElement addOnelement) {
		this(addOnType);
		this.setName(addOnelement.getAttribute("name", null));
		this.setVersion(addOnelement.getAttribute("version", null));
		this.setFreeplaneVersionFrom(FreeplaneVersion.getVersion(addOnelement.getAttribute("freeplaneVersionFrom",
		    null)));
		this.setFreeplaneVersionTo(FreeplaneVersion.getVersion(addOnelement.getAttribute("freeplaneVersionTo", null)));
		this.setSourceUrl(parseSourceUrl(addOnelement.getAttribute("source.url", null)));
		this.setActive(Boolean.parseBoolean(addOnelement.getAttribute("active", "true")));
		this.setDescription(getContentOfFirstElement(addOnelement.getChildrenNamed("description")));
		this.setTranslations(parseTranslations(addOnelement.getChildrenNamed("translations")));
		this.setPreferencesXml(getContentOfFirstElement(addOnelement.getChildrenNamed("preferences.xml")));
		this.setDefaultProperties(parseAttributesToProperties(addOnelement.getChildrenNamed("default.properties")));
		this.setDeinstallationRules(parseDeinstallationRules(addOnelement.getChildrenNamed("deinstall")));
		validate();
	}

	private URL parseSourceUrl(String sourceUrl) {
		try {
			return new URL(sourceUrl);
		}
		catch (Exception e) {
			return null;
		}
	}

	private String getContentOfFirstElement(Vector<XMLElement> xmlElements) {
		if (xmlElements == null || xmlElements.isEmpty())
			return null;
		return xmlElements.get(0).getContent();
	}

	private Map<String, Map<String, String>> parseTranslations(Vector<XMLElement> xmlElements) {
		final Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		if (xmlElements != null && !xmlElements.isEmpty()) {
			for (XMLElement xmlElement : xmlElements.get(0).getChildren()) {
				Map<String, String> properties = new LinkedHashMap<String, String>();
				result.put(xmlElement.getAttribute("name", null), properties);
				for (XMLElement entryXmlElement : xmlElement.getChildren()) {
					properties.put(entryXmlElement.getAttribute("key", null), entryXmlElement.getContent());
				}
			}
		}
		return result;
	}

	private Map<String, String> parseAttributesToProperties(Vector<XMLElement> xmlElements) {
		if (xmlElements == null || xmlElements.isEmpty())
			return Collections.emptyMap();
		return propertiesToStringMap(xmlElements.get(0).getAttributes());
	}

	private Map<String, String> propertiesToStringMap(Properties props) {
		final LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		for (Entry<Object, Object> entry : props.entrySet()) {
			result.put((String) entry.getKey(), (String) entry.getValue());
		}
		return result;
	}

	private List<String[]> parseDeinstallationRules(Vector<XMLElement> xmlElements) {
		final List<String[]> result = new ArrayList<String[]>();
		if (xmlElements != null && !xmlElements.isEmpty()) {
			for (XMLElement xmlElement : xmlElements.get(0).getChildren()) {
				result.add(new String[] { xmlElement.getName(), xmlElement.getContent() });
			}
		}
		return result;
	}

	private void validate() {
		if (empty(name))
			throw new RuntimeException("while parsing .addon.xml file: name must be set");
		if (empty(version))
			throw new RuntimeException("while parsing .addon.xml file: version must be set");
		if (freeplaneVersionFrom == null)
			throw new RuntimeException("while parsing .addon.xml file: freeplaneVersionFrom must be set");
		if (empty(description))
			throw new RuntimeException("while parsing .addon.xml file: description must be set");
	}

	public AddOnType getAddOnType() {
		return addOnType;
	}

	/** returns the key that is used to lookup the translated name of the add-on. */
	public String getNameKey() {
		return "addons." + name;
	}

	public String getTranslatedName() {
		return TextUtils.getText(getNameKey());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public FreeplaneVersion getFreeplaneVersionFrom() {
		return freeplaneVersionFrom;
	}

	public void setFreeplaneVersionFrom(FreeplaneVersion freeplaneVersionFrom) {
		this.freeplaneVersionFrom = freeplaneVersionFrom;
	}

	public FreeplaneVersion getFreeplaneVersionTo() {
		return freeplaneVersionTo;
	}

	public void setFreeplaneVersionTo(FreeplaneVersion freeplaneVersionTo) {
		this.freeplaneVersionTo = freeplaneVersionTo;
	}

	public URL getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(URL sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, Map<String, String>> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<String, Map<String, String>> translations) {
		this.translations = translations;
	}

	public String getPreferencesXml() {
		return preferencesXml;
	}

	public void setPreferencesXml(String preferencesXml) {
		this.preferencesXml = preferencesXml;
	}

	public Map<String, String> getDefaultProperties() {
		return defaultProperties;
	}

	public void setDefaultProperties(Map<String, String> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public List<String[]> getDeinstallationRules() {
		return deinstallationRules;
	}

	public void setDeinstallationRules(List<String[]> rules) {
		this.deinstallationRules = rules;
	}
	
	/** the persistence location of this AddOnProperties object. */
	public File getAddOnPropertiesFile() {
		return addOnPropertiesFile;
	}

	public void setAddOnPropertiesFile(File file) {
		addOnPropertiesFile = file;
    }

	public boolean supportsOperation(String opName) {
		if (opName.equals(OP_CONFIGURE))
			return !empty(preferencesXml);
		if (opName.equals(OP_DEACTIVATE))
			return active;
		if (opName.equals(OP_ACTIVATE))
			return !active;
		if (opName.equals(OP_DEINSTALL))
			return deinstallationRules != null && !deinstallationRules.isEmpty();
		return false;
	}

	public String toXmlString() {
		try {
			final String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			final StringWriter writer = new StringWriter();
			final CdataContentXmlWriter xmlWriter = new CdataContentXmlWriter(writer);
			xmlWriter.addRawContent(header);
			xmlWriter.addRawContent(System.getProperty("line.separator"));
			xmlWriter.write(toXml(), true);
			return writer.toString();
		}
		catch (IOException e) {
			// StringWriter does not throw an exception but anyhow...
			throw new RuntimeException(e);
		}
	}

	// // this is the Groovy equivalent for all this below - removed from the script to avoid redundancies
	// writer.write('<?xml version="1.0" encoding="UTF-8"?>' + System.getProperty("line.separator"))
	// new MarkupBuilder(writer).addon( configMap['properties'] ) {
	// 	description('') {
	// 		writer.write('<![CDATA[' + configMap['description'] + ']]>')
	// 	}
	// 	permissions( configMap['permissions'] )
	// 	translations {
	// 		configMap['translations'].collect{ loc, translationMap ->
	// 	        locale( name:loc ) {
	// 	            translationMap.collect{ k,v ->
	// 	                entry(key:k, v)
	// 	            }
	// 	        }
	// 		}
	// 	}
	// 	'preferences.xml'('') {
	// 		writer.write('<![CDATA[' + configMap['preferences.xml'] + ']]>')
	// 	}
	// 	'default.properties'( configMap['default.properties'] )
	// 	deinstall {
	// 		configMap['deinstall'].collect { pair ->
	// 			"${pair[0]}"(pair[1])
	// 		}
	// 	}
	// }
	// writer.close()
	public XMLElement toXml() {
		final XMLElement addonElement = new XMLElement("addon");
		addonElement.setAttribute("name", name);
		addonElement.setAttribute("version", version);
		addonElement.setAttribute("freeplaneVersionFrom", freeplaneVersionFrom.toString());
		if (freeplaneVersionTo != null)
			addonElement.setAttribute("freeplaneVersionTo", freeplaneVersionTo.toString());
		if (sourceUrl != null)
			addonElement.setAttribute("source.url", sourceUrl.toString());
		addonElement.setAttribute("active", Boolean.toString(active));
		addDescriptionAsChild(addonElement);
		addTranslationsAsChild(addonElement);
		addPreferencesXmlAsChild(addonElement);
		addDefaultPropertiesAsChild(addonElement);
		addDeinstallationRulesAsChild(addonElement);
		return addonElement;
	}

	private void addDescriptionAsChild(XMLElement parent) {
		final XMLElement xmlElement = new XMLElement("description");
		xmlElement.setContent(description);
		parent.addChild(xmlElement);
	}

	private void addTranslationsAsChild(XMLElement parent) {
		final XMLElement translationsElement = new XMLElement("translations");
		for (Entry<String, Map<String, String>> localeEntry : translations.entrySet()) {
			final XMLElement localeElement = new XMLElement("locale");
			localeElement.setAttribute("name", localeEntry.getKey());
			for (Entry<String, String> translationEntry : localeEntry.getValue().entrySet()) {
				final XMLElement translationElement = new XMLElement("entry");
				translationElement.setAttribute("key", translationEntry.getKey());
				translationElement.setContent(translationEntry.getValue());
				localeElement.addChild(translationElement);
			}
			translationsElement.addChild(localeElement);
		}
		parent.addChild(translationsElement);
	}

	private void addPreferencesXmlAsChild(XMLElement parent) {
		final XMLElement xmlElement = new XMLElement("preferences.xml");
		xmlElement.setContent(preferencesXml);
		parent.addChild(xmlElement);
	}

	private void addDefaultPropertiesAsChild(XMLElement parent) {
		final XMLElement xmlElement = new XMLElement("default.properties");
		for (Entry<String, String> entry : defaultProperties.entrySet()) {
			xmlElement.setAttribute(entry.getKey(), entry.getValue());
		}
		parent.addChild(xmlElement);
	}

	private void addDeinstallationRulesAsChild(XMLElement parent) {
		final XMLElement xmlElement = new XMLElement("deinstall");
		for (String[] rule : deinstallationRules) {
			final XMLElement ruleElement = new XMLElement(rule[0]);
			ruleElement.setContent(rule[1]);
			xmlElement.addChild(ruleElement);
		}
		parent.addChild(xmlElement);
	}

	private boolean empty(String string) {
		return string == null || string.length() == 0;
	}

	@Override
	public String toString() {
		return "AddOnProperties(addOnType=" + addOnType + ", active=" + active + ", name=" + name + ", version="
		        + version + ")";
	}
}
