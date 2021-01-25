package org.freeplane.features.note.mindmapmode;

import java.util.regex.Pattern;

import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.collection.OptionalReference;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;
import org.freeplane.features.text.mindmapmode.MTextController;

class NoteDialogStarter{
	private class NoteEditor implements EditNodeBase.IEditControl {
        private final OptionalReference<NodeModel> nodeModel;

        private NoteEditor(NodeModel nodeModel) {
            this.nodeModel = new OptionalReference<>(nodeModel);
        }

        public void cancel() {
        }

        public void ok(final String newText) {
            nodeModel.ifPresent(x -> setHtmlText(x, newText));
        }

        public void split(final String newText, final int position) {
        }

        public boolean canSplit() {
            return false;
        }

        public EditedComponent getEditType() {
            return EditedComponent.NOTE;
        }
    }


    private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);

	void editNoteInDialog(final NodeModel nodeModel) {
		final Controller controller = Controller.getCurrentController();
		NoteModel note = NoteModel.getNote(nodeModel);
		if(note ==  null){
			note = new NoteModel();
		}
		final EditNodeBase.IEditControl editControl = new NoteEditor(nodeModel);
		final RootPaneContainer frame = (RootPaneContainer) SwingUtilities.getWindowAncestor(controller.getMapViewManager().getMapViewComponent());
		EditNodeBase editor = MTextController.getController().createEditor(nodeModel, note, note.getTextOr(""), editControl, false, true, true);
		editor.show(frame);

    }


	private void setHtmlText(final NodeModel node, final String newText) {
		final String body = HTML_HEAD.matcher(newText).replaceFirst("");
		final MNoteController noteController = (MNoteController) MNoteController.getController();
		final String trimmed = body.replaceFirst("\\s+$", "");
		if(HtmlUtils.isEmpty(trimmed))
			noteController.setNoteText(node, null);
		else
			noteController.setNoteText(node, trimmed);
	}
}