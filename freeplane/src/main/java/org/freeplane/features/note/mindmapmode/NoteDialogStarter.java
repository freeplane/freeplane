package org.freeplane.features.note.mindmapmode;

import java.util.regex.Pattern;

import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.EditNodeBase.EditedComponent;

class NoteDialogStarter{
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);

	void editNoteInDialog(final NodeModel nodeModel) {
		final Controller controller = Controller.getCurrentController();
		Controller.getCurrentModeController().setBlocked(true);
		String text = NoteModel.getNoteText(nodeModel);
		if(text ==  null){
			text = "";
		}
		final EditNodeBase.IEditControl editControl = new EditNodeBase.IEditControl() {
			public void cancel() {
				Controller.getCurrentModeController().setBlocked(false);
			}

			public void ok(final String newText) {
				setHtmlText(nodeModel, newText);
				cancel();
			}

			public void split(final String newText, final int position) {
			}
			public boolean canSplit() {
                return false;
            }

			public EditedComponent getEditType() {
                return EditedComponent.NOTE;
            }
		};
		final IEditBaseCreator textFieldCreator = (IEditBaseCreator) Controller.getCurrentController().getMapViewManager();
		final RootPaneContainer frame = (RootPaneContainer) SwingUtilities.getWindowAncestor(controller.getMapViewManager().getMapViewComponent());
		EditNodeBase editor = textFieldCreator.createEditor(nodeModel, editControl, text, true);
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