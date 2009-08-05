package org.freeplane.core.util;

import java.util.HashMap;
import java.util.Map;

public class I18nMail {
	User user;
	CountryCode c;
	int issueCnt;
	String url;
	String missingKeys;
	private Map<String, String> map;

	public I18nMail() {
		super();
		map = new HashMap<String, String>();
	}

	public String asText() {
		map.clear();
		map.put("$name",user.getName());
		map.put("$lang", c.toString());
		map.put("$issueCnt", Integer.toString(issueCnt));
		map.put("$url", url);
		map.put("$missingKeys",missingKeys);		
		return new PoorMansTemplate("/mailTemplate.txt").eval(map);
	}



	public User getUser() {
    	return user;
    }

	public void setUser(User user) {
    	this.user = user;
    }

	public CountryCode getC() {
		return c;
	}

	public void setC(CountryCode c) {
		this.c = c;
	}

	public int getIssueCnt() {
		return issueCnt;
	}

	public void setIssueCnt(int issueCnt) {
		this.issueCnt = issueCnt;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMissingKeys() {
		return missingKeys;
	}

	public void setMissingKeys(String missingKeys) {
		this.missingKeys = missingKeys;
	}
}
