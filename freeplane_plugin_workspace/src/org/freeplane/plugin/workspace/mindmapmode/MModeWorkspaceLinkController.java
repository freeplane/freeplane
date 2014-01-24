package org.freeplane.plugin.workspace.mindmapmode;

import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;

public class MModeWorkspaceLinkController extends MLinkController {
	
	public final static int LINK_RELATIVE_TO_PROJECT = 2;
	private final static String LINK_RELATIVE_TO_PROJECT_PROPERTY = "relative_to_workspace";
	
	private static MModeWorkspaceLinkController self;
	
	public MModeWorkspaceLinkController(ModeController modeController) {
		super(modeController);
	}

	protected void init() {
	}
	
	public static MModeWorkspaceLinkController getController() {
     	final ModeController modeController = Controller.getCurrentModeController();
		if(self == null) {
			self = new MModeWorkspaceLinkController(modeController);
		}
		return self;
	}
	
	public void setLink(final NodeModel node, final URI argUri, final int linkType) {
		URI uri = argUri;
		int finalLinkType = linkType;
		if (linkType == LINK_RELATIVE_TO_PROJECT) {			
			WorkspaceMapModelExtension mapExt = WorkspaceController.getMapModelExtension(node.getMap());
			if(mapExt != null && mapExt.getProject() != null) {
				uri = mapExt.getProject().getRelativeURI(argUri);
				if(uri == null) {
					uri = argUri;
				}
				else {
					finalLinkType = LINK_ABSOLUTE;
				}
			}
			else {
				if(node.getMap().getFile() != null) {
					finalLinkType = LINK_RELATIVE_TO_MINDMAP;
				}
				else {
					finalLinkType = LINK_ABSOLUTE;
				}
			}
		}		
		super.setLink(node, uri, finalLinkType);
		
	}
	
	public int linkType() {
		String linkTypeProperty = ResourceController.getResourceController().getProperty("links");
		if (linkTypeProperty.equals(LINK_RELATIVE_TO_PROJECT_PROPERTY)) {
			return LINK_RELATIVE_TO_PROJECT;
		}
		return super.linkType();
	}
	
	public URI createRelativeURI(final File map, final File input, final int linkType) {
		if (linkType == LINK_ABSOLUTE) {
			return null;
		}
		try {
			if (linkType == LINK_RELATIVE_TO_PROJECT) {
				return WorkspaceController.getCurrentProject().getRelativeURI(input.getAbsoluteFile().toURI());
			}
			else {
				return super.createRelativeURI(map, input, linkType);
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return null;
	}
	
	/**
	 * similar to the File(File base, File ext) contructor. The extend path (child) will be appended to the base path.
	 * @param base
	 * @param child
	 * @return
	 */
	public static URI extendPath(URI base, String child) {
		return new File(URIUtils.getAbsoluteFile(base), child).toURI();
	}
	
	public void prepareOptionPanelBuilder(OptionPanelBuilder builder) {
		IndexedTree.Node node = (IndexedTree.Node) builder.getRoot();
		String path = "Environment/hyperlink_types/links";
		final IndexedTree.Node found = getNodeForPath(path, node);
		if(found != null) {
			found.setUserObject(new IPropertyControlCreator() {
				private final IPropertyControlCreator creator = (IPropertyControlCreator) found.getUserObject();
				public IPropertyControl createControl() {
					ComboProperty property = (ComboProperty) creator.createControl();
					List<String> list = property.getPossibleValues();
					list.add(MModeWorkspaceLinkController.LINK_RELATIVE_TO_PROJECT_PROPERTY);
					return new ComboProperty("links", list.toArray(new String[] {}));
				}

			});
		}
	}
	
	private IndexedTree.Node getNodeForPath(String path, IndexedTree.Node node) {
		Enumeration<?> children = node.children();
		while(children.hasMoreElements()) {
			IndexedTree.Node child = (IndexedTree.Node)children.nextElement();
			if(child.getKey() != null && path.startsWith(child.getKey().toString())) {
				if(path.equals(child.getKey().toString())) {
					return child;
				}
				return getNodeForPath(path, child);
				
			}
		}
		return null;
	}
}
