package org.docear.plugin.core.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class SwingWorkerDialogLite extends JDialog implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private ExecutorService executor;
	private SwingWorker<?,?> thread;
	private JProgressBar progressBar;
	private JLabel labelHeadline;
	
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
	
	
	public SwingWorkerDialogLite() {
		init();
	}
	
	public SwingWorkerDialogLite(Frame parent) {
		super(parent);		
		init();		
		this.setHeadlineText("");
		this.setLocationRelativeTo(parent);
	}
	
	/**
	 * Create the dialog.
	 */
	private void init() {
		setUndecorated(true);
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 65);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new BorderLayout(0, 0));
			{
				labelHeadline = new JLabel("<HeadLine>");
				panel.add(labelHeadline, BorderLayout.CENTER);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(5, 5));
			{
				progressBar = new JProgressBar();
				buttonPane.add(progressBar);
			}
		}
	}
	
	public void setHeadlineText(String text){
		this.labelHeadline.setText(text);
	}
	
	private void done() {
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

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(SwingWorkerDialog.PROGRESS)){
			progressBar.setValue((Integer)evt.getNewValue());
		}
		if(evt.getPropertyName().equals(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE)){
			this.progressBar.setIndeterminate(true);			
			this.repaint();
		}
		if(evt.getPropertyName().equals(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE)){					
			this.progressBar.setIndeterminate(false);
			this.repaint();
		}
		if(evt.getPropertyName().equals(SwingWorkerDialog.REPAINT)){
			this.repaint();
		}
		if(evt.getPropertyName().equals(SwingWorkerDialog.SET_HEADLINE)){
			this.setHeadlineText("" + evt.getNewValue());
		}
		if(evt.getPropertyName().equals(SwingWorkerDialog.IS_DONE)){
			this.progressBar.setIndeterminate(false);
			this.progressBar.setValue(100);			
			this.done();
		}
	}

}
