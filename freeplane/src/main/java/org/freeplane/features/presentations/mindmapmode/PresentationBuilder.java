package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.CENTERS_SELECTED_NODE;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.CHANGES_ZOOM;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.NAME;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.*;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.NODE_ID;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.NODE_ON_SLIDE;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.PRESENTATION;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.SHOWS_ANCESTORS;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.SHOWS_DESCENDANTS;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.SHOWS_ONLY_SPECIFIC_NODES;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.SLIDE;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.SLIDE_CONDITION;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.TRUE;
import static org.freeplane.features.presentations.mindmapmode.PresentationBuilder.ZOOM;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class PresentationBuilder {

	static final String PRESENTATIONS = "presentations";
	static final String NODE_ON_SLIDE = "NodeOnSlide";
	static final String NODES_ON_SLIDE = "NodesOnSlide";
	static final String FOLDED_NODES = "FoldedNodes";
	static final String SLIDE_CONDITION = "SlideCondition";
	static final String ZOOM = "zoom";
	static final String CENTERS_SELECTED_NODE = "centersSelectedNode";
	static final String CHANGES_ZOOM = "changesZoom";
	static final String SHOWS_ONLY_SPECIFIC_NODES = "showsOnlySpecificNodes";
	static final String SHOWS_DESCENDANTS = "showsDescendants";
	static final String TRUE = "true";
	static final String SHOWS_ANCESTORS = "showsAncestors";
	static final String NAME = "NAME";
	static final String SLIDE = "slide";
	static final String PRESENTATION = "presentation";
	static final String NODE_ID = "nodeId";

	void register(MapController mapController, final PresentationController presentationController) {
		mapController.getReadManager().addElementHandler("hook", new IElementDOMHandler() {
			private final ConditionFactory conditionFactory = FilterController.getCurrentFilterController()
			    .getConditionFactory();

			@Override
			public Object createElement(Object parent, String tag, XMLElement attributes) {
				if (attributes == null) {
					return null;
				}
				if (!PRESENTATIONS.equals(attributes.getAttribute("NAME", null))) {
					return null;
				}
				return parent;
			}

			@Override
			public void endElement(Object parent, String tag, Object element, XMLElement dom) {
				final NodeModel node = (NodeModel) parent;
				final MapModel map = node.getMap();
				final NamedElementFactory<Slide> slideFactory = presentationController.getSlideFactory(map);
				MapPresentations mapPresentationExtension = presentationController.getPresentations(map);
				NamedElementCollection<Presentation> presentations = mapPresentationExtension.presentations;
				Enumeration<XMLElement> xmlPresentations = dom.enumerateChildren();
				while (xmlPresentations.hasMoreElements()) {
					XMLElement xmlPresentation = xmlPresentations.nextElement();
					presentations.add(xmlPresentation.getAttribute(NAME, "noname"));
					Enumeration<XMLElement> xmlSlides = xmlPresentation.enumerateChildren();
					NamedElementCollection<Slide> slides = presentations.getCurrentElement().slides;
					while (xmlSlides.hasMoreElements()) {
						XMLElement xmlSlide = xmlSlides.nextElement();
						final String name = xmlSlide.getAttribute(NAME, "noname");
						Slide s = slideFactory.create(name);
						Slide slide = applySlideAttributes(xmlSlide, s);
						slides.add(slide);
					}
					if (slides.getSize() > 1)
						slides.selectCurrentElement(0);
				}
				if (presentations.getSize() > 1)
					presentations.selectCurrentElement(0);
				node.addExtension(mapPresentationExtension);
			}

			Slide applySlideAttributes(XMLElement xmlSlide, Slide s) {
				s.setShowsAncestors(toBoolean(xmlSlide, SHOWS_ANCESTORS));
				s.setShowsDescendants(toBoolean(xmlSlide, SHOWS_DESCENDANTS));
				s.setShowsOnlySpecificNodes(toBoolean(xmlSlide, SHOWS_ONLY_SPECIFIC_NODES));
				s.setChangesZoom(toBoolean(xmlSlide, CHANGES_ZOOM));
				s.setCentersSelectedNode(toBoolean(xmlSlide, CENTERS_SELECTED_NODE));
				s.setZoom(toFloat(xmlSlide, ZOOM));
				Enumeration<XMLElement> childAttributes = xmlSlide.enumerateChildren();
				while(childAttributes.hasMoreElements()) {
					XMLElement xmlElement = childAttributes.nextElement();
					if (xmlElement.getName().equals(NODES_ON_SLIDE)) {
						Set<String> ids = loadSpecificNodeIds(xmlElement);
						s.setSelectedNodeIds(ids);
					}
					else if (xmlElement.getName().equals(FOLDED_NODES)) {
						Set<String> ids = loadSpecificNodeIds(xmlElement);
						s.setFoldedNodeIDs(ids);
					}
					else if (xmlElement.getName().equals(SLIDE_CONDITION)) {
						ASelectableCondition condition = loadFilterCondition(xmlElement);
						s.setFilterCondition(condition);
					}
				}
				return s;
			}

			private Set<String> loadSpecificNodeIds(XMLElement xmlNodeIds) {
				LinkedHashSet<String> nodeIds = new LinkedHashSet<>();
				Enumeration<XMLElement> nodesEnumeration = xmlNodeIds.enumerateChildren();
				while(nodesEnumeration.hasMoreElements()){
					XMLElement nodeIdXml = nodesEnumeration.nextElement();
					if(nodeIdXml.getName().equals(NODE_ON_SLIDE)) {
						String id = nodeIdXml.getAttribute(NODE_ID, null);
						if (id != null)
							nodeIds.add(id);
					}
				}
				return nodeIds;
			}

			private ASelectableCondition loadFilterCondition(XMLElement xmlElement) {
				return conditionFactory.loadCondition(xmlElement.getChildAtIndex(0));
			}
			
			private float toFloat(XMLElement element, String attribute) {
				return Float.parseFloat(element.getAttribute(attribute, "1f"));
			}

			private boolean toBoolean(XMLElement element, String attribute) {
				return Boolean.parseBoolean(element.getAttribute(attribute, ""));
			}
		});

		mapController.getWriteManager().addExtensionElementWriter(MapPresentations.class,
		    new IExtensionElementWriter() {
			    @Override
			    public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
				    new PresentationWriter(((NodeModel) element).getMap()).writeContent(writer, extension);
			    }
		    });
	}
}

