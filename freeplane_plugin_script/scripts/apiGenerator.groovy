// @ExecutionModes({on_single_node="/menu_bar/help[scripting_api_generator_title]"})
// Copyright (C) 2009-2011 Dave (Dke211, initial author), Volker Boerchers (adaptation for Freeplane)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.

def lastSection(string){
    if (string == null)
        return null
    String[] p = string.split("\\.");
    return p[p.size()-1];
}

def removeModifiers(String[] parts) {
    def result = parts.toList()
    result.remove("final")
    result.remove("abstract")
    return result
}

def newChildNode(node, method){
    def apiNode    = node.createChild(0);
    def returnType = apiNode.createChild(0);
    def iProto     = apiNode.createChild(1);

    def text = method.toString();
    List parts = removeModifiers(text.split(" "));

    StringBuilder sb = new StringBuilder();
    sb.append(parts[0]);
    sb.append(" ");

    sb.append(lastSection(parts[1]));

    returnType.setText(parts[1].replace('Proxy$', ''));

    sb.append(" ");
    sb.append(method.getName());
    sb.append("(");

    def Class[] parms = method.getParameterTypes();
    def protoTxt = new StringBuffer();
    if(parms.size() >0){
        for(i in 0..parms.size()-1){
            protoTxt.append(parms[i].toString());
            sb.append(lastSection(parms[i].toString()));
            if(i<parms.size()-1){
                protoTxt.append("\n");
                sb.append(",");
            }
        }
    }
    else{
        protoTxt.append("*none*");
    }
    sb.append(")");
    apiNode.text = sb.toString().replace('Proxy$', '');
    apiNode.folded = true;
    iProto.text = protoTxt.toString().replace('Proxy$', '');
}

def makeApi(node, clazz, apiBase) {
    def child = node.createChild()
    child.text = clazz.simpleName.replace('Proxy$', '')
    def path = clazz.name.replace('.', '/').replace('$', '.')
    child.link.text = apiBase + '/' + path + '.html'
    def methods = clazz.getMethods().sort{ a,b -> b.name <=> a.name }
    for(i in 0..<methods.size()){
        newChildNode(child, methods[i]);
    }
    child.createChild(0).text = 'Package: ' + clazz.getPackage().name
    child.folded = true
}

def initHeading(node) {
    node.style.font.bold = true
}

def createChild(parent, text, link) {
    def result = parent.createChild(text)
    result.link.text = link
    return result
}

// == MAIN ==
def MAP_NAME = textUtils.getText('scripting_api_generator_title')
def PROXY_NODE = textUtils.getText('scripting_api_generator_proxy')
def UTILITES_NODE = textUtils.getText('scripting_api_generator_utilities')
def WEB_NODE = textUtils.getText('scripting_api_generator_web')
c.deactivateUndo()
def apiBase = 'file:/devel/freeplane-bazaar-repo/trunk/freeplane_framework/build/doc/api'
def newMap = c.newMap()
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
makeApi(proxy, Class.forName('org.freeplane.plugin.script.proxy.FreeplaneScriptBaseClass'), apiBase)

def utils = createChild(newMap.root, UTILITES_NODE, null)
initHeading(utils)
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

c.deactivateUndo()
newMap.saved = true
