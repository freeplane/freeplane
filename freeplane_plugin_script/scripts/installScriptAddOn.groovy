import org.apache.commons.lang.WordUtils;

import javax.swing.JOptionPane

import org.freeplane.core.resources.ResourceController
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.main.addons.AddOnProperties
import org.freeplane.plugin.script.ScriptingPermissions
import org.freeplane.plugin.script.addons.ScriptAddOnProperties
import org.freeplane.plugin.script.proxy.Proxy

/**
 * installs an add-on that is opened as the current map.
 */

// FIXME: i18n
dialogTitle = 'Add-on Installer'

def terminate(String message) {
	// FIXME: i18n
	throw new Exception('Installation cancelled: ' + message)
}

def letConfirmOrTerminate(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	// FIXME: i18n
	if (selection != JOptionPane.OK_OPTION)
		throw new Exception('Installation cancelled')
	return true
}

def yesNoOrTerminate(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	// FIXME: i18n
	if (selection != JOptionPane.OK_OPTION)
		throw new Exception('Installation cancelled')
	return selection == JOptionPane.OK_OPTION
}

def mapStructureAssert(check, String issue) {
	// FIXME: i18n
    if (! check)
        throw new Exception(WordUtils.wrap("Broken map: " + issue, 80, null, true))
}

def installationAssert(boolean check, String issue) {
	// FIXME: i18n
	if (! check)
		throw new Exception("Installation failed: " + issue)
}

def parseProperties(Map childNodeMap, Map configMap) {
	def property = 'properties'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.attributes.map.inject([:]){ map,k,v ->
		if (v)
			map[k] = k.toString().startsWith('freeplaneVersion') ? parseFreeplaneVersion(k, v) : v
		return map
	}
	def mandatoryPropertyNames = [
		'name',
		'version',
		'freeplaneVersionFrom'
		// optional: 'freeplaneVersionTo'
	]
	def missingProperties = mandatoryPropertyNames.findAll {
		! configMap[property][it]
	}
	// FIXME: i18n
	mapStructureAssert( ! missingProperties, 'Missing properties ' + missingProperties)
	println property + ": " + configMap[property]
}

def checkFreeplaneVersion(Map configMap) {
	FreeplaneVersion currentVersion = c.freeplaneVersion
	def versionFrom = configMap['properties']['freeplaneVersionFrom']
	// FIXME: i18n
	if (currentVersion.isOlderThan(versionFrom))
		terminate("Current Freeplane version ${currentVersion} is too old. This add-on needs at least ${versionFrom}")
	def versionTo = configMap['properties'].get('freeplaneVersionTo')
	// FIXME: i18n
	if (versionTo && currentVersion.isNewerThan(versionTo))
		terminate("current Freeplane version ${currentVersion} is too new. This add-on supports at most ${versionTo}")
}

def parseFreeplaneVersion(String propertyName, String versionString) {
	try {
		if (versionString)
			return FreeplaneVersion.getVersion(versionString)
		return null
	}
	catch (Exception e) {
		e.printStackTrace()
		// FIXME: i18n
		mapStructureAssert(false, "Format error in ${propertyName} (value: ${versionString})")
	}
}

def parseDescription(Map childNodeMap, Map configMap) {
	def property = 'description'
	Proxy.Node propertyNode = childNodeMap[property]
	// FIXME: i18n
	mapStructureAssert(propertyNode.children.size() == 1, 'Description node must have exactly one child node')
	configMap[property] = propertyNode.children[0].text
	println property + ": " + configMap[property]
}

def parsePermissions(Map childNodeMap, Map configMap) {
    def permissionNames = ScriptingPermissions.permissionNames.findAll { it.startsWith("execute_") }
    def property = 'permissions'
    Proxy.Node propertyNode = childNodeMap[property]
    def missingPermissions = permissionNames.findAll{ !propertyNode[it] }
	// FIXME: i18n
    mapStructureAssert( ! missingPermissions, "'permissions' node misses attributes " + missingPermissions)
	configMap[property] = propertyNode.attributes.map.findAll { k,v -> permissionNames.contains(k) }
	println property + ": " + configMap[property]
}

def parseTranslations(Map childNodeMap, Map configMap) {
	def property = 'translations'
	Proxy.Node propertyNode = childNodeMap[property]
	// a Map<locale, Map<key, translation>>

	def translationsMap = propertyNode.children.inject([:]){ map, localeNode ->
		def locale = localeNode.plainText
		map[locale] = localeNode.attributes.map.inject([:]){ localeMap, Map.Entry e ->
			localeMap[expandVariables(e.key, configMap['properties'])] = e.value
			return localeMap
		}
		def key = 'addons.${name}'
		def expKey = expandVariables(key, configMap['properties'])
		// FIXME: i18n
		mapStructureAssert(map[locale][expKey], "Missing translation of ${key} for locale ${locale}")
		return map
	}
	configMap[property] = translationsMap
	println property + ": " + configMap[property]
}

def parsePreferencesXml(Map childNodeMap, Map configMap) {
	def property = 'preferences.xml'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.isLeaf() ? null : propertyNode.children[0].text
	println property + ": " + configMap[property]
}

