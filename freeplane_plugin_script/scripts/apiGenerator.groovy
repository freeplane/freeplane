// @ExecutionModes({on_single_node="/menu_bar/help[scripting_api_generator_title]"})
// Copyright (C) 2009-2011 Dave (Dke211, initial author), Volker Boerchers (adaptation for Freeplane)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.

import java.lang.reflect.Method

import org.freeplane.api.Script

import org.freeplane.core.resources.ResourceController
import org.freeplane.core.ui.components.UITools
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.core.util.HtmlUtils
import org.freeplane.core.util.LogUtils
import org.freeplane.core.util.TextUtils
import org.freeplane.plugin.script.FreeplaneScriptBaseClass
import org.freeplane.plugin.script.proxy.Convertible
import org.freeplane.plugin.script.proxy.Proxy
import org.freeplane.plugin.script.proxy.ScriptUtils

URI getApiLink(String path) {
    try {
        def apiBase = path.startsWith('org/freeplane') ? freeplaneApiBase
                : 'http://groovy.codehaus.org/groovy-jdk'
        return new URI(apiBase + '/' + path)
    } catch (Exception e) {
        logger.severe("could not create link for class ${path}", e)
        return null
    }
}

URI getApiLink(Class clazz) {
    if (clazz == void.class)
        return null
    else if (clazz.isArray())
        clazz = List.class // that's a useful approximation in Groovy
    else if (clazz.isPrimitive())
        clazz = wrap(clazz)
    return getApiLink(clazz.name.replace('.', '/').replace('$', '.') + '.html')
}

// see http://stackoverflow.com/questions/1704634/simple-way-to-get-wrapper-class-type-in-java
private static <T> Class<T> wrap(Class<T> clazz) {
    if (clazz == boolean.class) return Boolean.class
    if (clazz == byte.class) return Byte.class
    if (clazz == char.class) return Character.class
    if (clazz == double.class) return Double.class
    if (clazz == float.class) return Float.class
    if (clazz == int.class) return Integer.class
    if (clazz == long.class) return Long.class
    if (clazz == short.class) return Short.class
    if (clazz == void.class) return Void.class
    return clazz
}

def makeApi(Proxy.Node node, Class clazz) {
    def classNode = node.createChild(typeToString(clazz))
    TreeMap<String, Map<String, Object>> memberMap = new TreeMap<String, Map<String, Object>>()
    classNode.link.uri = getApiLink(clazz)
    classNode.style.font.bold = true
    clazz.getMethods().findAll {
        it.declaringClass == clazz || it.declaringClass.simpleName.endsWith('RO') ||
		 it.declaringClass.getPackage().name == org.freeplane.api.Node.class.getPackage().name
    }.each {
        if (!addProperty(memberMap, it))
            addMethod(memberMap, it);
    }
    classNode.createChild('Package: ' + clazz.getPackage().name)
    classNode.folded = true
    memberMap.each { k,v ->
        createMemberNode(k, v, classNode)
    }
    // input for freeplane_plugin_script/src-jsyntaxpane/META-INF/services/jsyntaxpane/syntaxkits/groovysyntaxkit/combocompletions.txt
    boolean printCompletionList = false
    if (printCompletionList && classNode.to.plain == 'Node')
        printCompletions(memberMap)
}

def printCompletions(TreeMap<String, Map<String, Object>> memberMap) {
    TreeSet completions = new TreeSet()
    completions.addAll(['logger', 'ui', 'htmlUtils', 'textUtils', 'node', 'import', 'def', 'String'])
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

def createMemberNode(String memberName, Map<String, Object> attribs, Proxy.Node classNode) {
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
        memberNode = classNode.createChild(formatProperty(memberName, formatReturnType(type), mode))
        memberNode.icons.add('wizard')
        [ 'method_read', 'method_write' ].each {
            if (attribs[it]) {
                Proxy.Node methodNode = memberNode.createChild(formatMethod(attribs[it]))
                methodNode.icons.add('bookmark')
            }
        }
    }
    return memberNode
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
        propertyMap['method_read'] = method
        propertyMap['return_type'] = method.returnType
    }
    else if (isSetter(method) && method.parameterTypes.size() == 1) {
        def propertyMap = getOrCreatePropertiesMap(memberMap, getPropertyName(method))
        propertyMap['write'] = true
        propertyMap['type_write'] = method.parameterTypes[0]
        propertyMap['method_write'] = method
        propertyMap['return_type'] = method.returnType
    }
}

