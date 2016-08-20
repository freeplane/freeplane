package org.freeplane.main.application.survey;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.freeplane.core.resources.ResourceController;

public class FreeplaneSurveyProperties {
	static private final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000; 
	static final String NEVER_SHOW_SURVEY_PROPERTY = "neverShowSurvey";
	static final String NEXT_SURVEY_CHECK_PROPERTY = "nextSurveyCheck";
	static final String FILLED_SURVEY = "filledSurvey";
	private static final String SURVEY_URL_PROPERTY = "surveyUrl";
	final ResourceController resourceController = ResourceController.getResourceController();
	void setNextSurveyTime(int minimalDaysBetweenSurveys) {
		resourceController.setProperty(NEXT_SURVEY_CHECK_PROPERTY, dayNow() + minimalDaysBetweenSurveys);
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
		return  mayAskUserToFillSurveys() && ! hasUserAlreadyFilled(surveyId) && canAskAgain();
	}
	
	private boolean canAskAgain() {
		final long nextCheckDay = resourceController.getLongProperty(NEXT_SURVEY_CHECK_PROPERTY, 0);
		return dayNow() > nextCheckDay;
	}
	private boolean hasUserAlreadyFilled(String surveyId) {
		return Boolean.parseBoolean(resourceController.getProperty(filledSurveyPropertyOf(surveyId), "false"));
	}
	
	public URL getSurveyUrl() {
		try {
			String surveyUrl = resourceController.getProperty(SURVEY_URL_PROPERTY) + "_en.properties";
			return new URL(surveyUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
}