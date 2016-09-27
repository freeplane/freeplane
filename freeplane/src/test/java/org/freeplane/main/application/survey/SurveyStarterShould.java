package org.freeplane.main.application.survey;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class SurveyStarterShould {

	private static final String SURVEY_ID_KEY = "surveyId";
	private static final String SURVEY_URL_KEY = "surveyUrl";
	private static final String QUESTION_KEY = "question";
	private static final String TITLE_KEY = "title";
	private static final String RUN_ON_KEY = "runOn";
	private static final String FREQUENCY_KEY = "frequency";

	@Test
	public void givenRunOnStartIsTrue_loadPropertiesOnStart() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty(RUN_ON_KEY, "ON_START");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(surveyRunner).runServey("myId", "myTitle", "myQuestion", "mySurveyUrl");
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
		configProperties.setProperty(RUN_ON_KEY, "NEVER");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(surveyRunner, never()).runServey("myId", "myTitle", "myQuestion", "mySurveyUrl");
	}

	@Test
	public void givenRunOnStartIsTrue_loadPropertiesOnFinish() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty(RUN_ON_KEY, "ON_QUIT");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		surveyStarter.onApplicationStopped();
		verify(surveyRunner).runServey("myId", "myTitle", "myQuestion", "mySurveyUrl");
	}

	private Properties createSurveyProperites() {
		Properties configProperties = new Properties();
		configProperties.setProperty(SURVEY_ID_KEY, "myId");
		configProperties.setProperty(TITLE_KEY, "myTitle");
		configProperties.setProperty(QUESTION_KEY, "myQuestion");
		configProperties.setProperty(SURVEY_URL_KEY, "mySurveyUrl");
		configProperties.setProperty(FREQUENCY_KEY, "2");
		return configProperties;
	}

	private FreeplaneSurveyProperties mockSurveyProperties(Properties configProperties)
			throws IOException, FileNotFoundException, MalformedURLException {
		final File configFile = File.createTempFile("survey", ".properties");
		configFile.deleteOnExit();
		try (final FileOutputStream propertyStream = new FileOutputStream(configFile)){
			configProperties.store(propertyStream, "");
		}
		final URL configurationUrl = configFile.toURL();
		FreeplaneSurveyProperties freeplaneSurveyProperties = mock(FreeplaneSurveyProperties.class);
		when(freeplaneSurveyProperties.openRemoteConfiguration()).thenAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return configurationUrl.openStream();
			}
		});
		when(freeplaneSurveyProperties.mayAskUserAgain()).thenReturn(true);
		return freeplaneSurveyProperties;
	}

	@Test
	public void givenMayNotAskAgain_doesNotRequestSurveyConfiguration() throws Exception {
		Properties configProperties = createSurveyProperites();
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		when(freeplaneSurveyProperties.mayAskUserAgain()).thenReturn(false);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		verify(freeplaneSurveyProperties).mayAskUserAgain();
		verifyNoMoreInteractions(freeplaneSurveyProperties, surveyRunner);
	}
	
	@Test
	public void givenMayAskAgain_setsNextMinimalServerCheckDay() throws Exception {
		Properties configProperties = createSurveyProperites();
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		when(freeplaneSurveyProperties.mayAskUserAgain()).thenReturn(true);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		verify(freeplaneSurveyProperties).setNextSurveyDay(1);
	}
	

	@Test
	public void givenTooRandomNumberLargerThanMiddleRunFrequency_doesNotRunServey() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty(RUN_ON_KEY, "ON_START");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.51);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(freeplaneSurveyProperties).mayAskUserAgain();
		verify(freeplaneSurveyProperties).setNextSurveyDay(1);
		verify(freeplaneSurveyProperties).openRemoteConfiguration();
		verifyNoMoreInteractions(freeplaneSurveyProperties, surveyRunner);
	}


	@Test
	public void givenNonPositiveMiddleCheckRunPeriodMiddleRunFrequency_doesNotRunServey() throws Exception {
		Properties configProperties = createSurveyProperites();
		configProperties.setProperty(RUN_ON_KEY, "ON_START");
		configProperties.setProperty(FREQUENCY_KEY, "0");
		final SurveyRunner surveyRunner = mock(SurveyRunner.class);
		FreeplaneSurveyProperties freeplaneSurveyProperties = mockSurveyProperties(configProperties);
		final SurveyStarter surveyStarter = new SurveyStarter(freeplaneSurveyProperties, surveyRunner, 0.49);
		surveyStarter.onStartupFinished();
		waitForOtherThreadToRun();
		verify(freeplaneSurveyProperties).mayAskUserAgain();
		verify(freeplaneSurveyProperties).setNextSurveyDay(1);
		verify(freeplaneSurveyProperties).openRemoteConfiguration();
		verifyNoMoreInteractions(freeplaneSurveyProperties, surveyRunner);
	}
}
