package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Collection;

import javax.swing.JOptionPane;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.external.DroppedFileHandler;
import net.sf.jabref.imports.BibtexParser;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.dialogs.PdfMetadataListDialog;
import org.docear.plugin.bibtex.jabref.JabrefWrapper;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnPopup = true)
public class ImportMetadateForNodeLink extends AFreeplaneAction{
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
			File file = new File(Tools.getAbsoluteUri(node));
			
			InputStream is = this.getClass().getResourceAsStream("/bibtex-test.bib");
			CharBuffer buffer = CharBuffer.allocate(is.available());
			new InputStreamReader(is).read(buffer);
			is.close();
			System.out.println(buffer.rewind());
			Collection<BibtexEntry> entries = BibtexParser.fromString(buffer.rewind().toString());
			PdfMetadataListDialog metadata = new PdfMetadataListDialog(entries);
			int response = JOptionPane.showConfirmDialog(UITools.getFrame(), metadata, TextUtils.getText("docear.metadata.import.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(response == JOptionPane.OK_OPTION) {
				BibtexEntry selected = metadata.getSelectedEntry();
				if(selected == null) {
					return;
				}
				JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
				wrapper.getBasePanel().getDatabase().insertEntry(selected);
				
				DroppedFileHandler dfh = new DroppedFileHandler(wrapper.getJabrefFrame(), wrapper.getBasePanel());
				//DOCEAR - change file path to relative to bib-library path?
				dfh.linkPdfToEntry(file.getPath(), wrapper.getBasePanel().getMainTable(), selected);
                //LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), wrapper.getDatabase(), selected);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	public void setEnabled() {
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		if (node == null) {
			setEnabled(false);
			return;
		}
		if(MonitoringUtils.isPdfLinkedNode(node)) {
			setEnabled(true);
		}
		else {
			setEnabled(false);
		}
		
	}
}
