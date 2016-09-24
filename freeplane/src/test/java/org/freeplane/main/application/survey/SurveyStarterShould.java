package org.freeplane.main.application.survey;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.junit.Test;


public class SurveyStarterShould {

	private static final String SURVEY_ID_KEY = "surveyId";
	private static final String QUESTION_KEY = "question";
	private static final String TITLE_KEY = "title";

	@Test
	public void givenRunOnStartIsTrue_loadPropertiesOnStart() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty("runOnStart", "true");
		configProperties.setProperty("runOnQuit", "false");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		URL configurationUrl = createSurveyUrl(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(configurationUrl, surveyRunner);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(surveyRunner).runServey("myId", "myTitle", "myQuestion");
	}

	private void waitForOtherThreadToRun() throws InterruptedException, InvocationTargetException {
		Thread.sleep(100);
		SwingUtilities.invokeAndWait(new Runnable() {
			
			@Override
			public void run() {
			}
		});
	}

	@Test
	public void givenRunOnStartIsFalse_loadNoPropertiesOnStart() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty("runOnStart", "false");
		configProperties.setProperty("runOnQuit", "false");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		URL configurationUrl = createSurveyUrl(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(configurationUrl, surveyRunner);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(surveyRunner, never()).runServey("myId", "myTitle", "myQuestion");
	}

	@Test
	public void givenRunOnStartIsTrue_loadPropertiesOnFinish() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty("runOnStart", "false");
		configProperties.setProperty("runOnQuit", "true");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		URL configurationUrl = createSurveyUrl(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(configurationUrl, surveyRunner);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		surveyStarter.onApplicationStopped();
		verify(surveyRunner).runServey("myId", "myTitle", "myQuestion");
	}

	private Properties createSurveyProperites() {
		Properties configProperties = new Properties();
		configProperties.setProperty(SURVEY_ID_KEY, "myId");
		configProperties.setProperty(TITLE_KEY, "myTitle");
		configProperties.setProperty(QUESTION_KEY, "myQuestion");
		return configProperties;
	}

	private URL createSurveyUrl(Properties configProperties)
			throws IOException, FileNotFoundException, MalformedURLException {
		final File configFile = File.createTempFile("survey", ".properties");
		configFile.deleteOnExit();
		try (final FileOutputStream propertyStream = new FileOutputStream(configFile)){
			configProperties.store(propertyStream, "");
		}
		URL configurationUrl = configFile.toURL();
		return configurationUrl;
	}
}
