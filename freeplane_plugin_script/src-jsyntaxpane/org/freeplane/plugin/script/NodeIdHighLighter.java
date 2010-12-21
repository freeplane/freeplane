package org.freeplane.plugin.script;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.actions.ActionUtils;
import jsyntaxpane.components.SyntaxComponent;
import jsyntaxpane.util.Configuration;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.NodeModel;

public class NodeIdHighLighter implements SyntaxComponent, CaretListener {
	private final Pattern nodeIdPattern = Pattern.compile("(ID_\\d+)|(\"ID_\\d+\")");
	private JEditorPane pane;
	private Status status;
	private ArrayList<NodeModel> nodesOriginallyFolded = new ArrayList<NodeModel>();
	private NodeModel originallySelectedNode = null;

	/** remove as soon as SyntaxComponent has it in the JDK5 version. */
	private static enum Status {
        INSTALLING,
        DEINSTALLING
    }

	public void caretUpdate(CaretEvent e) {
		handle(e.getDot());
	}

	public void handle(int pos) {
		SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
		if (doc != null) {
			try {
				doc.readLock();
				Token token = doc.getTokenAt(pos);
				if (token == null || !handle(doc, token)) {
					deHighlight();
				}
			}
			finally {
				doc.readUnlock();
			}
		}
	}

	private boolean handle(SyntaxDocument doc, Token token) {
		final Matcher matcher = nodeIdPattern.matcher(token.getText(doc));
		if (matcher.matches()) {
			String id = matcher.group(1);
			final NodeModel node = Controller.getCurrentController().getMap().getNodeForID(id);
			if (node != null) {
				final MapController mapController = Controller.getCurrentModeController().getMapController();
				if (originallySelectedNode == null)
					originallySelectedNode = mapController.getSelectedNode();
				else
					foldOriginallyFolded(mapController);
				mapController.displayNode(node, nodesOriginallyFolded);
				mapController.select(node);
				pane.setToolTipText(node.getText());
				pane.requestFocus();
				return true;
			}
			else {
				pane.setToolTipText("<html><body bgcolor='#CC0000'>" //
					+ TextUtils.format(getResourceKey("node_is_not_defined"), id) + "</body></html>");
			}
		}
		return false;
	}

	public void deHighlight() {
		final Controller controller = Controller.getCurrentController();
		if (controller == null)
			return;
		final MapController mapController = controller.getModeController().getMapController();
		foldOriginallyFolded(mapController);
		if (originallySelectedNode != null) {
			mapController.select(originallySelectedNode);
			originallySelectedNode = null;
			pane.setToolTipText(null);
			pane.requestFocus();
		}
	}

	private void foldOriginallyFolded(final MapController mapController) {
		final int countNodesOriginallyUnfolded = nodesOriginallyFolded.size();
		if (countNodesOriginallyUnfolded > 0) {
			for (int i = countNodesOriginallyUnfolded - 1; i >= 0; i--)
				mapController.setFolded(nodesOriginallyFolded.get(i), true);
			nodesOriginallyFolded.clear();
		}
	}

	public void config(Configuration config) {
	}

	public void install(JEditorPane editor) {
		this.pane = editor;
		pane.addCaretListener(this);
		handle(editor.getCaretPosition());
		status = Status.INSTALLING;
		addFocusListener();
	}

	private void addFocusListener() {
		class NodeIdHighLightFocusListener extends FocusAdapter {
			public void focusLost(FocusEvent e) {
				deHighlight();
			}
		}
		final FocusListener[] focusListeners = pane.getFocusListeners();
		for (int i = 0; i < focusListeners.length; i++) {
			if (focusListeners[i] instanceof NodeIdHighLightFocusListener) {
				return;
			}
		}
		pane.addFocusListener(new NodeIdHighLightFocusListener());
	}

	public void deinstall(JEditorPane editor) {
		status = Status.DEINSTALLING;
		deHighlight();
		pane.removeCaretListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("document")) {
			pane.removeCaretListener(this);
			if (status.equals(Status.INSTALLING)) {
				pane.addCaretListener(this);
				deHighlight();
			}
		}
	}

	public String getResourceKey(final String key) {
		return "org.freeplane.plugin.script.NodeIdHighlighter." + key;
	}
}
