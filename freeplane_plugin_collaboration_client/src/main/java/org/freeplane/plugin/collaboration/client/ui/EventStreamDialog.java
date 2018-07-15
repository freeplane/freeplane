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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.collaboration.event.children.RootNodeIdUpdated;
import org.freeplane.collaboration.event.json.Jackson;
import org.freeplane.collaboration.event.messages.Credentials;
import org.freeplane.collaboration.event.messages.ImmutableMapDescription;
import org.freeplane.collaboration.event.messages.ImmutableMapId;
import org.freeplane.collaboration.event.messages.ImmutableUserId;
import org.freeplane.collaboration.event.messages.MapCreateRequested;
import org.freeplane.collaboration.event.messages.MapId;
import org.freeplane.collaboration.event.messages.MapUpdateProcessed.UpdateStatus;
import org.freeplane.collaboration.event.messages.MapUpdateRequested;
import org.freeplane.collaboration.event.messages.UpdateBlockCompleted;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.collaboration.client.event.json.UpdatesSerializer;
import org.freeplane.plugin.collaboration.client.server.Server;
import org.freeplane.plugin.collaboration.client.server.Subscription;
import org.freeplane.plugin.collaboration.client.session.Session;
import org.freeplane.plugin.collaboration.client.session.SessionController;
import org.freeplane.plugin.collaboration.client.websocketserver.WebSocketServer;

public class EventStreamDialog {

	SessionController sessionController = new SessionController();

	@SuppressWarnings("serial")
	private final class JRadioButtonExtension extends JRadioButton {
		private JRadioButtonExtension(String text, UpdateStatus updateStatus) {
			super(text);
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EventStreamDialog.this.updateStatus = updateStatus;
				}
			});
		}
	}

	private static final MapId SENDER_MAP_ID =  ImmutableMapId.of("sender");

	private class CompoundServer implements Server {

		private GuiServer guiServer = new GuiServer();
		private Server webServer = null;

		Server subscriptionServer = guiServer;

		@Override
		public CompletableFuture<MapId> createNewMap(MapCreateRequested request) {
			if(useWebSockets.isSelected())
				getWebServer().createNewMap(request);
			return guiServer.createNewMap(request);
		}
		@Override
		public CompletableFuture<UpdateStatus> update(MapUpdateRequested request) {
			if(useWebSockets.isSelected())
				getWebServer().update(request);
			return guiServer.update(request);
		}

		@Override
		public void subscribe(Subscription subscription) {
			subscriptionServer.subscribe(subscription);
		}
		@Override
		public void unsubscribe(Subscription subscription) {
			subscriptionServer.unsubscribe(subscription);
		}
		public void updateReceiver() {
			guiServer.updateReceiver();
		}
		private Server getWebServer() {
			if(webServer == null)
				webServer = new WebSocketServer();
			return webServer;
		}



	}

	private class GuiServer implements Server {
		private Subscription receiverSubscription;
		private Subscription senderSubscription;

		@Override
		public CompletableFuture<MapId> createNewMap(MapCreateRequested request) {
			text.setText("");
			return CompletableFuture.completedFuture(SENDER_MAP_ID);
		}

		@Override
		public CompletableFuture<UpdateStatus> update(MapUpdateRequested request) {
			UpdateBlockCompleted ev = request.update();
			if(ev.userId().equals(SENDER_USER_ID)) {
				final boolean isNewMapUpdate = ev.updateBlock().get(0) instanceof RootNodeIdUpdated;
				final UpdateStatus currentUpdateStatus =
						isNewMapUpdate ? UpdateStatus.ACCEPTED : updateStatus;
				if (currentUpdateStatus != UpdateStatus.REJECTED) {
					UpdatesSerializer printer = UpdatesSerializer.of(this::updateTextArea);
					printer.prettyPrint(ev);
				}
				final int delay;
				delay = currentUpdateStatus == UpdateStatus.MERGED ? 2000 : 100;
				callBack(senderSubscription, ev, delay);
				if(receiverSubscription != null)
					callBack(receiverSubscription, ev, 0);
				return  CompletableFuture.completedFuture(currentUpdateStatus);
			}
			return  CompletableFuture.completedFuture(UpdateStatus.ACCEPTED);
		}

		private void callBack(Subscription subscription, UpdateBlockCompleted ev, final int delay) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					subscription.consumer().accept(ev);
					timer.cancel();
				}
			}, delay);
		}

		private void updateTextArea(String addedText) {
			final String oldText = text.getText();
			final String newText = oldText.isEmpty()  ? addedText : oldText + ",\n" + addedText;
			text.setText(newText);
		}

		@Override
		public void subscribe(Subscription subscription) {
			if(subscription.credentials().userId().equals(SENDER_USER_ID)) {
				this.senderSubscription = subscription;
			}
			if(subscription.credentials().userId().equals(RECEIVER_USER_ID)) {
				this.receiverSubscription = subscription;
			}
		}

		void updateReceiver() {
			if(receiverSubscription != null) {
				UpdateBlockCompleted[] updates;
				try {
					updates = Jackson.objectMapper.readValue("[" + text.getText() + "]", UpdateBlockCompleted[].class);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				for(UpdateBlockCompleted u : updates)
					receiverSubscription.consumer().accept(u);
				text.setText("");
			}
		}

		@Override
		public void unsubscribe(Subscription subscription) {
			if(subscription == senderSubscription)
				senderSubscription = null;
			if(subscription == receiverSubscription)
				receiverSubscription = null;
		}

	}

	CompoundServer  guiServer = new CompoundServer();

	private static final ImmutableMapDescription MAP_DESCRIPTION = ImmutableMapDescription.of("map");
	private static final ImmutableUserId SENDER_USER_ID = ImmutableUserId.of("sender-user-id");
	private static final ImmutableUserId RECEIVER_USER_ID = ImmutableUserId.of("receiver-user-id");
	private class Map2Json implements ActionListener {



		@Override
		public void actionPerformed(ActionEvent e) {
			MMapModel map = (MMapModel) Controller.getCurrentController().getMap();
			if(map.containsExtension(Session.class)) {
				sessionController.stopSession(map.getExtension(Session.class));
			}
			Credentials credentials = Credentials.of(SENDER_USER_ID);
			sessionController.startSession(guiServer, credentials, map, MAP_DESCRIPTION);
		}
	}

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
					Credentials credentials = Credentials.of(RECEIVER_USER_ID);
					sessionController.joinSession(guiServer, credentials, map, SENDER_MAP_ID);
					guiServer.updateReceiver();
				}
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	final private JDialog dialog;
	private JTextArea text;

	private UpdateStatus updateStatus = UpdateStatus.ACCEPTED;

	private final JCheckBox useWebSockets;

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

		useWebSockets = new JCheckBox("use web sockets");
		buttons.add(useWebSockets);

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