def addMethod(Map<String, Map<String, Object>> memberMap, Method method) {
    def propertyMap = getOrCreatePropertiesMap(memberMap, formatMethodKey(method))
    propertyMap['types'] = method.parameterTypes
    propertyMap['method'] = formatMethod(method)
    propertyMap['return_type'] = method.returnType
}

def formatProperty(String property, String type, String mode) {
    return "<html><body><b>${property}</b>: ${type} (${mode})"
    // Plain text:
    //	return "${property}: ${type} (${mode})"
}

def formatMethodKey(Method method) {
		return method.name +
			'(' + method.parameterTypes.collect{ typeToString(it) }.join(', ') + ')'

}

def formatParameterType(Class clazz) {
    def uri = getApiLink(clazz)
    if (uri)
        "<a href='${uri.toURL()}'>${typeToString(clazz)}</a>"
}


def formatParameter(parameter) {
	def parameterType = formatParameterType(parameter.type)
	if(parameterType)
		parameterType + ' ' + parameter.name;
	else
		parameter.name;
}

def formatReturnType(Class clazz) {
	def parameterType = formatParameterType(clazz)
	if(parameterType)
		parameterType;
	else
		clazz.simpleName;
}

def formatMethod(Method method) {
	def parameters =  method.metaClass.respondsTo(method, "getParameters") ? method.getParameters().collect{ formatParameter(it) } : method.parameterTypes.collect{ formatParameterType(it) }
    return '<html><body>' + formatReturnType(method.returnType) +
    ' <b>' + method.name + '</b>' +
    '(' + parameters.join(', ') + ')'
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
this.freeplaneApiBase = new File(ResourceController.resourceController.installationBaseDir).toURI().toString() + 'doc/api';
def MAP_NAME = textUtils.getText('scripting_api_generator_title')
def PROXY_NODE = textUtils.getText('scripting_api_generator_proxy')
def UTILITES_NODE = textUtils.getText('scripting_api_generator_utilities')
def WEB_NODE = textUtils.getText('scripting_api_generator_web')
def LEGEND_NODE = textUtils.getText('scripting_api_generator_legend')
c.deactivateUndo()
def resourceBaseDir = ResourceController.resourceController.resourceBaseDir;
def allUserTemplates = new File(resourceBaseDir, 'templates');
def defaultTemplate = new File(allUserTemplates, 'standard.mm')
Proxy.Map newMap = defaultTemplate.canRead() ? c.newMapFromTemplate(defaultTemplate) : c.newMap()
def oldName = newMap.name
newMap.name = MAP_NAME
newMap.root.text = MAP_NAME
newMap.root.style.font.bold = true
newMap.root.link.uri = getApiLink('index.html')
initHeading(newMap.root)


// Proxy
def proxy = createChild(newMap.root, PROXY_NODE, getApiLink(Proxy.class))
initHeading(proxy)
makeApi(proxy, Proxy.Attributes.class)
makeApi(proxy, Proxy.Cloud.class)
makeApi(proxy, Proxy.Connector.class)
makeApi(proxy, Proxy.Controller.class)
makeApi(proxy, Proxy.Loader.class)
makeApi(proxy, Script.class)
makeApi(proxy, Proxy.Edge.class)
makeApi(proxy, Proxy.ExternalObject.class)
makeApi(proxy, Proxy.Font.class)
makeApi(proxy, Proxy.Icons.class)
makeApi(proxy, Proxy.Link.class)
makeApi(proxy, Proxy.Map.class)
makeApi(proxy, Proxy.Node.class)
makeApi(proxy, Proxy.DependencyLookup.class)
makeApi(proxy, org.freeplane.api.Dependencies.class)
makeApi(proxy, Proxy.NodeStyle.class)
makeApi(proxy, Convertible.class)

def utils = createChild(newMap.root, UTILITES_NODE, null)
initHeading(utils)
makeApi(utils, FreeplaneScriptBaseClass.class)
makeApi(proxy, ScriptUtils.class)
makeApi(utils, UITools.class)
makeApi(utils, TextUtils.class)
makeApi(utils, FreeplaneVersion.class)
makeApi(utils, HtmlUtils.class)
makeApi(utils, LogUtils.class)

def web = createChild(newMap.root, WEB_NODE, 'http://freeplane.sourceforge.net/wiki/index.php/Scripting')
initHeading(web)
createChild(web, 'Groovy tutorials (Codehaus)', 'http://groovy.codehaus.org/Beginners+Tutorial')
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
