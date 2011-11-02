/* @ExecutionModes({on_single_node="main_menu_scripting/scripts[addons.installer.title]"})
 * 
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Volker Boerchers
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
import java.io.File;
import java.util.zip.ZipInputStream

import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.KeyStroke
import javax.swing.tree.DefaultMutableTreeNode

import org.apache.commons.lang.WordUtils
import org.freeplane.core.resources.ResourceController
import org.freeplane.core.ui.MenuBuilder
import org.freeplane.core.ui.IndexedTree.Node
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.core.util.MenuUtils
import org.freeplane.core.util.TextUtils
import org.freeplane.features.mode.Controller
import org.freeplane.main.addons.AddOnProperties
import org.freeplane.plugin.script.ExecuteScriptAction
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions
import org.freeplane.plugin.script.addons.ScriptAddOnProperties
import org.freeplane.plugin.script.proxy.Proxy

//
// == script bindings (globals) ==
//
dialogTitle = textUtils.getText('addons.installer.title')
installationbase = c.userDirectory

// parse result
configMap = [:]

//
// == methods ==
//

def terminate(String message) {
	throw new Exception(textUtils.getText('addons.installer.canceled') + ': ' + message)
}

def letConfirmOrTerminate(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	if (selection != JOptionPane.OK_OPTION)
		throw new Exception(textUtils.getText('addons.installer.canceled'))
	return true
}

def yesNoOrTerminate(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	if (selection != JOptionPane.OK_OPTION)
		throw new Exception(textUtils.getText('addons.installer.canceled'))
	return selection == JOptionPane.OK_OPTION
}

def mapStructureAssert(check, String issue) {
    if (! check)
        throw new Exception(WordUtils.wrap(textUtils.format('addons.installer.map.structure', issue), 80, null, true))
}

def installationAssert(boolean check, String issue) {
	if (! check)
		throw new Exception(textUtils.format('addons.installer.failed', issue))
}

def parseProperties(Map childNodeMap) {
	def property = 'properties'
	Proxy.Node propertyNode = node.map.root
	configMap[property] = propertyNode.attributes.map.inject([:]){ map, k, v ->
		if (v)
			map[k] = k.startsWith('freeplaneVersion') ? parseFreeplaneVersion(k, v) : v
		return map
	}
	def mandatoryPropertyNames = [
		'name',
		'version',
		'author',
		'freeplaneVersionFrom'
		// optional: 'freeplaneVersionTo'
	]
	def missingProperties = mandatoryPropertyNames.findAll {
		! configMap[property][it]
	}
	mapStructureAssert( ! missingProperties, textUtils.format('addons.installer.missing.properties', missingProperties))
	println property + ': ' + configMap[property]
}

def checkFreeplaneVersion(Map configMap) {
	FreeplaneVersion currentVersion = c.freeplaneVersion
	def versionFrom = configMap['properties']['freeplaneVersionFrom']
	if (currentVersion.isOlderThan(versionFrom))
		terminate(textUtils.format('addons.installer.too.old', currentVersion, versionFrom))
	def versionTo = configMap['properties'].get('freeplaneVersionTo')
	if (versionTo && currentVersion.isNewerThan(versionTo))
		terminate(textUtils.format('addons.installer.too.new', currentVersion, versionTo))
}

def parseFreeplaneVersion(String propertyName, String versionString) {
	try {
		if (versionString)
			return FreeplaneVersion.getVersion(versionString)
		return null
	}
	catch (Exception e) {
		e.printStackTrace()
		mapStructureAssert(false, textUtils.format('addons.installer.freeplaneversion.format.error', propertyName, versionString))
	}
}

def parseDescription(Map childNodeMap) {
	def property = 'description'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = theOnlyChild(propertyNode).text
	println property + ': ' + configMap[property]
}

def parseLicence(Map childNodeMap) {
	def property = 'license'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = theOnlyChild(propertyNode).text
	println property + ': ' + configMap[property]
}

def parseTranslations(Map childNodeMap) {
	def property = 'translations'
	Proxy.Node propertyNode = childNodeMap[property]
	// a Map<locale, Map<key, translation>>

	def translationsMap = propertyNode.children.inject([:]){ map, localeNode ->
		def locale = localeNode.plainText
		map[locale] = localeNode.attributes.map.inject([:]){ localeMap, k, v ->
			localeMap[expandVariables(k)] = v
			return localeMap
		}
		def key = ScriptAddOnProperties.getNameKey(configMap['properties']['name'])
		def expKey = expandVariables(key)
		mapStructureAssert(map[locale][expKey], textUtils.format('addons.installer.missing.translation', key, locale))
		return map
	}
	configMap[property] = translationsMap
	println property + ': ' + configMap[property]
}

def parsePreferencesXml(Map childNodeMap) {
	def property = 'preferences.xml'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.isLeaf() ? null : propertyNode.children[0].text
	println property + ': ' + configMap[property]
}

def parseDefaultProperties(Map childNodeMap) {
	def property = 'default.properties'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.attributes.map.inject([:]){ map, k, v ->
		map[expandVariables(k)] = expandVariables(v)
		return map
	}
	println property + ': ' + configMap[property]
}

def parseZips(Map childNodeMap) {
	def property = 'zips'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.children.collect{ it.plainText }
	File destDir = installationbase
	propertyNode.children.each{ zipNode -> 
		try {
			unpack(destDir, ensureNoHtml(theOnlyChild(zipNode)))
		} catch (Exception e) {
			e.printStackTrace()
			installationAssert(false, e.message);
		}
	}
	println property + ': ' + configMap[property].dump()
}

void unpack(File destDir, Proxy.Node node) {
	byte[] zipData = node.binary
	ZipInputStream result = new ZipInputStream(new ByteArrayInputStream(zipData))
	result.withStream{
		def entry
		while(entry = result.nextEntry){
			if (!entry.isDirectory()){
				def destFile = new File(destDir, entry.name)
				destFile.parentFile?.mkdirs()
				def output = new FileOutputStream(destFile)
				output.withStream{
					int len = 0;
					byte[] buffer = new byte[4096]
					while ((len = result.read(buffer)) > 0){
						output.write(buffer, 0, len);
					}
				}
			}
		}
	}
}

/** ensures that parent has exactly one non-HTML child node. */
Proxy.Node theOnlyChild(Proxy.Node parent) {
	mapStructureAssert(parent.children.size() == 1,
		textUtils.format('addons.installer.one.child.expected', parent.plainText, parent.children.size()))
	return parent.children.first()
}

