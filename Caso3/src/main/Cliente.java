package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Cliente {
	
	private Socket socket;
	
	public static void main(String[] args) {
		try (Scanner sc = new Scanner(System.in)) {
			boolean simetrico = sc.nextBoolean();
			int clientes = sc.nextInt();
			if ( simetrico ) {
				KeyGenerator kg = KeyGenerator.getInstance("AES");
				kg.init(128);
				BufferedWriter bw = new BufferedWriter( new FileWriter(new File("./data/keys.properties"), false));
				for ( int i = 0; i < clientes; i++ ) {
					SecretKey key = kg.generateKey();
					bw.write("RS"+i+"="+byte2str(key.getEncoded())+"\n");
					key = kg.generateKey();
					bw.write("CR"+i+"="+byte2str(key.getEncoded())+"\n");
				}
				bw.close();
				/*
				 * byte[] decodedKey = Base64.getDecoder().decode(encodedKey); SecretKey
				 * originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
				 * return originalKey;
				 */
			} else {
				KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
				kg.initialize(1024);
				BufferedWriter bw = new BufferedWriter( new FileWriter(new File("./data/keys.properties"), false));
				for ( int i = 0; i < clientes; i++ ) {
					KeyPair key = kg.generateKeyPair();
					bw.write("C"+i+"+="+byte2str(key.getPublic().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("C"+i+"-="+byte2str(key.getPrivate().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("R"+i+"+="+byte2str(key.getPublic().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("R"+i+"-="+byte2str(key.getPrivate().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("S"+i+"+="+byte2str(key.getPublic().getEncoded())+"\n");
					key = kg.generateKeyPair();
					bw.write("S"+i+"-="+byte2str(key.getPrivate().getEncoded())+"\n");
				}
				bw.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String byte2str( byte[] b ) {
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}
	
	public static byte[] str2byte( String ss ) {
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}

}
