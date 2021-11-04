package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Repetidor extends Thread {
	
	private Socket socket;
	private ServerSocket server;
	
	public Repetidor() {
		try {
			server = new ServerSocket(8000);
			socket = new Socket("localhost", 8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class RepetidorDelegado extends Thread {
		private Socket socket;
		private ServerSocket server;
		private int port;
		
		public RepetidorDelegado() {
			try {
				server = new ServerSocket(0);
				port = server.getLocalPort();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run() {
		
	}

}
