package org.docear.plugin.communications.components.dialog;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

import org.freeplane.core.util.TextUtils;

public class DocearServiceConnectionWaitPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	
	public DocearServiceConnectionWaitPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblTryingToConnect = new JLabel(TextUtils.getText("docear.service.connect.wait.text"));
		add(lblTryingToConnect, "2, 2");
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		add(progressBar, "2, 4");
	}
	
}
