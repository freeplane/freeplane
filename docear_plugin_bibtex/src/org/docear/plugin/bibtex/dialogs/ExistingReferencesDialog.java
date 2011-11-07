package org.docear.plugin.bibtex.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.SearchManager2;

import org.docear.plugin.bibtex.JabrefWrapper;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.util.TextUtils;

public class ExistingReferencesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private BasePanel basePanel;

	/**
	 * Create the dialog.
	 */
	private void onCancelButton() {
		this.dispose();
	}

	private void onOkButton() {
		BibtexEntry entry = this.basePanel.getSelectedEntries()[0];
		if (entry != null) {
			ReferencesController.getController().getJabRefAttributes().addReferenceToNode(entry);
		}
		this.dispose();
	}

	public ExistingReferencesDialog(Frame frame) {
		super(frame, TextUtils.getText("add_reference"));
		this.setComponentOrientation(frame.getComponentOrientation());
		this.setModal(true);

		setBounds(100, 100, 900, 500);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JabrefWrapper jabRefWrapper = ReferencesController.getController().getJabrefWrapper();
			this.basePanel = new BasePanel(jabRefWrapper.getJabrefFrame(), jabRefWrapper.getDatabase(), jabRefWrapper.getFile(),
					jabRefWrapper.getMeta(), jabRefWrapper.getEncoding());
			contentPanel.setLayout(new BorderLayout(0, 0));
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new SearchManager2(jabRefWrapper.getJabrefFrame(),
					jabRefWrapper.getJabrefFrame().sidePaneManager), basePanel);
			contentPanel.add(splitPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton(TextUtils.getText("cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onCancelButton();
					}
				});
				buttonPane.add(cancelButton);
			}
			{
				JButton okButton = new JButton(TextUtils.getText("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOkButton();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
