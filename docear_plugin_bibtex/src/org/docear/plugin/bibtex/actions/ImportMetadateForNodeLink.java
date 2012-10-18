package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.JOptionPane;

import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnPopup = true)
public class ImportMetadateForNodeLink extends AFreeplaneAction {

	private static final String KEY = "menu_import_metadata";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportMetadateForNodeLink() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
			if (node == null || !MonitoringUtils.isPdfLinkedNode(node)) {
				return;
			}
			AnnotationModel model = AnnotationController.getModel(node, false);
			if (model == null) {
				return;
			}

			URI uri = model.getUri();
			JabRefCommons.showMetadataDialog(uri);
		} catch (Exception ex) {
			// ex.printStackTrace();
			JOptionPane.showMessageDialog(UITools.getFrame(), ex.getLocalizedMessage(), TextUtils.getText("docear.metadata.import.error"),
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void setEnabled() {
		String userName = CommunicationsController.getController().getUserName();
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();

		if (userName == null || node == null) {
			setEnabled(false);
			return;
		}

		if (MonitoringUtils.isPdfLinkedNode(node)) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}

	}
}
