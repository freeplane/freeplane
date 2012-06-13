package org.docear.plugin.services.recommendations.actions;

import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.JResizer.Direction;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.ui.components.OneTouchCollapseResizer.CollapseDirection;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.StyleEditorPanel;
import org.freeplane.features.ui.ViewController;

import org.docear.plugin.bibtex.ReferencesController;

@EnabledAction(checkOnPopup = true)
public class ShowRecommendationsAction extends AFreeplaneAction {
	public final static String TYPE = "ShowRecommendationsAction";

	private static final long serialVersionUID = 1L;

	public ShowRecommendationsAction() {
		super(TYPE);
	}

	public void setEnabled() {
		if (CommunicationsController.getController().getUserName() == null || !ServiceController.getController().isRecommendationsAllowed()) {
			setEnabled(false);
		}
		else {
			setEnabled(true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		ModeController modeController = Controller.getCurrentController().getModeController(DocearRecommendationsModeController.MODENAME);
		try {
			MUIFactory uiFactory = new MUIFactory();
			final StyleEditorPanel panel = new StyleEditorPanel(modeController, uiFactory, true);
			final JScrollPane styleScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			UITools.setScrollbarIncrement(styleScrollPane);
			Box resisableTabs = Box.createHorizontalBox();
			resisableTabs.add(new OneTouchCollapseResizer(Direction.RIGHT, CollapseDirection.COLLAPSE_RIGHT));
			resisableTabs.add(new JTabbedPane());			
			modeController.getUserInputListenerFactory().addToolBar("/format", ViewController.RIGHT, resisableTabs);			
			final JComponent tabs = (JComponent) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
			
			tabs.add(TextUtils.getText("jabref"), ReferencesController.getController().getJabrefWrapper().getJabrefFramePanel());
//			tabs.add(TextUtils.getText("format_panel"), styleScrollPane);
		}
		catch (Exception ex) {
			LogUtils.warn(ex);
		}

		Controller.getCurrentController().selectMode(modeController);

	}

}
