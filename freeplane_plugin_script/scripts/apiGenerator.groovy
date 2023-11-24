// @ExecutionModes({on_single_node="/menu_bar/help[scripting_api_generator_title]"})
// Copyright (C) 2009-2011 Dave (Dke211, initial author), Volker Boerchers (adaptation for Freeplane), edofro
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.

import java.lang.reflect.Method
import java.lang.reflect.Field

import org.freeplane.api.Dependencies
import org.freeplane.api.LengthUnit
import org.freeplane.api.NodeChangeListener
import org.freeplane.api.NodeChanged
import org.freeplane.api.NodeCondition
import org.freeplane.api.NodeShape
import org.freeplane.api.Side
import org.freeplane.api.Quantity
import org.freeplane.core.util.MenuUtils
import org.freeplane.features.cloud.CloudShape
import org.freeplane.features.edge.EdgeStyle
import org.freeplane.features.link.ConnectorShape

import org.freeplane.api.PhysicalUnit
import org.freeplane.api.Dependencies.Element
import org.freeplane.api.ConversionException
import org.freeplane.api.NodeNotFoundException

import org.freeplane.api.AttributeCondition
import org.freeplane.api.AttributeValueSerializer
import org.freeplane.api.Border
import org.freeplane.api.ChildNodesAlignment
import org.freeplane.api.ChildNodesLayout
import org.freeplane.api.ChildrenSides
import org.freeplane.api.ConditionalStyle
import org.freeplane.api.ConditionalStyleNotFoundException
import org.freeplane.api.ConditionalStyles
import org.freeplane.api.Dash
import org.freeplane.api.FreeplaneVersion
import org.freeplane.api.HeadlessLoader
import org.freeplane.api.HeadlessMapCreator
import org.freeplane.api.HorizontalTextAlignment
import org.freeplane.api.LayoutOrientation
import org.freeplane.api.NodeToComparableMapper
import org.freeplane.api.TextWritingDirection


import org.freeplane.api.Script

import org.freeplane.core.resources.ResourceController
import org.freeplane.core.ui.components.UITools
import org.freeplane.core.util.FreeplaneVersion
import org.freeplane.core.util.HtmlUtils
import org.freeplane.core.util.LogUtils
import org.freeplane.core.util.TextUtils
import org.freeplane.launcher.Launcher
import org.freeplane.plugin.script.FreeplaneScriptBaseClass
import org.freeplane.plugin.script.proxy.Convertible
import org.freeplane.plugin.script.proxy.Proxy
import org.freeplane.plugin.script.proxy.ScriptUtils

