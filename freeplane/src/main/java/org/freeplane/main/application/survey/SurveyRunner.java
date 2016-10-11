package org.freeplane.main.application.survey;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.mode.Controller;

public class SurveyRunner {

	private static class OptionButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Component source = (Component) e.getSource();
			JOptionPane pane = (JOptionPane) SwingUtilities.getAncestorOfClass(JOptionPane.class, source);
			pane.setValue(source);
		}
	}

	enum Options {GO_OPTION, NOT_INTERESTED_OPTION, REMIND_ME_LATER_OPTION, NEVER_OPTION};
	private static final int MINIMAL_DAYS_BETWEEN_SURVEYS = 11;
	private static final int MINIMAL_DAYS_BETWEEN_SURVEY_REMINDERS = 3;
	
	private final FreeplaneSurveyProperties freeplaneSurveyProperties;
	private String surveyId;
	private boolean userVisitedVotingLink = false;

	public SurveyRunner(FreeplaneSurveyProperties freeplaneSurveyProperties) {
		super();
		this.freeplaneSurveyProperties = freeplaneSurveyProperties;
	}

	public void runServey(String id, String title, String question, String surveyUrl) {
		if(! freeplaneSurveyProperties.mayAskUserToFillSurvey(surveyId))
			return;
		this.surveyId = id;
		freeplaneSurveyProperties.setNextSurveyDay(MINIMAL_DAYS_BETWEEN_SURVEYS);
		final JButton go = new JButton("With pleasure");
		go.setToolTipText("Thank you so much!");
		final JButton notInterested = new JButton("Not interested");
		notInterested.setToolTipText("We shall not repeat this question, but may be ask you another one.");
		final JButton remindMeLater = new JButton("Remind me later");
		remindMeLater.setToolTipText("The same question can be repeated some days later.");
		final JButton never = new JButton("Don't ask me anything again");
		never.setToolTipText("We are sorry! We shall never ask you any question like this again.");
		final JButton[] options = new JButton[]{go, notInterested, remindMeLater, never};
		final OptionButtonListener optionButtonListener = new OptionButtonListener();
		for(JButton button : options)
			button.addActionListener(optionButtonListener);
		final List<Image> iconImages = UITools.getFrame().getIconImages();
		final JEditorPane messageComponent = new JEditorPane("text/html", question);
		messageComponent.setBorder( BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		messageComponent.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						final URL url = e.getURL();
						openSurvey(url);
					} catch (Exception ex) {
					}
					userVisitedVotingLink = true;
					SwingUtilities.getWindowAncestor(messageComponent).setVisible(false);
				}
			}
		});
		messageComponent.setEditable(false);
		messageComponent.addHierarchyListener(new HierarchyListener() {
			
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(messageComponent.isShowing()){
					messageComponent.removeHierarchyListener(this);
					final Window window = SwingUtilities.getWindowAncestor(messageComponent);
					if(window instanceof JDialog)
						((JDialog)window).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					window.setIconImages(iconImages);
				}
				
			}
		});
		final int userDecision = JOptionPane.showOptionDialog(UITools.getCurrentFrame(), messageComponent, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null,
				options, remindMeLater);
		switch(userDecision) {
		case JOptionPane.CLOSED_OPTION:
			if(userVisitedVotingLink)
				freeplaneSurveyProperties.markSurveyAsFilled(surveyId);
			break;
		default:
			switch(Options.values()[userDecision]){
			case GO_OPTION:
				try {
					freeplaneSurveyProperties.markSurveyAsFilled(surveyId);
					final URL survey = new URL(surveyUrl);
					openSurvey(survey);
				} catch (Exception e) {
				}
				break;
			case NOT_INTERESTED_OPTION:
				freeplaneSurveyProperties.markSurveyAsFilled(surveyId);
				break;
			case REMIND_ME_LATER_OPTION:
				freeplaneSurveyProperties.setNextSurveyDay(MINIMAL_DAYS_BETWEEN_SURVEY_REMINDERS);
				freeplaneSurveyProperties.activateRemindMeLater();
				break;
			case NEVER_OPTION:
				freeplaneSurveyProperties.setNeverShowSurvey();
				break;
			}
		}
	}

	private void openSurvey(final URL url) throws Exception {
		Controller.getCurrentController().getViewController().openDocument(url);
	}
}
