package org.freeplane.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * class to create a report for i18n
 * 
 * @author Robert Ladstaetter
 */
public class I18nReporter {
	class ActionItem {
		Issue issue;
		String key;
		CountryCode lang;
		User owner;
		String standardTranslation;

		public Issue getIssue() {
			return issue;
		}

		public String getKey() {
			return key;
		}

		public CountryCode getLang() {
			return lang;
		}

		public User getOwner() {
			return owner;
		}

		public void setIssue(Issue issue) {
			this.issue = issue;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public void setLang(CountryCode lang) {
			this.lang = lang;
		}

		public void setOwner(User owner) {
			this.owner = owner;
		}

		public String getStandardTranslation() {
			return standardTranslation;
		}

		public void setStandardTranslation(String standardTranslation) {
			this.standardTranslation = standardTranslation;
		}
	}

	class PoorMansTemplate {
		private String template;

		public PoorMansTemplate(String classpathTemplate) {
			try {
				template = loadTemplateFromClasspath(classpathTemplate);
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		private String eval(Map<String, String> context) {
			String template = this.template;
			for (String k : context.keySet()) {
				template = template.replace(k, context.get(k));
			}
			return template;
		}
	}

	class Summary {
		Map<CountryCode, Integer> noEntryMap;
		Map<CountryCode, Integer> zeroLengthMap;

		public Summary() {
		}

		public Summary(Map<CountryCode, Integer> noEntryMap, Map<CountryCode, Integer> zeroLengthMap) {
			setNoEntryMap(noEntryMap);
			setZeroLengthMap(zeroLengthMap);
		}

		public Map<CountryCode, Integer> getNoEntryMap() {
			return noEntryMap;
		}

		public void setNoEntryMap(Map<CountryCode, Integer> noEntryMap) {
			this.noEntryMap = noEntryMap;
		}

		public Map<CountryCode, Integer> getZeroLengthMap() {
			return zeroLengthMap;
		}

		public void setZeroLengthMap(Map<CountryCode, Integer> zeroLengthMap) {
			this.zeroLengthMap = zeroLengthMap;
		}

		String asString() {
			return table(getHeader("Issues", Arrays.asList(Issue.MISSING, Issue.ZERO_LENGTH)) + //
			        tr(getSummaryRow(noEntryMap, zeroLengthMap)));
		}
	}

	private static final String CONTENT = "<!-- CONTENT -->";
	private static final String RESOURCES_TRANSLATIONS = "resources/translations/";
	private static final String STANDARD = "viewer-resources/translations/Resources_en.properties";
	private static final String SVN_URL = "https://freeplane.svn.sourceforge.net/svnroot/freeplane/freeplane_program/trunk/freeplane/";
	private static final String TITLE = "<!-- TITLE -->";

	private static String loadTemplateFromClasspath(String template) throws IOException {
		InputStream resourceAsStream = I18nReporter.class.getResourceAsStream(template);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
		StringBuilder strBldr = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			strBldr.append(line);
		}
		return strBldr.toString();
	}

	public static void main(String[] args) {
		I18nReporter reporter = new I18nReporter();
		reporter.setBaseDir("C:/Users/Robert/Documents/workspace-freeplane/freeplane/");
		reporter.setLeadPropertyFile(STANDARD);
		reporter.setSupportedLanguages(CountryCode.getSupportedLanguages());
		reporter.init();
		// summary
		writeOutputFile(reporter, reporter.getSummary().asString(), reporter.a(reporter.getOverviewName(),
		    "Freeplane I18N Overview"), reporter.getSummaryName());
		// overview
		writeOutputFile(reporter, reporter.getOverview(), "Freeplane I18N Overview ", reporter.getOverviewName());
		// action items
		Map<CountryCode, List<ActionItem>> actionItems = reporter.getActionItems();
		for (CountryCode lang : actionItems.keySet()) {
			writeOutputFile(reporter, reporter.getActionItemsAsString(lang, actionItems.get(lang)), reporter.a(reporter
			    .getSummaryName(), "Freeplane Action")
			        + " Item for "
			        + reporter.a(SVN_URL + RESOURCES_TRANSLATIONS + "Resources_" + lang + ".properties", lang
			            .toString()), reporter.getActionItemName() + lang + ".html");
		}
		System.out.println("Finished.");
	}

