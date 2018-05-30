package it.configuratori;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPClient {
	
    public static void main(String[] args) throws UnknownHostException, IOException {
    	
        Scanner scanner = new Scanner(System.in);
        
        try {
        	Socket clientSocket = new Socket("localhost", 4004);
            while (true) {
                System.out.println("Ready for new command ...");
                String line = scanner.nextLine();
                System.out.printf("Sending command = %s%n", line);
                sendCommand(clientSocket, line);
            }
        } catch(IllegalStateException | NoSuchElementException e) {
            System.out.println("System.in was closed; exiting");
        }
    }

	private static void sendCommand(Socket clientSocket, String command)
	{
        try {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.writeBytes(command + '\n');
            String response = inFromServer.readLine();
            System.out.println("RESPONSE to command=" + command +"  >>> " + response);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

	}

}
