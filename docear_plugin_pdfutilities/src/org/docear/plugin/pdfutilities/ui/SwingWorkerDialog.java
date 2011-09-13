package org.docear.plugin.pdfutilities.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.BadLocationException;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.jdesktop.swingworker.SwingWorker;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SwingWorkerDialog extends JDialog implements PropertyChangeListener{

	public static final String SET_HEADLINE = "setHeadLine";
	public static final String SET_SUB_HEADLINE = "setSubHeadLine";
	public static final String SET_PROGRESS_BAR_DETERMINATE = "setProgressBardeterminate";
	public static final String SET_PROGRESS_BAR_INDETERMINATE = "setProgressBarIndeterminate";
	public static final String IS_DONE = "isDone";
	public static final String IS_CANCELED = "isCanceled";
	public static final String NEW_NODES = "newNodes";
	public static final String NEW_FILE = "newFile";
	public static final String PROGRESS = "progress";
	public static final String PROGRESS_BAR_TEXT = "progress_bar_text";
	public static final String DETAILS_LOG_TEXT = "details_log_text";
	public static final String REPAINT = "repaint";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SwingWorker<?,?> thread;
	private JPanel mainPanel;
	private JProgressBar progressBar;
	private JScrollPane scrollPane;
	private JTextArea detailsLog;
	private JPanel buttonPanel;
	private JButton detailsButton;
	private JButton okButton;
	private JButton cancelButton;
	private HeaderPanel headerPanel;
	private JPanel labelPanel;
	private JLabel lblWorkingOn;
	private ExecutorService executor;

	/**
	 * Launch the application.
	 */
	public void showDialog(SwingWorker<?,?> thread) {
		executor = Executors.newSingleThreadExecutor();	
		this.thread = thread;
		thread.addPropertyChangeListener(this);
		executor.execute(thread);
		this.setVisible(true);		
	}

	/**
	 * Create the dialog.
	 */
	public SwingWorkerDialog() {		
		init();
	}
	
	public SwingWorkerDialog(Frame parent) {
		super(parent);		
		init();		
		this.lblWorkingOn.setText("");
		this.setLocationRelativeTo(parent);
	}

	private void init() {	
		setResizable(false);				
		setMinimumSize(new Dimension(640, 480));		
		
		
		this.addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {}
			
			public void windowIconified(WindowEvent e) {}
			
			public void windowDeiconified(WindowEvent e) {}
			
			public void windowDeactivated(WindowEvent e) {}
			
			public void windowClosing(WindowEvent e) {				
				onCancel();
			}
			
			public void windowClosed(WindowEvent e) {				
			}
			
			public void windowActivated(WindowEvent e) {}
		});
		
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 610, 403);
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		{
			headerPanel = new HeaderPanel();
			headerPanel.setSubHeadlineText("<SubHeadline Text>");
			headerPanel.setHeadlineText("<Headline Text>");
			getContentPane().add(headerPanel, "1, 1, fill, fill");
		}
		{
			mainPanel = new JPanel();
			mainPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
			getContentPane().add(mainPanel, "1, 2, fill, fill");
			mainPanel.setLayout(new FormLayout(new ColumnSpec[] {
					ColumnSpec.decode("10dlu"),
					ColumnSpec.decode("default:grow"),
					ColumnSpec.decode("10dlu"),},
				new RowSpec[] {
					RowSpec.decode("10dlu"),
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					RowSpec.decode("10dlu"),}));
			{
				progressBar = new JProgressBar();
				mainPanel.add(progressBar, "2, 2");
			}
			{
				labelPanel = new JPanel();
				mainPanel.add(labelPanel, "2, 4, left, fill");
				labelPanel.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,},
					new RowSpec[] {
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,}));
				{
					lblWorkingOn = new JLabel("<working on>");
					lblWorkingOn.setMinimumSize(new Dimension(550, 14));
					lblWorkingOn.setMaximumSize(new Dimension(550, 14));
					lblWorkingOn.setHorizontalTextPosition(SwingConstants.LEFT);
					labelPanel.add(lblWorkingOn, "2, 2, left, default");
				}
			}
			{
				scrollPane = new JScrollPane();
				mainPanel.add(scrollPane, "2, 6, fill, fill");
				{
					detailsLog = new JTextArea();
					detailsLog.setMaximumSize(new Dimension(550, 225));
					detailsLog.setFont(new Font("Tahoma", Font.PLAIN, 11));
					detailsLog.setEditable(false);
					scrollPane.setViewportView(detailsLog);
				}
			}
			{
				buttonPanel = new JPanel();
				mainPanel.add(buttonPanel, "2, 8, right, bottom");
				buttonPanel.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.BUTTON_COLSPEC,
						ColumnSpec.decode("20dlu"),
						FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC,},
					new RowSpec[] {
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,}));
				{
					detailsButton = new JButton("Details...");
					detailsButton.setVisible(false);
					detailsButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onDetails();
						}
					});
					buttonPanel.add(detailsButton, "1, 2");
				}
				{
					okButton = new JButton("Ok");
					okButton.setEnabled(false);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCancel();
						}
					});
					buttonPanel.add(okButton, "3, 2");
				}
				{
					cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCancel();
						}
					});
					buttonPanel.add(cancelButton, "5, 2");
				}
			}
		}
	}

	protected void onDetails() {
		this.scrollPane.setVisible(!this.scrollPane.isVisible());
		this.detailsLog.setVisible(!this.isVisible());
		this.pack();		
	}

	protected void onCancel() {
		// It's very important to set the thread and the executor = null, 
		// because otherwise a reference is hold and the thread never finishes but keeps waiting.
		if(this.thread != null){
			this.thread.cancel(true);			
			this.thread = null;
		}
		if(this.executor != null){			
			this.executor.shutdownNow();			
			this.executor = null;
		}		
		this.dispose();
	}
	
	public void setHeadlineText(String text){
		this.headerPanel.setHeadlineText(text);
	}
	
	public void setSubHeadlineText(String text){
		this.headerPanel.setSubHeadlineText(text);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(PROGRESS)){
			progressBar.setValue((Integer)evt.getNewValue());
		}
		if(evt.getPropertyName().equals(NEW_FILE)){
			lblWorkingOn.setText("Checking " + evt.getNewValue());
			//textArea.append("\n-----------------------------------------\n");
			//textArea.append("Importing " + evt.getNewValue() +"\n\n");
		}
		if(evt.getPropertyName().equals(NEW_NODES)){
			@SuppressWarnings("unchecked")
			Collection<AnnotationModel> annotations = (Collection<AnnotationModel>)evt.getNewValue();
			for(AnnotationModel annotation : annotations){
				if(detailsLog.getLineCount() > 250){
					try {
						detailsLog.replaceRange("", detailsLog.getLineStartOffset(0), detailsLog.getLineStartOffset(1)-1);
					} catch (BadLocationException e) {			
					}
				}
				detailsLog.append("Imported " + annotation.getTitle() +"\n");
			}			
		}
		if(evt.getPropertyName().equals(IS_DONE)){
			this.progressBar.setIndeterminate(false);
			this.progressBar.setValue(100);
			lblWorkingOn.setText("" + evt.getNewValue());
			okButton.setEnabled(true);
			cancelButton.setEnabled(false);
		}
		if(evt.getPropertyName().equals(SET_PROGRESS_BAR_INDETERMINATE)){
			this.progressBar.setIndeterminate(true);
			lblWorkingOn.setText("Reading files...");
			this.repaint();
		}
		if(evt.getPropertyName().equals(SET_PROGRESS_BAR_DETERMINATE)){					
			this.progressBar.setIndeterminate(false);
			this.repaint();
		}
		if(evt.getPropertyName().equals(PROGRESS_BAR_TEXT)){
			lblWorkingOn.setText("" + evt.getNewValue());
		}
		if(evt.getPropertyName().equals(DETAILS_LOG_TEXT)){
			detailsLog.append("" + evt.getNewValue() +"\n");
		}
		if(evt.getPropertyName().equals(IS_CANCELED)){
			this.progressBar.setValue(100);
			lblWorkingOn.setText("" + evt.getNewValue());
			onCancel();
		}
		if(evt.getPropertyName().equals(REPAINT)){
			this.repaint();
		}
		if(evt.getPropertyName().equals(SET_HEADLINE)){
			this.setHeadlineText("" + evt.getNewValue());
		}
		if(evt.getPropertyName().equals(SET_SUB_HEADLINE)){
			this.setSubHeadlineText("" + evt.getNewValue());
		}
	}	

}
