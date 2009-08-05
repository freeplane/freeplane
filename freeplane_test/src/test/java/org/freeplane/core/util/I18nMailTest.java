package org.freeplane.core.util;

import org.junit.Test;

public class I18nMailTest {
	@Test
	public void testGetActions() {
		I18nMail mail = new I18nMail();
		mail.setC(CountryCode.pl);
		mail.setIssueCnt(914);
		mail.setMissingKeys("missingKeys");
		mail.setUser(User.rafalM);
		mail.setUrl(I18nReporter.SVN_URL + I18nReporter.RESOURCES_TRANSLATIONS + "Resources_" + CountryCode.hu
		        + ".properties");
		System.out.println(mail.asText());
	}
}
