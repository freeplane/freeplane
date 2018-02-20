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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import org.freeplane.collaboration.event.batch.Credentials;
import org.freeplane.collaboration.event.batch.ImmutableMapDescription;
import org.freeplane.collaboration.event.batch.ImmutableMapId;
import org.freeplane.collaboration.event.batch.ImmutableUserId;
import org.freeplane.collaboration.event.batch.MapCreateRequest;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.collaboration.event.batch.MapUpdateRequest;
import org.freeplane.collaboration.event.batch.UpdateBlockCompleted;
import org.freeplane.collaboration.event.children.RootNodeIdUpdated;
import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Server.UpdateStatus;
import org.freeplane.plugin.collaboration.client.server.Subscription;
import org.freeplane.plugin.collaboration.client.session.Session;
import org.freeplane.plugin.collaboration.client.session.SessionController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class EventStreamDialog {

	SessionController sessionController = new SessionController();
	
	private final class JRadioButtonExtension extends JRadioButton {
		private JRadioButtonExtension(String text, Server.UpdateStatus updateStatus) {
			super(text);
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EventStreamDialog.this.updateStatus = updateStatus;
				}
			});
		}
	}

	private class MyServer implements Server {
		private final MapId SENDER_MAP_ID =  ImmutableMapId.of("sender");
		private Subscription recieverSubscription;
		private Subscription senderSubscription;
		
		@Override
		public MapId createNewMap(MapCreateRequest request) {
			text.setText("");
			return SENDER_MAP_ID;
		}

		@Override
		public UpdateStatus update(MapUpdateRequest request) {
			UpdateBlockCompleted ev = request.update();
			if(ev.mapId().equals(SENDER_MAP_ID)) {
				final boolean isNewMapUpdate = ev.updateBlock().get(0) instanceof RootNodeIdUpdated;
				final UpdateStatus currentUpdateStatus =
						isNewMapUpdate ? UpdateStatus.ACCEPTED : updateStatus;
				if (currentUpdateStatus != UpdateStatus.REJECTED) {
					UpdatesSerializer printer = UpdatesSerializer.of(this::updateTextArea);
					printer.prettyPrint(ev);
				}
				if (currentUpdateStatus == UpdateStatus.MERGED) {
					final Timer timer = new Timer(2000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							senderSubscription.consumer().accept(ev);
						}
					});
					timer.setRepeats(false);
					timer.start();
				}
				return currentUpdateStatus;
			}
			return UpdateStatus.ACCEPTED;
		}

		private void updateTextArea(String addedText) {
			final String oldText = text.getText();
			final String newText = oldText.isEmpty()  ? addedText : oldText + ",\n" + addedText;
			text.setText(newText);
		}

		@Override
		public void subscribe(Subscription subscription) {
			if(subscription.mapId().equals(RECEIVER_MAP_ID)) {
				this.recieverSubscription = subscription;
			}
			if(subscription.mapId().equals(SENDER_MAP_ID)) {
				this.senderSubscription = subscription;
			}
		}

		void updateReceiver() throws IOException, JsonParseException, JsonMappingException {
			if(recieverSubscription != null) {
				final UpdateBlockCompleted[] updates = Jackson.objectMapper.readValue("[" + text.getText() + "]", UpdateBlockCompleted[].class);
				for(UpdateBlockCompleted u : updates)
					recieverSubscription.consumer().accept(u);
				text.setText("");
			}
		}

		@Override
		public void unsubscribe(Subscription subscription) {
		}
		
	}
	
	MyServer server = new MyServer();
	Credentials credentials = Credentials.of(ImmutableUserId.of("user-id"));
	
	private class Map2Json implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			MMapModel map = (MMapModel) Controller.getCurrentController().getMap();
			if(map.containsExtension(Session.class)) {
				sessionController.stopSession(map.getExtension(Session.class));
			}
			sessionController.startSession(server, credentials, map, ImmutableMapDescription.of("sender-map"));
		}
	}

	private final MapId RECEIVER_MAP_ID = ImmutableMapId.of("receiver");
	public class Json2Map implements ActionListener {
		private MMapController mapController;
		public MMapModel map;

		public Json2Map() {
			final ModeController modeController = Controller.getCurrentController()
			    .getModeController(MModeController.MODENAME);
			mapController = (MMapController) modeController.getMapController();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(map == null || ! text.getText().trim().isEmpty() 
						&& Jackson.objectMapper.readValue(text.getText(),UpdateBlockCompleted.class)
							.updateBlock().get(0) instanceof RootNodeIdUpdated) {
					map = (MMapModel) mapController.newMap();
					sessionController.joinSession(server, credentials, map, RECEIVER_MAP_ID);
				}
				server.updateReceiver();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	final private JDialog dialog;
	private JTextArea text;
	
	private Server.UpdateStatus updateStatus = Server.UpdateStatus.ACCEPTED;

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
		
		ButtonGroup strategy = new ButtonGroup();
		JRadioButton acceptSubmissions = new JRadioButtonExtension("accept", UpdateStatus.ACCEPTED);
		buttons.add(acceptSubmissions);
		strategy.add(acceptSubmissions);
		JRadioButton rejectSubmissions = new JRadioButtonExtension("reject", UpdateStatus.REJECTED);
		buttons.add(rejectSubmissions);
		strategy.add(rejectSubmissions);
		JRadioButton delaySubmissions = new JRadioButtonExtension("delay", UpdateStatus.MERGED);
		buttons.add(delaySubmissions);
		strategy.add(delaySubmissions);
		acceptSubmissions.setSelected(true);

		
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