URI getApiLink(String path) {
    try {
        def apiBase = path.startsWith('org/freeplane') ? freeplaneApiBase
                : 'http://docs.groovy-lang.org/latest/html/groovy-jdk'
        def uri = new URI(apiBase + '/' + path)
        if (path.startsWith('org/freeplane') && !new File(uri).exists())
            return null
        return uri
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
    classNode.link.uri = getApiLink(clazz)
    classNode.style.font.bold = true
    if(clazz.isEnum()) {
        clazz.getEnumConstants().each {
            classNode.createChild("${clazz.simpleName}.${it.name()}:   $it".toString())
        }
    }
    else { // skip methods and properties for enums
        TreeMap<String, Map<String, Object>> memberMap = new TreeMap<String, Map<String, Object>>()
        clazz.getFields().findAll {
            it.declaringClass == clazz || it.declaringClass.simpleName.endsWith('RO') ||
                    it.declaringClass.getPackage().name == org.freeplane.api.Node.class.getPackage().name
        }.each {
            addField(memberMap, it, clazz);
        }
    
        clazz.getMethods().findAll {
            it.declaringClass == clazz || it.declaringClass.simpleName.endsWith('RO') ||
                    it.declaringClass.getPackage().name == org.freeplane.api.Node.class.getPackage().name
        }.each {
            if (!addProperty(memberMap, it))
                addMethod(memberMap, it);
        }
        classNode.createChild('Package: ' + clazz.getPackage().name)
        memberMap.each { k,v ->
            createMemberNode(k, v, classNode)
        }
        // input for freeplane_plugin_script/src-jsyntaxpane/META-INF/services/jsyntaxpane/syntaxkits/groovysyntaxkit/combocompletions.txt
        boolean printCompletionList = false
        if (printCompletionList && classNode.to.plain == 'Node')
            printCompletions(memberMap)
    }
    classNode.folded = true
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
    else if (attribs['read'] || attribs['write']){
        // property
        def mode = (attribs['type_read'] ? 'r' : '') + (attribs['type_write'] ? 'w' : '')
        def type = attribs['type_read'] ? attribs['type_read'] : attribs['type_write']
        //    if (mode == 'rw' && attribs['type_read'] != attribs['type_write']) {
        //        logger.severe("property ${memberName} has differing getter and setter types")
        //    }
        memberNode = classNode.createChild(formatProperty(memberName, formatReturnType(type), mode))
        memberNode.icons.add('wizard')
        [ 'method_read', 'method_write' ].each {
            if (attribs[it]) {
                Proxy.Node methodNode = memberNode.createChild(formatMethod(attribs[it]))
                methodNode.icons.add('bookmark')
            }
        }
    }
    else {
        memberNode = classNode.createChild(formatField(attribs))
        memberNode.icons.add(attribs['enumConstant']?'list':'info')
        memberNode.details = (attribs['enumConstant']?'enum':'constant')
    }
    if (attribs['deprecated']) {
        memberNode.icons.add('closed')
    }
    return memberNode
}

def typeToString(Class clazz) {
    return clazz.name.split(/\./).getAt(-1).replace('Proxy$', '').replace('$', '.')
}

// returns a value if this method is a getter or setter otherwise it returns null
def addProperty(Map<String, Map<String, Object>> memberMap, Method method) {
    if (isGetter(method) && ! method.parameterTypes) {
        def propertyMap = getOrCreatePropertiesMap(memberMap, getPropertyName(method))
        propertyMap['read'] = true
        propertyMap['type_read'] = method.returnType
        propertyMap['method_read'] = method
        propertyMap['return_type'] = method.returnType
        propertyMap['deprecated'] = isDeprecated(method)
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
    propertyMap['deprecated'] = isDeprecated(method)
}

def addField(Map<String, Map<String, Object>> memberMap, Field field, Class clazz) {
    def propertyMap = getOrCreatePropertiesMap(memberMap, formatFieldKey(field))    
    propertyMap['enumConstant'] = field.isEnumConstant()
    propertyMap['return_type'] = field.getType()
    propertyMap['name'] = field.getName()
    propertyMap['value'] = clazz.getProperties().get(field.getName())
}

def formatFieldKey(Field field){
    return "_ ${field.name}"
}

def formatField(Map att){
    return "<html><body><b>${formatReturnType(att['return_type'] )}.${att['name']}</b> =   ${att['value']}</body></html>".toString()
}
 
def formatProperty(String property, String type, String mode) {
    return "<html><body><b>${property}</b>: ${type} (${mode})</body></html>".toString()
    // Plain text:
    //    return "${property}: ${type} (${mode})"
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
            '(' + parameters.join(', ') + ')' +'</body></html>'
}

def isGetter(Method method) {
    return method.name =~ '^(?:[gs]et|is)[A-Z].*'
}

def isSetter(Method method) {
    return method.name =~ '^set[A-Z].*'
}

def isDeprecated(Method method) {
    return method.isAnnotationPresent(Deprecated.class)
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

def noDuplicatedMapName(name){
    def names = ([] + c.openMindMaps*.name + c.openMindMaps*.root*.text).unique().sort()
    def i = 1
    def nombre = name
    while (names.contains(nombre)){
        nombre = "${name} x${i++}".toString()
    }
    return nombre
}

// == MAIN ==
def showIcons = true
this.freeplaneApiBase = new File(ResourceController.resourceController.installationBaseDir).toURI().toString() + 'doc/api';
def MAP_NAME = textUtils.getText('scripting_api_generator_title')
def PROXY_NODE = textUtils.getText('scripting_api_generator_proxy')
def UTILITES_NODE = textUtils.getText('scripting_api_generator_utilities')
def WEB_NODE = textUtils.getText('scripting_api_generator_web')
def LEGEND_NODE = textUtils.getText('scripting_api_generator_legend')
def ICONS_NODE = textUtils.getText('icons')
c.deactivateUndo()
def resourceBaseDir = ResourceController.resourceController.resourceBaseDir;
def allUserTemplates = new File(resourceBaseDir, 'templates');
def defaultTemplate = new File(allUserTemplates, 'standard-1.6.mm')
Proxy.Map newMap = defaultTemplate.canRead() ? c.newMapFromTemplate(defaultTemplate) : c.newMap()
if(newMap == null) {
    ui.errorMessage('Can not load map template')
    return
}
def oldName = newMap.name
MAP_NAME = noDuplicatedMapName(MAP_NAME)
newMap.name = MAP_NAME
newMap.root.text = MAP_NAME
newMap.root.style.font.bold = true
newMap.root.link.uri = getApiLink('index.html')
initHeading(newMap.root)


// Proxy
def proxy = createChild(newMap.root, PROXY_NODE, getApiLink(Proxy.class))
initHeading(proxy)
//org.freeplane.plugin.script.proxy
makeApi(proxy, Proxy.Attributes.class)
makeApi(proxy, Proxy.Cloud.class)
makeApi(proxy, CloudShape.class)
makeApi(proxy, Proxy.Connector.class)
makeApi(proxy, ConnectorShape.class)
makeApi(proxy, Proxy.Controller.class)
makeApi(proxy, Proxy.Loader.class)
makeApi(proxy, Script.class)
makeApi(proxy, Proxy.Edge.class)
makeApi(proxy, EdgeStyle.class)
makeApi(proxy, Proxy.ExternalObject.class)
makeApi(proxy, Proxy.Font.class)
makeApi(proxy, Proxy.Icons.class)
makeApi(proxy, Proxy.Link.class)
makeApi(proxy, Proxy.MindMap.class)
makeApi(proxy, NodeChangeListener.class)
makeApi(proxy, NodeChanged.class)
makeApi(proxy, NodeChanged.ChangedElement.class)
makeApi(proxy, NodeCondition.class)
makeApi(proxy, Proxy.Properties.class)
makeApi(proxy, Proxy.Node.class)
makeApi(proxy, Convertible.class)
makeApi(proxy, Proxy.NodeStyle.class)
makeApi(proxy, Proxy.NodeGeometry.class)
makeApi(proxy, NodeShape.class)
makeApi(proxy, Side.class)
makeApi(proxy, Quantity.class)
makeApi(proxy, LengthUnit.class)
makeApi(proxy, Proxy.Reminder.class)
makeApi(proxy, Proxy.DependencyLookup.class)
makeApi(proxy, Dependencies.class)
makeApi(proxy, ScriptUtils.class)

makeApi(proxy, PhysicalUnit.class)
makeApi(proxy, Dependencies.Element.class)
makeApi(proxy, ConversionException.class)
makeApi(proxy, NodeNotFoundException.class)
makeApi(proxy, AttributeCondition.class)
makeApi(proxy, AttributeValueSerializer.class)
makeApi(proxy, Border.class)
makeApi(proxy, ChildNodesAlignment.class)
makeApi(proxy, ChildNodesLayout.class)
makeApi(proxy, ChildrenSides.class)
makeApi(proxy, ConditionalStyle.class)
makeApi(proxy, ConditionalStyleNotFoundException.class)
makeApi(proxy, ConditionalStyles.class)
makeApi(proxy, Dash.class)
makeApi(proxy, org.freeplane.api.FreeplaneVersion.class)
makeApi(proxy, HeadlessLoader.class)
makeApi(proxy, HeadlessMapCreator.class)
makeApi(proxy, HorizontalTextAlignment.class)
makeApi(proxy, LayoutOrientation.class)
makeApi(proxy, NodeToComparableMapper.class)
makeApi(proxy, TextWritingDirection.class)
proxy.sortChildrenBy{it.plainText}

def utils = createChild(newMap.root, UTILITES_NODE, null)
initHeading(utils)
// org.freeplane.plugin.script
makeApi(utils, FreeplaneScriptBaseClass.class)
//org.freeplane.core.ui.components
makeApi(utils, UITools.class)
makeApi(utils, UITools.Defaults.class)
makeApi(utils, UITools.InsertEolAction.class)
//org.freeplane.core.util
makeApi(utils, LogUtils.class)
makeApi(utils, HtmlUtils.class)
makeApi(utils, HtmlUtils.IndexPair.class)
makeApi(utils, TextUtils.class)
makeApi(utils, MenuUtils.class)
makeApi(utils, MenuUtils.MenuEntry.class)
makeApi(utils, MenuUtils.MenuEntryTreeBuilder.class)
// org.freeplane.plugin.script
makeApi(utils, FreeplaneScriptBaseClass.ConfigProperties.class)
//org.freeplane.core.util
makeApi(utils, FreeplaneVersion.class)
//org.freeplane.launcher
makeApi(utils, Launcher.class)
utils.sortChildrenBy{it.plainText}

def icons = newMap.root.createChild(ICONS_NODE)
initHeading(icons)
def bundle = ResourceController.getResourceController().getResources()
bundle.getKeys().toList()
        .findAll{ it.startsWith('icon_') }
        .collect {
            def key = it.substring('icon_'.length())
            def translation = bundle.getResourceString(it, it).replaceAll('[&]', '')
            translation + '@@@' + key
        }
        .sort()
        .each {
            def translationAndKey = it.split('@@@')
            def tnode = icons.createChild(translationAndKey[0])
            tnode.createChild(translationAndKey[1])
            if (showIcons) tnode.icons.add(translationAndKey[1])
        }
icons.folded = true

def web = createChild(newMap.root, WEB_NODE, 'https://docs.freeplane.org/scripting/Scripting.html')
initHeading(web)
createChild(web, 'Groovy - learn', 'https://groovy-lang.org/learn.html')
createChild(web, 'Groovy - learn X in Y minutes', 'https://learnxinyminutes.com/docs/groovy/')
createChild(web, 'Example scripts', 'https://docs.freeplane.org/scripting/Scripts_collection.html')

def legend = newMap.root.createChild(LEGEND_NODE)
initHeading(legend)
def methodLegend = legend.createChild("Normal methods have a 'bookmark' icon")
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
propertyLegend.createChild("Properties with differing type of setter and getter have two nodes")
propertyLegend.folded = true
def deprecatedLegend = legend.createChild("Deprecated methods have a 'closed' icon.")
deprecatedLegend.createChild("Follow the class' link to the detailed API description to find out what to use instead.")
deprecatedLegend.icons.add('closed')
deprecatedLegend.folded = true

c.deactivateUndo()
newMap.saved = true
