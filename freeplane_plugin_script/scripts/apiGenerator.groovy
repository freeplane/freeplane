// @ExecutionModes({on_single_node="/menu_bar/help[scripting_api_generator_title]"})
// Copyright (C) 2009-2011 Dave (Dke211, initial author), Volker Boerchers (adaptation for Freeplane)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.

import java.lang.reflect.Method
import org.freeplane.plugin.script.proxy.Proxy
import org.freeplane.plugin.script.proxy.Convertible


def makeApi(Proxy.Node node, Class clazz, String apiBase) {
	def classNode = node.createChild(typeToString(clazz))
	TreeMap<String, Map<String, Object>> memberMap = new TreeMap<String, Map<String, Object>>()
	classNode.link.text = getApiLink(apiBase, clazz)
	classNode.style.font.bold = true
	clazz.getMethods().findAll {
		it.declaringClass == clazz || it.declaringClass.simpleName.endsWith('RO')
	}.sort {
		a,b -> b.name <=> a.name
	}.each {
		if (!addProperty(memberMap, it))
			addMethod(memberMap, it);
	}
	classNode.createChild('Package: ' + clazz.getPackage().name)
	classNode.folded = true
	memberMap.each { k,v ->
		createMemberNode(k, v, classNode, apiBase)
	}
	// input for freeplane_plugin_script/src-jsyntaxpane/META-INF/services/jsyntaxpane/syntaxkits/groovysyntaxkit/combocompletions.txt
	boolean printCompletionList = false
	if (printCompletionList && classNode.to.plain == 'Node')
		printCompletions(memberMap)
}

def printCompletions(TreeMap<String, Map<String, Object>> memberMap) {
	TreeSet completions = new TreeSet()
	completions.addAll(['logger', 'ui', 'htmlUtils', 'textUtils', 'node', 'import', 'def', 'String'])
	completions.addAll(['single_node', 'selected_node', 'selected_node_recursively'].collect{ "// @ExecutionModes($it)" })
	
	memberMap.each { memberName,attribs ->
		if (attribs['method'])
			completions << memberName + '(|)'
		else {
			completions << memberName
			if (attribs['type_read'] && Collection.class.isAssignableFrom(attribs['type_read'])) {
				completions << memberName + '.each{ | }'
				completions << memberName + '.collect{ | }'
				completions << memberName + '.sum(|){  }'
			}
		}
	}
	println completions.join("\n")
}

def createMemberNode(String memberName, Map<String, Object> attribs, Proxy.Node classNode, String apiBase) {
	Proxy.Node memberNode
	if (attribs['method']) {
		memberNode = classNode.createChild(attribs['method'])
		memberNode.icons.add('bookmark')
	}
	else {
		// property
		def mode = (attribs['type_read'] ? 'r' : '') + (attribs['type_write'] ? 'w' : '')
		def type = attribs['type_read'] ? attribs['type_read'] : attribs['type_write']
		//	if (mode == 'rw' && attribs['type_read'] != attribs['type_write']) {
		//		logger.severe("property ${memberName} has differing getter and setter types")
		//	}
		memberNode = classNode.createChild(formatProperty(memberName, typeToString(type), mode))
		memberNode.icons.add('wizard')
		[ 'method_read', 'method_write' ].each {
			if (attribs[it]) {
				memberNode.createChild(formatMethod(attribs[it])).icons.add('bookmark')
			}
		}
	}
	attribs['types'].each {
		if (it.declaringClass == Proxy.class || it == Convertible.class) {
			def typeNode = memberNode.createChild(typeToString(it))
			typeNode.link.text = getApiLink(apiBase, it)
		}
	}
	memberNode.folded = true
	return memberNode
}

def getApiLink(String apiBase, Class clazz) {
	def path = clazz.name.replace('.', '/').replace('$', '.')
	return apiBase + '/' + path + '.html'
}

def typeToString(Class clazz) {
	return clazz.simpleName.replace('Proxy$', '')
}

// returns a value if this method is a getter or setter otherwise it returns null
def addProperty(Map<String, Map<String, Object>> memberMap, Method method) {
	if (isGetter(method) && ! method.parameterTypes) {
		def propertyMap = getOrCreatePropertiesMap(memberMap, getPropertyName(method))
		propertyMap['read'] = true
		propertyMap['type_read'] = method.returnType
		propertyMap['types'] = [ method.returnType ]
		propertyMap['method_read'] = method
	}
	else if (isSetter(method) && method.parameterTypes.size() == 1) {
		def propertyMap = getOrCreatePropertiesMap(memberMap, getPropertyName(method))
		propertyMap['write'] = true
		propertyMap['type_write'] = method.parameterTypes[0]
		propertyMap['types'] = [ method.returnType ]
		propertyMap['method_write'] = method
	}
}

def addMethod(Map<String, Map<String, Object>> memberMap, Method method) {
	def propertyMap = getOrCreatePropertiesMap(memberMap, method.name)
	propertyMap['types'] = method.parameterTypes
	propertyMap['method'] = formatMethod(method)
}

def formatProperty(String property, String type, String mode) {
	return "<html><body><b>${property}</b>: ${type} (${mode})"
// Plain text:
//	return "${property}: ${type} (${mode})"
}

