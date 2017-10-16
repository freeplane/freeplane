package org.freeplane.plugin.collaboration.client.ui;

import static java.awt.Dialog.ModalityType.MODELESS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.collaboration.client.event.batch.MapUpdateTimerFactory;
import org.freeplane.plugin.collaboration.client.event.batch.ModifiableUpdateHeaderExtension;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdateGeneratorFactory;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventFactory;
import org.freeplane.plugin.collaboration.client.event.children.UpdateEventGenerator;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;

public class EventStreamDialog {
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
		map2json.addActionListener(new ActionListener() {
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
				NodeModel rootNode = map.getRootNode();
				if(rootNode.getChildCount() > 0)
					updateEventGenerator.onNodeInserted(rootNode, rootNode.getChildAt(0), 0);
				
			}
		});
		buttons.add(map2json);
		contentPane.add(buttons, BorderLayout.WEST);
		dialog.pack();
		
	}

	public void show() {
		dialog.setVisible(true);
	}
}
