package org.freeplane.features.url;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.XsltPipeReaderFactory;
import org.freeplane.features.map.MapModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MapVersionInterpreter implements IExtension{
/*
	FREEPLANE1_2_0("freeplane 1.2.0", false, false, "Freeplane", "freeplane.url"),
	FREEPLANE1_1("0.9.0", false, false, "Freeplane", "freeplane.url"),
	FREEMIND_1("1.", false, true, "FreeMind", "freemind.url"),
	DOCEAR("docear ", false, true, "Docear", "docear.url"),

*/
	static final public IMapInputStreamConverter DEFAULT_INPUTSTREAM_CONVERTER = new IMapInputStreamConverter() {
		private static final String FREEPLANE_VERSION_UPDATER_XSLT = "/xslt/freeplane_version_updater.xslt";
		public Reader getConvertedStream(File f) throws FileNotFoundException, IOException {
			return new XsltPipeReaderFactory(FREEPLANE_VERSION_UPDATER_XSLT).getReader(new FileInputStream(f));
		}
	};
	static final public MapVersionInterpreter DEFAULT = new MapVersionInterpreter("", 0, "", true, true, null, null);
	final public String mapBegin;
	final public String name;
	final public int version;
	final public boolean needsConversion;
	final public boolean anotherDialect;
	final public String appName;
	final public String url;
	final public IMapInputStreamConverter inputStreamConverter;
	final public IMapConverter mapConverter;

	MapVersionInterpreter(String name, int version, String versionBegin, boolean needsConversion, boolean anotherDialect,
			String appName, String url) {
		this(name, version, versionBegin, needsConversion, anotherDialect, appName, url, DEFAULT_INPUTSTREAM_CONVERTER, null);
	}

	public MapVersionInterpreter(String name, int version, String versionBegin, boolean needsConversion, boolean anotherDialect,
			String appName, String url, IMapInputStreamConverter inputStreamConverter, IMapConverter mapConverter) {

		this.inputStreamConverter = inputStreamConverter;
		this.mapConverter = mapConverter;

		this.name = name;
		this.version = version;
		this.mapBegin = "<map version=\"" + versionBegin;
		this.needsConversion = needsConversion;
		this.anotherDialect = anotherDialect;
		this.appName = appName;
		this.url = url;
	}

	public static MapVersionInterpreter getVersionInterpreter(String mapBegin){
		for (MapVersionInterpreter interpreter : MapVersionInterpreter.values()){
			if(interpreter.knows(mapBegin))
				return interpreter;
		}
		return DEFAULT;
	}

	private static MapVersionInterpreter[] values = null;
	private static MapVersionInterpreter[] values() {
		if(values == null){
			try (InputStream resource = ResourceController.getResourceController().getResource("/xml/mapVersions.xml").openStream()){
	            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
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
					String name = dialectElement.getAttribute("name");
					String appName = dialectElement.getAttribute("appName");
					String url = dialectElement.getAttribute("url");
					int version = Integer.parseInt(dialectElement.getAttribute("version"));
					values[i] = new MapVersionInterpreter(name, version, versionBegin, needsConversion, anotherDialect, appName, url);
				}
			} catch (Exception e) {
				LogUtils.severe(e);
				values = new MapVersionInterpreter[]{};
			}
		}
		return values;
	}

	public static void addMapVersionInterpreter(MapVersionInterpreter interpreter) {
		ArrayList<MapVersionInterpreter> list = new ArrayList<MapVersionInterpreter>();
		list.add(interpreter);
		list.addAll(Arrays.asList(values()));
		values = list.toArray(values);
	}

	private boolean knows(String mapBegin) {
		return mapBegin.startsWith(this.mapBegin);
	}

	public IMapConverter getMapConverter() {
		return this.mapConverter;
	}

	public IMapInputStreamConverter getMapInputStreamConverter() {
		return this.inputStreamConverter;
	}

	public String getDialectInfo(String path){
		final String appInfo;
		if(appName != null) {
			if("Freeplane".equals(appName))
				appInfo = TextUtils.format("dialect_info.later_version", path);
			else
				appInfo = TextUtils.format("dialect_info.app", path, appName);
        }
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

	static public boolean isOlderThan(MapModel map, int version){
		MapVersionInterpreter versionInterpreter = map.getExtension(MapVersionInterpreter.class);
		return versionInterpreter != null  && versionInterpreter.version < version;
	}
}
