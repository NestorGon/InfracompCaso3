package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Cliente extends Thread {
	
	private int id;
	private Socket socket;
	
	public Cliente(int id) {
		this.id = id;
	}
	
	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			socket = new Socket("localhost",8000);
			out = new PrintWriter(socket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.println("CLIENTE_"+this.id);
			String port = in.readLine().split("_")[1];
			socket = new Socket("localhost",Integer.parseInt(port));
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			int randomNum = (new Random()).nextInt(10);
			String message = "0"+randomNum;
			Cipher encrypt, decrypt;
			String instance;
			Key encryptKey, decryptKey;
			
			if ( Util.symetric ) {
				instance = "AES/ECB/PKCS5Padding";
				encryptKey = Util.getSymmetricKey("CR"+this.id);
				decryptKey = encryptKey;
			} else {
				instance = "RSA";
				encryptKey = Util.getPublicAsymmetricKey("R+");
				decryptKey = Util.getPrivateAsymmetricKey("C"+this.id+"-");
			}
			encrypt = Cipher.getInstance(instance);
			encrypt.init(Cipher.ENCRYPT_MODE, encryptKey);
			decrypt = Cipher.getInstance(instance);
			decrypt.init(Cipher.DECRYPT_MODE, decryptKey);
			out.println(Util.byte2str(encrypt.doFinal(Util.str2byte(message))));
			String resp = new String(decrypt.doFinal(Util.str2byte(in.readLine())),"UTF-8");
			System.out.println("Mensaje del cliente "+this.id+": "+resp);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if ( out != null )
					out.close();
				if ( in != null )
					in.close();
				socket.close();
				System.out.println("Tiempo total en gestión solicitud Cliente y preparación de su envío al Servidor (actual): " + Repetidor.tRES);
				System.out.println("Tiempo total en gestión solicitud Servidor y preparación de su envío al Cliente (actual): " + Repetidor.tREC);
				Util.barrier.await();				
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
	
	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("¿Desea cifrado simétrico (true) o asimétrico (false)?");
			boolean simetrico = sc.nextBoolean();
			System.out.println("¿Cuantos clientes quiere ejecutar?");
			int clientes = sc.nextInt();
			Util.symetric = simetrico;
			if ( simetrico ) {
				KeyGenerator kg = KeyGenerator.getInstance("AES");
				kg.init(128);
				BufferedWriter bw = new BufferedWriter( new FileWriter(new File("./data/keys.properties"), false));
				SecretKey key = kg.generateKey();
				bw.write("RS="+Util.byte2str(key.getEncoded())+"\n");				
				for ( int i = 0; i < clientes; i++ ) {					
					key = kg.generateKey();
					bw.write("CR"+i+"="+Util.byte2str(key.getEncoded())+"\n");
				}
				bw.close();
			} else {
				KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
				kg.initialize(1024);
				BufferedWriter bw = new BufferedWriter( new FileWriter(new File("./data/keys.properties"), false));
				for ( int i = 0; i < clientes; i++ ) {
					KeyPair key = kg.generateKeyPair();
					bw.write("C"+i+"+="+Util.byte2str(key.getPublic().getEncoded())+"\n");
					bw.write("C"+i+"-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");					
				}
				
				KeyPair key = kg.generateKeyPair();
				bw.write("R+="+Util.byte2str(key.getPublic().getEncoded())+"\n");
				bw.write("R-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");
				key = kg.generateKeyPair();
				bw.write("S+="+Util.byte2str(key.getPublic().getEncoded())+"\n");
				bw.write("S-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");
				
				bw.close();
			}
			Util.initializeProperties();
			Util.barrier = new CyclicBarrier(clientes);
			new Servidor().start();
			new Repetidor().start();
			for ( int i = 0; i < clientes; i++ ) {
				new Cliente(i).start();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
