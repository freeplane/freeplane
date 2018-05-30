package org.freeplane.plugin.configurationservice;

import java.io.*;
import java.net.*;
import java.util.*;

import org.freeplane.core.util.LogUtils;

public class TCPServer implements Runnable {
	private int port;
	private List<Client> clients = new ArrayList<Client>();
	private ConfigurationSession configurationSession;

	public TCPServer(int port, ConfigurationSession configurationSession) {
		this.port = port;
		this.configurationSession = configurationSession;
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("TCP ServerSocket started @port: " + port);
			while (true) {
				Socket s = ss.accept();
				new Thread(new Client(s)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class Client implements Runnable {
		private Socket socket = null;
		private Writer output = null;
		private String clientName = null;

		public Client(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				socket.setSendBufferSize(16384);
				socket.setTcpNoDelay(true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new OutputStreamWriter(socket.getOutputStream());

				String line = null;
				while ((line = input.readLine()) != null) {
					line = line.trim();
					int a_value = Integer.parseInt(line);
					String response = testCommand(a_value);
					write(response+ "\r\n");
					if (line == "DOIT") {
						//String response = testCommand();
						//write(response+ "\r\n");
					} else {
						write("Command NOT MAPPED" + "\r\n");
					}
					continue;
				}
			} catch (Exception e) {
			} finally {
				output = null;
				try {
					socket.close();
				} catch (Exception e) {
				}
				socket = null;
			}
		}

		public void write(String msg) throws IOException {
			output.write(msg);
			output.flush();
		}

		public String testCommand(int value) {

			String response = "";
			configurationSession.update("ID_1053277958", "a", value);

			List<String> attributesList = new ArrayList<>();
			attributesList.add("a");
			attributesList.add("b");
			attributesList.add("area");

			Map<String, Object> attributeMap = configurationSession.readValues("ID_1053277958", attributesList);

			for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
				response = response + entry.getKey() + ":" + entry.getValue() + ";";
			}

			return response;
		}

		public boolean equals(Client client) {
			return (client != null) && (client instanceof Client) && (clientName != null) && (client.clientName != null)
					&& clientName.equals(client.clientName);
		}
	}
}