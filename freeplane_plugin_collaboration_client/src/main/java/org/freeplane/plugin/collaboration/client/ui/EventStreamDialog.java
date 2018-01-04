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
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.UpdateEventGenerator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessorChain;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockCompleted;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.children.NodeFactory;
import org.freeplane.plugin.collaboration.client.event.children.RootNodeIdUpdatedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeProcessor;
import org.freeplane.plugin.collaboration.client.event.children.StructureUpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.content.other.MapContentUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.other.NodeContentUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.json.Jackson;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;

public class EventStreamDialog {
	private class Map2Json implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			UpdateBlockGeneratorFactory f = new UpdateBlockGeneratorFactory(ev -> {
				UpdatesSerializer printer = UpdatesSerializer.of(t -> text.setText(t));
				printer.prettyPrint(ev);
			}, 100);
			final MapWriter mapWriter = Controller.getCurrentModeController().getMapController().getMapWriter();
			ContentUpdateEventFactory contentEventFactory = new ContentUpdateEventFactory(mapWriter);
			StructureUpdateEventFactory structuralEventFactory = new StructureUpdateEventFactory();
			ChildrenUpdateGenerators childrenUpdateGenerators = new ChildrenUpdateGenerators(f, structuralEventFactory, contentEventFactory);
			ContentUpdateGeneratorFactory contentUpdateGeneratorFactory = new ContentUpdateGeneratorFactory(f, contentEventFactory);
			CoreUpdateGeneratorFactory coreUpdateGeneratorFactory = new CoreUpdateGeneratorFactory(f, contentEventFactory);
			ContentUpdateGenerators contentGenerators = new ContentUpdateGenerators(contentUpdateGeneratorFactory, coreUpdateGeneratorFactory);
			UpdateEventGenerator updateEventGenerator = new UpdateEventGenerator(childrenUpdateGenerators,  contentGenerators);
			MapModel map = Controller.getCurrentController().getMap();
			if(! map.containsExtension(ModifiableUpdateHeaderExtension.class))
				map.addExtension(ModifiableUpdateHeaderExtension.create().setMapId("id").setMapRevision(1));
			updateEventGenerator.onNewMap(map);
		}
	}

	private class Json2Map implements ActionListener {
		
		private final UpdateProcessorChain processor;
		private MMapController mapController;

		public Json2Map() {
			final NodeFactory nodeFactory = new NodeFactory();
			final ModeController modeController = Controller.getCurrentController().getModeController(MModeController.MODENAME);
			mapController = (MMapController) modeController.getMapController();
			final SingleNodeStructureManipulator singleNodeStructureManipulator = new SingleNodeStructureManipulator(mapController);
			final RootNodeIdUpdatedProcessor rootNodeIdUpdatedProcessor = new RootNodeIdUpdatedProcessor();
			final ChildrenUpdateProcessor childrenUpdateProcessor = new ChildrenUpdateProcessor(singleNodeStructureManipulator, nodeFactory);
			final SpecialNodeTypeProcessor specialNodeTypeProcessor = new SpecialNodeTypeProcessor();
			NodeContentManipulator updater = new NodeContentManipulator(mapController);
			processor = new UpdateProcessorChain().add(rootNodeIdUpdatedProcessor)
					.add(childrenUpdateProcessor).add(specialNodeTypeProcessor)
					.add(new MapContentUpdateProcessor(updater))
					.add(new NodeContentUpdateProcessor(updater))
					.add(new CoreUpdateProcessor((MTextController)TextController.getController(modeController)));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				final UpdateBlockCompleted updates = Jackson.objectMapper.readValue(text.getText(), UpdateBlockCompleted.class);
				final MapModel map = mapController.newMap();
				for(MapUpdated event: updates.updateBlock())
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
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
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
