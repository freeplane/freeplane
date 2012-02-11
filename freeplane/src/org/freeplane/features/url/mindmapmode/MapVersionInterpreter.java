package org.freeplane.features.url.mindmapmode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

public enum MapVersionInterpreter{
	FREEPLANE1_2_0("freeplane 1.2.0", false, false, "Freeplane", "freeplane.url"),
	FREEPLANE1_1("0.9.0", false, false, "Freeplane", "freeplane.url"),
	FREEMIND_1("1.", false, true, "FreeMind", "freemind.url"),
	DOCEAR("docear ", false, true, "Docear", "docear.url"),
	DEFAULT("", false, true, null, null);
	final public String mapBegin;
	final public boolean needsConversion;
	final public boolean anotherDialect;
	final public String appName;
	final public String URL;
	MapVersionInterpreter(String versionBegin, boolean needsConversion, boolean anotherDialect,
			String appName, String uRL) {
		this.mapBegin = "<map version=\"" + versionBegin;
		this.needsConversion = needsConversion;
		this.anotherDialect = anotherDialect;
		this.appName = appName;
		URL = uRL;
	}
	
	static MapVersionInterpreter getVersionInterpreter(String mapBegin){
		for (MapVersionInterpreter interpreter : MapVersionInterpreter.values()){
			if(interpreter.knows(mapBegin))
				return interpreter;
		}
		return DEFAULT;
	}

	private boolean knows(String mapBegin) {
		return mapBegin.startsWith(this.mapBegin);
	}
	
	String getDialectInfo(){
		final String appInfo;
		if(appName != null)
			appInfo = TextUtils.format("dialect_info.app", appName);
		else
			appInfo = TextUtils.getText("dialect_info.unknownApp");
		final String warning = TextUtils.getText("dialect_info.warning");
		String url = null;
		if(URL != null) 
			url = ResourceController.getResourceController().getProperty(URL, null);
		final String urlInfo;
		if(url != null)
			urlInfo = TextUtils.format("dialect_info.url", url);
		else
			urlInfo = TextUtils.getText("dialect_info.unknownURL");
		return appInfo +" "+ warning +" "+ urlInfo;
	}
}
