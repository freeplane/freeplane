package org.freeplane.main.application.survey;

import java.awt.Image;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.URL;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;

public class SurveyRunner {

	private static final String YES = "Yes";
	private static final String NO = "No";
	private static final String LATER = "Remind me later";
	private static final String NEVER = "Never ask me for help";

	private static final int YES_OPTION = 0;
	private static final int NO_OPTION = 1;
	private static final int LATER_OPTION = 2;
	private static final int NEVER_OPTION = 3;
	private static final int MINIMAL_DAYS_BETWEEN_SURVEYS = 11;
	private static final int MINIMAL_DAYS_BETWEEN_SURVEY_REMINDERS = 3;
	
	private final FreeplaneSurveyProperties freeplaneSurveyProperties;
	private String surveyId;

	public SurveyRunner(FreeplaneSurveyProperties freeplaneSurveyProperties) {
		super();
		this.freeplaneSurveyProperties = freeplaneSurveyProperties;
	}

	public void runServey(String id, String title, String question, String surveyUrl) {
		if(! freeplaneSurveyProperties.mayAskUserToFillSurvey(surveyId))
			return;
		this.surveyId = id;
		freeplaneSurveyProperties.setNextSurveyTime(MINIMAL_DAYS_BETWEEN_SURVEYS);
		final String[] options = new String[]{YES, NO, LATER, NEVER};
		final List<Image> iconImages = UITools.getFrame().getIconImages();
		final JLabel messageComponent = new JLabel(question);
		messageComponent.addHierarchyListener(new HierarchyListener() {
			
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(messageComponent.isShowing()){
					messageComponent.removeHierarchyListener(this);
					SwingUtilities.getWindowAncestor(messageComponent).setIconImages(iconImages);
				}
				
			}
		});
		final int userDecision = JOptionPane.showOptionDialog(UITools.getCurrentFrame(), messageComponent, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null,
				options, YES);
		switch(userDecision) {
		case YES_OPTION: 
			try {
				freeplaneSurveyProperties.markSurveyAsFilled(surveyId);
				final URL survey = new URL(surveyUrl);
				Controller.getCurrentController().getViewController().openDocument(survey);
			} catch (Exception e) {
			}
			break;
		case NO_OPTION:
			freeplaneSurveyProperties.markSurveyAsFilled(surveyId);
			break;
		case LATER_OPTION:
			freeplaneSurveyProperties.setNextSurveyTime(MINIMAL_DAYS_BETWEEN_SURVEY_REMINDERS);
			break;
		case NEVER_OPTION:
			freeplaneSurveyProperties.setNeverShowSurvey();
		}
	}
}
