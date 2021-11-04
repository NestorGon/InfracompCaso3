package main;

import java.io.IOException;
import java.net.ServerSocket;

public class Servidor extends Thread {
	
	private ServerSocket server;
	
	public Servidor() {
		try {
			server = new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class ServidorDelegado extends Thread {
		private ServerSocket server;
	}
	
	public void run() {
		
	}
	
}