	public static void writeFile(String fileName, String content) {
		// Create file
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fileName));
			out.write(content);
			System.out.println("Wrote " + fileName + ".");
		}
		catch (IOException e) {
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e1) {
					// ?
				}
			}
		}
	}

	private static void writeOutputFile(I18nReporter reporter, String content, String title, String fileName) {
		Map<String, String> context = new HashMap<String, String>();
		context.put(CONTENT, content);
		context.put(TITLE, title);
		String processedContent = reporter.new PoorMansTemplate("/I18nReporterTemplate.html").eval(context);
		reporter.writeFile(fileName, processedContent);
	}

	private String baseDir;
	private String leadPropertyFile;
	private Properties mainProperties;
	private Summary summary;
	private List<CountryCode> supportedLanguages;
	private Map<CountryCode, Properties> translations;

	public I18nReporter() {
	}

	private String a(String href, String title) {
		return ("<a href=\"" + href + "\">" + title + "</a>");
	}

	private String addTag(String tag, String content) {
		return "<" + tag + ">" + content + "</" + tag + ">";
	}

	private String aName(String name, String title) {
		return "<a name=\"" + name + "\">" + title + "</a>";
	}

	private String getActionItemName() {
		return getClass().getSimpleName() + "ActionItems_";
	}

	private Map<CountryCode, List<ActionItem>> getActionItems() {
		List<String> keySet = new ArrayList(Arrays.asList(mainProperties.keySet().toArray(new String[] {})));
		Map<CountryCode, List<ActionItem>> ais = new HashMap<CountryCode, List<ActionItem>>();
		Collections.sort(keySet);
		for (Object o : keySet) {
			for (CountryCode lang : supportedLanguages) {
				if ((!translations.get(lang).keySet().contains(o))) {
					ActionItem ai = new ActionItem();
					ai.setIssue(Issue.MISSING);
					ai.setKey(o.toString());
					ai.setLang(lang);
					ai.setOwner(new User());
					ai.setStandardTranslation(mainProperties.getProperty((String) o));
					lazyAdd(ais, lang, ai);
				}
			}
		}
		return ais;
	}

	private void lazyAdd(Map<CountryCode, List<ActionItem>> ais, CountryCode lang, ActionItem ai) {
		if (!ais.keySet().contains(lang)) {
			ais.put(lang, new ArrayList<ActionItem>());
		}
		ais.get(lang).add(ai);
	}

	private String getActionItemsAsString(CountryCode l, List<ActionItem> list) {
		StringBuilder strBldr = new StringBuilder();
		strBldr.append(getHeader(l.toString(), Arrays.asList("Issue")));
		strBldr.append("<pre>");
		for (ActionItem i : list) {
			strBldr.append("# " + i.getIssue() + " : standard: " + i.getStandardTranslation() + "\n");
			strBldr.append(i.getKey() + "=\n");
		}
		strBldr.append("</pre>");
		for (ActionItem i : list) {
			strBldr.append(tr(td(i.getKey()) + td(i.getIssue().toString())));
		}
		return table(strBldr.toString());
	}

	public String getBaseDir() {
		return baseDir;
	}

	String getHeader(String title, List<?> tds) {
		StringBuilder content = new StringBuilder();
		for (Object lang : tds) {
			content.append(td(lang.toString()));
		}
		return tr(th(title) + content.toString());
	}

	public String getLeadPropertyFile() {
		return leadPropertyFile;
	}

	/**
	 * 
	 * @param mainProperties
	 * @param translations
	 * @param langs
	 * @return
	 */
	public Map<CountryCode, Integer> getNoEntryMap(Properties mainProperties,
	                                               Map<CountryCode, Properties> translations, List<CountryCode> langs) {
		Map<CountryCode, Integer> map = new HashMap<CountryCode, Integer>();
		for (CountryCode lang : langs) {
			Properties properties = translations.get(lang);
			// get all keys from mainProperties
			for (Object o : mainProperties.keySet()) {
				if (!properties.keySet().contains(o)) {
					if (!map.keySet().contains(lang)) {
						map.put(lang, 0);
					}
					map.put(lang, map.get(lang) + 1);
				}
			}
		}
		return map;
	}

	public Map<CountryCode, Integer> getZeroLengthMap(Properties mainProperties,
	                                                  Map<CountryCode, Properties> translations, List<CountryCode> langs) {
		Map<CountryCode, Integer> map = new HashMap<CountryCode, Integer>();
		for (CountryCode lang : langs) {
			Properties properties = translations.get(lang);
			for (Object o : mainProperties.keySet()) {
				String property = properties.getProperty((String) o);
				if (property != null) {
					if (((String) property).length() == 0) {
						if (!map.keySet().contains(lang)) {
							map.put(lang, 0);
						}
						map.put(lang, map.get(lang) + 1);
					}
				}
			}
		}
		return map;
	}

	private String getOverview() {
		StringBuilder content = new StringBuilder();
		content.append(getHeader("Translation", supportedLanguages));
		List<String> listOfKeys = new ArrayList(Arrays.asList(mainProperties.keySet().toArray(new String[] {})));
		content.append(getOverviewContent(translations, supportedLanguages, listOfKeys));
		return table(content.toString());
	}

	private String getOverviewContent(Map<CountryCode, Properties> translations, List<CountryCode> langs,
	                                  List<String> keySet) {
		StringBuilder content = new StringBuilder();
		Collections.sort(keySet);
		for (Object o : keySet) {
			StringBuilder line = new StringBuilder();
			line.append(th(o.toString()));
			for (CountryCode lang : langs) {
				if ((!translations.get(lang).keySet().contains(o))) {
					line.append(td(a(getActionItemName() + lang.toString() + ".html", "NE")));
				}
				else {
					line.append(td(""));
				}
			}
			content.append(tr(line.toString()));
		}
		return content.toString();
	}

	private String getOverviewName() {
		return getClass().getSimpleName() + "Overview.html";
	}

	public Summary getSummary() {
		return summary;
	}

	private String getSummaryName() {
		return getClass().getSimpleName() + "Summary.html";
	}

	private String getSummaryRow(Map<CountryCode, Integer> map, Map<CountryCode, Integer> zeroLengthMap) {
		final int maxSize = 20;
		final int minSize = 6;
		int c = map.size();
		int tc = 0;
		for (Integer p : map.values()) {
			tc += p;
		}
		float middle = tc / c;
		StringBuilder line = new StringBuilder();
		for (CountryCode l : map.keySet()) {
			Integer i = zeroLengthMap.get(l);
			String s = td("0");
			if (i != null) {
				s = td(Integer.toString(i));
			}
			line.append(tr(td(l.toString())
			        + td(a(getActionItemName() + l.toString() + ".html", Integer.toString(map.get(l)))) + s));
		}
		return line.toString();
	}

	public List<CountryCode> getSupportedLanguages() {
		return supportedLanguages;
	}

	private void init() {
		translations = loadTranslations(supportedLanguages);
		mainProperties = loadPropertyFile(new File(baseDir + leadPropertyFile));
		summary = new Summary(getNoEntryMap(mainProperties, translations, supportedLanguages), getZeroLengthMap(
		    mainProperties, translations, supportedLanguages));
	}

	private Properties loadPropertyFile(File file) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(file));
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}

	private Map<CountryCode, Properties> loadTranslations(List<CountryCode> langs) {
		Map<CountryCode, Properties> translations = new HashMap<CountryCode, Properties>();
		for (CountryCode l : langs) {
			translations.put(l, loadPropertyFile(new File(baseDir + RESOURCES_TRANSLATIONS + "Resources_" + l
			        + ".properties")));
		}
		return translations;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public void setLeadPropertyFile(String leadPropertyFile) {
		this.leadPropertyFile = leadPropertyFile;
	}

	public void setSupportedLanguages(List<CountryCode> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

	private String table(String content) {
		return addTag("table", content);
	}

	private String td(String translation) {
		return addTag("td", translation);
	}

	private String th(String content) {
		return addTag("th", content);
	}

	private String tr(String content) {
		return addTag("tr", content) + "\n";
	}
}
