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
	private static final String TITLE = "<!-- TITLE -->";
	private static final String CONTENT = "<!-- CONTENT -->";
	private static final String SVN_URL = "https://freeplane.svn.sourceforge.net/svnroot/freeplane/freeplane_program/trunk/freeplane/";
	private static final String STANDARD = "viewer-resources/translations/Resources_en.properties";
	private static final String RESOURCES_TRANSLATIONS = "resources/translations/";
	private String baseDir;
	private String leadPropertyFile;
	private List<Language> supportedLanguages;

	public List<Language> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(List<Language> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

	public I18nReporter() {
	}

	private void init() {
		translations = loadTranslations(supportedLanguages);
		mainProperties = loadPropertyFile(new File(baseDir + leadPropertyFile));
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getLeadPropertyFile() {
		return leadPropertyFile;
	}

	public void setLeadPropertyFile(String leadPropertyFile) {
		this.leadPropertyFile = leadPropertyFile;
	}

	public static void main(String[] args) {
		I18nReporter reporter = new I18nReporter();
		reporter.setBaseDir("C:/Users/Robert/Documents/workspace-freeplane/freeplane/");
		reporter.setLeadPropertyFile(STANDARD);
		reporter.setSupportedLanguages(Arrays.asList(Language.values()));
		reporter.init();
		// summary
		writeOutputFile(reporter, reporter.getSummary(), reporter.a(reporter.getOverviewName(), "Freeplane I18N Summary"), reporter.getSummaryName());
		// overview
		writeOutputFile(reporter, reporter.getOverview(), "Freeplane I18N Overview ", reporter.getOverviewName());
		// action items
		Map<Language, List<ActionItem>> actionItems = reporter.getActionItems();
		for (Language lang : actionItems.keySet()) {
			writeOutputFile(reporter, reporter.getActionItemsAsString(lang, actionItems.get(lang)),
			    reporter.a(reporter.getSummaryName(), "Freeplane Action") + " Item for "
			            + reporter.a(SVN_URL + RESOURCES_TRANSLATIONS + "Resources_" + lang + ".properties", lang
			                .toString()), reporter.getActionItemName() + lang + ".html");
		}
		System.out.println("Finished.");
	}

	private String getSummaryName() {
		return getClass().getSimpleName() + "Summary.html";
	}

	private String getOverviewName() {
		return getClass().getSimpleName() + "Overview.html";
	}

	private static void writeOutputFile(I18nReporter reporter, String content, String title, String fileName) {
		Map<String, String> context = new HashMap<String, String>();
		context.put(CONTENT, content);
		context.put(TITLE, title);
		String processedContent = reporter.new PoorMansTemplate("/I18nReporterTemplate.html").eval(context);
		reporter.writeFile(fileName, processedContent);
	}

	private Map<Language, Properties> loadTranslations(List<Language> langs) {
		Map<Language, Properties> translations = new HashMap<Language, Properties>();
		for (Language l : langs) {
			translations.put(l, loadPropertyFile(new File(baseDir + RESOURCES_TRANSLATIONS + "Resources_" + l
			        + ".properties")));
		}
		return translations;
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

	private String getActionItemsAsString(Language l, List<ActionItem> list) {
		StringBuilder strBldr = new StringBuilder();
		strBldr.append(getHeader(l.toString(), Arrays.asList("Issue")));
		strBldr.append("<pre>");
		for (ActionItem i : list) {
			strBldr.append("# " + i.getIssue()+"\n");
			strBldr.append(i.getKey() + "=\n");
		}
		strBldr.append("</pre>");

		for (ActionItem i : list) {
			strBldr.append(tr(td(i.getKey()) + td(i.getIssue().toString())));
		}
		

		return table(strBldr.toString());
	}

	private Map<Language, List<ActionItem>> getActionItems() {
		List<String> keySet = new ArrayList(Arrays.asList(mainProperties.keySet().toArray(new String[] {})));
		Map<Language, List<ActionItem>> ais = new HashMap<Language, List<ActionItem>>();
		Collections.sort(keySet);
		for (Object o : keySet) {
			for (Language lang : supportedLanguages) {
				if ((!translations.get(lang).keySet().contains(o))) {
					ActionItem ai = new ActionItem();
					ai.setIssue(Issue.MISSING);
					ai.setKey(o.toString());
					ai.setLang(lang);
					ai.setOwner(new User());
					if (!ais.keySet().contains(lang)) {
						ais.put(lang, new ArrayList<ActionItem>());
					}
					ais.get(lang).add(ai);
				}
			}
		}
		return ais;
	}

	private String aName(String name, String title) {
		return "<a name=\"" + name + "\">" + title + "</a>";
	}

	class ActionItem {
		Language lang;
		String key;
		Issue issue;
		User owner;

		public Language getLang() {
			return lang;
		}

		public void setLang(Language lang) {
			this.lang = lang;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Issue getIssue() {
			return issue;
		}

		public void setIssue(Issue issue) {
			this.issue = issue;
		}

		public User getOwner() {
			return owner;
		}

		public void setOwner(User owner) {
			this.owner = owner;
		}
	}

	class Summary {
		public Summary() {
		}
	}

	Summary summary = new Summary();
	private Map<Language, Properties> translations;
	private Properties mainProperties;

	private String getSummary() {
		StringBuilder content = new StringBuilder();
		content.append(getHeader("Issues", Arrays.asList("NO ENTRY")));
		Map<Language, Integer> noEntryMap = getNoEntryMap(mainProperties, translations, supportedLanguages);
		content.append(tr(getSummaryRow(noEntryMap)));
		return table(content.toString());
	}

	/**
	 * 
	 * @param mainProperties
	 * @param translations
	 * @param langs
	 * @return
	 */
	public Map<Language, Integer> getNoEntryMap(Properties mainProperties, Map<Language, Properties> translations,
	                                            List<Language> langs) {
		Map<Language, Integer> map = new HashMap<Language, Integer>();
		for (Language lang : langs) {
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

	private String getSummaryRow(Map<Language, Integer> map) {
		final int maxSize = 20;
		final int minSize = 6;
		int c = map.size();
		int tc = 0;
		for (Integer p : map.values()) {
			tc += p;
		}
		float middle = tc / c;
		StringBuilder line = new StringBuilder();
		for (Language l : map.keySet()) {
			line.append(tr(td(a(getActionItemName() + l.toString() + ".html", l.toString()))
			        + td(Integer.toString(map.get(l)))));
		}
		return line.toString();
	}

	private String getActionItemName() {
		return getClass().getSimpleName() + "ActionItems_";
	}

	private String getOverview() {
		StringBuilder content = new StringBuilder();
		content.append(getHeader("Translation", supportedLanguages));
		List<String> listOfKeys = new ArrayList(Arrays.asList(mainProperties.keySet().toArray(new String[] {})));
		content.append(getOverviewContent(translations, supportedLanguages, listOfKeys));
		return table(content.toString());
	}

	private String getOverviewContent(Map<Language, Properties> translations, List<Language> langs, List<String> keySet) {
		StringBuilder content = new StringBuilder();
		Collections.sort(keySet);
		for (Object o : keySet) {
			StringBuilder line = new StringBuilder();
			line.append(th(o.toString()));
			for (Language lang : langs) {
				if ((!translations.get(lang).keySet().contains(o))) {
					line.append(td(a("#" + lang.toString(), "NE")));
				}
				else {
					line.append(td(""));
				}
			}
			content.append(tr(line.toString()));
		}
		return content.toString();
	}

	private String a(String href, String title) {
		return ("<a href=\"" + href + "\">" + title + "</a>");
	}

	String getHeader(String title, List<?> tds) {
		StringBuilder content = new StringBuilder();
		for (Object lang : tds) {
			content.append(td(lang.toString()));
		}
		return tr(th(title) + content.toString());
	}

	private String tr(String content) {
		return addTag("tr", content) + "\n";
	}

	private String td(String translation) {
		return addTag("td", translation);
	}

	private String th(String content) {
		return addTag("th", content);
	}

	private String table(String content) {
		return addTag("table", content);
	}

	private String addTag(String tag, String content) {
		return "<" + tag + ">" + content + "</" + tag + ">";
	}

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
}
