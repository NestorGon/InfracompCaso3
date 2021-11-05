package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;

import javax.crypto.Cipher;

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
		private int id;
		public int port;
		
		public ServidorDelegado(int id) {
			try {
				server = new ServerSocket(0);
				port = server.getLocalPort();
				this.id = id;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			PrintWriter out = null;
			BufferedReader in = null;
			try {
				Socket repetidor = server.accept();
				out = new PrintWriter(repetidor.getOutputStream(),true);
				in = new BufferedReader(new InputStreamReader(repetidor.getInputStream()));
				
				Cipher encrypt, decrypt;
				String instance;
				Key encryptKey, decryptKey;
				if ( Util.symetric ) {
					instance = "AES/ECB/PKCS5Padding";
					encryptKey = Util.getSymmetricKey("RS"+this.id);
					decryptKey = encryptKey;
				} else {
					instance = "RSA";
					encryptKey = Util.getPublicAsymmetricKey("R"+this.id+"+");
					decryptKey = Util.getPrivateAsymmetricKey("S"+this.id+"-");
				}
				encrypt = Cipher.getInstance(instance);
				encrypt.init(Cipher.ENCRYPT_MODE, encryptKey);
				decrypt= Cipher.getInstance(instance);
				decrypt.init(Cipher.DECRYPT_MODE, decryptKey);
				
				String message = Util.byte2str(decrypt.doFinal(Util.str2byte(in.readLine())));
				String resp = Util.getSecretMessage(message);
				out.println(Util.byte2str(encrypt.doFinal(Util.str2byte(Util.toHex(resp)))));
			} catch ( Exception e ) {
				e.printStackTrace();
			} finally {
				try {
					if ( out != null )
						out.close();
					if ( in != null )
						in.close();
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
				String serverId = in.readLine().split("_")[1];
				ServidorDelegado delegado = new ServidorDelegado(Integer.parseInt(serverId));
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
