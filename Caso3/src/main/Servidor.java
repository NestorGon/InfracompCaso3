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
		private PrintWriter out;	
		private BufferedReader in;
		
		public ServidorDelegado(BufferedReader in, PrintWriter out) {
			try {
				this.out = out;
				this.in = in;						
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			PrintWriter out = this.out;
			BufferedReader in = this.in;
			try {					
				Cipher encrypt, decrypt;
				String instance;
				Key encryptKey, decryptKey;
				if ( Util.symetric ) {
					instance = "AES/ECB/PKCS5Padding";
					encryptKey = Util.getSymmetricKey("RS");
					decryptKey = encryptKey;
				} else {
					instance = "RSA";
					encryptKey = Util.getPublicAsymmetricKey("R+");
					decryptKey = Util.getPrivateAsymmetricKey("S-");
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
				ServidorDelegado delegado = new ServidorDelegado(in, out);
				delegado.start();				
			} catch (Exception e) {
				e.printStackTrace();
			} 			
		}
		try {
			server.close();			
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
}