def formatMethod(Method method) {
	return '<html><body>' + typeToString(method.returnType) +
		' <b>' + method.name + '</b>' +
		'(' + method.parameterTypes.collect{ typeToString(it) }.join(', ') + ')'
// Plain text:
//	return typeToString(method.returnType) +
//		' ' + method.name +
//		'(' + method.parameterTypes.collect{ typeToString(it) }.join(', ') + ')'
}

def isGetter(Method method) {
	return method.name =~ '^(?:[gs]et|is)[A-Z].*'
}

def isSetter(Method method) {
	return method.name =~ '^set[A-Z].*'
}

/** returns null if this is not a proper bean method name (get/set/is). */
def getPropertyName(Method method) {
	def name = method.name.replaceFirst('^(?:[gs]et|is)([A-Z])', '$1')
	if (name != method.name)
		return name.substring(0, 1).toLowerCase() + name.substring(1)
	else
		return null
}

private Map getOrCreatePropertiesMap(Map properties, String name) {
	def propertyMap = properties[name]
	if (propertyMap == null) {
		propertyMap = [:]
		properties[name] = propertyMap
	}
	return propertyMap
}

def initHeading(Proxy.Node node) {
	node.style.font.bold = true
}

def createChild(Proxy.Node parent, text, link) {
	def result = parent.createChild(text)
	result.link.text = link
	return result
}

// == MAIN ==
def MAP_NAME = textUtils.getText('scripting_api_generator_title')
def PROXY_NODE = textUtils.getText('scripting_api_generator_proxy')
def UTILITES_NODE = textUtils.getText('scripting_api_generator_utilities')
def WEB_NODE = textUtils.getText('scripting_api_generator_web')
def LEGEND_NODE = textUtils.getText('scripting_api_generator_legend')
c.deactivateUndo()
// FIXME: api is installed locally but is there a portable way to find it?
def apiBase = 'http://freeplane.sourceforge.net/doc/api'
Proxy.Map newMap = c.newMap()
def oldName = newMap.name
newMap.name = MAP_NAME
newMap.root.text = MAP_NAME
newMap.root.style.font.bold = true
newMap.root.link.text = apiBase + '/index.html'
initHeading(newMap.root)


// Proxy
def proxy = createChild(newMap.root, PROXY_NODE, apiBase + '/org/freeplane/plugin/script/proxy/Proxy.html')
initHeading(proxy)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Attributes'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Connector'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Controller'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Edge'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$ExternalObject'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Font'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Icons'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Link'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Map'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$Node'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Proxy$NodeStyle'), apiBase)
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.Convertible'), apiBase)

def utils = createChild(newMap.root, UTILITES_NODE, null)
initHeading(utils)
makeApi(utils, Class.forName('org.freeplane.plugin.script.FreeplaneScriptBaseClass'), apiBase)
makeApi(utils, Class.forName('org.freeplane.core.ui.components.UITools'), apiBase)
makeApi(utils, Class.forName('org.freeplane.core.util.TextUtils'), apiBase)
makeApi(utils, Class.forName('org.freeplane.core.util.FreeplaneVersion'), apiBase)
makeApi(utils, Class.forName('org.freeplane.core.util.HtmlUtils'), apiBase)
makeApi(utils, Class.forName('org.freeplane.core.util.LogUtils'), apiBase)

def web = createChild(newMap.root, WEB_NODE, 'http://freeplane.sourceforge.net/wiki/index.php/Scripting')
initHeading(web)
createChild(web, 'Groovy tutorials (Codehaus)', 'http://groovy.codehaus.org/Beginners+Tutorial')
createChild(web, 'Groovy presentation (Paul King)', 'http://www.asert.com/pubs/Groovy/Groovy.pdf')
createChild(web, 'Example scripts', 'http://freeplane.sourceforge.net/wiki/index.php/Scripting:_Example_scripts')
createChild(web, 'Scripting API changes', 'http://freeplane.sourceforge.net/wiki/index.php/Scripting:_API_Changes')

def legend = newMap.root.createChild(LEGEND_NODE)
initHeading(legend)
def methodLegend = legend.createChild("normal methods have a 'bookmark' icon")
methodLegend.icons.add('bookmark')
methodLegend.folded = true
def propertyLegend = legend.createChild("Groovy properties have a 'wizard' icon")
propertyLegend.icons.add('wizard')
propertyLegend.folded = true
def propertyBasics = propertyLegend.createChild("With properties you can write simpler expressions than with getters and setters")
propertyBasics.createChild("  node.text = 'Hello, world!'")
propertyBasics.createChild("instead of")
propertyBasics.createChild("  node.setText('Hello, world!')")
propertyLegend.createChild("read-only properties are indicated by a trailing (r)").
	createChild('if (node.leaf)\n    println "the id of this leaf node is " + node.id')
propertyLegend.createChild("write-only and read-write properties are indicated by a trailing (w) or (rw)").
	createChild('node.text += " some suffix"')
propertyLegend.createChild("properties with differing type of setter and getter have two nodes")
legend.folded = true

c.deactivateUndo()
newMap.saved = true
