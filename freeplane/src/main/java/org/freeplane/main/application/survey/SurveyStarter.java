package org.freeplane.main.application.survey;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.freeplane.main.application.ApplicationLifecycleListener;

public class SurveyStarter implements ApplicationLifecycleListener{
	private static final String SURVEY_ID_KEY = "surveyId";
	private static final String SURVEY_URL_KEY = "surveyUrl";
	private static final String QUESTION_KEY = "question";
	private static final String TITLE_KEY = "title";


	private final URL configurationUrl;
	private final SurveyRunner surveyRunner;
	private String surveyId;
	private String title;
	private String question;
	private String surveyUrl;
	private boolean runOnQuit;
	private boolean runOnStart;

	public SurveyStarter(URL configurationUrl, SurveyRunner surveyRunner) {
		this.configurationUrl = configurationUrl;
		this.surveyRunner = surveyRunner;
	}

	@Override
	public void onStartupFinished() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Properties surveyProperties = new Properties();
				try (final InputStream input = configurationUrl.openStream()) {
					surveyProperties.load(input);
					surveyId = surveyProperties.getProperty(SURVEY_ID_KEY);
					title = surveyProperties.getProperty(TITLE_KEY);
					question = surveyProperties.getProperty(QUESTION_KEY);
					surveyUrl = surveyProperties.getProperty(SURVEY_URL_KEY);
					runOnStart = Boolean.parseBoolean(surveyProperties.getProperty("runOnStart"));
					runOnQuit =  Boolean.parseBoolean(surveyProperties.getProperty("runOnQuit"));
				} catch (Exception e) {
				}
				if(runOnStart)
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							surveyRunner.runServey(surveyId, title, question, surveyUrl);
						}
					});
			}
		}).start();
		
	}

	@Override
	public void onApplicationStopped() {
		if(runOnQuit)
			surveyRunner.runServey(surveyId, title, question, surveyUrl);
	}

}
