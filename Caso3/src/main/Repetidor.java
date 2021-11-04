package main;

import java.net.ServerSocket;
import java.net.Socket;

public class Repetidor extends Thread {
	
	private class RepetidorDelegado extends Thread {
		private Socket socket;
		private ServerSocket server;
	}
	
	public void run() {
		
	}

}
