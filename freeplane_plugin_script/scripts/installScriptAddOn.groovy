// @ExecutionModes({on_single_node="main_menu_scripting/scripts[addons.installer.title]"})
// Copyright (C) 2011 Volker Boerchers
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.

import groovy.swing.SwingBuilder

import java.awt.Component;
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Toolkit
import java.util.zip.ZipInputStream

import javax.swing.BoxLayout
import javax.swing.JDialog;
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.KeyStroke
import javax.swing.tree.DefaultMutableTreeNode

import org.apache.commons.lang.WordUtils
import org.freeplane.core.resources.ResourceController
import org.freeplane.core.ui.MenuBuilder
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.core.util.MenuUtils
import org.freeplane.core.util.TextUtils
import org.freeplane.features.mode.Controller
import org.freeplane.main.addons.AddOnProperties
import org.freeplane.main.addons.AddOnsController
import org.freeplane.plugin.script.ExecuteScriptAction
import org.freeplane.plugin.script.ScriptingEngine
import org.freeplane.plugin.script.ScriptingPermissions
import org.freeplane.plugin.script.addons.AddOnDetailsPanel
import org.freeplane.plugin.script.addons.ScriptAddOnProperties
import org.freeplane.plugin.script.proxy.Proxy

//
// == script bindings (globals) ==
//
dialogTitle = textUtils.getText('addons.installer.title')
installationbase = c.userDirectory
addonsUrl = "http://freeplane.sourceforge.net/addons"

// parse result
configMap = [:]

//
// == methods ==
//

def terminate(String message) {
	throw new Exception(textUtils.getText('addons.installer.canceled') + ': ' + message)
}

def confirm(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	return selection == JOptionPane.OK_OPTION
}