/** ensures that parent has exactly one non-HTML child node. */
Proxy.Node ensureNoHtml(Proxy.Node first) {
	mapStructureAssert( ! htmlUtils.isHtmlNode(first.text), textUtils.getText('addons.installer.html.script'))
	return first
}

def parseScripts(Map childNodeMap) {
	def property = 'scripts'
	Proxy.Node propertyNode = childNodeMap[property]
	configMap[property] = propertyNode.children.inject([]){ scripts, scriptNode ->
		def script = new ScriptAddOnProperties.Script()
		script.name = expandVariables(scriptNode.plainText)
		script.file = new File(ScriptingEngine.getUserScriptDir(), script.name)
		script.scriptBody = ensureNoHtml(theOnlyChild(scriptNode)).text
		mapStructureAssert( ! htmlUtils.isHtmlNode(script.scriptBody), textUtils.getText('addons.installer.html.script'))
		scriptNode.attributes.map.each { k,v ->
			if (k == 'executionMode')
				script[k] = ScriptAddOnProperties.parseExecutionMode(v)
			else if ( ! k.toString().toLowerCase().startsWith('execute_scripts_'))
				script[k] = expandVariables(v)
		}
		script.permissions = parsePermissions(scriptNode, script.name)
		if (script.keyboardShortcut)
			createKeyboardShortcut(script)
		mapStructureAssert(script.name.endsWith('.groovy'), textUtils.format('addons.installer.groovy.script.name', script.name))
		mapStructureAssert(script.menuTitleKey, textUtils.format('addons.installer.script.no.menutitle', script))
		mapStructureAssert(script.menuLocation, textUtils.format('addons.installer.script.no.menulocation', script))
		mapStructureAssert(script.executionMode, textUtils.format('addons.installer.script.no.execution_mode', script))
		mapStructureAssert(script.permissions, textUtils.format('addons.installer.script.no.permissions', script))
		scripts << script
		return scripts
	}
//	mapStructureAssert(configMap[property], textUtils.getText('addons.installer.no.scripts'))
	println property + ': ' + configMap[property].dump()
}

