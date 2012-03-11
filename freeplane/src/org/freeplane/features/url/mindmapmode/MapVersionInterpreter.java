package org.freeplane.features.url.mindmapmode;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MapVersionInterpreter{
/*	
	FREEPLANE1_2_0("freeplane 1.2.0", false, false, "Freeplane", "freeplane.url"),
	FREEPLANE1_1("0.9.0", false, false, "Freeplane", "freeplane.url"),
	FREEMIND_1("1.", false, true, "FreeMind", "freemind.url"),
	DOCEAR("docear ", false, true, "Docear", "docear.url"),
	
*/	
	static final public MapVersionInterpreter DEFAULT = new MapVersionInterpreter("", false, true, null, null);
	final public String mapBegin;
	final public boolean needsConversion;
	final public boolean anotherDialect;
	final public String appName;
	final public String url;
	MapVersionInterpreter(String versionBegin, boolean needsConversion, boolean anotherDialect,
			String appName, String url) {
		this.mapBegin = "<map version=\"" + versionBegin;
		this.needsConversion = needsConversion;
		this.anotherDialect = anotherDialect;
		this.appName = appName;
		this.url = url;
	}
	
	static MapVersionInterpreter getVersionInterpreter(String mapBegin){
		for (MapVersionInterpreter interpreter : MapVersionInterpreter.values()){
			if(interpreter.knows(mapBegin))
				return interpreter;
		}
		return DEFAULT;
	}

	private static MapVersionInterpreter[] values = null;
	private static MapVersionInterpreter[] values() {
		if(values == null){
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
				InputStream resource = ResourceController.getResourceController().getResource("/xml/mapVersions.xml").openStream();
				Document dom = documentBuilder.parse(resource);
				Element root = dom.getDocumentElement();
				NodeList dialectElements = root.getElementsByTagName("dialect");
				final int dialectNumber = dialectElements.getLength();
				values = new MapVersionInterpreter[dialectNumber];
				for(int i = 0; i < dialectNumber; i++){
					Element dialectElement = (Element) dialectElements.item(i);
					String versionBegin = dialectElement.getAttribute("versionBegin");
					boolean needsConversion = Boolean.parseBoolean(dialectElement.getAttribute("needsConversion"));
					boolean anotherDialect = Boolean.parseBoolean(dialectElement.getAttribute("anotherDialect"));
					String appName = dialectElement.getAttribute("appName");
					String url = dialectElement.getAttribute("url");
					values[i] = new MapVersionInterpreter(versionBegin, needsConversion, anotherDialect, appName, url);
				}
				resource.close();
			} catch (Exception e) {
				LogUtils.severe(e);
				values = new MapVersionInterpreter[]{};
			}
		}
		return values;
	}

	private boolean knows(String mapBegin) {
		return mapBegin.startsWith(this.mapBegin);
	}
	
	String getDialectInfo(String path){
		final String appInfo;
		if(appName != null)
			appInfo = TextUtils.format("dialect_info.app", path, appName);
		else
			appInfo = TextUtils.format("dialect_info.unknownApp", path);
		final String warning = TextUtils.getText("dialect_info.warning");
		final String urlInfo;
		if(url != null)
			urlInfo = TextUtils.format("dialect_info.url", url);
		else
			urlInfo = TextUtils.getText("dialect_info.unknownURL");
		return appInfo +" "+ warning +" "+ urlInfo;
	}
}
