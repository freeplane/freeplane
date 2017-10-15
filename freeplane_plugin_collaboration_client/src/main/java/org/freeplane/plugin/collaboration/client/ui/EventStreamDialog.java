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

import org.freeplane.plugin.collaboration.client.event.batch.UpdatesFinished;
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
				UpdatesSerializer.of(t -> text.setText(t)).prettyPrint(UpdatesFinished.builder()
				.mapId("id").mapRevision(1)
				.addUpdateEvents().build());
			}
		});
		buttons.add(map2json);
		contentPane.add(buttons, BorderLayout.WEST);
		dialog.pack();
		dialog.setVisible(true);
		
	};
}