class PresentationWriter {
	private static final String HOOK = "hook";
	private MapModel map;

	public PresentationWriter(MapModel map) {
		this.map = map;
	}

	void writeContent(ITreeWriter writer, IExtension extension) throws IOException {
		MapPresentations mapPresentations = (MapPresentations) extension;
		NamedElementCollection<Presentation> presentations = mapPresentations.presentations;
		if (presentations.getSize() > 0) {
			XMLElement xmlPresentations = new XMLElement(HOOK);
			xmlPresentations.setAttribute(NAME, PresentationBuilder.PRESENTATIONS);
			for (int i = 0; i < presentations.getSize(); i++) {
				Presentation p = presentations.getElement(i);
				writePresentation(xmlPresentations, p);
			}
			writer.addElement(null, xmlPresentations);
		}
	}

	private void writePresentation(XMLElement xmlPresentations, Presentation p) {
		XMLElement xmlPresentation = xmlPresentations.createElement(PRESENTATION);
		xmlPresentation.setAttribute(NAME, p.getName());
		for (int i = 0; i < p.slides.getSize(); i++)
			writeSlide(xmlPresentation, p.slides.getElement(i));
		xmlPresentations.addChild(xmlPresentation);
	}

	private void writeSlide(XMLElement xmlPresentation, Slide s) {
		XMLElement xmlSlide = xmlPresentation.createElement(SLIDE);
		xmlPresentation.addChild(xmlSlide);
		xmlSlide.setAttribute(NAME, s.getName());
		if (s.showsAncestors())
			xmlSlide.setAttribute(SHOWS_ANCESTORS, TRUE);
		if (s.showsDescendants())
			xmlSlide.setAttribute(SHOWS_DESCENDANTS, TRUE);
		if (s.showsOnlySpecificNodes())
			xmlSlide.setAttribute(SHOWS_ONLY_SPECIFIC_NODES, TRUE);
		if (s.changesZoom())
			xmlSlide.setAttribute(CHANGES_ZOOM, TRUE);
		if (s.centersSelectedNode())
			xmlSlide.setAttribute(CENTERS_SELECTED_NODE, TRUE);
		float zoom = s.getZoom();
		if (zoom != 1f)
			xmlSlide.setAttribute(ZOOM, Float.toString(zoom));
		ASelectableCondition filterCondition = s.getFilterCondition();
		if (filterCondition != null) {
			XMLElement xmlCondition = new XMLElement(SLIDE_CONDITION);
			filterCondition.toXml(xmlCondition);
			xmlSlide.addChild(xmlCondition);
		}
		XMLElement xmlNodes = new XMLElement(NODES_ON_SLIDE);
		for (String nodeId : s.getSelectedNodeIds()) {
			if (map.getNodeForID(nodeId) != null) {
				XMLElement xmlNode = new XMLElement(NODE_ON_SLIDE);
				xmlNode.setAttribute(NODE_ID, nodeId);
				xmlNodes.addChild(xmlNode);
			}
		}
		if (xmlNodes.hasChildren())
			xmlSlide.addChild(xmlNodes);
		if(s.foldsNodes()) {
			XMLElement xmlFoldedNodes = new XMLElement(FOLDED_NODES);
			for (String nodeId : s.getFoldedNodeIds()) {
				if (map.getNodeForID(nodeId) != null) {
					XMLElement xmlNode = new XMLElement(NODE_ON_SLIDE);
					xmlNode.setAttribute(NODE_ID, nodeId);
					xmlFoldedNodes.addChild(xmlNode);
				}
			}
			xmlSlide.addChild(xmlFoldedNodes);
		}
	}
}