boolean yesNoOrTerminate(String question) {
	final int selection = JOptionPane.showConfirmDialog(ui.frame, question, dialogTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	if (selection != JOptionPane.YES_OPTION)
		throw new Exception(textUtils.getText('addons.installer.canceled'))
	return selection == JOptionPane.YES_OPTION
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
	configMap[property]['title'] = propertyNode.plainText
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
	configMap[property]['homepage'] = propertyNode.link.text ?
		propertyNode.link.uri.toURL() : new URL(expandVariables(addonsUrl + '/${name}'))
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
			return FreeplaneVersion.getVersion(versionString.replaceFirst('^v', ''))
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
	configMap[property] = propertyNode.children.collect{ ensureNoHtml(theOnlyChild(it)).binary }
	println property + ': ' + configMap[property].dump()
}

def parseImages(Map childNodeMap) {
    def property = 'images'
    Proxy.Node propertyNode = childNodeMap[property]
    if (!propertyNode)
        return
    configMap[property] = propertyNode.children.inject([:]){ map, child ->
        map[child.plainText] = ensureNoHtml(theOnlyChild(child)).binary
        return map
    }
    println property + ': ' + configMap[property].dump()
}

def installZips() {
	File destDir = installationbase
	configMap['zips'].each{ zipData ->
		try {
			unpack(destDir, zipData)
		} catch (Exception e) {
			e.printStackTrace()
			installationAssert(false, e.message);
		}
	}
}

def installImages() {
    File destDir = new File(installationbase, 'resources/images')
    destDir.mkdirs()
    configMap['images'].each{ filename, imageData ->
        try {
            new File(destDir, expandVariables(filename)).bytes = imageData
        } catch (Exception e) {
            e.printStackTrace()
            installationAssert(false, e.message);
        }
    }
}

void unpack(File destDir, byte[] zipData) {
    mapStructureAssert(zipData, textUtils.getText('addons.installer.no.zipdata'))
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
	def newShortcut = script.keyboardShortcut
    // check key syntax
	KeyStroke newKeyStroke = ui.getKeyStroke(newShortcut)
	mapStructureAssert(newKeyStroke, textUtils.format('addons.installer.invalid.keyboard.shortcut', newShortcut))
	// check if key is used (see AccelerateableAction.newAccelerator())
	String menuItemKey = ExecuteScriptAction.makeMenuItemKey(script.menuTitleKey, script.executionMode)
	String shortcutKey = MenuUtils.makeAcceleratorKey(menuItemKey)
	String oldShortcut = ResourceController.getResourceController().getProperty(shortcutKey);
	if (oldShortcut) {
	    // script had been installed before
        if (oldShortcut.equals(newShortcut) || !askIfNewFunctionWasAssignedToAnotherShortcut(oldShortcut))
            return
        // FIXME: improved message would be:
        //    insertInlineImage.groovy currently is assigned the shortcut xy\nReplace this assignment by yz?
	}
	else {
	    MenuBuilder menuBuilder = Controller.currentModeController.userInputListenerFactory.menuBuilder
		// it's a long way to the menu item title
		DefaultMutableTreeNode menubarNode = menuBuilder.getMenuBar(menuBuilder.get("main_menu_scripting"));
		assert menubarNode != null : "can't find menubar"
		def priorAssigned = MenuUtils.findAssignedMenuItemNodeRecursively(menubarNode, newKeyStroke);
		if (priorAssigned != null && !priorAssigned.getKey().equals(menuItemKey)) {
			if (askIfNewShortcutWasAssignedToAnotherFunction(((JMenuItem) priorAssigned.getUserObject()).getText())) {
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

String keyStrokeToString(KeyStroke keyStroke) {
    return keyStroke.toString().replaceFirst("pressed ", "");
}

private boolean askIfNewShortcutWasAssignedToAnotherFunction(String currentAssignee) {
	int replace = JOptionPane.showConfirmDialog(ui.frame,
		TextUtils.format("replace_shortcut_question", currentAssignee),
		TextUtils.getText("replace_shortcut_title"), JOptionPane.YES_NO_OPTION);
	return replace == JOptionPane.YES_OPTION;
}

private boolean askIfNewFunctionWasAssignedToAnotherShortcut(String oldShortcut) {
    // this is a irritating dialog but we can't change it before the 1.2 release
    int replace = JOptionPane.showConfirmDialog(ui.frame, oldShortcut,
        TextUtils.getText("remove_shortcut_question"), JOptionPane.YES_NO_OPTION);
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

def handlePermissions() {
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

def createScripts() {
	List<ScriptAddOnProperties.Script> scripts = configMap['scripts']
	scripts.each { script -> 
		File file = script.file
		try {
			file.text = script.scriptBody
		}
		catch (Exception e) {
			terminate(e.message)
		}
		if (script.keyboardShortcut)
			createKeyboardShortcut(script)
	}
}

def expandVariables(String string) {
	Map variableMap = configMap['properties']
	// expands strings like "${name}.groovy"
	string.replaceAll(/\$\{([^}]+)\}/, { match, key -> variableMap[key] ? variableMap[key] : match })
}

AddOnProperties parse() {
	def propertyNames = [
		'description',
		'license',
		'translations',
		'preferences.xml',
		'default.properties',
		'scripts',
		'zips',
		'deinstall',
		'images',
	]
	Map<String, Proxy.Node> childNodeMap = propertyNames.inject([:]) { map, key ->
		map[key] = node.map.root.find{ it.plainText == key }[0]
		return map
	}
	def Map<String, Proxy.Node> missingChildNodes = childNodeMap.findAll{ k,v->
		v == null
	}
    // note: images came after the first beta
    missingChildNodes.remove('images')
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
	parseImages(childNodeMap)
	parseDeinstallationRules(childNodeMap)

	def addOn = new ScriptAddOnProperties(configMap['properties']['name'])
	configMap['properties'].each { k,v ->
		if (addOn.hasProperty(k))
			addOn[k] = v
		else if (k != "title")
			logger.warn("add-on has no property $k (hopefully that's not bad)")
	}
	addOn.description = configMap['description']
	addOn.license = configMap['license']
	addOn.translations = configMap['translations']
	addOn.preferencesXml = configMap['preferences.xml']
	addOn.defaultProperties = configMap['default.properties']
	addOn.deinstallationRules = configMap['deinstall']
    addOn.images = configMap['images'] ? configMap['images'].keySet() : []
	addOn.scripts = configMap['scripts']

	return addOn
}

boolean confirmInstall(ScriptAddOnProperties addOn, ScriptAddOnProperties installedAddOn) {
	def screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	def dialogPrefSize = new Dimension((int) screenSize.getWidth() * 3 / 5, (int) screenSize.getHeight() * 1 / 2);
	def warning = textUtils.removeTranslateComment(textUtils.getText('addons.installer.warning'))
	def addOnDetailsPanel = new AddOnDetailsPanel(addOn, warning)
	addOnDetailsPanel.maxWidth = 600
	def installButtonText = installedAddOn ? textUtils.format('addons.installer.update', installedAddOn.version)
		: textUtils.getText('addons.installer.install')

	def s = new SwingBuilder()
	s.setVariable('myDialog-properties',[:])
	def vars = s.variables
	def dial = s.dialog(title:dialogTitle, id:'myDialog', modal:true,
						locationRelativeTo:ui.frame, owner:ui.frame, pack:true, preferredSize:dialogPrefSize) {
		scrollPane() {
			panel() {
				boxLayout(axis:BoxLayout.Y_AXIS)
				widget(addOnDetailsPanel)
				panel(alignmentX:0f) {
					flowLayout(alignment:FlowLayout.RIGHT)
					button(action: action(name: textUtils.getText('cancel'), mnemonic: 'C', closure: {dispose()}))
					defaultButton = button(id:'defBtn', action: action(name: installButtonText,
						mnemonic: 'I', defaultButton:true, selected:true, closure: {vars.ok = true; dispose()}))
				}
			}
		}
	}
	defaultButton.requestFocusInWindow()
    ui.addEscapeActionToDialog(dial)
    ui.setDialogLocationRelativeTo(dial, ui.frame)
    dial.visible = true
	if (!vars.ok)
		return false
	// 2. license
	boolean licenseUnchanged = addOn.license && installedAddOn?.license && addOn.license.equals(installedAddOn.license)
	def license = addOn.license.replaceAll('</?(html|body|head)>', '').trim()
	def question = textUtils.removeTranslateComment(textUtils.format('addons.installer.confirm.licence', license)).replace("\n", "<p>")
	if (licenseUnchanged)
		c.statusInfo = textUtils.getText('addons.installer.licence.unchanged')
	if (addOn.license && !licenseUnchanged && !confirm(question))
		return false
    // really bother the user with such details?
	// 3. permissions
	//	handlePermissions()
	return true
}

def install(AddOnProperties addOn) {
	createScripts()
	installZips()
	installImages()
	new File(addOnDir(), expandVariables('${name}.script.xml')).text = addOn.toXmlString()
}

// == main ==
try {
	def addOn = parse()
	AddOnsController.registerAddOnResources(addOn, ResourceController.resourceController)
	def installedAddOn = AddOnsController.getController().getInstalledAddOn(addOn.name)
	def isUpdate = installedAddOn != null
	if (confirmInstall(addOn, installedAddOn)) {
		def message
		if (isUpdate) {
			AddOnsController.getController().deinstall(installedAddOn)
			message = textUtils.format('addons.installer.success.update', installedAddOn.version, addOn.version)
		}
		else {
			message = textUtils.getText('addons.installer.success')
		}
		install(addOn)
		JOptionPane.showMessageDialog(ui.frame, message, dialogTitle, JOptionPane.INFORMATION_MESSAGE)
		return addOn
	}
	return null
} catch (Exception e) {
	JOptionPane.showMessageDialog(ui.frame, e.message, dialogTitle, JOptionPane.ERROR_MESSAGE)
	logger.warn("installation failure", e)
	return null
}