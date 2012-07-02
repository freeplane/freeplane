package org.docear.plugin.bibtex.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.SearchManager2;
import net.sf.jabref.SidePaneManager;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.docear.plugin.bibtex.jabref.ResolveDuplicateEntryAbortedException;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import spl.PdfImporter;

public class ExistingReferencesDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private BasePanel basePanel;
	private URI link = null;

	/**
	 * Create the dialog.
	 */
	private void onCancelButton() {
		this.dispose();
	}

	private void onOkButton() {
		BibtexEntry entry = this.basePanel.getSelectedEntries()[0];
		if (entry != null) {
			Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
			JabRefAttributes attributes = ReferencesController.getController().getJabRefAttributes();					
			
			//import pdf into jabref after adding a reference to a node linking to a pdf
			int yesorno = JOptionPane.YES_OPTION;
			if (link != null) {
				if (link.getPath().toLowerCase().endsWith(".pdf")) {
					BasePanel basePanel = ReferencesController.getController().getJabrefWrapper().getBasePanel();
					int position = basePanel.getMainTable().findEntry(entry);
					basePanel.selectSingleEntry(position);
					JabrefWrapper jabrefWrapper = ReferencesController.getController().getJabrefWrapper();
					try {
						BibtexEntry foundEntry = attributes.findBibtexEntryForPDF(link, nodes.iterator().next().getMap(), true);
						if (foundEntry == null) {
							new PdfImporter(jabrefWrapper.getJabrefFrame(), jabrefWrapper.getJabrefFrame().basePanel(), basePanel.getMainTable(), position)
									.importPdfFiles(new String[] { link.getPath() }, Controller.getCurrentController().getViewController().getFrame(),
											false);
						}
					}
					catch (ResolveDuplicateEntryAbortedException e) {
						LogUtils.warn(e);
					}
				}
				else {
					if (entry.getField("file") != null || entry.getField("url") != null) {
						yesorno = JOptionPane.showConfirmDialog(Controller.getCurrentController().getViewController().getContentPane(),
								TextUtils.getText("overwrite_existing_file_link"), TextUtils.getText("overwrite_existing_file_link_title"),
								JOptionPane.YES_NO_OPTION);
					}
				}
			}
			
			//set references to the selected nodes
			for (NodeModel node : nodes) {
				if (node == null) {
					continue;
				}
				
				if (yesorno == JOptionPane.YES_OPTION) {
					try {
						ReferencesController.getController().getJabRefAttributes().setReferenceToNode(entry, node);
					}
					catch (ResolveDuplicateEntryAbortedException e) {
					}
				}
			}
		}
		this.dispose();
	}

	public ExistingReferencesDialog(Frame frame, URI link) {
		super(frame, TextUtils.getText("add_reference"));
		this.link = link;
		this.setComponentOrientation(frame.getComponentOrientation());
		this.setModal(true);

		setBounds(100, 100, 1000, 500);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JabrefWrapper jabRefWrapper = ReferencesController.getController().getJabrefWrapper();
			this.basePanel = new BasePanel(jabRefWrapper.getJabrefFrame(), jabRefWrapper.getDatabase(), jabRefWrapper.getFile(), jabRefWrapper.getMeta(),
					jabRefWrapper.getEncoding());
			Globals.fileUpdateMonitor.removeUpdateListener(this.basePanel.getFileMonitorHandle());
			contentPanel.setLayout(new BorderLayout(0, 0));

			SidePaneManager sidePaneManager = new SidePaneManager(jabRefWrapper.getJabrefFrame());
			SearchManager2 searchManager = new SearchManager2(jabRefWrapper.getJabrefFrame(), jabRefWrapper.getJabrefFrame().sidePaneManager);
			searchManager.setActiveBasePanel(this.basePanel);
			sidePaneManager.register("search", searchManager);

			sidePaneManager.show("search");

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchManager, this.basePanel);
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
