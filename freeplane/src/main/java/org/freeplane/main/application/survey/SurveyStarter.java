package org.freeplane.main.application.survey;

import java.io.InputStream;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.freeplane.main.application.ApplicationLifecycleListener;

public class SurveyStarter implements ApplicationLifecycleListener{
	
	enum RunningPoint{
		ON_START, ON_QUIT, NEVER
	}
	
	private static final String SURVEY_ID_KEY = "surveyId";
	private static final String SURVEY_URL_KEY = "surveyUrl";
	private static final String QUESTION_KEY = "question";
	private static final String TITLE_KEY = "title";
	private static final String RUN_ON_KEY = "runOn";
	private static final String FREQUENCY_KEY = "frequency";


	private final FreeplaneSurveyProperties freeplaneSurveyProperties;
	private final SurveyRunner surveyRunner;
	private String surveyId;
	private String title;
	private String question;
	private String surveyUrl;
	private RunningPoint runOn;
	private final double randomNumber;

	public SurveyStarter(FreeplaneSurveyProperties freeplaneSurveyProperties, SurveyRunner surveyRunner, double randomNumber) {
		this.freeplaneSurveyProperties = freeplaneSurveyProperties;
		this.surveyRunner = surveyRunner;
		this.randomNumber = randomNumber;
	}

	@Override
	public void onStartupFinished() {
		if (freeplaneSurveyProperties.mayAskUserAgain()) {
			freeplaneSurveyProperties.setNextSurveyDay(1);
			new Thread(new Runnable() {
				@Override
				public void run() {
					final Properties surveyProperties = new Properties();
					try (final InputStream input = freeplaneSurveyProperties.openRemoteConfiguration()) {
						surveyProperties.load(input);
						final int frequency = Integer.parseInt(surveyProperties.getProperty(FREQUENCY_KEY));
						if(frequency > 0 && randomNumber < 1. / frequency || freeplaneSurveyProperties.remindMeLaterIsActive()) {
							surveyId = surveyProperties.getProperty(SURVEY_ID_KEY);
							title = surveyProperties.getProperty(TITLE_KEY);
							question = surveyProperties.getProperty(QUESTION_KEY);
							surveyUrl = surveyProperties.getProperty(SURVEY_URL_KEY);
							runOn = RunningPoint.valueOf(surveyProperties.getProperty(RUN_ON_KEY, RunningPoint.NEVER.name()));
						}
						else
							runOn = RunningPoint.NEVER;
					} catch (Exception e) {
						runOn = RunningPoint.NEVER;
					}
					if(RunningPoint.ON_START == runOn)
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								surveyRunner.runServey(surveyId, title, question, surveyUrl);
							}
						});
				}
			}).start();
		}
		
	}

	@Override
	public void onApplicationStopped() {
		if(RunningPoint.ON_QUIT == runOn)
			surveyRunner.runServey(surveyId, title, question, surveyUrl);
	}

}
