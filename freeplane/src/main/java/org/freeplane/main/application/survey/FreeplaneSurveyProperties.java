package org.freeplane.main.application.survey;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.freeplane.core.resources.ResourceController;

public class FreeplaneSurveyProperties {
	static private final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000; 
	static final String NEVER_SHOW_SURVEY_PROPERTY = "neverShowSurvey";
	static final String NEXT_SURVEY_CHECK_DAY_PROPERTY = "nextSurveyCheck";
	private static final String REMIND_ME_LATER_PROPERTY = "remindMeAboutSurveyIsActive";
	static final String FILLED_SURVEY = "filledSurvey";
	private static final String SURVEY_URL_PROPERTY = "surveyUrl";
	
	final private ResourceController resourceController;
	final private long nextCheckDay;
	private boolean remindMeLaterIsActive;
	
	public FreeplaneSurveyProperties() {
		resourceController = ResourceController.getResourceController();
		nextCheckDay = resourceController.getLongProperty(NEXT_SURVEY_CHECK_DAY_PROPERTY, 0);
		remindMeLaterIsActive = resourceController.getBooleanProperty(REMIND_ME_LATER_PROPERTY, false);
		if(remindMeLaterIsActive)
			resourceController.setProperty(REMIND_ME_LATER_PROPERTY, false);
	}

	void setNextSurveyDay(int minimalDaysBetweenSurveys) {
		resourceController.setProperty(NEXT_SURVEY_CHECK_DAY_PROPERTY, dayNow() + minimalDaysBetweenSurveys);
	}
	
	long getNextCheckDay() {
		return nextCheckDay;
	}
	
	private long dayNow() {
		return new Date().getTime() / ONE_DAY_IN_MILLISECONDS;
	}
	void markSurveyAsFilled(String surveyId) {
		resourceController.setProperty(filledSurveyPropertyOf(surveyId), "true");
	}
	private String filledSurveyPropertyOf(String surveyId) {
		return FILLED_SURVEY + "." + surveyId;
	}
	
	void setNeverShowSurvey() {
		resourceController.setProperty(NEVER_SHOW_SURVEY_PROPERTY, "true");
	}
	
	public boolean mayAskUserToFillSurveys() {
		return ! resourceController.getBooleanProperty(NEVER_SHOW_SURVEY_PROPERTY)
				&& isLanguageSupported();
	}
	private boolean isLanguageSupported() {
		return resourceController.getLanguageCode().equals("en");
	}
	
	boolean mayAskUserToFillSurvey(String surveyId) {
		return  mayAskUserToFillSurveys() && ! hasUserAlreadyFilled(surveyId) && mayAskUserAgain();
	}
	
	boolean mayAskUserAgain() {
		return dayNow() > getNextCheckDay();
	}
	
	private boolean hasUserAlreadyFilled(String surveyId) {
		return Boolean.parseBoolean(resourceController.getProperty(filledSurveyPropertyOf(surveyId), "false"));
	}
	
	private URL getSurveyUrl() {
		try {
			String surveyUrl = resourceController.getProperty(SURVEY_URL_PROPERTY) + "_en.properties";
			return new URL(surveyUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public InputStream openRemoteConfiguration() throws IOException {
		return getSurveyUrl().openStream();
	}

	public void activateRemindMeLater() {
		resourceController.setProperty(REMIND_ME_LATER_PROPERTY, "true");
	}

	public boolean remindMeLaterIsActive() {
		return remindMeLaterIsActive;
	}
	
}