def parseDefaultProperties(Map childNodeMap, Map configMap) {
	def property = 'default.properties'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.attributes.map
	println property + ": " + configMap[property]
}

def parseScript(Map childNodeMap, Map configMap) {
	def property = 'script'
	Proxy.Node propertyNode = childNodeMap[property]
	// FIXME: i18n
	mapStructureAssert( ! propertyNode.isLeaf(), 'Missing script')
	configMap[property] = propertyNode.children.first().text
	// FIXME: i18n
	mapStructureAssert( ! htmlUtils.isHtmlNode(configMap[property]), 'Script may not be formatted as HTML')
	println property + ": " + configMap[property]
}

// a list of [action, file] pairs
def parseDeinstallationRules(Map childNodeMap, Map configMap) {
	def property = 'deinstall'
	Proxy.Node propertyNode = childNodeMap[property]
	def attribs = propertyNode.attributes
	// we can't use a simple map since most entries have the same key -> iterate over index
	configMap[property] = (0..attribs.size()-1).collect {
		// the right type for AddOnProperties
		[attribs.getKey(it), expandVariables(attribs.get(it), configMap['properties']).trim()] as String[]
	}
	def knownDeinstallationRules = [
		'delete'
	]
	def unknownDeinstallationRules = attribs.names.findAll{ k -> ! knownDeinstallationRules.contains(k) }
	// FIXME: i18n
	mapStructureAssert( ! unknownDeinstallationRules, "Unknown deinstallation rule(s) ${unknownDeinstallationRules}")
	println property + ": " + configMap[property]
}

def handlePermissions(Map configMap) {
	def permissionMap = configMap['permissions']
    def nonStandardPermissions = permissionMap.keySet().findAll{
		config.getProperty(it, 'false') == 'false' && permissionMap[it] == true
	}.collect {
		textUtils.getText(it)
	}
	// FIXME: i18n
	if (yesNoOrTerminate("The script requests the following permissions that are currently not enabled: ${nonStandardPermissions}." +
		"\nShould they be be added to the standard permissions?")) {
		nonStandardPermissions.each {
			ResourceController.resourceController.setProperty(it, 'true')
		}
	}
}

def scriptDir() {
	File dir = new File(c.userDirectory, 'scripts')
	installationAssert(dir.exists(), null)
	return dir
}

def addOnDir() {
	File dir = new File(c.userDirectory, 'addons')
	installationAssert(dir.exists(), null)
	return dir
}

def createScript(Map configMap) {
	File script = new File(scriptDir(), expandVariables('${name}.groovy', configMap['properties']))
	try {
		script.text = configMap['script']
	}
	catch (Exception e) {
		terminate(e.message)
	}
}

def expandVariables(String string, Map variableMap) {
	// expands strings like "${name}.groovy"
	string.replaceAll(/\$\{([^}]+)\}/, { match, key -> variableMap[key] ? variableMap[key] : '${' + key + '}'})
}

AddOnProperties install() {
	def propertyNames = [
		'properties',
		'description',
		'permissions',
		'translations',
		'preferences.xml',
		'default.properties',
		'script',
		'deinstall',
	]
	Map<String, Proxy.Node> childNodeMap = propertyNames.inject([:]) { map, key ->
		map[key] = c.find{ it.plainText == key }[0]
		return map
	}
	def Map<String, Proxy.Node> missingChildNodes = childNodeMap.findAll{ k,v->
		v == null
	}
	mapStructureAssert( ! missingChildNodes, 'root node misses the following children: ' + missingChildNodes.keySet())

	// parse into configMap
	Map<String, Object> configMap = [:]

	parseProperties(childNodeMap, configMap)
	checkFreeplaneVersion(configMap)
	parseDescription(childNodeMap, configMap)
	parsePermissions(childNodeMap, configMap)
	parseTranslations(childNodeMap, configMap)
	parsePreferencesXml(childNodeMap, configMap)
	parseDefaultProperties(childNodeMap, configMap)
	parseScript(childNodeMap, configMap)
	parseDeinstallationRules(childNodeMap, configMap)
	createScript(configMap)

	def addOn = new ScriptAddOnProperties(configMap['properties']['name'])
	configMap['properties'].each { k,v -> addOn[k] = v	}
	addOn.description = configMap['description']
	addOn.permissions = new ScriptingPermissions(configMap['permissions'] as Properties)
	addOn.translations = configMap['translations']
	addOn.preferencesXml = configMap['preferences.xml']
	addOn.defaultProperties = configMap['default.properties']
	addOn.deinstallationRules = configMap['deinstall']
	new File(addOnDir(), expandVariables('${name}.script.xml', configMap['properties'])).text = addOn.toXmlString()

	return addOn
}

// == main ==
try {
	def addOn = install()
	// FIXME: i18n
	JOptionPane.showMessageDialog(ui.frame, 'Installation successful.\nThe new add-on will be available after a restart.',
		dialogTitle, JOptionPane.INFORMATION_MESSAGE)
	return addOn
} catch (Exception e) {
	JOptionPane.showMessageDialog(ui.frame, e.message, dialogTitle, JOptionPane.ERROR_MESSAGE)
	return null
}