package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;

import javax.crypto.Cipher;

public class Repetidor extends Thread {
	
	private ServerSocket server;
	
	//Tiempo total de la recepción y envío de la solicitud al servidor
	public static long tRES;
	//Tiempo total de la recepción y envío de la solicitud al cliente
	public static long tREC;
	
	public Repetidor() {
		try {
			server = new ServerSocket(8000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class RepetidorDelegado extends Thread {
		private Socket socket;
		private ServerSocket server;
		private int id;
		public int port;
		
		public RepetidorDelegado(int id) {
			try {
				server = new ServerSocket(0);
				port = server.getLocalPort();
				this.id = id;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			PrintWriter outClient = null, outServer = null;
			BufferedReader inClient = null, inServer = null;
			try {
				socket = new Socket("localhost",8080);
				outServer = new PrintWriter(socket.getOutputStream(),true);
				inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
				
				Socket cliente = server.accept();
				outClient = new PrintWriter(cliente.getOutputStream(), true);
				inClient = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
				
				Cipher encryptClient, encryptServer, decryptClient, decryptServer;
				String instance;
				Key encryptKeyClient, encryptKeyServer, decryptKeyClient, decryptKeyServer;
				if ( Util.symetric ) {
					instance = "AES/ECB/PKCS5Padding";
					encryptKeyClient = Util.getSymmetricKey("CR"+this.id);
					decryptKeyClient = encryptKeyClient;
					encryptKeyServer = Util.getSymmetricKey("RS");
					decryptKeyServer = encryptKeyServer;
				} else {
					instance = "RSA";
					encryptKeyClient = Util.getPublicAsymmetricKey("C"+this.id+"+");
					decryptKeyClient = Util.getPrivateAsymmetricKey("R-");
					encryptKeyServer = Util.getPublicAsymmetricKey("S+");
					decryptKeyServer = decryptKeyClient;
				}
				encryptClient = Cipher.getInstance(instance);
				encryptClient.init(Cipher.ENCRYPT_MODE, encryptKeyClient);
				decryptClient = Cipher.getInstance(instance);
				decryptClient.init(Cipher.DECRYPT_MODE, decryptKeyClient);
				encryptServer = Cipher.getInstance(instance);
				encryptServer.init(Cipher.ENCRYPT_MODE, encryptKeyServer);
				decryptServer = Cipher.getInstance(instance);
				decryptServer.init(Cipher.DECRYPT_MODE, decryptKeyServer);
				
				long startRES = System.currentTimeMillis();
				//Recibir y desencriptar el mensaje del cliente
				String message = Util.byte2str(decryptClient.doFinal(Util.str2byte(inClient.readLine())));
				long endRES = System.currentTimeMillis();
				Repetidor.tRES += endRES - startRES;
				
				//Encriptar y enviar mensaje al servidor
				outServer.println(Util.byte2str(encryptServer.doFinal(Util.str2byte(message))));
				
				long startREC = System.currentTimeMillis();
				String resp = Util.byte2str(decryptServer.doFinal(Util.str2byte(inServer.readLine())));
				long endREC = System.currentTimeMillis();
				Repetidor.tREC += endREC - startREC;
				
				outClient.println(Util.byte2str(encryptClient.doFinal(Util.str2byte(resp))));
			} catch ( Exception e ) {
				e.printStackTrace();
			} finally {
				try {
					if ( outClient != null )
						outClient.close();
					if ( inClient != null )
						inClient.close();
					if ( outServer != null )
						outServer.close();
					if ( inServer != null )
						inServer.close();
					socket.close();
					server.close();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
	}
	
	public void run() {
		while ( !Util.barrier.isBroken() ) {
			PrintWriter out = null;
			BufferedReader in = null;
			try {
				Socket s = server.accept();
				out = new PrintWriter(s.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String clientId = in.readLine().split("_")[1];
				RepetidorDelegado delegado = new RepetidorDelegado(Integer.parseInt(clientId));
				delegado.start();
				out.println("OK_"+delegado.port);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if ( out != null )
						out.close();
					if ( in != null )
						in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}
	}

}
