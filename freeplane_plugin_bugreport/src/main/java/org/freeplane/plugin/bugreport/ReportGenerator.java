package org.freeplane.plugin.bugreport;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.resizer.UIComponentVisibilityDispatcher;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

public class ReportGenerator extends StreamHandler {
	private static final String BUGREPORT_USER_ID = "org.freeplane.plugin.bugreport.userid";
	private static final String REMOTE_LOG = "RemoteLog";
	private static final String NO_REPORTS_SENT_BEFORE = "no reports sent before";
	static final String LAST_BUG_REPORT_INFO = "last_bug_report_info";

	private class SubmitRunner implements Runnable {
		public SubmitRunner() {
		}

		@Override
		public void run() {
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					runSubmit();
					return null;
				}
			});
		}
	}

	private final static String BUG_TRACKER_REFERENCE_URL = "https://www.freeplane.org/info/bugtracker.ref.txt";
	private static String BUG_TRACKER_URL = null;
	static boolean isDisabled = false;
	private static int errorCounter = 0;
	private static String info;
	static final private String OPTION = "org.freeplane.plugin.bugreport";
	private static ByteArrayOutputStream out = null;
	private static String version;
	private static String revision;

	private static String toHexString(final byte[] v) {
		final String HEX_DIGITS = "0123456789abcdef";
		final StringBuffer sb = new StringBuffer(v.length * 2);
		for (int i = 0; i < v.length; i++) {
			final int b = v[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4)).append(HEX_DIGITS.charAt(b & 0xF));
		}
		return sb.toString();
	}

	private String hash = null;
	private String log = null;
	private MessageDigest md = null;
	private boolean isReportGenerationInProgress = false;
	private IBugReportListener bugReportListener;

	public IBugReportListener getBugReportListener() {
		return bugReportListener;
	}

	public void setBugReportListener(final IBugReportListener bugReportListener) {
		this.bugReportListener = bugReportListener;
	}

	public ReportGenerator() {
		super();
		try {
			setEncoding("UTF-8");
		}
		catch (final SecurityException e) {
		}
		catch (final UnsupportedEncodingException e) {
		}
		setFormatter(new BugFormatter());
		setLevel(Level.SEVERE);
	}

	private String calculateHash(final String errorMessage) {
		final String[] lines = errorMessage.split("\n");
		final StringBuffer hashInput = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			final String s = lines[i];
			if (s.startsWith("\tat org.freeplane.")
			        || s.startsWith("missing key ")) {
				hashInput.append(s);
			}
		}
		if (hashInput.length() == 0) {
			return null;
		}
		hashInput.append(version);
		hashInput.append(revision);
		try {
			return calculateHash(hashInput.toString().getBytes(getEncoding()));
		}
		catch (final UnsupportedEncodingException e) {
			return null;
		}
	}

	private String calculateHash(final byte[] byteArray) {
		try {
			if (md == null) {
				md = MessageDigest.getInstance("MD5");
			}
			final byte[] digest = md.digest(byteArray);
			return ReportGenerator.toHexString(digest);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			return null;
		}
	}

	private void createInfo() {
		if (info == null) {
			final StringBuilder sb = new StringBuilder();
			sb.append("freeplane_version = ");
			version = FreeplaneVersion.getVersion().toString();
			sb.append(version);
			sb.append("; freeplane_xml_version = ");
			sb.append(FreeplaneVersion.XML_VERSION);
			revision = FreeplaneVersion.getVersion().getRevision();
			if (!revision.equals("")) {
				sb.append("\ngit revision = ");
				sb.append(revision);
			}
			sb.append("\njava_version = ");
			sb.append(System.getProperty("java.version"));
			sb.append("; os_name = ");
			sb.append(System.getProperty("os.name"));
			sb.append("; os_version = ");
			sb.append(System.getProperty("os.version"));
			sb.append('\n');
			info = sb.toString();
		}
	}

	private String getBugTrackerUrl() {
		if (BUG_TRACKER_URL != null) {
			return BUG_TRACKER_URL;
		}
		try {
			final URL url = new URL(BUG_TRACKER_REFERENCE_URL);
			final BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
			BUG_TRACKER_URL = in.readLine();
			return BUG_TRACKER_URL;
		}
		catch (final Exception e) {
			isDisabled = true;
			return null;
		}
	}

	private static class LogOpener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final String freeplaneLogDirectoryPath = LogUtils.getLogDirectory();
			final File file = new File(freeplaneLogDirectoryPath);
			if (file.isDirectory()) {
				final ViewController viewController = Controller.getCurrentController().getViewController();
				try {
					viewController.openDocument(file.toURL());
				}
				catch (Exception ex) {
				}
			}
		}
	}

	private JButton logButton;

	@Override
	public synchronized void publish(final LogRecord record) {
		final Controller controller = Controller.getCurrentController();
		if (controller == null) {
			// ReportGenerator is not available during controller initialization
			return;
		}
		final ViewController viewController = controller.getViewController();
		if (viewController == null) {
			// ReportGenerator is not available during controller initialization
			return;
		}
		if (out == null) {
			out = new ByteArrayOutputStream();
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					setOutputStream(out);
					return null;
				}
			});
		}
		if (!isLoggable(record)) {
			return;
		}
		if (!(isReportGenerationInProgress)) {
			isReportGenerationInProgress = true;
			viewController.invokeLater(new Runnable() {
				@Override
				@SuppressWarnings("serial")
				public void run() {
					try {
						errorCounter++;
						if (TextUtils.getRawText("internal_error.tooltip", null) != null) {
							if (logButton == null) {
								final Icon errorIcon = ResourceController.getResourceController()
								    .getIcon("messagebox_warning_icon");
								logButton = new JButton() {
									@Override
									public Dimension getPreferredSize() {
										Dimension preferredSize = super.getPreferredSize();
										preferredSize.height = Math.max(getIcon().getIconHeight(), getFont().getSize());
										return preferredSize;
									}
								};
								logButton.addActionListener(new LogOpener());
								logButton.setIcon(errorIcon);
								String tooltip = TextUtils.getText("internal_error.tooltip");
								logButton.setToolTipText(tooltip);
								viewController.addStatusComponent("internal_error", logButton);
							}
							logButton.setText(TextUtils.format("errornumber", errorCounter));
							final JComponent statusBar = viewController.getStatusBar();
							if (!statusBar.isVisible())
								UIComponentVisibilityDispatcher.of(statusBar).setVisible(true);
						}
					}
					catch (Exception e) {
					}
					runSubmitAfterTimeout();
				}
			});
		}
		if (!isDisabled)
			super.publish(record);
	}

	private void runSubmit() {
		try {
			close();
			final String errorMessage = out.toString(getEncoding());
			if (errorMessage.indexOf(getClass().getPackage().getName()) != -1) {
				// avoid infinite loops
				System.err.println("don't send bug reports from bugreport plugin");
				return;
			}
			createInfo();
			hash = calculateHash(errorMessage);
			if (hash == null) {
				return;
			}
			final String reportHeader = createReportHeader();
			StringBuilder sb = new StringBuilder();
			sb.append(reportHeader).append('\n').append("previous report : ");
			String lastReportInfo = ResourceController.getResourceController().getProperty(LAST_BUG_REPORT_INFO,
			    NO_REPORTS_SENT_BEFORE);
			sb.append(lastReportInfo).append('\n');
			final String userId = ResourceController.getResourceController().getProperty(BUGREPORT_USER_ID);
			if (userId.length() > 0) {
				sb.append("user : ").append(userId).append('\n');
			}
			sb.append(info);
			sb.append(errorMessage);
			log = sb.toString();
			if (log.equals("")) {
				return;
			}
			final ReportRegistry register = ReportRegistry.getInstance();
			if (register.isReportRegistered(hash)) {
				return;
			}
			final String option = showBugReportDialog();
			if (BugReportDialogManager.ALLOWED.equals(option)) {
				register.registerReport(hash, reportHeader);
				final Map<String, String> report = new LinkedHashMap<String, String>();
				report.put("hash", hash);
				report.put("log", log);
				report.put("version", version);
				report.put("revision", revision);
				String status = AccessController.doPrivileged(new PrivilegedAction<String>() {
					@Override
					public String run() {
						final String status = sendReport(report);
						return status;
					}
				});
				if (bugReportListener != null && status != null) {
					bugReportListener.onReportSent(report, status);
				}
			}
		}
		catch (final UnsupportedEncodingException e) {
			LogUtils.severe(e);
		}
		finally {
			out = null;
			isReportGenerationInProgress = false;
		}
	}

	private String createReportHeader() {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		String time = dateFormatGmt.format(new Date());
		final String currentReportInfo = "at " + time + " CMT,  hash " + hash;
		return currentReportInfo;
	}

	private String showBugReportDialog() {
		final ResourceController resourceController = ResourceController.getResourceController();
		String option = resourceController.getProperty(OPTION, BugReportDialogManager.ASK);
		if (option.equals(BugReportDialogManager.ASK)) {
			if (resourceController.getBooleanProperty("org.freeplane.plugin.bugreport.dialog.disabled") || isHeadlessMode())
				return BugReportDialogManager.DENIED;
			String question = TextUtils.getText("org.freeplane.plugin.bugreport.question");
			if (!question.startsWith("<html>")) {
				question = HtmlUtils.plainToHTML(question);
			}
			final Object[] options = new Object[] { TextUtils.getText("org.freeplane.plugin.bugreport.always_agree"),
			        TextUtils.getText("org.freeplane.plugin.bugreport.agree"),
			        TextUtils.getText("org.freeplane.plugin.bugreport.deny"),
			        TextUtils.getText("org.freeplane.plugin.bugreport.always_deny") };
			final String title = TextUtils.getText("org.freeplane.plugin.bugreport.dialog.title");
			final String reportName = TextUtils.getText("org.freeplane.plugin.bugreport.report");
			final int choice = BugReportDialogManager.showBugReportDialog(title, question,
			    JOptionPane.INFORMATION_MESSAGE, options, options[1], reportName, log);
			switch (choice) {
				case 0:
					option = BugReportDialogManager.ALLOWED;
					resourceController.setProperty(OPTION, option);
					break;
				case 1:
					option = BugReportDialogManager.ALLOWED;
					break;
				case 2:
					option = BugReportDialogManager.DENIED;
					break;
				case 3:
					option = BugReportDialogManager.DENIED;
					resourceController.setProperty(OPTION, option);
					break;
				default:
					option = BugReportDialogManager.DENIED;
					break;
			}
		}
		return option;
	}

	private boolean isHeadlessMode() {
		return Controller.getCurrentController().getViewController().isHeadless();
	}

	private String sendReport(final Map<String, String> reportFields) {
		try {
			// Construct data
			final StringBuilder data = new StringBuilder();
			for (final Entry<String, String> entry : reportFields.entrySet()) {
				if (data.length() != 0) {
					data.append('&');
				}
				data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				data.append('=');
				data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			// Send data
			final URL url = new URL(getBugTrackerUrl());
			final URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			final String report = data.toString();
			wr.write(report);
			wr.flush();
			// Get the response
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String line = rd.readLine();
			if (line != null) {
				System.out.println(line);
			}
			wr.close();
			rd.close();
			return line;
		}
		catch (final Exception e) {
		}
		return null;
	}

	private void runSubmitAfterTimeout() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				Controller.getCurrentController().getViewController().invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!isDisabled) {
							final Thread submitterThread = new Thread(new SubmitRunner(), REMOTE_LOG);
							submitterThread.start();
						}
						else
							isReportGenerationInProgress = false;
					}
				});
			}
		}).start();
	}
}