void createKeyboardShortcut(ScriptAddOnProperties.Script script) {
	// check key syntax
	KeyStroke keyStroke = ui.getKeyStroke(script.keyboardShortcut)
	mapStructureAssert(keyStroke, textUtils.format('addons.installer.invalid.keyboard.shortcut', script.keyboardShortcut))
	String newShortcut = keyStrokeToString(keyStroke)
	// check if key is used (see AccelerateableAction.newAccelerator())
	MenuBuilder menuBuilder = Controller.currentModeController.userInputListenerFactory.menuBuilder
	String menuItemKey = ExecuteScriptAction.makeMenuItemKey(script.menuTitleKey, script.executionMode)
	String shortcutKey = makeAcceleratorKey(menuItemKey)
	String oldShortcut = ResourceController.getResourceController().getProperty(shortcutKey);
	if (oldShortcut && !oldShortcut.equals(newShortcut)
			&& !askForRemoveShortcutViaDialog(script.name, oldShortcut, newShortcut)) {
		// script had been installed before
		return
	}
	else {
		// it's a long way to the menu item title
		DefaultMutableTreeNode menubarNode = menuBuilder.getMenuBar(menuBuilder.get("main_menu_scripting"));
		assert menubarNode != null : "can't find menubar"
		Node priorAssigned = MenuUtils.findAssignedMenuItemNodeRecursively(menubarNode, keyStroke);
		if (priorAssigned != null) {
			if (askForReplaceShortcutViaDialog(((JMenuItem) priorAssigned.getUserObject()).getText())) {
				String priorShortcutKey = menuBuilder.getShortcutKey(priorAssigned.getKey().toString());
				if (priorShortcutKey)
					ResourceController.getResourceController().setProperty(priorShortcutKey, "")
			}
			else {
				return
			}
		}
	}
	println "set keyboardShortcut $shortcutKey to $newShortcut"
	ResourceController.getResourceController().setProperty(shortcutKey, newShortcut)
}

private static String makeAcceleratorKey(String menuItemKey) {
	return 'acceleratorForMindMap/$' + menuItemKey + '$0';
}

private String keyStrokeToString(KeyStroke keyStroke) {
	return keyStroke.toString().replaceAll("pressed |typed ", "").replace("ctrl", "control")
}

private boolean askForRemoveShortcutViaDialog(String scriptName, String oldShortcut, String newShortcut) {
	int replace = JOptionPane.showConfirmDialog(ui.frame,
		TextUtils.format("remove_shortcut_question", scriptName, oldShortcut, newShortcut),
		TextUtils.format("remove_shortcut_title"), JOptionPane.YES_NO_OPTION);
	return replace == JOptionPane.YES_OPTION;
}

private boolean askForReplaceShortcutViaDialog(String oldMenuItemTitle) {
	int replace = JOptionPane.showConfirmDialog(ui.frame,
		TextUtils.format("replace_shortcut_question", oldMenuItemTitle),
		TextUtils.format("replace_shortcut_title"), JOptionPane.YES_NO_OPTION);
	return replace == JOptionPane.YES_OPTION;
}

ScriptingPermissions parsePermissions(Proxy.Node propertyNode, String scriptName) {
	def permissionNames = ScriptingPermissions.permissionNames.findAll { it.startsWith('execute_') }
	def missingPermissions = permissionNames.findAll{ !propertyNode[it] }
	mapStructureAssert( ! missingPermissions, textUtils.format('addons.installer.missing.permission.attribute', scriptName, missingPermissions))
	def permissions = propertyNode.attributes.map.findAll { k,v -> permissionNames.contains(k) }
	return new ScriptingPermissions(permissions as Properties)
}

