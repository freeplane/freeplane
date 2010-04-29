package org.freeplane.plugin.bugreport;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;

class ManualBugReporter implements IBugReportListener {
	final private Controller controller;

	public ManualBugReporter(final Controller controller) {
		super();
		this.controller = controller;
	}

	public void onReportSent(final Map<String, String> report, final String status) {
		if (!status.equals("wanted")) {
			return;
		}
		final String log = report.get("log");
		final String hash = report.get("hash");
		EventQueue.invokeLater(new Runnable() {
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
			controller.getViewController().openDocument(new URL(location));
		}
		catch (final MalformedURLException ex) {
			UITools.errorMessage(ResourceBundles.getText("url_error") + "\n" + ex);
			LogTool.warn(ex);
		}
		catch (final Exception ex) {
			UITools.errorMessage(ex);
			LogTool.warn(ex);
		}
	}

	private String showBugReportDialog(final String log, final String hash) {
		final String title = ResourceBundles.getText("org.freeplane.plugin.bugreport.freeplane_team").replaceAll("\\n",
		    "\n");
		String option = ResourceController.getResourceController().getProperty(OPTION, BugReportDialogManager.ASK);
		if (option.equals(BugReportDialogManager.ASK)) {
			String question = ResourceBundles.getText("org.freeplane.plugin.bugreport.wanted_bug");
			if (!question.startsWith("<html>")) {
				question = HtmlTools.plainToHTML(question);
			}
			final Object[] options = new Object[] { FpStringUtils.removeMnemonic(ResourceBundles.getText("ok")),
			        FpStringUtils.removeMnemonic(ResourceBundles.getText("cancel")),
			        ResourceBundles.getText("org.freeplane.plugin.bugreport.never") };
			final String reportName = ResourceBundles.getText("org.freeplane.plugin.bugreport.lastreport");
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
