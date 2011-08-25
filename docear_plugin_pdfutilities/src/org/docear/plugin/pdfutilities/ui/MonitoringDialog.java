package org.docear.plugin.pdfutilities.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.jdesktop.swingworker.SwingWorker;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

public class MonitoringDialog extends JDialog implements PropertyChangeListener {

	public static final String SET_PROGRESS_BAR_DETERMINATE = "setProgressBardeterminate";
	public static final String SET_PROGRESS_BAR_INDETERMINATE = "setProgressBarIndeterminate";
	public static final String IS_DONE = "isDone";
	public static final String NEW_NODES = "newNodes";
	public static final String NEW_FILE = "newFile";
	public static final String PROGRESS = "progress";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JProgressBar progressBar;
	private JLabel lblWorkingOn;
	private JTextArea textArea;
	private JButton okButton;
	private SwingWorker<?,?> thread;

	/**
	 * Launch the application.
	 */
	public void showDialog(SwingWorker<?,?> thread) {
		this.thread = thread;
		thread.addPropertyChangeListener(this);
		thread.execute();
		this.setVisible(true);		
	}

	/**
	 * Create the dialog.
	 */
	public MonitoringDialog() {		
		init();
	}
	
	public MonitoringDialog(Frame parent) {
		super(parent);		
		init();		
		this.setLocationRelativeTo(parent);
	}

	private void init() {
		this.addWindowStateListener(new WindowStateListener() {
			
			public void windowStateChanged(WindowEvent e) {
				if(e.getID() == WindowEvent.WINDOW_CLOSING || e.getID() == WindowEvent.WINDOW_CLOSED){
					onCancel();
				}
				
			}
		});
		
		this.addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {}
			
			public void windowIconified(WindowEvent e) {}
			
			public void windowDeiconified(WindowEvent e) {}
			
			public void windowDeactivated(WindowEvent e) {}
			
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
			
			public void windowClosed(WindowEvent e) {
				onCancel();
			}
			
			public void windowActivated(WindowEvent e) {}
		});
		
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.SOUTH);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel_1.add(panel_2, BorderLayout.NORTH);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						progressBar = new JProgressBar();
						panel_2.add(progressBar, BorderLayout.NORTH);
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel_1.add(panel_2, BorderLayout.SOUTH);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						lblWorkingOn = new JLabel("Working on <File>");
						panel_2.add(lblWorkingOn);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new EmptyBorder(5, 5, 5, 5));
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JScrollPane scrollPane = new JScrollPane();
					panel_1.add(scrollPane, BorderLayout.CENTER);
					{
						textArea = new JTextArea();
						textArea.setEditable(false);
						scrollPane.setViewportView(textArea);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOK();
					}
				});
				okButton.setEnabled(false);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	protected void onCancel() {
		this.thread.cancel(true);
		this.dispose();
	}

	protected void onOK() {
		this.dispose();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(PROGRESS)){
			progressBar.setValue((Integer)evt.getNewValue());
		}
		if(evt.getPropertyName().equals(NEW_FILE)){
			lblWorkingOn.setText("Importing " + evt.getNewValue());
			//textArea.append("\n-----------------------------------------\n");
			//textArea.append("Importing " + evt.getNewValue() +"\n\n");
		}
		if(evt.getPropertyName().equals(NEW_NODES)){
			@SuppressWarnings("unchecked")
			Collection<AnnotationModel> annotations = (Collection<AnnotationModel>)evt.getNewValue();
			for(AnnotationModel annotation : annotations){
				textArea.append("Imported " + annotation.getTitle() +"\n");
			}			
		}
		if(evt.getPropertyName().equals(IS_DONE)){
			this.progressBar.setValue(100);
			lblWorkingOn.setText("Import complete.");
			okButton.setEnabled(true);
		}
		if(evt.getPropertyName().equals(SET_PROGRESS_BAR_INDETERMINATE)){
			this.progressBar.setIndeterminate(true);
			lblWorkingOn.setText("Reading files...");			
		}
		if(evt.getPropertyName().equals(SET_PROGRESS_BAR_DETERMINATE)){			
			this.progressBar.setIndeterminate(false);
		}
	}

}
