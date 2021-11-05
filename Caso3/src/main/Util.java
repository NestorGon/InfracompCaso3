package main;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Util {
	
	private static Properties p;
	private static Properties m;
	public static boolean symetric;
	public static CyclicBarrier barrier;
	
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
	
	public static String toHex(String arg) {
	    return String.format("%040x", new BigInteger(1, arg.getBytes()));
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	
	public static void initializeProperties() {
		try (InputStream input = new FileInputStream("./data/keys.properties");
				InputStream input2 = new FileInputStream("./data/messages.properties")) {
			p = new Properties();
			p.load(input);
			m = new Properties();
			m.load(input2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getSecretMessage(String property) {
		return m.getProperty(property);
	}
	
	public static SecretKey getSymmetricKey(String property) {
		String key = p.getProperty(property);
		byte[] decodedKey = Base64.getDecoder().decode(key); 
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		return originalKey;
	}
	
	public static PublicKey getPublicAsymmetricKey(String property) throws Exception {
		String key = p.getProperty(property);
		byte[] decodedKey = str2byte(key);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedKey);
		return keyFactory.generatePublic(publicKeySpec);
	}
	
	public static PrivateKey getPrivateAsymmetricKey(String property) throws Exception {
		String key = p.getProperty(property);
		byte[] decodedKey = str2byte(key);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedKey);
		return keyFactory.generatePrivate(privateKeySpec);
	}
}