// a list of [action, file] pairs
def parseDeinstallationRules(Map childNodeMap) {
	def property = 'deinstall'
	Proxy.Node propertyNode = childNodeMap[property]
	def attribs = propertyNode.attributes
	// we can't use a simple map since most entries have the same key -> iterate over index
	configMap[property] = (0..attribs.size()-1).collect {
		// the right type for AddOnProperties
		[attribs.getKey(it), expandVariables(attribs.get(it)).trim()] as String[]
	}
	def knownDeinstallationRules = [
		'delete'
	]
	def unknownDeinstallationRules = attribs.names.findAll{ k -> ! knownDeinstallationRules.contains(k) }
	mapStructureAssert( ! unknownDeinstallationRules, textUtils.format('addons.installer.unknown.deinstallation.rules', unknownDeinstallationRules))
	println property + ': ' + configMap[property]
}

def handlePermissions(Map configMap) {
	def permissionMap = configMap['permissions']
    def nonStandardPermissions = permissionMap.keySet().findAll{
		config.getProperty(it, 'false') == 'false' && permissionMap[it] == true
	}.collect {
		textUtils.getText(it)
	}
	if (yesNoOrTerminate(textUtils.format('addons.installer.nonstandard.permissions', nonStandardPermissions))) {
		nonStandardPermissions.each {
			ResourceController.resourceController.setProperty(it, 'true')
		}
	}
}

def scriptDir() {
	File dir = new File(installationbase, 'scripts')
	installationAssert(dir.exists(), null)
	return dir
}

def addOnDir() {
	File dir = new File(installationbase, 'addons')
	installationAssert(dir.exists(), null)
	return dir
}

def createScripts(Map configMap) {
	List<ScriptAddOnProperties.Script> scripts = configMap['scripts']
	scripts.each { script -> 
		File file = script.file
		try {
			file.text = script.scriptBody
		}
		catch (Exception e) {
			terminate(e.message)
		}
	}
}

def expandVariables(String string) {
	Map variableMap = configMap['properties']
	// expands strings like "${name}.groovy"
	string.replaceAll(/\$\{([^}]+)\}/, { match, key -> variableMap[key] ? variableMap[key] : match })
}

AddOnProperties install() {
	def propertyNames = [
		'description',
		'license',
		'translations',
		'preferences.xml',
		'default.properties',
		'scripts',
		'zips',
		'deinstall',
	]
	Map<String, Proxy.Node> childNodeMap = propertyNames.inject([:]) { map, key ->
		map[key] = node.find{ it.plainText == key }[0]
		return map
	}
	def Map<String, Proxy.Node> missingChildNodes = childNodeMap.findAll{ k,v->
		v == null
	}
	mapStructureAssert( ! missingChildNodes, textUtils.format('addons.installer.missing.child.nodes', missingChildNodes.keySet()))

	parseProperties(childNodeMap)
	checkFreeplaneVersion(configMap)
	parseDescription(childNodeMap)
	parseLicence(childNodeMap)
	parseTranslations(childNodeMap)
	parsePreferencesXml(childNodeMap)
	parseDefaultProperties(childNodeMap)
	parseScripts(childNodeMap)
	parseZips(childNodeMap)
	parseDeinstallationRules(childNodeMap)
	createScripts(configMap)

	def addOn = new ScriptAddOnProperties(configMap['properties']['name'])
	configMap['properties'].each { k,v ->
		if (addOn.hasProperty(k))
			addOn[k] = v
		else
			logger.warn("add-on has no property $k (hopefully that's not bad)")
	}
	addOn.description = configMap['description']
	addOn.license = configMap['license']
	addOn.translations = configMap['translations']
	addOn.preferencesXml = configMap['preferences.xml']
	addOn.defaultProperties = configMap['default.properties']
	addOn.deinstallationRules = configMap['deinstall']
	addOn.scripts = configMap['scripts']
	new File(addOnDir(), expandVariables('${name}.script.xml')).text = addOn.toXmlString()

	return addOn
}

// == main ==
try {
	def addOn = install()
	JOptionPane.showMessageDialog(ui.frame, textUtils.getText('addons.installer.success'),
		dialogTitle, JOptionPane.INFORMATION_MESSAGE)
	return addOn
} catch (Exception e) {
	JOptionPane.showMessageDialog(ui.frame, e.message, dialogTitle, JOptionPane.ERROR_MESSAGE)
	logger.warn("installation failure", e)
	return null
}