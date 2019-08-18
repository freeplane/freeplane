package org.freeplane.plugin.bugreport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

class ManualBugReporter implements IBugReportListener {
// // 	final private Controller controller;

	public ManualBugReporter() {
		super();
//		this.controller = controller;
	}

	public void onReportSent(final Map<String, String> report, final String status) {
		if (!status.equals("wanted")) {
			return;
		}
		final String log = report.get("log");
		final String hash = report.get("hash");
		Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
			public void run() {
				openBugTracker(log, hash);
			}
		});
	}

	static final private String OPTION = "org.freeplane.plugin.manualbugreport";

	private void openBugTracker(final String log, final String hash) {
		final String option = showBugReportDialog(log, hash);
		if (!BugReportDialogManager.ALLOWED.equals(option)) {
			return;
		}
		try {
			final ResourceController resourceController = ResourceController.getResourceController();
			final String location = resourceController.getProperty("bugTrackerLocation");
			final Controller controller = Controller.getCurrentController();
			controller.getViewController().openDocument(new URL(location));
		}
		catch (final MalformedURLException ex) {
			UITools.errorMessage(TextUtils.getText("url_error") + "\n" + ex);
			LogUtils.warn(ex);
		}
		catch (final Exception ex) {
			UITools.errorMessage(ex);
			LogUtils.warn(ex);
		}
	}

	private String showBugReportDialog(final String log, final String hash) {
		final String title = TextUtils.getText("org.freeplane.plugin.bugreport.freeplane_team").replaceAll("\\n", "\n");
		String option = ResourceController.getResourceController().getProperty(OPTION, BugReportDialogManager.ASK);
		if (option.equals(BugReportDialogManager.ASK)) {
			String question = TextUtils.getText("org.freeplane.plugin.bugreport.wanted_bug");
			if (!question.startsWith("<html>")) {
				question = HtmlUtils.plainToHTML(question);
			}
			final Object[] options = new Object[] { TextUtils.getText("ok"),
			        TextUtils.getText("cancel"),
			        TextUtils.getText("org.freeplane.plugin.bugreport.never") };
			final String reportName = TextUtils.getText("org.freeplane.plugin.bugreport.lastreport");
			final int choice = BugReportDialogManager.showBugReportDialog(title, question,
			    JOptionPane.QUESTION_MESSAGE, options, options[0], reportName, log);
			final ReportRegistry register = ReportRegistry.getInstance();
			if (choice != 2) {
				register.unregisterReport(hash);
			}
			switch (choice) {
				case 0:
					option = BugReportDialogManager.ALLOWED;
					break;
				case 1:
					option = BugReportDialogManager.DENIED;
					break;
				case 2:
					option = BugReportDialogManager.DENIED;
					ResourceController.getResourceController().setProperty(OPTION, option);
					break;
				default:
					option = BugReportDialogManager.DENIED;
					break;
			}
		}
		return option;
	}
}
