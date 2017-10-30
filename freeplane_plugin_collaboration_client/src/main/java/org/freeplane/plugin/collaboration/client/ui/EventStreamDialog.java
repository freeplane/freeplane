package org.freeplane.plugin.collaboration.client.ui;

import static java.awt.Dialog.ModalityType.MODELESS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessors;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesFinished;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children.NodeFactory;
import org.freeplane.plugin.collaboration.client.event.children.RootNodeIdUpdatedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeProcessor;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventGenerator;
import org.freeplane.plugin.collaboration.client.event.json.Jackson;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;

public class EventStreamDialog {
	private class Map2Json implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			MapUpdateTimerFactory f = new MapUpdateTimerFactory(ev -> {
				UpdatesSerializer printer = UpdatesSerializer.of(t -> text.setText(t));
				printer.prettyPrint(ev);
			}, 100);
			UpdateEventGenerator updateEventGenerator = new UpdateEventGenerator(f, new ChildrenUpdateGeneratorFactory(new UpdateEventFactory()));
			MapModel map = Controller.getCurrentController().getMap();
			if(! map.containsExtension(ModifiableUpdateHeaderExtension.class))
				map.addExtension(ModifiableUpdateHeaderExtension.create().setMapId("id").setMapRevision(1));
			updateEventGenerator.onNewMap(map);
			NodeModel rootNode = map.getRootNode();
			for (int i = 0; i < rootNode.getChildCount(); i++)
				updateEventGenerator.onNodeInserted(rootNode, rootNode.getChildAt(i), i);
		}
	}

	private class Json2Map implements ActionListener {
		
		private final UpdateProcessors processor;
		private MMapController mapController;

		public Json2Map() {
			final NodeFactory nodeFactory = new NodeFactory();
			mapController = (MMapController) Controller.getCurrentController().getModeController(MModeController.MODENAME).getMapController();
			final SingleNodeStructureManipulator singleNodeStructureManipulator = new SingleNodeStructureManipulator(mapController);
			final ChildrenUpdateProcessor childrenUpdateProcessor = new ChildrenUpdateProcessor(singleNodeStructureManipulator, nodeFactory);
			final SpecialNodeTypeProcessor specialNodeTypeProcessor = new SpecialNodeTypeProcessor();
			processor = new UpdateProcessors().add(new RootNodeIdUpdatedProcessor()).add(childrenUpdateProcessor).add(specialNodeTypeProcessor);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				final UpdatesFinished updates = Jackson.objectMapper.readValue(text.getText(), UpdatesFinished.class);
				final MapModel map = mapController.newMap();
				for(MapUpdated event: updates.updateEvents())
					processor.onUpdate(map, event);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	final private JDialog dialog;
	private JTextArea text;

	public EventStreamDialog(Window owner) {
		super();
		this.dialog = new JDialog(owner, MODELESS);
		dialog.setTitle("freeplane collaboration events");
		text = new JTextArea();
		JScrollPane textPane = new JScrollPane(text, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
		text.setColumns(80);
		text.setRows(40);
		Container contentPane = dialog.getContentPane();
		contentPane.add(textPane, BorderLayout.CENTER);
		Box buttons = Box.createVerticalBox();
		JButton map2json = new JButton("map2json");
		map2json.addActionListener(new Map2Json());
		buttons.add(map2json);
		JButton json2map = new JButton("json2map");
		json2map.addActionListener(new Json2Map());
		buttons.add(json2map);
		contentPane.add(buttons, BorderLayout.WEST);
		dialog.pack();
		
	}

	public void show() {
		dialog.setVisible(true);
	}
}
