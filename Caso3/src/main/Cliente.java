package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Random;
import java.util.Scanner;

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
		PrintWriter out;
		BufferedReader in;
		try {
			socket = new Socket("localhost",8000);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.write("CLIENTE_"+this.id);
			String port = in.readLine().split("_")[1];
			
			socket = new Socket("localhost",Integer.parseInt(port));
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			int randomNum = (new Random()).nextInt();
			String message = ""+randomNum;
			Cipher encrypt, decrypt;
			String instance;
			SecretKey encryptKey, decryptKey;
			
			if ( Util.symetric ) {
				instance = "AES/ECB/PKCS5Padding";
				encryptKey = Util.getSymmetricKey("CR_"+this.id);
				decryptKey = encryptKey;
			} else {
				instance = "RSA/ECB/PKCS5Padding";
				encryptKey = Util.getSymmetricKey("R"+this.id+"+");
				decryptKey = Util.getSymmetricKey("C"+this.id+"-");
			}
			encrypt = Cipher.getInstance(instance);
			encrypt.init(Cipher.ENCRYPT_MODE, encryptKey);
			decrypt = Cipher.getInstance(instance);
			decrypt.init(Cipher.DECRYPT_MODE, decryptKey);
			out.write(Util.byte2str(encrypt.doFinal(Util.str2byte(message))));
			System.out.println("Mensaje del cliente "+this.id+": "+decrypt.doFinal(Util.str2byte(in.readLine())));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}	
	}
	
	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			boolean simetrico = sc.nextBoolean();
			int clientes = sc.nextInt();
			Util.symetric = simetrico;
			if ( simetrico ) {
				KeyGenerator kg = KeyGenerator.getInstance("AES");
				kg.init(128);
				BufferedWriter bw = new BufferedWriter( new FileWriter(new File("./data/keys.properties"), false));
				for ( int i = 0; i < clientes; i++ ) {
					SecretKey key = kg.generateKey();
					bw.write("RS"+i+"="+Util.byte2str(key.getEncoded())+"\n");
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
					key = kg.generateKeyPair();
					bw.write("C"+i+"-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("R"+i+"+="+Util.byte2str(key.getPublic().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("R"+i+"-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("S"+i+"+="+Util.byte2str(key.getPublic().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("S"+i+"-="+Util.byte2str(key.getPrivate().getEncoded())+"\n");
				}
				bw.close();
			}
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
